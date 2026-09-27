package com.videorder.downloader.downloader.core

import com.videorder.downloader.domain.models.MediaItem

interface MediaExtractor {
    val providerName: String
    fun canHandle(url: String): Boolean
    suspend fun extract(url: String): MediaItem
}
