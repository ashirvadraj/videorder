package com.videorder.downloader.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.videorder.downloader.domain.models.MediaType
import java.io.File
import java.util.Locale

object FileUtils {

    fun sanitizeFilename(input: String, defaultName: String = "download"): String {
        var clean = input.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim()
        if (clean.isEmpty()) clean = defaultName
        return clean.take(120)
    }

    fun getMimeTypeFromExtension(extension: String): String {
        val ext = extension.lowercase(Locale.ROOT).removePrefix(".")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: when (ext) {
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "webm" -> "video/webm"
            "mov" -> "video/quicktime"
            "m4v" -> "video/x-m4v"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "aac" -> "audio/aac"
            "flac" -> "audio/flac"
            "m4a" -> "audio/mp4"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "gif" -> "image/gif"
            "pdf" -> "application/pdf"
            "zip" -> "application/zip"
            "apk" -> "application/vnd.android.package-archive"
            else -> "application/octet-stream"
        }
    }

    fun getMediaTypeFromMimeOrExt(mimeType: String, filename: String): MediaType {
        val mime = mimeType.lowercase(Locale.ROOT)
        val ext = filename.substringAfterLast('.', "").lowercase(Locale.ROOT)

        return when {
            mime.startsWith("video/") || ext in listOf("mp4", "webm", "mkv", "mov", "m4v", "avi", "flv", "3gp") -> MediaType.VIDEO
            mime.startsWith("audio/") || ext in listOf("mp3", "wav", "m4a", "aac", "flac", "ogg", "opus") -> MediaType.AUDIO
            mime.startsWith("image/") || ext in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "svg") -> MediaType.IMAGE
            mime.startsWith("application/pdf") || mime.contains("document") || mime.contains("sheet") ||
                    ext in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "zip", "rar", "apk") -> MediaType.DOCUMENT
            else -> MediaType.OTHER
        }
    }

    fun getTargetDownloadDirectory(context: Context, customFolder: String? = null): File {
        val baseDir = if (customFolder != null && customFolder.isNotBlank()) {
            File(customFolder)
        } else {
            val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            File(publicDownloads, "Videorder")
        }

        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
        return baseDir
    }

    fun getUniqueFile(directory: File, preferredFilename: String): File {
        var file = File(directory, preferredFilename)
        if (!file.exists()) return file

        val nameWithoutExt = preferredFilename.substringBeforeLast('.', preferredFilename)
        val ext = preferredFilename.substringAfterLast('.', "")
        val extSuffix = if (ext.isNotEmpty() && ext != preferredFilename) ".$ext" else ""

        var counter = 1
        while (file.exists()) {
            file = File(directory, "$nameWithoutExt ($counter)$extSuffix")
            counter++
        }
        return file
    }

    fun openFile(context: Context, filePath: String, mimeType: String = "*/*") {
        try {
            val file = File(filePath)
            if (!file.exists()) return

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open with...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareFile(context: Context, filePath: String, mimeType: String = "*/*") {
        try {
            val file = File(filePath)
            if (!file.exists()) return

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share via").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun forwardMedia(context: Context, filePath: String, mimeType: String = "*/*", caption: String? = null) {
        try {
            val file = File(filePath)
            if (!file.exists()) return

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                if (!caption.isNullOrBlank()) {
                    putExtra(Intent.EXTRA_TEXT, caption)
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Forward Video to...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun messageViaTelegram(context: Context, filePath: String? = null, mimeType: String = "*/*", caption: String? = null) {
        try {
            val tgIntent = Intent(Intent.ACTION_SEND).apply {
                if (!filePath.isNullOrBlank()) {
                    val file = File(filePath)
                    if (file.exists()) {
                        val uri: Uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        type = mimeType
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    } else {
                        type = "text/plain"
                    }
                } else {
                    type = "text/plain"
                }
                if (!caption.isNullOrBlank()) {
                    putExtra(Intent.EXTRA_TEXT, caption)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val telegramPackages = listOf(
                "org.telegram.messenger",
                "org.telegram.plus",
                "org.thunderdog.challegram",
                "org.telegram.messenger.web"
            )
            val pm = context.packageManager
            val installedTg = telegramPackages.firstOrNull { pkg ->
                try {
                    pm.getPackageInfo(pkg, 0)
                    true
                } catch (e: Exception) {
                    false
                }
            }

            if (installedTg != null) {
                tgIntent.setPackage(installedTg)
                context.startActivity(tgIntent)
            } else {
                context.startActivity(Intent.createChooser(tgIntent, "Send via Telegram").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareTelegramLink(context: Context, url: String, title: String? = null) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                val text = if (!title.isNullOrBlank()) "$title\n$url" else url
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share Telegram Video Link").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
