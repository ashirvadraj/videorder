package com.videorder.downloader.downloader.core

import com.videorder.downloader.domain.models.DownloadTask
import java.io.File

interface Downloader {
    val name: String
    fun canHandle(task: DownloadTask): Boolean
    
    suspend fun download(
        task: DownloadTask,
        destinationFile: File,
        onProgress: suspend (downloadedBytes: Long, totalBytes: Long, speedBytesPerSec: Long) -> Unit,
        isCancelled: () -> Boolean,
        isPaused: () -> Boolean
    ): File
}
