package com.ethy.passeon

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TypesScreen(
    tickets: List<Ticket>,
    navController: NavController,
    viewModel: PasseonViewModel

) {
    var expandedType by remember { mutableStateOf<String?>(null) }
    // ✨ [新增] 用一個狀態，記住使用者「正準備要刪除」哪一張票
    var ticketToDelete by remember { mutableStateOf<Ticket?>(null) }

    val groupedTickets = tickets.groupBy { it.type }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp) // 給每個膠囊之間一點間距
    ) {
        groupedTickets.forEach { (type, ticketsInGroup) ->
            // 1. 可點擊的標題
            item {
                Column {
                    Row(
                        // ✨ [修改] 我們對這個 Modifier 做了大改造
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape) // 1. 先把整個元件裁切成膠囊的形狀
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) // 2. 再為它上一個淺灰色背景
                            .clickable { // 3. 讓它可以被點擊
                                expandedType = if (expandedType == type) null else type
                            }
                            .padding(horizontal = 16.dp, vertical = 16.dp), // 4. 最後加上內邊距，讓文字和圖示不會貼邊
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expandedType == type) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "展開/收合"
                        )
                    }

                    // 2. 動畫 + 條件顯示區塊
                    AnimatedVisibility(visible = expandedType == type) {
                        Column(modifier = Modifier.padding(top = 8.dp)) { // 讓展開的內容跟膠囊有點間距
                            val recentTickets = ticketsInGroup.sortedByDescending { it.departureTimestamp }.take(5)
                            recentTickets.forEach { ticket ->
                                Box(modifier = Modifier.padding(vertical = 4.dp)) {
                                    TicketCard(
                                        ticket = ticket,
                                        onLongClick = { ticketToDelete = ticket }
                                    )
                                }
                            }

                            if (ticketsInGroup.size > 5) {
                                TextButton(
                                    onClick = { navController.navigate("type_details/${type}") },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("查看更多...")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}