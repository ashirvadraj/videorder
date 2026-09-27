package com.videorder.downloader.domain.usecases

import com.videorder.downloader.data.repositories.DownloadRepository
import com.videorder.downloader.domain.models.DownloadStatus
import com.videorder.downloader.domain.models.DownloadTask
import com.videorder.downloader.domain.models.MediaItem
import com.videorder.downloader.domain.models.MediaQuality
import com.videorder.downloader.utils.FileUtils
import java.util.UUID

class StartDownloadUseCase(private val downloadRepository: DownloadRepository) {

    operator fun invoke(
        mediaItem: MediaItem,
        selectedQuality: MediaQuality,
        customFilename: String? = null
    ): DownloadTask {
        val ext = selectedQuality.format.ifBlank { "mp4" }
        val rawBase = customFilename?.ifBlank { null } ?: mediaItem.title
        val cleanBase = FileUtils.sanitizeFilename(rawBase)
        val finalFilename = if (cleanBase.endsWith(".$ext", ignoreCase = true)) {
            cleanBase
        } else {
            "$cleanBase.$ext"
        }

        val task = DownloadTask(
            id = UUID.randomUUID().toString(),
            url = selectedQuality.downloadUrl,
            filename = finalFilename,
            mimeType = selectedQuality.mimeType,
            totalBytes = selectedQuality.approxSizeBytes,
            downloadedBytes = 0L,
            status = DownloadStatus.QUEUED,
            progress = 0f,
            speed = 0L,
            createdAt = System.currentTimeMillis(),
            qualityLabel = selectedQuality.label,
            thumbnailUrl = mediaItem.thumbnailUrl,
            mediaType = mediaItem.mediaType
        )

        downloadRepository.enqueue(task)
        return task
    }
}
