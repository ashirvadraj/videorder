package com.videorder.downloader.data.repositories

import com.videorder.downloader.data.database.dao.DownloadTaskDao
import com.videorder.downloader.downloader.queue.DownloadQueueManager
import com.videorder.downloader.domain.models.DownloadTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface DownloadRepository {
    fun getAllTasks(): Flow<List<DownloadTask>>
    fun getActiveTasks(): Flow<List<DownloadTask>>
    fun getCompletedTasks(): Flow<List<DownloadTask>>
    suspend fun getTaskById(id: String): DownloadTask?
    fun enqueue(task: DownloadTask)
    fun pause(taskId: String)
    fun resume(taskId: String)
    fun cancel(taskId: String)
    fun pauseAll()
    fun resumeAll()
    fun cancelAll()
    fun deleteTask(taskId: String, deleteFile: Boolean = false)
}

class DownloadRepositoryImpl(
    private val taskDao: DownloadTaskDao,
    private val queueManager: DownloadQueueManager
) : DownloadRepository {

    override fun getAllTasks(): Flow<List<DownloadTask>> {
        return taskDao.getAllTasks().map { list -> list.map { it.toDomain() } }
    }

    override fun getActiveTasks(): Flow<List<DownloadTask>> {
        return taskDao.getActiveTasks().map { list -> list.map { it.toDomain() } }
    }

    override fun getCompletedTasks(): Flow<List<DownloadTask>> {
        return taskDao.getCompletedTasks().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getTaskById(id: String): DownloadTask? {
        return taskDao.getTaskById(id)?.toDomain()
    }

    override fun enqueue(task: DownloadTask) {
        queueManager.enqueue(task)
    }

    override fun pause(taskId: String) {
        queueManager.pause(taskId)
    }

    override fun resume(taskId: String) {
        queueManager.resume(taskId)
    }

    override fun cancel(taskId: String) {
        queueManager.cancel(taskId)
    }

    override fun pauseAll() {
        queueManager.pauseAll()
    }

    override fun resumeAll() {
        queueManager.resumeAll()
    }

    override fun cancelAll() {
        queueManager.cancelAll()
    }

    override fun deleteTask(taskId: String, deleteFile: Boolean) {
        queueManager.deleteTask(taskId, deleteFile)
    }
}
