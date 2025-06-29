package com.ethy.passeon

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.node.CanFocusChecker.start
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 主入口：根據票券類型，選擇要顯示哪種卡片
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TicketCard(
    ticket: Ticket,
    onLongClick: () -> Unit
) {
    // ✨ [Debug] 在這裡印出 TicketCard 收到的最原始的資料！
    Log.d("PasseonDebug", "Rendering TicketCard with data: $ticket")
    // 為了預覽，我們先假設有這些資訊
    val origin = ticket.origin
    val destination = ticket.destination
    // ✨ [修改] 從時間戳反向翻譯回看得懂的日期格式
    val date = try {
        // 如果時間戳不是預設的 0，就進行翻譯
        if (ticket.departureTimestamp != 0L) {
            val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            sdf.format(Date(ticket.departureTimestamp))
        } else {
            // 如果是 0，就從 customFields 裡找看看有沒有舊的日期文字
            ticket.customFields["date"] ?: ""
        }
    } catch (e: Exception) {
        Log.e("TicketCard", "日期格式化失敗: ${e.message}")
        "" // 如果翻譯失敗，就顯示空白
    }

    when (ticket.type) {
        "高鐵" -> ThsrTicketCard(origin = origin, destination = destination, date = date, onLongClick = onLongClick)
        "台鐵" -> TraTicketCard(origin = origin, destination = destination, date = date, onLongClick = onLongClick)
        else -> DefaultTicketCard(ticket = ticket, onLongClick = onLongClick) // 其他類型暫時用預設樣式
    }
}


// --- 高鐵卡片樣板 (V5 - 圖片背景版) ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ThsrTicketCard(origin: String, destination: String, date: String, onLongClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) { detectTapGestures(onLongPress = { onLongClick() }) },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            // ✨ [修改] 使用 Image 元件來顯示我們的背景圖
            Image(
                painter = painterResource(id = R.drawable.bg_thsr_card), // 讀取 drawable 裡的圖檔
                contentDescription = "高鐵票券背景",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds // 拉伸圖片以填滿整個 Box
            )

            // 日期，疊加在圖片右上角
            Text(
                text = date,
                fontSize = 14.sp,
                color = DarkGray,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )

            // 起訖站，疊加在圖片正中間
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(y = 8.5.dp) // 往下移動 15.dp，你可以調整這個數值
                    .padding(start = 80.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(origin, fontSize = 25.sp, color = DarkGray)
                Spacer(modifier = Modifier.width(40.dp))

                Spacer(modifier = Modifier.width(40.dp))
                Text(destination, fontSize = 25.sp, color = DarkGray)
            }
        }
    }
}

// --- 台鐵卡片樣板 (V5 - 圖片背景版) ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TraTicketCard(origin: String, destination: String, date: String, onLongClick: () -> Unit) {


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) { detectTapGestures(onLongPress = { onLongClick() }) },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp), // 給定一個固定高度
            contentAlignment = Alignment.CenterStart
        ) {
            // ✨ [修改] 使用 Image 元件來顯示我們的背景圖
            Image(
                painter = painterResource(id = R.drawable.bg_tra_card), // 讀取 drawable 裡的圖檔
                contentDescription = "台鐵票券背景",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            // 文字內容，疊加在圖片上
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 因為 Logo 已經在背景圖裡了，所以我們用一個空白來佔位，把文字往右推
                Spacer(modifier = Modifier.size(110.dp))

                Column {
                    Text(
                        "$origin - $destination",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(date, fontSize = 18.sp, color = DarkGray)
                }
            }
        }
    }
}


// --- 預設卡片樣板 ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DefaultTicketCard(ticket: Ticket, onLongClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) { detectTapGestures(onLongPress = { onLongClick() }) },
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Text(ticket.title)
        }
    }
}
