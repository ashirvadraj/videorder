package com.videorder.downloader.downloader.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.videorder.downloader.MainActivity
import com.videorder.downloader.R
import com.videorder.downloader.downloader.services.DownloadForegroundService
import com.videorder.downloader.domain.models.DownloadStatus
import com.videorder.downloader.domain.models.DownloadTask
import com.videorder.downloader.utils.Formatters
import java.io.File

class DownloadNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_DOWNLOADS = "videorder_downloads"
        const val CHANNEL_COMPLETIONS = "videorder_completions"
        const val FOREGROUND_NOTIFICATION_ID = 9001
    }

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val downloadChannel = NotificationChannel(
                CHANNEL_DOWNLOADS,
                "Active Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress of ongoing downloads"
                setShowBadge(false)
            }

            val completionChannel = NotificationChannel(
                CHANNEL_COMPLETIONS,
                "Download Completed",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when downloads finish"
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(downloadChannel)
            notificationManager.createNotificationChannel(completionChannel)
        }
    }

    fun buildForegroundNotification(task: DownloadTask?): Notification {
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            context,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_DOWNLOADS)
            .setSmallIcon(R.drawable.ic_stat_download)
            .setContentIntent(appPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        if (task == null) {
            builder.setContentTitle("Videorder Service")
                .setContentText("Download manager is ready")
            return builder.build()
        }

        val progressPercent = (task.progress * 100).toInt().coerceIn(0, 100)
        val downloadedText = Formatters.formatBytes(task.downloadedBytes)
        val totalText = if (task.totalBytes > 0) Formatters.formatBytes(task.totalBytes) else "--"
        val speedText = Formatters.formatSpeed(task.speed)

        val title = when (task.status) {
            DownloadStatus.DOWNLOADING -> "Downloading: ${task.filename}"
            DownloadStatus.PAUSED -> "Paused: ${task.filename}"
            DownloadStatus.WAITING_FOR_WIFI -> "Waiting for Wi-Fi: ${task.filename}"
            else -> "Videorder: ${task.filename}"
        }

        val body = "$progressPercent% • $downloadedText / $totalText • $speedText"

        builder.setContentTitle(title)
            .setContentText(body)
            .setProgress(100, progressPercent, task.totalBytes <= 0)

        // Actions: Pause / Resume & Cancel
        if (task.status == DownloadStatus.DOWNLOADING) {
            val pauseIntent = Intent(context, DownloadForegroundService::class.java).apply {
                action = DownloadForegroundService.ACTION_PAUSE
                putExtra(DownloadForegroundService.EXTRA_TASK_ID, task.id)
            }
            val pausePendingIntent = PendingIntent.getService(
                context,
                task.id.hashCode() + 1,
                pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent)
        } else if (task.status == DownloadStatus.PAUSED || task.status == DownloadStatus.WAITING_FOR_WIFI) {
            val resumeIntent = Intent(context, DownloadForegroundService::class.java).apply {
                action = DownloadForegroundService.ACTION_RESUME
                putExtra(DownloadForegroundService.EXTRA_TASK_ID, task.id)
            }
            val resumePendingIntent = PendingIntent.getService(
                context,
                task.id.hashCode() + 2,
                resumeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_media_play, "Resume", resumePendingIntent)
        }

        val cancelIntent = Intent(context, DownloadForegroundService::class.java).apply {
            action = DownloadForegroundService.ACTION_CANCEL
            putExtra(DownloadForegroundService.EXTRA_TASK_ID, task.id)
        }
        val cancelPendingIntent = PendingIntent.getService(
            context,
            task.id.hashCode() + 3,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelPendingIntent)

        return builder.build()
    }

    fun showCompletionNotification(task: DownloadTask, filePath: String) {
        val file = File(filePath)
        if (!file.exists()) return

        val uri: Uri = try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            return
        }

        // Open action
        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, task.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            task.id.hashCode() + 10,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Share action
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = task.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val sharePendingIntent = PendingIntent.getActivity(
            context,
            task.id.hashCode() + 20,
            Intent.createChooser(shareIntent, "Share via"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_COMPLETIONS)
            .setSmallIcon(R.drawable.ic_stat_download)
            .setContentTitle("Download completed")
            .setContentText("${task.filename} (${Formatters.formatBytes(task.totalBytes)})")
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_view, "Open", openPendingIntent)
            .addAction(android.R.drawable.ic_menu_share, "Share", sharePendingIntent)
            .build()

        notificationManager.notify(task.id.hashCode() + 100, notification)
    }

    fun cancelAll() {
        notificationManager.cancel(FOREGROUND_NOTIFICATION_ID)
    }
}
