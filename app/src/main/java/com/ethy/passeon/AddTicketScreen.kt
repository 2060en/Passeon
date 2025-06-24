package com.ethy.passeon

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions

// ✨ [新增] 我們定義一個更明確的「對話框狀態」
private sealed class DialogState {
    object CreatePassHolder : DialogState()
    // 未來可以新增 object EditTicket : DialogState() 等等
}
// ✨ [修改] Composable 函式現在會接收一個票夾列表
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTicketScreen(
    passHolders: List<PassHolder>,
    onNavigateBack: () -> Unit,
    onAddTicket: (Ticket) -> Unit,
    viewModel: PasseonViewModel
) {
    // ✨ [修改] 我們現在用一個 Map 來儲存所有解析出的資訊，更有彈性
    var parsedInfo by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedPassHolder by remember { mutableStateOf<PassHolder?>(null) }
    // ✨ [修改] 不再用 Boolean，而是用我們定義好的 DialogState 來管理狀態
    var activeDialog by remember { mutableStateOf<DialogState?>(null) }
    // ✨ [修改] 根據 activeDialog 的狀態，來決定要顯示哪個對話框
    when (activeDialog) {
        is DialogState.CreatePassHolder -> {
            CreatePassHolderDialog(
                onDismiss = { activeDialog = null },
                onConfirm = { newName ->
                    viewModel.insertPassHolder(PassHolder(name = newName, description = ""))
                    activeDialog = null
                }
            )
        }
        null -> {
            // 當 activeDialog 是 null 時，不顯示任何對話框
        }
    }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val image = InputImage.fromFilePath(context, it)
                val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        Log.d("PasseonDebug", "OCR Result: ---START---\n${visionText.text}\n---END---")
                        val result = TicketParser.parse(visionText.text)
                        if (result.isEmpty()) {
                            parsedInfo = mapOf("title" to "無法解析的票券")
                        } else {
                            parsedInfo = result
                        }
                    }
                    .addOnFailureListener { e ->
                        parsedInfo = mapOf("title" to "辨識失敗: ${e.message}")
                    }
            }
        }
    )
    // ✨ [修改] 我們從 parsedInfo 這個 Map 中安全地取出 customFields
    val customFields = parsedInfo["customFields"] as? Map<String, String> ?: emptyMap()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("從票券截圖匯入")
        }

        ExposedDropdownMenuBox(
            expanded = isDropdownExpanded,
            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
        ) {
            TextField(
                value = selectedPassHolder?.name ?: "不指定",
                onValueChange = {},
                readOnly = true,
                label = { Text("選擇票夾") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                    )
            )
            ExposedDropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("＋ 新增票夾...") },
                    onClick = {
                        // ✨ [修改] 點擊時，直接更新 activeDialog 的狀態
                        activeDialog = DialogState.CreatePassHolder
                        isDropdownExpanded = false
                    }
                )
                Divider()
                DropdownMenuItem(
                    text = { Text("不指定") },
                    onClick = {
                        selectedPassHolder = null
                        isDropdownExpanded = false
                    }
                )
                passHolders.forEach { passHolder ->
                    DropdownMenuItem(
                        text = { Text(passHolder.name) },
                        onClick = {
                            selectedPassHolder = passHolder
                            isDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Divider()
        // ✨ [修改] 所有 TextField 的 value 都改成從 parsedInfo 或 customFields 中讀取
        // 我們讓使用者可以手動修改解析結果
        var titleState by remember(parsedInfo) { mutableStateOf(parsedInfo["title"] as? String ?: "") }
        var typeState by remember(parsedInfo) { mutableStateOf(parsedInfo["type"] as? String ?: "") }
        var dateState by remember(parsedInfo) { mutableStateOf(parsedInfo["date"] as? String ?: "") }
        var timeState by remember(parsedInfo) { mutableStateOf(parsedInfo["time"] as? String ?: "") }
        var trainNoState by remember(customFields) { mutableStateOf(customFields["車次"] ?: "") }
        var carNoState by remember(customFields) { mutableStateOf(customFields["車廂"] ?: "") }
        var seatNoState by remember(customFields) { mutableStateOf(customFields["座位"] ?: "") }
        TextField(
                    value = titleState,
                    onValueChange = { titleState = it },
                    label = { Text("票券標題") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                    ))
        TextField(value = typeState, onValueChange = { typeState = it }, label = { Text("類型") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                    ))
        TextField(value = dateState, onValueChange = { dateState = it }, label = { Text("日期") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                    ))
        TextField(value = timeState, onValueChange = { timeState = it }, label = { Text("時間") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                    ))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(value = trainNoState, onValueChange = { trainNoState = it }, label = { Text("車次") }, modifier = Modifier.weight(1f), shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                ))
            TextField(value = carNoState, onValueChange = { carNoState = it }, label = { Text("車廂") }, modifier = Modifier.weight(1f), shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                ))
            TextField(value = seatNoState, onValueChange = { seatNoState = it }, label = { Text("座位") }, modifier = Modifier.weight(1f), shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                ))

        }

        // ✨ 這是我們新的「儲存」按鈕，它的邏輯是正確的
        Button(
            onClick = {
                val finalCustomFields = mutableMapOf<String, String>()
                if (trainNoState.isNotBlank()) finalCustomFields["車次"] = trainNoState
                if (carNoState.isNotBlank()) finalCustomFields["車廂"] = carNoState
                if (seatNoState.isNotBlank()) finalCustomFields["座位"] = seatNoState

                val newTicket = Ticket(
                    id = 0,
                    title = titleState,
                    type = typeState,
                    origin = "",
                    destination = "",
                    departureTimestamp = 0L,
                    seatInfo = seatNoState,
                    imageUri = null,
                    customFields = finalCustomFields, // ✨ 儲存最終的魔術袋內容
                    passHolderId = selectedPassHolder?.id
                )
                onAddTicket(newTicket)
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("儲存")
        }
    }
}

@Composable
fun CreatePassHolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增票夾") },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("票夾名稱") }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank()
            ) {
                Text("確定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
