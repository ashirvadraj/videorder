package com.videorder.downloader.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.videorder.downloader.domain.models.DownloadStatus
import com.videorder.downloader.domain.models.DownloadTask
import com.videorder.downloader.domain.models.MediaType

@Entity(tableName = "download_tasks")
data class DownloadTaskEntity(
    @PrimaryKey
    val id: String,
    val url: String,
    val filename: String,
    val mimeType: String,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val status: String, // String representation of DownloadStatus
    val progress: Float,
    val speed: Long,
    val createdAt: Long,
    val completedAt: Long?,
    val localPath: String?,
    val errorMessage: String?,
    val qualityLabel: String? = null,
    val thumbnailUrl: String? = null,
    val mediaType: String = MediaType.VIDEO.name
) {
    fun toDomain(): DownloadTask {
        val parsedStatus = try {
            DownloadStatus.valueOf(status)
        } catch (e: Exception) {
            DownloadStatus.FAILED
        }
        val parsedMediaType = try {
            MediaType.valueOf(mediaType)
        } catch (e: Exception) {
            MediaType.VIDEO
        }
        val remainingBytes = if (totalBytes > downloadedBytes) totalBytes - downloadedBytes else 0L
        val eta = if (speed > 0) remainingBytes / speed else 0L

        return DownloadTask(
            id = id,
            url = url,
            filename = filename,
            mimeType = mimeType,
            totalBytes = totalBytes,
            downloadedBytes = downloadedBytes,
            status = parsedStatus,
            progress = progress,
            speed = speed,
            createdAt = createdAt,
            completedAt = completedAt,
            localPath = localPath,
            errorMessage = errorMessage,
            qualityLabel = qualityLabel,
            thumbnailUrl = thumbnailUrl,
            etaSeconds = eta,
            mediaType = parsedMediaType
        )
    }

    companion object {
        fun fromDomain(task: DownloadTask): DownloadTaskEntity {
            return DownloadTaskEntity(
                id = task.id,
                url = task.url,
                filename = task.filename,
                mimeType = task.mimeType,
                totalBytes = task.totalBytes,
                downloadedBytes = task.downloadedBytes,
                status = task.status.name,
                progress = task.progress,
                speed = task.speed,
                createdAt = task.createdAt,
                completedAt = task.completedAt,
                localPath = task.localPath,
                errorMessage = task.errorMessage,
                qualityLabel = task.qualityLabel,
                thumbnailUrl = task.thumbnailUrl,
                mediaType = task.mediaType.name
            )
        }
    }
}
