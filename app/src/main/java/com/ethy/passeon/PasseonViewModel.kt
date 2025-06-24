package com.ethy.passeon

import androidx.lifecycle.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
