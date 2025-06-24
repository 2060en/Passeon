package com.ethy.passeon


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp



// ✨ [修改] TimelineScreen 現在非常乾淨，不再有 Scaffold 和 FAB
@Composable
fun TimelineScreen(
    tickets: List<Ticket>,
    viewModel: PasseonViewModel // ✨ 為了執行刪除，我們需要直接跟 ViewModel 溝通


) {
    // ✨ 用一個狀態，記住使用者「正準備要刪除」哪一張票
    var ticketToDelete by remember { mutableStateOf<Ticket?>(null) }

    // 如果 ticketToDelete 不是空的，就顯示確認對話框
    ticketToDelete?.let { ticket ->
        DeleteConfirmationDialog(
            itemType = "票券",
            itemName = ticket.title,
            onConfirm = {
                viewModel.deleteTicket(ticket)
                ticketToDelete = null // 刪除後，將狀態清空，關閉對話框
            },
            onDismiss = {
                ticketToDelete = null // 使用者取消，同樣清空狀態
            }
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(tickets, key = { it.id }) { ticket ->
            TicketCard(
                ticket = ticket,
                // ✨ 當卡片被長按時，更新我們要刪除的目標
                onLongClick = { ticketToDelete = ticket }
            )
        }
    }
}



