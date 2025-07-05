package com.ethy.passeon

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ✨ 全新的「新增票夾」頁面
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPassHolderScreen(
    onNavigateBack: () -> Unit,
    onAddPassHolder: (PassHolder) -> Unit,
    viewModel: PasseonViewModel
) {
    var nameState by remember { mutableStateOf("") }
    var descriptionState by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) { // ✨ 直接使用 Column 並設定填滿和邊距
        TextField(
            value = nameState,
            onValueChange = { nameState = it },
            label = { Text("票夾名稱 (例如：我的電影票)") },
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = descriptionState,
            onValueChange = { descriptionState = it },
            label = { Text("備註 (可選)") },
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
            )
        )
        // ✨ 新增「儲存」按鈕
        Spacer(modifier = Modifier.height(24.dp)) // 提供一些間距
        Button(
            onClick = {
                val newPassHolder = PassHolder(
                    // id = (0..10000).random(), // ✨ 移除手動生成 ID，讓 Room 自動處理
                    name = nameState,
                    description = descriptionState
                )
                onAddPassHolder(newPassHolder)
                // onNavigateBack() // ✨ 移除此行，因為導航已經在 MainActivity 的 onAddPassHolder 處理
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = nameState.isNotBlank() // 只有名稱不為空時才能儲存
        ) {
            Text("儲存")
        }
    }
}

