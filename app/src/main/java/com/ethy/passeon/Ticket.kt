package com.ethy.passeon

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
/**
 * 這是我們 Passeon App 的核心資料結構：「票券」
 * v1.2 版本 - 新增了 tripId，用來跟「旅程」產生關聯
 */
@Entity(tableName = "tickets")
data class Ticket(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val type: String,
    val origin: String,
    val destination: String,
    val departureTimestamp: Long,
    val seatInfo: String?,
    val imageUri: String?,
    // ✨ [修改] 告訴檔案櫃，先不要理這個欄位，我們之後再處理
    @Ignore
    val customFields: Map<String, String>,
    @ColumnInfo(name = "pass_holder_id")
    val passHolderId: Int?
) {
    // ✨ Room 需要一個沒有 @Ignore 欄位的建構子，所以我們提供一個讓它使用
    constructor(
        id: Int = 0,
        title: String,
        type: String,
        origin: String,
        destination: String,
        departureTimestamp: Long,
        seatInfo: String?,
        imageUri: String?,
        passHolderId: Int?
    ) : this(id, title, type, origin, destination, departureTimestamp, seatInfo, imageUri, emptyMap(), passHolderId)
}