package com.ethy.passeon

/**
 * 這是我們 Passeon App 的核心資料結構：「票券」
 * v1.2 版本 - 新增了 tripId，用來跟「旅程」產生關聯
 */
data class Ticket(
    val id: Int,
    val title: String,
    val type: String,
    val origin: String,
    val destination: String,
    val departureTimestamp: Long,
    val seatInfo: String,
    val imageUri: String?,
    val customFields: Map<String, String>,
    // ✨ [正名] 將 tripId 改為 passHolderId，語意更清晰
    val passHolderId: Int?
)