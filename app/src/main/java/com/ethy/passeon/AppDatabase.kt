package com.ethy.passeon

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * ✨ Passeon 的資料庫總管理處 (檔案櫃實體)
 * 連結所有 Entities 和 DAO
 */
@Database(entities = [PassHolder::class, Ticket::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // 告訴資料庫，我們的檔案管理員是誰
    abstract fun passeonDao(): PasseonDao

    // ✨ 使用「單例模式」確保整個 App 只有一個資料庫實體
    companion object {
        // @Volatile 確保這個變數的值在所有執行緒中都是最新的
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // 如果 INSTANCE 已經存在，就直接回傳
            // 如果不存在，就進入 synchronized 區塊來建立它
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "passeon_database" // 我們資料庫檔案的名稱
                ).build()
                INSTANCE = instance
                // 回傳新建好的實體
                instance
            }
        }
    }
}
