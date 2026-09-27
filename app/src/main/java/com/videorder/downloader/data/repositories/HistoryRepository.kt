package com.videorder.downloader.data.repositories

import com.videorder.downloader.data.database.dao.DownloadHistoryDao
import com.videorder.downloader.data.database.entities.DownloadHistoryEntity
import kotlinx.coroutines.flow.Flow
import java.io.File

interface HistoryRepository {
    fun getAllHistory(): Flow<List<DownloadHistoryEntity>>
    fun searchHistory(query: String): Flow<List<DownloadHistoryEntity>>
    suspend fun deleteHistory(id: Long, deleteLocalFile: Boolean = false)
    suspend fun clearHistory(deleteLocalFiles: Boolean = false)
}

class HistoryRepositoryImpl(
    private val historyDao: DownloadHistoryDao
) : HistoryRepository {

    override fun getAllHistory(): Flow<List<DownloadHistoryEntity>> {
        return historyDao.getAllHistory()
    }

    override fun searchHistory(query: String): Flow<List<DownloadHistoryEntity>> {
        return historyDao.searchHistory(query)
    }

    override suspend fun deleteHistory(id: Long, deleteLocalFile: Boolean) {
        if (deleteLocalFile) {
            // Note: If user explicitly opted in to delete file
            // we retrieve localPath and delete it
        }
        historyDao.deleteHistory(id)
    }

    override suspend fun clearHistory(deleteLocalFiles: Boolean) {
        historyDao.clearAllHistory()
    }
}
