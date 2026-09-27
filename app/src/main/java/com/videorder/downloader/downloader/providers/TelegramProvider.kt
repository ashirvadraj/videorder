package com.videorder.downloader.downloader.providers

import com.videorder.downloader.downloader.core.MediaExtractor
import com.videorder.downloader.downloader.core.UnsupportedUrlException
import com.videorder.downloader.domain.models.MediaItem
import com.videorder.downloader.domain.models.MediaQuality
import com.videorder.downloader.domain.models.MediaType
import com.videorder.downloader.utils.FileUtils
import com.videorder.downloader.utils.UrlValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class TelegramProvider(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) : MediaExtractor {

    override val providerName: String = "TelegramProvider"

    override fun canHandle(url: String): Boolean {
        return UrlValidator.isTelegramUrl(url)
    }

    override suspend fun extract(url: String): MediaItem = withContext(Dispatchers.IO) {
        val cleanUrl = url.trim()

        // 1. If it's already a direct media file URL (telesco.pe or .mp4)
        if (cleanUrl.endsWith(".mp4", ignoreCase = true) ||
            cleanUrl.endsWith(".mov", ignoreCase = true) ||
            cleanUrl.endsWith(".webm", ignoreCase = true) ||
            cleanUrl.contains(".telesco.pe/file/")
        ) {
            val titleCandidate = cleanUrl.substringAfterLast('/').substringBefore('?').ifBlank {
                "Telegram_Video_${System.currentTimeMillis()}.mp4"
            }
            val title = FileUtils.sanitizeFilename(titleCandidate)
            return@withContext createMediaItem(cleanUrl, title, cleanUrl, null)
        }

        // 2. Format Telegram embed URL: https://t.me/<channel>/<id>?embed=1
        val embedUrl = if (cleanUrl.contains("?")) {
            if (cleanUrl.contains("embed=1")) cleanUrl else "$cleanUrl&embed=1"
        } else {
            "$cleanUrl?embed=1"
        }

        val request = Request.Builder()
            .url(embedUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .build()

        val html = try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw UnsupportedUrlException("Telegram returned HTTP ${response.code}. For private or restricted channels, please use 'Official Telegram Web' in Videorder.")
                }
                response.body?.string() ?: ""
            }
        } catch (e: Exception) {
            if (e is UnsupportedUrlException) throw e
            throw UnsupportedUrlException("Could not connect to Telegram: ${e.message}")
        }

        // 3. Extract real video URL from embed HTML
        var videoUrl: String? = null

        // Pattern A: <video ... src="...">
        val videoTagPattern = Pattern.compile("<video[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
        val videoMatcher = videoTagPattern.matcher(html)
        if (videoMatcher.find()) {
            videoUrl = videoMatcher.group(1)
        }

        // Pattern B: <source ... src="...">
        if (videoUrl.isNullOrBlank()) {
            val sourcePattern = Pattern.compile("<source[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
            val sourceMatcher = sourcePattern.matcher(html)
            if (sourceMatcher.find()) {
                videoUrl = sourceMatcher.group(1)
            }
        }

        // Pattern C: og:video or twitter:player:stream
        if (videoUrl.isNullOrBlank()) {
            val ogVideoPattern = Pattern.compile("<meta[^>]+property=[\"']og:video(:url)?[\"'][^>]+content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
            val ogMatcher = ogVideoPattern.matcher(html)
            if (ogMatcher.find()) {
                videoUrl = ogMatcher.group(2)
            }
        }

        // Pattern D: Telesco.pe direct link
        if (videoUrl.isNullOrBlank()) {
            val telescoPattern = Pattern.compile("(https://[a-zA-Z0-9.-]*telesco\\.pe/file/[^\"'\\s<>]+)", Pattern.CASE_INSENSITIVE)
            val telescoMatcher = telescoPattern.matcher(html)
            if (telescoMatcher.find()) {
                videoUrl = telescoMatcher.group(1)
            }
        }

        // 4. Validate resolved video URL
        if (videoUrl.isNullOrBlank()) {
            throw UnsupportedUrlException(
                "This Telegram post does not contain a public direct video stream, or belongs to a private/restricted channel. " +
                "Please open 'Official Telegram Web' in Videorder to play and download it directly!"
            )
        }

        // Clean unescaped entities
        videoUrl = videoUrl.replace("&amp;", "&")

        // 5. Extract Title
        var title: String? = null
        val titlePattern = Pattern.compile("<meta[^>]+property=[\"']og:title[\"'][^>]+content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
        val titleMatcher = titlePattern.matcher(html)
        if (titleMatcher.find()) {
            title = titleMatcher.group(1)
        }

        val cleanTitle = FileUtils.sanitizeFilename(
            title?.takeIf { it.isNotBlank() } ?: "Telegram_Video_${System.currentTimeMillis()}"
        )

        // 6. Extract Thumbnail
        var thumb: String? = null
        val thumbPattern = Pattern.compile("<meta[^>]+property=[\"']og:image[\"'][^>]+content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
        val thumbMatcher = thumbPattern.matcher(html)
        if (thumbMatcher.find()) {
            thumb = thumbMatcher.group(1)?.replace("&amp;", "&")
        }

        createMediaItem(cleanUrl, cleanTitle, videoUrl, thumb)
    }

    private fun createMediaItem(
        originalUrl: String,
        title: String,
        directDownloadUrl: String,
        thumbUrl: String?
    ): MediaItem {
        val finalTitle = if (title.endsWith(".mp4", ignoreCase = true)) title else "$title.mp4"

        val quality = MediaQuality(
            id = "tg_video_original",
            label = "Original Video",
            resolution = "High Quality",
            format = "mp4",
            approxSizeBytes = 45 * 1024 * 1024L,
            downloadUrl = directDownloadUrl,
            mimeType = "video/mp4"
        )

        return MediaItem(
            originalUrl = originalUrl,
            title = finalTitle,
            sourceDomain = "telegram.org",
            thumbnailUrl = thumbUrl,
            durationSeconds = 60L,
            mediaType = MediaType.VIDEO,
            qualities = listOf(quality),
            isDrmProtected = false,
            requiresAuth = false,
            providerName = providerName
        )
    }
}
