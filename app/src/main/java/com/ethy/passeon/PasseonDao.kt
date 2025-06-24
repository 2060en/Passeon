package com.ethy.passeon

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * ✨ Passeon 的檔案管理員 (DAO)
 * 我們在這裡定義所有對資料庫的存取操作
 */
@Dao
interface PasseonDao {

    // --- 票夾 (PassHolder) 的操作 ---

    /**
     * 新增一個票夾
     * onConflict = OnConflictStrategy.IGNORE 代表如果ID衝突，就忽略這次新增
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPassHolder(passHolder: PassHolder)

    /**
     * 取得所有的票夾，並按照ID降序排列 (最新的在最前面)
     * 回傳值是 Flow<List<PassHolder>>，這就像一個「即時更新的水管」
     * 只要資料庫裡的票夾有任何變化，這個水管就會立刻流出最新的列表
     */
    @Query("SELECT * FROM pass_holders ORDER BY id DESC")
    fun getAllPassHolders(): Flow<List<PassHolder>>


    // ✨ [新增] 刪除一個票夾
    @Delete
    suspend fun deletePassHolder(passHolder: PassHolder)

    // --- 票券 (Ticket) 的操作 ---

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTicket(ticket: Ticket)

    /**
     * 取得所有的票券，並按照出發時間降序排列 (最新的在最前面)
     * 同樣使用 Flow 來即時更新
     */
    @Query("SELECT * FROM tickets ORDER BY departureTimestamp DESC")
    fun getAllTickets(): Flow<List<Ticket>>

    /**
     * 根據傳入的票夾 ID，取得該票夾底下的所有票券
     */
    // ✨ [修正] 這裡的查詢也使用明確的欄位名稱 `pass_holder_id`
    @Query("SELECT * FROM tickets WHERE pass_holder_id = :passHolderId ORDER BY departureTimestamp DESC")
    fun getTicketsByPassHolderId(passHolderId: Int): Flow<List<Ticket>>

    // ✨ [新增] 刪除一張票券
    @Delete
    suspend fun deleteTicket(ticket: Ticket)

    // ✨ [新增] 刪除某個票夾底下的所有票券 (當我們刪除票夾時會用到)
    @Query("DELETE FROM tickets WHERE pass_holder_id = :passHolderId")
    suspend fun deleteTicketsByPassHolderId(passHolderId: Int)
}
