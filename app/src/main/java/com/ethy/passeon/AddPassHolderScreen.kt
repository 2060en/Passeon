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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新增票夾", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val newPassHolder = PassHolder(
                                id = (0..10000).random(),
                                name = nameState,
                                description = descriptionState
                            )
                            onAddPassHolder(newPassHolder)
                            onNavigateBack()
                        }
                    ) {
                        Text("儲存")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
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
        }
    }
}

