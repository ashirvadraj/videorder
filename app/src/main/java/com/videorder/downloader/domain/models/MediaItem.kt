package com.videorder.downloader.domain.models

data class MediaItem(
    val originalUrl: String,
    val title: String,
    val sourceDomain: String,
    val thumbnailUrl: String? = null,
    val durationSeconds: Long? = null,
    val mediaType: MediaType = MediaType.VIDEO,
    val qualities: List<MediaQuality> = emptyList(),
    val isDrmProtected: Boolean = false,
    val requiresAuth: Boolean = false,
    val providerName: String = "Generic"
)
