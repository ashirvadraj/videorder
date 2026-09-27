package com.videorder.downloader.domain.usecases

import com.videorder.downloader.downloader.core.MediaResolver
import com.videorder.downloader.downloader.core.UnsupportedUrlException
import com.videorder.downloader.downloader.providers.DirectMediaProvider
import com.videorder.downloader.downloader.providers.GenericProvider
import com.videorder.downloader.downloader.providers.TelegramProvider
import com.videorder.downloader.domain.models.MediaItem
import com.videorder.downloader.utils.UrlValidator

class DetectMediaUseCase(
    private val mediaResolver: MediaResolver = MediaResolver(
        listOf(
            DirectMediaProvider(),
            TelegramProvider(),
            GenericProvider()
        )
    )
) {
    suspend operator fun invoke(rawInput: String): Result<MediaItem> {
        val detectedUrl = UrlValidator.extractUrlFromText(rawInput)
            ?: return Result.failure(UnsupportedUrlException("No valid web URL detected in input."))

        return try {
            val resolved = mediaResolver.resolve(detectedUrl)
            Result.success(resolved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
