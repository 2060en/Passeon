package com.ethy.passeon

import android.app.Application

/**
 * ✨ Passeon 的「公司總部」
 * 這是 App 生命週期的起點，也是建立全域資源 (如資料庫) 的最佳地點。
 */
class PasseonApplication : Application() {

    // 我們使用 by lazy 這個技巧，來確保資料庫的實體 (instance)
    // 只會在它第一次被需要時才建立，並且整個 App 從頭到尾都只會有這一個實體。
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}