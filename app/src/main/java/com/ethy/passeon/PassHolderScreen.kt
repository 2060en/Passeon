package com.ethy.passeon

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ✨ [修改] 加上 Scaffold 和 FAB
@Composable
fun PassHolderScreen(
    passHolders: List<PassHolder>,
    tickets: List<Ticket>,
    onNavigateToAddPassHolder: () -> Unit // ✨ 新增參數，用來接收「如何跳去新增頁」的指令
) {
    Scaffold(
        // ✨ [修改] 將 FloatingActionButton 換成 ExtendedFloatingActionButton
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("新增票夾") },
                icon = { Icon(Icons.Default.Add, contentDescription = "新增票夾") },
                onClick = onNavigateToAddPassHolder,
                modifier = Modifier.height(72.dp) // ✨ 加上跟主畫面一樣的高度
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(passHolders) { passHolder ->
                val ticketCount = tickets.count { it.passHolderId == passHolder.id }
                PassHolderCard(passHolder = passHolder, ticketCount = ticketCount)
            }
        }
    }
}

// ✨ [正名]
@Composable
fun PassHolderCard(passHolder: PassHolder, ticketCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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