package com.videorder.downloader.downloader.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.videorder.downloader.VideorderApp
import com.videorder.downloader.downloader.notifications.DownloadNotificationManager
import com.videorder.downloader.domain.models.DownloadStatus
import com.videorder.downloader.domain.models.DownloadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DownloadForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var notificationManager: DownloadNotificationManager

    companion object {
        const val ACTION_START = "com.videorder.downloader.action.START"
        const val ACTION_PAUSE = "com.videorder.downloader.action.PAUSE"
        const val ACTION_RESUME = "com.videorder.downloader.action.RESUME"
        const val ACTION_CANCEL = "com.videorder.downloader.action.CANCEL"
        const val EXTRA_TASK_ID = "extra_task_id"

        fun startService(context: Context) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = DownloadNotificationManager(this)
        startForeground(
            DownloadNotificationManager.FOREGROUND_NOTIFICATION_ID,
            notificationManager.buildForegroundNotification(null)
        )

        observeDownloads()
    }

    private fun observeDownloads() {
        val app = application as? VideorderApp ?: return
        serviceScope.launch {
            app.queueManager.onTaskUpdate.collectLatest { task ->
                if (task != null) {
                    if (task.status == DownloadStatus.DOWNLOADING ||
                        task.status == DownloadStatus.PAUSED ||
                        task.status == DownloadStatus.WAITING_FOR_WIFI
                    ) {
                        val notification = notificationManager.buildForegroundNotification(task)
                        val sysNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                        sysNotificationManager.notify(DownloadNotificationManager.FOREGROUND_NOTIFICATION_ID, notification)
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as? VideorderApp
        val taskId = intent?.getStringExtra(EXTRA_TASK_ID)

        when (intent?.action) {
            ACTION_PAUSE -> {
                if (!taskId.isNullOrBlank() && app != null) {
                    app.queueManager.pause(taskId)
                }
            }
            ACTION_RESUME -> {
                if (!taskId.isNullOrBlank() && app != null) {
                    app.queueManager.resume(taskId)
                }
            }
            ACTION_CANCEL -> {
                if (!taskId.isNullOrBlank() && app != null) {
                    app.queueManager.cancel(taskId)
                }
            }
            ACTION_START -> {
                // Ensure foreground notification active
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
    }
}
