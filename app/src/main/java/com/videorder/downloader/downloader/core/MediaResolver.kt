package com.videorder.downloader.downloader.core

import com.videorder.downloader.domain.models.MediaItem

class MediaResolver(private val extractors: List<MediaExtractor>) {

    fun getExtractor(url: String): MediaExtractor {
        val trimmed = url.trim()
        return extractors.firstOrNull { it.canHandle(trimmed) }
            ?: throw UnsupportedUrlException("This website or URL format is not currently supported.")
    }

    suspend fun resolve(url: String): MediaItem {
        val extractor = getExtractor(url)
        return extractor.extract(url.trim())
    }
}
