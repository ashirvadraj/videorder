package com.videorder.downloader.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.videorder.downloader.data.database.entities.DownloadHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadHistoryDao {
    @Query("SELECT * FROM download_history ORDER BY completedAt DESC")
    fun getAllHistory(): Flow<List<DownloadHistoryEntity>>

    @Query("SELECT * FROM download_history WHERE filename LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' ORDER BY completedAt DESC")
    fun searchHistory(query: String): Flow<List<DownloadHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: DownloadHistoryEntity): Long

    @Query("DELETE FROM download_history WHERE id = :id")
    suspend fun deleteHistory(id: Long)

    @Query("DELETE FROM download_history")
    suspend fun clearAllHistory()
}
