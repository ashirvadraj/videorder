package com.videorder.downloader.downloader.queue

import android.content.Context
import com.videorder.downloader.data.database.AppDatabase
import com.videorder.downloader.data.database.entities.DownloadHistoryEntity
import com.videorder.downloader.data.database.entities.DownloadTaskEntity
import com.videorder.downloader.data.database.entities.FileMetadataEntity
import com.videorder.downloader.downloader.core.Downloader
import com.videorder.downloader.downloader.network.HttpDownloader
import com.videorder.downloader.downloader.network.NetworkMonitor
import com.videorder.downloader.downloader.notifications.DownloadNotificationManager
import com.videorder.downloader.domain.models.DownloadStatus
import com.videorder.downloader.domain.models.DownloadTask
import com.videorder.downloader.domain.models.MediaType
import com.videorder.downloader.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class DownloadQueueManager(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context),
    private val networkMonitor: NetworkMonitor = NetworkMonitor(context),
    private val notificationManager: DownloadNotificationManager = DownloadNotificationManager(context)
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val taskDao = database.downloadTaskDao()
    private val historyDao = database.downloadHistoryDao()
    private val fileDao = database.fileMetadataDao()
    private val settingsDao = database.settingsDao()

    private val activeJobs = ConcurrentHashMap<String, Job>()
    private val pausedFlags = ConcurrentHashMap<String, Boolean>()
    private val cancelledFlags = ConcurrentHashMap<String, Boolean>()

    private val downloader: Downloader = HttpDownloader()

    private val _onTaskUpdate = MutableStateFlow<DownloadTask?>(null)
    val onTaskUpdate = _onTaskUpdate.asStateFlow()

    init {
        // Monitor network state for Wi-Fi Only constraint & auto-resume
        scope.launch {
            networkMonitor.currentNetworkType.collect {
                checkNetworkConstraints()
            }
        }
    }

    private suspend fun checkNetworkConstraints() {
        val settings = settingsDao.getSettingsSync() ?: return
        if (settings.wifiOnly && !networkMonitor.isWifiConnected()) {
            // Pause active downloads with WAITING_FOR_WIFI
            activeJobs.keys.toList().forEach { taskId ->
                pause(taskId, isWaitingWifi = true)
            }
        } else if (settings.autoResume && networkMonitor.isConnected()) {
            // Auto resume tasks waiting for Wi-Fi or interrupted
            val allTasks = taskDao.getQueuedTasks()
            allTasks.forEach { task ->
                if (task.status == DownloadStatus.WAITING_FOR_WIFI.name || task.status == DownloadStatus.QUEUED.name) {
                    resume(task.id)
                }
            }
        }
    }

    fun enqueue(task: DownloadTask) {
        scope.launch {
            taskDao.insertTask(DownloadTaskEntity.fromDomain(task))
            processQueue()
        }
    }

    fun processQueue() {
        scope.launch {
            val settings = settingsDao.getSettingsSync()
            val maxSimultaneous = settings?.maxSimultaneousDownloads ?: 3
            val wifiOnly = settings?.wifiOnly ?: false

            if (wifiOnly && !networkMonitor.isWifiConnected()) {
                return@launch
            }

            val runningCount = activeJobs.size
            val slotsAvailable = (maxSimultaneous - runningCount).coerceAtLeast(0)

            if (slotsAvailable > 0) {
                val queued = taskDao.getQueuedTasks().take(slotsAvailable)
                for (queuedTask in queued) {
                    startDownloadInternal(queuedTask.toDomain())
                }
            }
        }
    }

    private fun startDownloadInternal(task: DownloadTask) {
        if (activeJobs.containsKey(task.id)) return

        pausedFlags[task.id] = false
        cancelledFlags[task.id] = false

        val job = scope.launch {
            val targetDir = FileUtils.getTargetDownloadDirectory(context)
            val destFile = File(targetDir, task.filename)

            taskDao.updateStatus(task.id, DownloadStatus.DOWNLOADING.name)

            val runningTask = task.copy(status = DownloadStatus.DOWNLOADING, localPath = destFile.absolutePath)
            _onTaskUpdate.value = runningTask

            try {
                downloader.download(
                    task = runningTask,
                    destinationFile = destFile,
                    onProgress = { downloaded, total, speed ->
                        val progress = if (total > 0) (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
                        taskDao.updateProgress(
                            id = task.id,
                            downloaded = downloaded,
                            total = total,
                            progress = progress,
                            speed = speed,
                            status = DownloadStatus.DOWNLOADING.name
                        )
                        val updated = runningTask.copy(
                            downloadedBytes = downloaded,
                            totalBytes = total,
                            progress = progress,
                            speed = speed
                        )
                        _onTaskUpdate.value = updated
                    },
                    isCancelled = { cancelledFlags[task.id] == true },
                    isPaused = { pausedFlags[task.id] == true }
                )

                if (cancelledFlags[task.id] == true) {
                    taskDao.updateStatus(task.id, DownloadStatus.CANCELLED.name)
                    if (destFile.exists()) destFile.delete()
                    _onTaskUpdate.value = runningTask.copy(status = DownloadStatus.CANCELLED)
                } else if (pausedFlags[task.id] == true) {
                    // Preserved partial file on disk for resume!
                    _onTaskUpdate.value = runningTask.copy(status = DownloadStatus.PAUSED)
                } else {
                    // Successfully completed!
                    val completedTime = System.currentTimeMillis()
                    taskDao.markCompleted(
                        id = task.id,
                        status = DownloadStatus.COMPLETED.name,
                        completedAt = completedTime,
                        localPath = destFile.absolutePath
                    )

                    // Record in History
                    historyDao.insertHistory(
                        DownloadHistoryEntity(
                            taskId = task.id,
                            url = task.url,
                            filename = task.filename,
                            mimeType = task.mimeType,
                            fileSize = destFile.length(),
                            quality = task.qualityLabel,
                            localPath = destFile.absolutePath,
                            completedAt = completedTime,
                            thumbnailUrl = task.thumbnailUrl,
                            mediaType = task.mediaType.name
                        )
                    )

                    // Index in File Metadata
                    fileDao.insertFile(
                        FileMetadataEntity(
                            filename = task.filename,
                            localPath = destFile.absolutePath,
                            sizeBytes = destFile.length(),
                            mimeType = task.mimeType,
                            mediaType = task.mediaType.name,
                            lastModified = completedTime,
                            thumbnailUri = task.thumbnailUrl
                        )
                    )

                    val finishedTask = runningTask.copy(
                        status = DownloadStatus.COMPLETED,
                        downloadedBytes = destFile.length(),
                        totalBytes = destFile.length(),
                        progress = 1.0f,
                        speed = 0L,
                        completedAt = completedTime
                    )
                    _onTaskUpdate.value = finishedTask

                    notificationManager.showCompletionNotification(finishedTask, destFile.absolutePath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                taskDao.updateStatus(task.id, DownloadStatus.FAILED.name, e.message)
                _onTaskUpdate.value = runningTask.copy(
                    status = DownloadStatus.FAILED,
                    errorMessage = e.message ?: "Download failed"
                )
            } finally {
                activeJobs.remove(task.id)
                pausedFlags.remove(task.id)
                cancelledFlags.remove(task.id)
                processQueue()
            }
        }

        activeJobs[task.id] = job
    }

    fun pause(taskId: String, isWaitingWifi: Boolean = false) {
        pausedFlags[taskId] = true
        scope.launch {
            val status = if (isWaitingWifi) DownloadStatus.WAITING_FOR_WIFI else DownloadStatus.PAUSED
            taskDao.updateStatus(taskId, status.name)
            activeJobs[taskId]?.cancel()
            activeJobs.remove(taskId)
            processQueue()
        }
    }

    fun resume(taskId: String) {
        scope.launch {
            val entity = taskDao.getTaskById(taskId) ?: return@launch
            pausedFlags[taskId] = false
            cancelledFlags[taskId] = false
            taskDao.updateStatus(taskId, DownloadStatus.QUEUED.name)
            processQueue()
        }
    }

    fun cancel(taskId: String) {
        cancelledFlags[taskId] = true
        scope.launch {
            activeJobs[taskId]?.cancel()
            activeJobs.remove(taskId)
            taskDao.updateStatus(taskId, DownloadStatus.CANCELLED.name)
            processQueue()
        }
    }

    fun pauseAll() {
        scope.launch {
            activeJobs.keys.toList().forEach { pause(it) }
        }
    }

    fun resumeAll() {
        scope.launch {
            val tasks = taskDao.getQueuedTasks()
            tasks.forEach { resume(it.id) }
        }
    }

    fun cancelAll() {
        scope.launch {
            activeJobs.keys.toList().forEach { cancel(it) }
            taskDao.deleteAll()
        }
    }

    fun deleteTask(taskId: String, deleteFileFromDisk: Boolean = false) {
        scope.launch {
            cancel(taskId)
            val entity = taskDao.getTaskById(taskId)
            if (deleteFileFromDisk && entity?.localPath != null) {
                try {
                    val file = File(entity.localPath)
                    if (file.exists()) file.delete()
                } catch (ignored: Exception) {}
            }
            taskDao.deleteTask(taskId)
        }
    }
}
