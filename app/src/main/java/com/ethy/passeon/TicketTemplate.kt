package com.ethy.passeon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Train
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// 定義一個欄位的輸入類型
enum class FieldType {
    TEXT,
    NUMBER,
    DATE,
    TIME
}

// 定義一個樣板中，單一欄位的規格
data class TemplateField(
    val key: String,       // 儲存到資料庫用的獨特鍵值，例如 "movie_title"
    val label: String,     // 顯示在輸入框上的提示文字，例如 "電影名稱"
    val fieldType: FieldType = FieldType.TEXT // 欄位的輸入類型
)

// 定義一個完整的票券樣板
data class TicketTemplate(
    val typeName: String,               // 樣板的名稱，例如 "電影票"
    val fields: List<TemplateField>,    // 這個樣板需要的所有欄位列表
    val style: TicketTypeStyle          // 這個樣板對應的卡片樣式
)

// 建立一個「樣板倉庫」，用來存放所有我們預先定義好的樣板
object TemplateRepository {

    // 我們可以重複使用之前定義好的卡片樣式物件
    private val hsrStyle = TicketStyles.getStyle("高鐵")
    private val traStyle = TicketStyles.getStyle("台鐵")
    private val boardingpassStyle = TicketStyles.getStyle("登機證")
    private val movieStyle = TicketStyles.getStyle("電影票")
    private val membershipStyle = TicketTypeStyle("會員卡", Color(0xFFE0F2F1), Color(0xFF004D40), Icons.Default.CardMembership)

    // 這是我們目前擁有的所有樣板
    val templates = listOf(
        TicketTemplate(
            typeName = "高鐵",
            fields = listOf(
                TemplateField(key = "origin", label = "起點"),
                TemplateField(key = "destination", label = "迄點"),
                TemplateField(key = "date", label = "日期", fieldType = FieldType.DATE),
                TemplateField(key = "time", label = "時間", fieldType = FieldType.TIME),
                TemplateField(key = "train_no", label = "車次"),
                TemplateField(key = "car_no", label = "車廂"),
                TemplateField(key = "seat_no", label = "座位")
            ),
            style = hsrStyle
        ),
        TicketTemplate(
            typeName = "台鐵",
            fields = listOf(
                TemplateField(key = "origin", label = "起點"),
                TemplateField(key = "destination", label = "迄點"),
                TemplateField(key = "date", label = "日期", fieldType = FieldType.DATE),
                TemplateField(key = "time", label = "時間", fieldType = FieldType.TIME),
                TemplateField(key = "train_no", label = "車次"),
                TemplateField(key = "car_no", label = "車廂"),
                TemplateField(key = "seat_no", label = "座位")
            ),
            style = traStyle
        ),
        TicketTemplate(
            typeName = "電影票",
            fields = listOf(
                TemplateField(key = "movie_title", label = "電影名稱"),
                TemplateField(key = "cinema", label = "影城"),
                TemplateField(key = "hall", label = "影廳"),
                TemplateField(key = "seat", label = "座位"),
                TemplateField(key = "show_time", label = "放映時間", fieldType = FieldType.TIME)
            ),
            style = movieStyle
        ),
        TicketTemplate(
            typeName = "會員卡",
            fields = listOf(
                TemplateField(key = "card_name", label = "會員卡名稱"),
                TemplateField(key = "card_number", label = "會員編號", fieldType = FieldType.NUMBER),
                TemplateField(key = "member_name", label = "持卡人姓名")
            ),
            style = membershipStyle
        ),
        TicketTemplate(
            typeName = "登機證",
            fields = listOf(
                TemplateField(key = "flight_no", label = "航班號碼"),
                TemplateField(key = "passenger_name", label = "乘客姓名"),
                TemplateField(key = "boarding_time", label = "登機時間", fieldType = FieldType.TIME)
            ),
            style = boardingpassStyle
        )
        
    )

    fun findTemplate(typeName: String?): TicketTemplate? {
        return templates.find { it.typeName == typeName }
    }
    
    
}

