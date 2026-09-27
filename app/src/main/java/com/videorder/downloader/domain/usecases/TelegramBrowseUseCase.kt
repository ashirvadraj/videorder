package com.videorder.downloader.domain.usecases

import com.videorder.downloader.data.repositories.DownloadRepository
import com.videorder.downloader.data.repositories.TelegramRepository
import com.videorder.downloader.domain.models.DownloadStatus
import com.videorder.downloader.domain.models.DownloadTask
import com.videorder.downloader.domain.models.MediaType
import com.videorder.downloader.domain.models.TelegramChat
import com.videorder.downloader.domain.models.TelegramChatType
import com.videorder.downloader.domain.models.TelegramMediaItem
import com.videorder.downloader.utils.FileUtils
import java.util.UUID

class TelegramBrowseUseCase(
    private val telegramRepository: TelegramRepository,
    private val downloadRepository: DownloadRepository
) {
    suspend fun getChats(typeFilter: TelegramChatType? = null): List<TelegramChat> {
        return telegramRepository.getChats(typeFilter)
    }

    suspend fun getChatMedia(chatId: Long, typeFilter: MediaType? = null): List<TelegramMediaItem> {
        return telegramRepository.getChatMedia(chatId, typeFilter)
    }

    fun enqueueMediaItem(item: TelegramMediaItem): DownloadTask {
        val cleanName = FileUtils.sanitizeFilename(item.filename)
        val task = DownloadTask(
            id = UUID.randomUUID().toString(),
            url = item.downloadUrl,
            filename = cleanName,
            mimeType = item.mimeType,
            totalBytes = item.sizeBytes,
            downloadedBytes = 0L,
            status = DownloadStatus.QUEUED,
            progress = 0f,
            speed = 0L,
            createdAt = System.currentTimeMillis(),
            qualityLabel = "Telegram Original",
            thumbnailUrl = item.thumbnailUrl,
            mediaType = item.mediaType
        )
        downloadRepository.enqueue(task)
        return task
    }

    fun enqueueMultiple(items: List<TelegramMediaItem>): List<DownloadTask> {
        return items.map { enqueueMediaItem(it) }
    }
}
