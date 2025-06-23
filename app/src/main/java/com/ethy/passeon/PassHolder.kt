package com.ethy.passeon

/**
 * 這是我們 Phase 2 的新核心資料結構：「旅程」
 * 它像一個資料夾，用來歸納多張票券
 */
data class PassHolder(
    val id: Int,
    val name: String,        // 票夾名稱，例如：「2025 東京之旅」或「我的電影票根」
    val description: String? // 票夾的備註 (可選)
)

