package com.ethy.passeon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// ✨ 我們把 TicketTypeStyle 的定義，提升到這個全域可見的檔案中
data class TicketTypeStyle(
    val typeName: String,
    val backgroundColor: Color,
    val contentColor: Color,
    val icon: ImageVector
)

// ✨ 建立一個樣板庫，未來可以不斷擴充
object TicketStyles {
    private val hsrColor = Color(0xFFFDEFE1)
    private val traColor = Color(0xFFE6F0FA)
    private val movieColor = Color(0xFFF3E5F5)
    private val membershipColor = Color(0xFFE0F2F1)
    private val flightColor = Color(0xFFE0F2F1)
    private val defaultColor = Color(0xFFF5F5F5)
    private val darkContentColor = Color(0xFF1F1F1F)


    val styles = mapOf(
        "高鐵" to TicketTypeStyle("高鐵", hsrColor, Color(0xFFE65100), Icons.Default.Train),
        "台鐵" to TicketTypeStyle("台鐵", traColor, Color(0xFF0D47A1), Icons.Default.Train),
        "電影票" to TicketTypeStyle("電影票", movieColor, Color(0xFF4A148C), Icons.Default.Movie),
        "會員卡" to TicketTypeStyle("會員卡", membershipColor, Color(0xFF004D40), Icons.Default.CardMembership),
        "登機證" to TicketTypeStyle("登機證", flightColor, Color(0xFF004D40), Icons.Default.Flight),
    )

    fun getStyle(type: String): TicketTypeStyle {
        return styles[type] ?: TicketTypeStyle(type, defaultColor, darkContentColor, Icons.Outlined.ConfirmationNumber)
    }
}


