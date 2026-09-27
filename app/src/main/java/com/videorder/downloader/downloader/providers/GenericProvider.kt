package com.videorder.downloader.downloader.providers

import com.videorder.downloader.downloader.core.DrmProtectedException
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
import java.net.URI
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class GenericProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) : MediaExtractor {

    override val providerName: String = "GenericProvider"

    private val drmKeywords = listOf(
        "widevine", "fairplay", "playready", "clearkey", "drm_enabled",
        "enc:stream", "cenc", "key_system", "encrypted-media"
    )

    override fun canHandle(url: String): Boolean {
        return UrlValidator.isValidUrl(url) && !UrlValidator.isTelegramUrl(url)
    }

    override suspend fun extract(url: String): MediaItem = withContext(Dispatchers.IO) {
        val cleanUrl = url.trim()
        val domain = try {
            URI(cleanUrl).host ?: "web"
        } catch (e: Exception) {
            "web"
        }

        val request = Request.Builder()
            .url(cleanUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()

        val response = try {
            client.newCall(request).execute()
        } catch (e: Exception) {
            throw UnsupportedUrlException("Could not connect to URL: ${e.message}")
        }

        val html = response.body?.string() ?: throw UnsupportedUrlException("Empty webpage response.")

        // 1. Strict DRM Detection check
        val lowerHtml = html.lowercase()
        for (kw in drmKeywords) {
            if (lowerHtml.contains(kw)) {
                // If it explicitly references DRM licensing or protection mechanisms
                if (lowerHtml.contains("license") || lowerHtml.contains("widevine") || lowerHtml.contains("fairplay")) {
                    throw DrmProtectedException("This media is protected by DRM and cannot be downloaded by Videorder.")
                }
            }
        }

        // 2. Extract Title
        var title = extractPattern(html, "<title>(.*?)</title>")?.trim()
        if (title.isNullOrBlank()) {
            title = extractMeta(html, "og:title") ?: extractMeta(html, "twitter:title") ?: "Video_${System.currentTimeMillis()}"
        }
        title = title.replace("&quot;", "\"").replace("&amp;", "&").replace("&#39;", "'")
        val cleanTitle = FileUtils.sanitizeFilename(title)

        // 3. Extract Thumbnail
        val thumbnail = extractMeta(html, "og:image")
            ?: extractMeta(html, "twitter:image")
            ?: extractPattern(html, "poster=[\"'](.*?)[\"']")

        // 4. Extract Media Streams
        val mediaStreams = mutableListOf<String>()

        // Video tag src
        val videoSrc = extractPattern(html, "<video[^>]+src=[\"'](.*?)[\"']")
        if (!videoSrc.isNullOrBlank()) mediaStreams.add(resolveRelativeUrl(cleanUrl, videoSrc))

        // Source tags
        val sourceMatcher = Pattern.compile("<source[^>]+src=[\"'](.*?)[\"']", Pattern.CASE_INSENSITIVE).matcher(html)
        while (sourceMatcher.find()) {
            val src = sourceMatcher.group(1)
            if (!src.isNullOrBlank()) {
                mediaStreams.add(resolveRelativeUrl(cleanUrl, src))
            }
        }

        // OpenGraph video
        val ogVideo = extractMeta(html, "og:video") ?: extractMeta(html, "og:video:secure_url")
        if (!ogVideo.isNullOrBlank()) {
            mediaStreams.add(resolveRelativeUrl(cleanUrl, ogVideo))
        }

        val cleanStreams = mediaStreams.distinct().filter {
            UrlValidator.isValidUrl(it) && !it.contains("drm", ignoreCase = true)
        }

        val primaryStream = cleanStreams.firstOrNull() ?: cleanUrl

        // Estimate qualities for video (1080p, 720p, 480p, 360p)
        val qualities = listOf(
            MediaQuality(
                id = "1080p",
                label = "1080p Full HD",
                resolution = "1920x1080",
                format = "mp4",
                approxSizeBytes = 250 * 1024 * 1024L,
                downloadUrl = primaryStream,
                mimeType = "video/mp4"
            ),
            MediaQuality(
                id = "720p",
                label = "720p HD",
                resolution = "1280x720",
                format = "mp4",
                approxSizeBytes = 120 * 1024 * 1024L,
                downloadUrl = primaryStream,
                mimeType = "video/mp4"
            ),
            MediaQuality(
                id = "480p",
                label = "480p Standard",
                resolution = "854x480",
                format = "mp4",
                approxSizeBytes = 70 * 1024 * 1024L,
                downloadUrl = primaryStream,
                mimeType = "video/mp4"
            ),
            MediaQuality(
                id = "360p",
                label = "360p Low",
                resolution = "640x360",
                format = "mp4",
                approxSizeBytes = 40 * 1024 * 1024L,
                downloadUrl = primaryStream,
                mimeType = "video/mp4"
            ),
            MediaQuality(
                id = "audio_mp3",
                label = "Audio Only (MP3)",
                resolution = null,
                format = "mp3",
                approxSizeBytes = 15 * 1024 * 1024L,
                downloadUrl = primaryStream,
                mimeType = "audio/mpeg"
            )
        )

        MediaItem(
            originalUrl = cleanUrl,
            title = cleanTitle,
            sourceDomain = domain,
            thumbnailUrl = thumbnail,
            durationSeconds = 180L,
            mediaType = MediaType.VIDEO,
            qualities = qualities,
            isDrmProtected = false,
            requiresAuth = false,
            providerName = providerName
        )
    }

    private fun extractMeta(html: String, property: String): String? {
        val pattern = Pattern.compile(
            "<meta[^>]+(?:property|name)=[\"']" + Pattern.quote(property) + "[\"'][^>]+content=[\"'](.*?)[\"']",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = pattern.matcher(html)
        if (matcher.find()) return matcher.group(1)

        // Try reversed attributes (content before property)
        val reversedPattern = Pattern.compile(
            "<meta[^>]+content=[\"'](.*?)[\"'][^>]+(?:property|name)=[\"']" + Pattern.quote(property) + "[\"']",
            Pattern.CASE_INSENSITIVE
        )
        val revMatcher = reversedPattern.matcher(html)
        return if (revMatcher.find()) revMatcher.group(1) else null
    }

    private fun extractPattern(html: String, regex: String): String? {
        val pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val matcher = pattern.matcher(html)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun resolveRelativeUrl(baseUrl: String, rel: String): String {
        return try {
            URI(baseUrl).resolve(rel).toString()
        } catch (e: Exception) {
            rel
        }
    }
}
