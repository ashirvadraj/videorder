package com.videorder.downloader.downloader.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.videorder.downloader.VideorderApp

class DownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? VideorderApp ?: return Result.failure()
        return try {
            app.queueManager.processQueue()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
