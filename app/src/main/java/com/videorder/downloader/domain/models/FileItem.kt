package com.videorder.downloader.domain.models

data class FileItem(
    val id: Long,
    val filename: String,
    val localPath: String,
    val sizeBytes: Long,
    val mimeType: String,
    val mediaType: MediaType,
    val lastModified: Long,
    val durationMs: Long? = null,
    val resolution: String? = null,
    val thumbnailUri: String? = null
)
