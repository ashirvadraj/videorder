package com.videorder.downloader.domain.models

data class DownloadTask(
    val id: String,
    val url: String,
    val filename: String,
    val mimeType: String,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val status: DownloadStatus,
    val progress: Float, // 0.0 to 1.0 (or 0 to 100%)
    val speed: Long, // bytes per second
    val createdAt: Long,
    val completedAt: Long? = null,
    val localPath: String? = null,
    val errorMessage: String? = null,
    val qualityLabel: String? = null,
    val thumbnailUrl: String? = null,
    val etaSeconds: Long = 0L,
    val mediaType: MediaType = MediaType.VIDEO
)
