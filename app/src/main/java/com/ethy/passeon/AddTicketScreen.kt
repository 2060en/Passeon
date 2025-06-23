package com.ethy.passeon

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTicketScreen(
    onNavigateBack: () -> Unit,
    onAddTicket: (Ticket) -> Unit
) {
    var titleState by remember { mutableStateOf("") }
    var typeState by remember { mutableStateOf("") }
    // ✨ [修改] 新增狀態來記住日期和時間
    var dateState by remember { mutableStateOf("") }
    var timeState by remember { mutableStateOf("") }

    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val image = InputImage.fromFilePath(context, it)
                val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

                recognizer.process(image)
                    .addOnSuccessListener { visionText ->

                        Log.d("PasseonDebug", "OCR Result: ---START---\n${visionText.text}\n---END---")

                        val parsedResult = parseTicket(visionText.text)
                        if (parsedResult.isEmpty()) {
                            titleState = "無法解析的票券"
                            typeState = ""
                        } else {
                            titleState = parsedResult["title"] ?: ""
                            typeState = parsedResult["type"] ?: ""
                            // ✨ [修改] 將解析出的日期和時間，填入對應的狀態
                            dateState = parsedResult["date"] ?: ""
                            timeState = parsedResult["time"] ?: ""
                        }
                    }
                    .addOnFailureListener { e ->
                        // 辨識失敗的處理可以更細緻，但我們先簡化
                        titleState = "辨識失敗"
                    }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新增票券", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val newTicket = Ticket(
                                id = (0..10000).random(),
                                title = titleState,
                                type = typeState,
                                origin = "待填寫",
                                destination = "待填寫",
                                departureTimestamp = System.currentTimeMillis(),
                                seatInfo = "",
                                imageUri = null,
                                customFields = emptyMap(),
                                // ✨ [修正] 加上這一行，告訴 App 這張新票暫不屬於任何旅程
                                passHolderId = null
                            )
                            onAddTicket(newTicket)
                            onNavigateBack()
                        }
                    ) {
                        Text("儲存")
                    }
                }
            )
        }
    ) { innerPadding ->
        // ✨ [修改] 為了容納更多輸入框，我們讓頁面可以上下滑動
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("從票券截圖匯入")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = titleState,
                onValueChange = { titleState = it },
                label = { Text("票券標題") }, // ✨ 標題不再提示使用者輸入，因為我們要自動填入
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                ),
                readOnly = false // ✨ 我們可以暫時把它設為只讀，代表這是自動生成的
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = typeState,
                onValueChange = { typeState = it },
                label = { Text("類型") },
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                ),
                readOnly = false
            )
            // ✨ [修改] 新增「日期」和「時間」的輸入框來顯示結果
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = dateState,
                onValueChange = { dateState = it },
                label = { Text("日期") },
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                ),
                readOnly = false
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = timeState,
                onValueChange = { timeState = it },
                label = { Text("時間") },
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                ),
                readOnly = false
            )
        }
    }
}
// ✨ [修改] 偵探社總指揮 -> 採用你提議的「優先級判斷」策略！
private fun parseTicket(text: String): Map<String, String> {
    // 優先判斷是否為台鐵
    if (isDefinitelyTRA(text)) {
        return parseTraTicket(text)

    }
    // 如果不是台鐵，再判斷是否為高鐵
    else if (isProbablyTHSR(text)) {
        return parseThsrTicket(text)
    }
    // 都不是，回報無法解析
    return emptyMap()
}

// ✨ 新增的台鐵「決定性證據」判斷器
private fun isDefinitelyTRA(text: String): Boolean {
    // 關鍵線索：台鐵的車種名稱
    val traTrainTypes = listOf("普悠瑪", "太魯閣", "自強", "莒光", "區間")
    if (traTrainTypes.any { text.contains(it) }) {
        return true
    }
    // 關鍵線索：出現「訂票代碼」文字 或是 「台鐵」,「臺鐵」文字
    val traCodeTypes = listOf("訂票代碼", "台鐵", "臺鐵")
    if (traCodeTypes.any { text.contains(it) }) {
        return true
    }
    return false
}

