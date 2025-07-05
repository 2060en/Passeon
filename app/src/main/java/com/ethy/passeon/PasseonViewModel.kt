package com.ethy.passeon

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ✨ Passeon 的總經理 (ViewModel)
 * 負責所有資料的業務邏輯
 */
class PasseonViewModel(private val dao: PasseonDao) : ViewModel() {

    // 我們透過 DAO 的「即時水管 (Flow)」，來取得所有票夾和票券
    // 並把它們轉換成 ViewModel 的「公開資產 (StateFlow)」
    val allPassHolders: StateFlow<List<PassHolder>> = dao.getAllPassHolders().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allTickets: StateFlow<List<Ticket>> = dao.getAllTickets().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    // ✨ [新增] 建立一個私有的、可變的 StateFlow，專門用來存放 OCR 的解析結果
    private val _parsedOcrResult = MutableStateFlow<Map<String, String>>(emptyMap())
    // ✨ [新增] 對外提供一個公開的、不可變的 StateFlow，讓 UI 畫面可以訂閱它
    val parsedOcrResult: StateFlow<Map<String, String>> = _parsedOcrResult.asStateFlow()

    // 提供「新增票夾」的服務給 UI 呼叫
    fun insertPassHolder(passHolder: PassHolder) = viewModelScope.launch {
        dao.insertPassHolder(passHolder)
    }

    // 提供「新增票券」的服務給 UI 呼叫
    fun insertTicket(ticket: Ticket) = viewModelScope.launch {
        dao.insertTicket(ticket)
    }

    // ✨ [新增] 提供「刪除票券」的服務
    fun deleteTicket(ticket: Ticket) = viewModelScope.launch {
        dao.deleteTicket(ticket)
    }

    // ✨ [新增] 提供「刪除票夾」的服務
    // 注意：刪除票夾時，我們會一併刪除它底下的所有票券，避免產生孤兒資料
    fun deletePassHolder(passHolder: PassHolder) = viewModelScope.launch {
        dao.deleteTicketsByPassHolderId(passHolder.id)
        dao.deletePassHolder(passHolder)
    }
    // ✨ [新增] 這是我們新的 OCR 處理中心
    fun processImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val image = InputImage.fromFilePath(context, uri)
                val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        Log.d("PasseonDebug", "OCR Result: ---START---\n${visionText.text}\n---END---")
                        val result = TicketParser.parse(visionText.text)
                        if (result.isEmpty()) {
                            _parsedOcrResult.value = mapOf("title" to "無法解析的票券")
                        } else {
                            _parsedOcrResult.value = result as Map<String, String>
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("PasseonDebug", "OCR 辨識失敗: ${e.message}")
                        _parsedOcrResult.value = mapOf("title" to "辨識失敗: ${e.message}")
                    }
            } catch (e: Exception) {
                Log.e("PasseonDebug", "處理圖片時發生錯誤: ${e.message}")
                _parsedOcrResult.value = mapOf("title" to "讀取圖片失敗")
            }
        }
    }
    // ✨ [新增] 提供一個方法，讓 UI 在離開頁面或完成操作後，可以清空上次的辨識結果
    fun clearOcrResult() {
        _parsedOcrResult.value = emptyMap()
    }
}

/**
 * ✨ 這是一個「總經理產生器 (ViewModel Factory)」
 * 它負責告訴系統，要如何建立一個需要傳入 DAO 的 PasseonViewModel
 */
class PasseonViewModelFactory(private val dao: PasseonDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PasseonViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PasseonViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
