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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ✨ 這是全新的「特定類型詳情」頁面
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeDetailsScreen(
    typeName: String, // 接收傳來的類型名稱，例如 "高鐵"
    tickets: List<Ticket>,
    //onNavigateBack: () -> Unit,
    viewModel: PasseonViewModel

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
                ticketToDelete = null // 刪除後，將狀態清空，關閉對話框
            },
            onDismiss = {
                ticketToDelete = null // 使用者取消，同樣清空狀態
            }
        )
    }

    // 過濾出所有符合該類型的票券
    val filteredTickets = tickets.filter { it.type == typeName }
    LazyColumn(
        // modifier = Modifier.padding(innerPadding), // ✨ 移除 innerPadding，改為直接使用 Modifier.fillMaxSize().padding(16.dp) 或僅 contentPadding
        contentPadding = PaddingValues(16.dp), // 直接在這裡設定內容邊距
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(filteredTickets) { ticket ->
            TicketCard(
                ticket = ticket,
                onLongClick = { ticketToDelete = ticket }
            )
        }
    }
}

