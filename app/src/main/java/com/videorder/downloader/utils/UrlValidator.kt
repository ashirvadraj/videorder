package com.videorder.downloader.utils

import android.webkit.URLUtil
import java.net.URI
import java.util.Locale
import java.util.regex.Pattern

object UrlValidator {

    private val URL_REGEX = Pattern.compile(
        "https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",
        Pattern.CASE_INSENSITIVE
    )

    private val DIRECT_EXTENSIONS = setOf(
        "mp4", "webm", "mkv", "mov", "m4v", "avi", "flv", "3gp",
        "mp3", "wav", "m4a", "aac", "flac", "ogg", "opus",
        "jpg", "jpeg", "png", "webp", "gif", "svg",
        "pdf", "zip", "apk", "doc", "docx", "xls", "xlsx"
    )

    fun isValidUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val trimmed = url.trim()
        if (!trimmed.startsWith("http://", ignoreCase = true) &&
            !trimmed.startsWith("https://", ignoreCase = true) &&
            !trimmed.startsWith("tg://", ignoreCase = true)
        ) {
            return false
        }
        return try {
            val uri = URI(trimmed)
            uri.host != null || trimmed.startsWith("tg://")
        } catch (e: Exception) {
            false
        }
    }

    fun extractUrlFromText(text: String?): String? {
        if (text.isNullOrBlank()) return null
        val matcher = URL_REGEX.matcher(text)
        if (matcher.find()) {
            return matcher.group()
        }
        val trimmed = text.trim()
        if (isValidUrl(trimmed)) {
            return trimmed
        }
        return null
    }

    fun isDirectMediaUrl(url: String): Boolean {
        val cleanUrl = url.substringBefore('?').substringBefore('#')
        val ext = cleanUrl.substringAfterLast('.', "").lowercase(Locale.ROOT)
        return ext in DIRECT_EXTENSIONS
    }

    fun isTelegramUrl(url: String): Boolean {
        val lower = url.lowercase(Locale.ROOT)
        return lower.startsWith("tg://") || lower.contains("t.me/") || lower.contains("telegram.me/")
    }

    fun getExtensionFromUrl(url: String): String {
        val cleanUrl = url.substringBefore('?').substringBefore('#')
        return cleanUrl.substringAfterLast('.', "").lowercase(Locale.ROOT)
    }

    fun extractFilenameFromUrl(url: String, fallback: String = "media"): String {
        return try {
            val cleanUrl = url.substringBefore('?').substringBefore('#')
            val name = cleanUrl.substringAfterLast('/')
            if (name.isNotBlank()) name else fallback
        } catch (e: Exception) {
            fallback
        }
    }
}
