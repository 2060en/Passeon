package com.ethy.passeon

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ✨ [新增] 我們定義一個更明確的「對話框狀態」
private sealed class DialogState {
    object CreatePassHolder : DialogState()
    // 未來可以新增 object EditTicket : DialogState() 等等
}
// ✨ [修改] Composable 函式現在會接收一個票夾列表
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTicketScreen(
    passHolders: List<PassHolder>, // ✨ 這裡把 passHolders 加回來，因為之後會用來選票夾
    onAddTicket: (Ticket) -> Unit,
    viewModel: PasseonViewModel
) {
    Log.d("AddTicketScreenDebug", "AddTicketScreen is being composed.")
    var selectedTemplate by remember { mutableStateOf<TicketTemplate?>(null) }
    val fieldValues = remember { mutableStateMapOf<String, String>() }

    // ✨ 1. 從 ViewModel 訂閱 OCR 的解析結果
    // collectAsStateWithLifecycle 可以更安全地處理生命週期
    val ocrResult by viewModel.parsedOcrResult.collectAsStateWithLifecycle()

    // ✨ 2. 使用 LaunchedEffect 來「回應」結果的變化
    LaunchedEffect(ocrResult) {
        // 如果結果不是空的 (代表 ViewModel 處理完了)
        if (ocrResult.isNotEmpty()) {
            val parsedType = ocrResult["type"]

            // 根據解析出的類型，自動選擇樣板
            selectedTemplate = TemplateRepository.findTemplate(parsedType)

            // 清空並預先填入解析出的值
            fieldValues.clear()
            ocrResult.forEach { (key, value) ->
                fieldValues[key] = value
            }

            // ✨ 重要：處理完畢後，通知 ViewModel 清空結果，避免重複觸發
            viewModel.clearOcrResult()
        }
    }

    val context = LocalContext.current
    // ✨ 3. 大幅簡化 imagePickerLauncher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                // ✨ 現在只需要把任務交給 ViewModel 就好！
                viewModel.processImage(context, it)
            }
        }
    )

    val currentTemplate = selectedTemplate

    if (currentTemplate == null) {
        // 如果還沒選樣板，顯示樣板選擇畫面，並加上匯入按鈕
        TemplateSelectionScreen(
            onTemplateSelected = { template ->
                selectedTemplate = template
                fieldValues.clear()
            },
            onImportClick = {
                imagePickerLauncher.launch("image/*")
            }
        )
    } else {
        // 如果選了樣板，就顯示動態產生的表單
        DynamicTicketForm(
            template = currentTemplate,
            fieldValues = fieldValues,
            onFieldValueChange = { key, value ->
                fieldValues[key] = value
            },
            onSave = {
                val title = fieldValues[currentTemplate.fields.first().key] ?: "無標題"
                val newTicket = Ticket(
                    id = 0,
                    title = title,
                    type = currentTemplate.typeName,
                    origin = fieldValues["origin"] ?: "",
                    destination = fieldValues["destination"] ?: "",
                    departureTimestamp = 0L, // TODO
                    seatInfo = fieldValues["seat_no"] ?: fieldValues["seat"],
                    imageUri = null,
                    customFields = fieldValues.toMap(),
                    passHolderId = null // TODO
                )
                onAddTicket(newTicket)
            },
            onBack = {
                selectedTemplate = null
            }
        )
    }
}

// ✨ 「樣板選擇」畫面，現在多了一個觸發匯入的按鈕
@Composable
private fun TemplateSelectionScreen(
    onTemplateSelected: (TicketTemplate) -> Unit,
    onImportClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.padding(16.dp)) {
            Button(onClick = onImportClick, modifier = Modifier.fillMaxWidth()) {
                Text("從圖片智慧匯入")
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("或手動選擇樣板：", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
            }
            items(TemplateRepository.templates) { template ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTemplateSelected(template) },
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = template.style.icon, contentDescription = template.typeName, tint = template.style.contentColor)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(template.typeName, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

// ✨ 「動態表單產生器」
@Composable
private fun DynamicTicketForm(
    template: TicketTemplate,
    fieldValues: Map<String, String>,
    onFieldValueChange: (key: String, value: String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 顯示一個可以返回樣板選擇的按鈕
        TextButton(onClick = onBack) {
            Text("返回選擇樣板")
        }

        Text(template.typeName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Divider()

        // 根據樣板定義的 fields 列表，動態產生所有輸入框
        template.fields.forEach { field ->
            TextField(
                value = fieldValues[field.key] ?: "",
                onValueChange = { onFieldValueChange(field.key, it) },
                label = { Text(field.label) },
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape ,
                colors = TextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
            )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Text("儲存")
        }
    }
}