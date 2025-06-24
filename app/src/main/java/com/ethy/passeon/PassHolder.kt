package com.ethy.passeon
import androidx.room.Entity
import androidx.room.PrimaryKey
/**
 * ✨ [修改] 加上 @Entity 標籤，讓它成為一個標準化的資料卡片
 */
@Entity(tableName = "pass_holders") // 我們為這個抽屜取名叫 "pass_holders"
data class PassHolder(
    @PrimaryKey(autoGenerate = true) // ✨ 告訴檔案櫃這是身分證號，而且要自動產生
    val id: Int = 0, // ✨ 給 id 一個預設值 0 是個好習慣
    val name: String,        // 票夾名稱，例如：「2025 東京之旅」或「我的電影票根」
    val description: String? // 票夾的備註 (可選)
)

