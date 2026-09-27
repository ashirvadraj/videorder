package com.videorder.downloader.domain.models

data class MediaQuality(
    val id: String,
    val label: String,
    val resolution: String? = null,
    val format: String = "mp4",
    val approxSizeBytes: Long = 0L,
    val downloadUrl: String,
    val isDirectStream: Boolean = true,
    val mimeType: String = "video/mp4",
    val headers: Map<String, String> = emptyMap()
)
