package com.videorder.downloader.downloader.providers

import com.videorder.downloader.downloader.core.MediaExtractor
import com.videorder.downloader.domain.models.MediaItem
import com.videorder.downloader.domain.models.MediaQuality
import com.videorder.downloader.domain.models.MediaType
import com.videorder.downloader.utils.FileUtils
import com.videorder.downloader.utils.UrlValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import java.util.concurrent.TimeUnit

class DirectMediaProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) : MediaExtractor {

    override val providerName: String = "DirectMediaProvider"

    override fun canHandle(url: String): Boolean {
        return UrlValidator.isValidUrl(url) && UrlValidator.isDirectMediaUrl(url)
    }

    override suspend fun extract(url: String): MediaItem = withContext(Dispatchers.IO) {
        val cleanUrl = url.trim()
        val defaultName = UrlValidator.extractFilenameFromUrl(cleanUrl, "download")
        val ext = UrlValidator.getExtensionFromUrl(cleanUrl)
        var detectedMime = FileUtils.getMimeTypeFromExtension(ext)
        var detectedSize = 0L
        var filename = defaultName

        try {
            val headRequest = Request.Builder()
                .url(cleanUrl)
                .head()
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0) Gecko/120.0 Firefox/120.0")
                .build()

            client.newCall(headRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val lengthHeader = response.header("Content-Length")
                    if (!lengthHeader.isNullOrBlank()) {
                        detectedSize = lengthHeader.toLongOrNull() ?: 0L
                    }

                    val typeHeader = response.header("Content-Type")
                    if (!typeHeader.isNullOrBlank()) {
                        detectedMime = typeHeader.substringBefore(';').trim()
                    }

                    val disposition = response.header("Content-Disposition")
                    if (!disposition.isNullOrBlank() && disposition.contains("filename=")) {
                        val parsed = disposition.substringAfter("filename=")
                            .replace("\"", "")
                            .trim()
                        if (parsed.isNotBlank()) {
                            filename = parsed
                        }
                    }
                }
            }
        } catch (ignored: Exception) {
            // Fallback to URL-based filename and extension
        }

        val domain = try {
            URI(cleanUrl).host ?: "direct"
        } catch (e: Exception) {
            "direct"
        }

        val mediaType = FileUtils.getMediaTypeFromMimeOrExt(detectedMime, filename)

        val directQuality = MediaQuality(
            id = "direct_orig",
            label = if (mediaType == MediaType.VIDEO) "Original Quality" else "Original File",
            resolution = if (mediaType == MediaType.VIDEO) "Auto (Source)" else null,
            format = ext.ifBlank { "mp4" },
            approxSizeBytes = detectedSize,
            downloadUrl = cleanUrl,
            isDirectStream = true,
            mimeType = detectedMime
        )

        MediaItem(
            originalUrl = cleanUrl,
            title = FileUtils.sanitizeFilename(filename),
            sourceDomain = domain,
            thumbnailUrl = null,
            durationSeconds = null,
            mediaType = mediaType,
            qualities = listOf(directQuality),
            isDrmProtected = false,
            requiresAuth = false,
            providerName = providerName
        )
    }
}