// ✨ [修改] isProbablyTHSR 現在會去 StationData 查資料
private fun isProbablyTHSR(text: String): Boolean {
    val hasThsrStation = StationData.thsrStations.any { text.contains(it) }
    val ticketNumberPattern = Pattern.compile("\\d{2}-\\d{1}-\\d{2}-\\d{1}-\\d{3}-\\d{4}")
    val hasTicketNumber = ticketNumberPattern.matcher(text).find()

    if (hasThsrStation && hasTicketNumber) return true
    if (text.contains("訂位代號")) return true // 舊的判斷邏輯保留，但優先級較低
    // ✨ 從新的中央資料庫查詢站名
    if (StationData.thsrStations.any { text.contains(it) }) return true
    return false
}


// ✨ [修改] 為台鐵專家加入解析日期和時間的 SOP
private fun parseTraTicket(text: String): Map<String, String> {
    val result = mutableMapOf<String, String>()

    // --- 解析起訖站 ---
    val traStations = StationData.traStations
    val condensedText = text.replace("\\s".toRegex(), "")
    val foundStationsInOrder = mutableListOf<String>()

    var searchIndex = 0
    while(searchIndex < condensedText.length) {
        val remainingText = condensedText.substring(searchIndex)
        // 找到第一個符合的站名
        val matchedStation = traStations.find { remainingText.startsWith(it) }

        if (matchedStation != null) {
            // 如果這個站名還沒被加進來過，就加進去
            if (!foundStationsInOrder.contains(matchedStation)) {
                foundStationsInOrder.add(matchedStation)
            }
            // 從找到的站名之後繼續搜尋
            searchIndex += matchedStation.length
        } else {
            // 沒找到，就往下一個字移動
            searchIndex++
        }
    }

    // 如果成功依序找到了至少兩個站名
    if (foundStationsInOrder.size >= 2) {
        val origin = foundStationsInOrder[0]
        val destination = foundStationsInOrder[1]
        result["title"] = "$origin → $destination"
        result["type"] = "台鐵"
    }

    // --- 解析日期和時間 ---
    val dateRegex = Regex("(\\d{4})[./-](\\d{2})[./-](\\d{2})")
    val timeRegex = Regex("(\\d{2}:\\d{2})")      // 範本：HH:MM

    dateRegex.find(text)?.let {
        result["date"] = it.value // 找到符合範本的，就存起來
    }
    timeRegex.find(text)?.let {
        result["time"] = it.value // 台鐵票通常第一個時間就是出發時間
    }

    return result
}



// ✨ [修改] 高鐵專家現在會去 StationData 查資料
private fun parseThsrTicket(text: String): Map<String, String> {
    val result = mutableMapOf<String, String>()
    // ✨ 從新的中央資料庫查詢站名
    val thsrStations = StationData.thsrStations
    val condensedText = text.replace("\\s".toRegex(), "")
    val foundStationsInOrder = mutableListOf<String>()

    var searchIndex = 0
    while(searchIndex < condensedText.length) {
        val remainingText = condensedText.substring(searchIndex)
        val matchedStation = thsrStations.find { remainingText.startsWith(it) }

        if (matchedStation != null) {
            if (!foundStationsInOrder.contains(matchedStation)) {
                foundStationsInOrder.add(matchedStation)
            }
            searchIndex += matchedStation.length
        } else {
            searchIndex++
        }
    }

    if (foundStationsInOrder.size >= 2) {
        val origin = foundStationsInOrder[0]
        val destination = foundStationsInOrder[1]
        result["title"] = "$origin → $destination"
        result["type"] = "高鐵"
    }
    // --- 解析日期和時間 ---
    // 高鐵日期格式通常是 YYYY/MM/DD
    val dateRegex = Regex("(\\d{4})/(\\d{2})/(\\d{2})")
    // 高鐵時間格式是 HH:MM，我們找第一個作為出發時間
    val timeRegex = Regex("(\\d{2}:\\d{2})")

    // 從原始有換行和空白的文字中尋找，比較準確
    dateRegex.find(text)?.let {
        result["date"] = it.value
    }
    timeRegex.find(text)?.let {
        result["time"] = it.value
    }

    return result
}