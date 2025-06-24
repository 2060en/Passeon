package com.ethy.passeon

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ✨ [修改] 整個結構都用 Scaffold 包起來，並加上 TopAppBar
@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PassHolderScreen(
//    passHolders: List<PassHolder>,
//    tickets: List<Ticket>,
//    onNavigateToAddPassHolder: () -> Unit
//) {
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("我的票夾") },
//                // ✨ [修改] 在右上角新增一個「+」圖示按鈕
//                actions = {
//                    IconButton(onClick = onNavigateToAddPassHolder) {
//                        Icon(Icons.Default.Add, contentDescription = "新增票夾")
//                    }
//                }
//            )
//        }
//    ) { innerPadding ->
//        LazyColumn(
//            modifier = Modifier.padding(innerPadding),
//            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            items(passHolders) { passHolder ->
//                val ticketCount = tickets.count { it.passHolderId == passHolder.id }
//                PassHolderCard(passHolder = passHolder, ticketCount = ticketCount)
//            }
//        }
//    }
//}
@Composable
//fun PassHolderScreen(
//    passHolders: List<PassHolder>,
//    tickets: List<Ticket>,
//
//) {
//    LazyColumn(
//        contentPadding = PaddingValues(16.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        items(passHolders) { passHolder ->
//            val ticketCount = tickets.count { it.passHolderId == passHolder.id }
//            PassHolderCard(passHolder = passHolder, ticketCount = ticketCount)
//        }
//    }
//}

fun PassHolderScreen(
    passHolders: List<PassHolder>,
    tickets: List<Ticket>,
    viewModel: PasseonViewModel,
    onPassHolderClick: (Int) -> Unit
) {
    // ✨ [新增] 用一個狀態，記住使用者「正準備要刪除」哪一個票夾
    var passHolderToDelete by remember { mutableStateOf<PassHolder?>(null) }

    // 如果 passHolderToDelete 不是空的，就顯示確認對話框
    passHolderToDelete?.let { passHolder ->
        // 我們可以直接重複使用之前在 TimelineScreen 裡建立的對話框元件
        DeleteConfirmationDialog(
            itemType = "票夾",
            itemName = passHolder.name,
            onConfirm = {
                viewModel.deletePassHolder(passHolder)
                passHolderToDelete = null // 刪除後，清空狀態
            },
            onDismiss = {
                passHolderToDelete = null // 取消，同樣清空狀態
            }
        )
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(passHolders) { passHolder ->
            val ticketCount = tickets.count { it.passHolderId == passHolder.id }
            PassHolderCard(
                passHolder = passHolder,
                ticketCount = ticketCount,
                onClick = { onPassHolderClick(passHolder.id) }, // ✨ [新增] 當卡片被長按時，更新我們要刪除的目標
                onLongClick = { passHolderToDelete = passHolder }
            )
        }
    }
}

// ✨ [正名]
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassHolderCard(
    passHolder: PassHolder,
    ticketCount: Int,
    onClick: () -> Unit, // ✨ 卡片現在知道自己可以被點擊了
    onLongClick: () -> Unit
) {
    Card(
        // ✨ 用 clickable 修飾符讓整張卡片都可以被點擊
        modifier = Modifier
            .fillMaxWidth()
            // ✨ 使用 pointerInput 來同時偵測「點擊」和「長按」
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },

        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = passHolder.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            passHolder.description?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "包含 $ticketCount 張票券",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

