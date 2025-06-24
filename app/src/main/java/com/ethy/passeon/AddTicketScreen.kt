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

// ✨ [修改] Composable 函式現在會接收一個票夾列表
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTicketScreen(
    passHolders: List<PassHolder>,
    onNavigateBack: () -> Unit,
    onAddTicket: (Ticket) -> Unit
) {
    var titleState by remember { mutableStateOf("") }
    var typeState by remember { mutableStateOf("") }
    var dateState by remember { mutableStateOf("") }
    var timeState by remember { mutableStateOf("") }
    var trainNoState by remember { mutableStateOf("") }
    var carNoState by remember { mutableStateOf("") }
    var seatNoState by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedPassHolder by remember { mutableStateOf<PassHolder?>(null) }

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

                        val parsedResult = TicketParser.parse(visionText.text)
                        if (parsedResult.isEmpty()) {
                            titleState = "無法解析的票券"
                            typeState = ""
                        } else {
                            titleState = parsedResult["title"] ?: ""
                            typeState = parsedResult["type"] ?: ""
                            dateState = parsedResult["date"] ?: ""
                            timeState = parsedResult["time"] ?: ""
                            // ✨ [修改] 將解析出的新資訊，填入對應的狀態
                            trainNoState = parsedResult["trainNo"] ?: ""
                            carNoState = parsedResult["carNo"] ?: ""
                            seatNoState = parsedResult["seatNo"] ?: ""
                        }
                    }
                    .addOnFailureListener { e ->
                        // 辨識失敗的處理可以更細緻，但我們先簡化
                        titleState = "辨識失敗"
                    }
            }
        }
    )

