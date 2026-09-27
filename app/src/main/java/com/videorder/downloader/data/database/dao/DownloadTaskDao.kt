package com.videorder.downloader.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.videorder.downloader.data.database.entities.DownloadTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadTaskDao {
    @Query("SELECT * FROM download_tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM download_tasks WHERE status IN ('DOWNLOADING', 'QUEUED', 'PAUSED', 'WAITING_FOR_WIFI') ORDER BY createdAt DESC")
    fun getActiveTasks(): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM download_tasks WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedTasks(): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM download_tasks WHERE id = :id")
    suspend fun getTaskById(id: String): DownloadTaskEntity?

    @Query("SELECT * FROM download_tasks WHERE status = 'QUEUED' ORDER BY createdAt ASC")
    suspend fun getQueuedTasks(): List<DownloadTaskEntity>

    @Query("SELECT COUNT(*) FROM download_tasks WHERE status = 'DOWNLOADING'")
    suspend fun getActiveRunningCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DownloadTaskEntity)

    @Update
    suspend fun updateTask(task: DownloadTaskEntity)

    @Query("UPDATE download_tasks SET downloadedBytes = :downloaded, totalBytes = :total, progress = :progress, speed = :speed, status = :status WHERE id = :id")
    suspend fun updateProgress(id: String, downloaded: Long, total: Long, progress: Float, speed: Long, status: String)

    @Query("UPDATE download_tasks SET status = :status, errorMessage = :error WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, error: String? = null)

    @Query("UPDATE download_tasks SET status = :status, completedAt = :completedAt, localPath = :localPath, progress = 1.0 WHERE id = :id")
    suspend fun markCompleted(id: String, status: String, completedAt: Long, localPath: String)

    @Query("DELETE FROM download_tasks WHERE id = :id")
    suspend fun deleteTask(id: String)

    @Query("DELETE FROM download_tasks")
    suspend fun deleteAll()
}
