package com.ethy.passeon

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ✨ 全新的「票夾內容」頁面
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassHolderDetailScreen(
    tickets: List<Ticket>,
    viewModel: PasseonViewModel // ✨ 為了執行刪除，我們需要總經理
) {
    // ✨ [新增] 用一個狀態，記住使用者「正準備要刪除」哪一張票
    var ticketToDelete by remember { mutableStateOf<Ticket?>(null) }

    // 如果 ticketToDelete 不是空的，就顯示確認對話框
    ticketToDelete?.let { ticket ->
        DeleteConfirmationDialog(
            itemType = "票券",
            itemName = ticket.title,
            onConfirm = {
                viewModel.deleteTicket(ticket)
                ticketToDelete = null
            },
            onDismiss = {
                ticketToDelete = null
            }
        )
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(tickets, key = { it.id }) { ticket ->
            // ✨ 我們可以直接沿用已經支援長按的 TicketCard
            TicketCard(
                ticket = ticket,
                onLongClick = { ticketToDelete = ticket }
            )
        }
    }
}