//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("新增票券", fontWeight = FontWeight.Bold) },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
//                    }
//                },
//                actions = {
//                    TextButton(
//                        onClick = {
//                            val newTicket = Ticket(
//                                id = (0..10000).random(),
//                                title = titleState,
//                                type = typeState,
//                                origin = "待填寫",
//                                destination = "待填寫",
//                                departureTimestamp = System.currentTimeMillis(),
//                                seatInfo = "",
//                                imageUri = null,
//                                customFields = emptyMap(),
//                                // ✨ [修改] 儲存時，使用選中的票夾 ID
//                                passHolderId = selectedPassHolder?.id
//                            )
//                            onAddTicket(newTicket)
//                            onNavigateBack()
//                        }
//                    ) {
//                        Text("儲存")
//                    }
//                }
//            )
//        }
//    ) { innerPadding ->
//        // ✨ [修改] 為了容納更多輸入框，我們讓頁面可以上下滑動
//        Column(
//            modifier = Modifier
//                .padding(innerPadding)
//                .padding(16.dp)
//                .verticalScroll(rememberScrollState())
//        ) {
//            Button(
//                onClick = { imagePickerLauncher.launch("image/*") },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("從票券截圖匯入")
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // ✨ [修改] 加入全新的「下拉選單」元件
//            ExposedDropdownMenuBox(
//                expanded = isDropdownExpanded,
//                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
//            ) {
//                TextField(
//                    value = selectedPassHolder?.name ?: "不指定", // 顯示選中的票夾名稱，或預設文字
//                    onValueChange = {},
//                    readOnly = true,
//                    label = { Text("選擇票夾") },
//                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
//                    modifier = Modifier
//                        .menuAnchor() // 告訴系統這是下拉選單的「錨點」
//                        .fillMaxWidth(),
//                    shape = CircleShape,
//                    colors = TextFieldDefaults.colors(
//                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
//                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
//                        unfocusedIndicatorColor = Color.Transparent,
//                        focusedIndicatorColor = Color.Transparent,
//                    )
//                )
//                ExposedDropdownMenu(
//                    expanded = isDropdownExpanded,
//                    onDismissRequest = { isDropdownExpanded = false }
//                ) {
//                    // 第一個選項是「不指定」
//                    DropdownMenuItem(
//                        text = { Text("不指定") },
//                        onClick = {
//                            selectedPassHolder = null
//                            isDropdownExpanded = false
//                        }
//                    )
//                    // 根據傳進來的票夾列表，動態產生所有選項
//                    passHolders.forEach { passHolder ->
//                        DropdownMenuItem(
//                            text = { Text(passHolder.name) },
//                            onClick = {
//                                selectedPassHolder = passHolder
//                                isDropdownExpanded = false
//                            }
//                        )
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//            Divider()
//            Spacer(modifier = Modifier.height(16.dp))
//
//            TextField(
//                value = titleState,
//                onValueChange = { titleState = it },
//                label = { Text("票券標題") }, // ✨ 標題不再提示使用者輸入，因為我們要自動填入
//                modifier = Modifier.fillMaxWidth(),
//                shape = CircleShape,
//                colors = TextFieldDefaults.colors(
//                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
//                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
//                    unfocusedIndicatorColor = Color.Transparent,
//                    focusedIndicatorColor = Color.Transparent,
//                ),
//                readOnly = false // ✨ 我們可以暫時把它設為只讀，代表這是自動生成的
//            )
//            Spacer(modifier = Modifier.height(16.dp))
//            TextField(
//                value = typeState,
//                onValueChange = { typeState = it },
//                label = { Text("類型") },
//                modifier = Modifier.fillMaxWidth(),
//                shape = CircleShape,
//                colors = TextFieldDefaults.colors(
//                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
//                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
//                    unfocusedIndicatorColor = Color.Transparent,
//                    focusedIndicatorColor = Color.Transparent,
//                ),
//                readOnly = false
//            )
//            // ✨ [修改] 新增「日期」和「時間」的輸入框來顯示結果
//            Spacer(modifier = Modifier.height(16.dp))
//            TextField(
//                value = dateState,
//                onValueChange = { dateState = it },
//                label = { Text("日期") },
//                modifier = Modifier.fillMaxWidth(),
//                shape = CircleShape,
//                colors = TextFieldDefaults.colors(
//                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
//                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
//                    unfocusedIndicatorColor = Color.Transparent,
//                    focusedIndicatorColor = Color.Transparent,
//                ),
//                readOnly = false
//            )
//            Spacer(modifier = Modifier.height(16.dp))
//            TextField(
//                value = timeState,
//                onValueChange = { timeState = it },
//                label = { Text("時間") },
//                modifier = Modifier.fillMaxWidth(),
//                shape = CircleShape,
//                colors = TextFieldDefaults.colors(
//                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
//                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
//                    unfocusedIndicatorColor = Color.Transparent,
//                    focusedIndicatorColor = Color.Transparent,
//                ),
//                readOnly = false
//            )
//            // ✨ [修改] 新增「車次」、「車廂」和「座位」的輸入框來顯示結果
//            Spacer(modifier = Modifier.height(16.dp))
//            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                TextField(
//                    value = trainNoState,
//                    onValueChange = { trainNoState = it },
//                    label = { Text("車次") },
//                    modifier = Modifier.weight(1f),
//                    shape = CircleShape,
//                    colors = TextFieldDefaults.colors(
//                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
//                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
//                        unfocusedIndicatorColor = Color.Transparent,
//                        focusedIndicatorColor = Color.Transparent,
//                    ),
//                    readOnly = false
//                )
//                TextField(
//                    value = carNoState,
//                    onValueChange = { carNoState = it },
//                    label = { Text("車廂") },
//                    modifier = Modifier.weight(1f),
//                    shape = CircleShape,
//                    colors = TextFieldDefaults.colors(
//                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
//                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
//                        unfocusedIndicatorColor = Color.Transparent,
//                        focusedIndicatorColor = Color.Transparent,
//                    ),
//                    readOnly = false
//                )
//                TextField(
//                    value = seatNoState,
//                    onValueChange = { seatNoState = it },
//                    label = { Text("座位") },
//                    modifier = Modifier.weight(1f),
//                    shape = CircleShape,
//                    colors = TextFieldDefaults.colors(
//                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
//                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
//                        unfocusedIndicatorColor = Color.Transparent,
//                        focusedIndicatorColor = Color.Transparent,
//                    ),
//                    readOnly = false
//                )
//            }
//        }
//    }
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
                val newTicket = Ticket(
                    id = (0..10000).random(),
                    title = titleState,
                    type = typeState,
                    origin = "", // TODO
                    destination = "", // TODO
                    departureTimestamp = 0L, // TODO
                    seatInfo = seatNoState, // TODO
                    imageUri = null,
                    customFields = mapOf("車次" to trainNoState, "車廂" to carNoState),
                    passHolderId = selectedPassHolder?.id
                )
                // ✨ 直接呼叫從 MainActivity 傳來的「二合一」指令
                onAddTicket(newTicket)
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("儲存")
        }
    }
}