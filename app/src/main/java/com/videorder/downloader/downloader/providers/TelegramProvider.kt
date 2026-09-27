package com.videorder.downloader.downloader.providers

import com.videorder.downloader.downloader.core.MediaExtractor
import com.videorder.downloader.domain.models.MediaItem
import com.videorder.downloader.domain.models.MediaQuality
import com.videorder.downloader.domain.models.MediaType
import com.videorder.downloader.utils.FileUtils
import com.videorder.downloader.utils.UrlValidator

class TelegramProvider : MediaExtractor {

    override val providerName: String = "TelegramProvider"

    override fun canHandle(url: String): Boolean {
        return UrlValidator.isTelegramUrl(url)
    }

    override suspend fun extract(url: String): MediaItem {
        val cleanUrl = url.trim()
        val titleCandidate = cleanUrl.substringAfterLast('/').ifBlank { "Telegram_Media_${System.currentTimeMillis()}" }
        val title = FileUtils.sanitizeFilename(titleCandidate)

        val quality = MediaQuality(
            id = "tg_original",
            label = "Original File",
            resolution = "Source Resolution",
            format = "mp4",
            approxSizeBytes = 85 * 1024 * 1024L,
            downloadUrl = cleanUrl,
            mimeType = "video/mp4"
        )

        return MediaItem(
            originalUrl = cleanUrl,
            title = title,
            sourceDomain = "telegram.org",
            thumbnailUrl = null,
            durationSeconds = 120L,
            mediaType = MediaType.VIDEO,
            qualities = listOf(quality),
            isDrmProtected = false,
            requiresAuth = true,
            providerName = providerName
        )
    }
}
