package com.videorder.downloader

import com.videorder.downloader.domain.models.DownloadStatus
import com.videorder.downloader.domain.models.DownloadTask
import com.videorder.downloader.domain.models.MediaType
import org.junit.Assert.*
import org.junit.Test

class DownloadQueueTest {

    @Test
    fun testTaskProgressAndEtaCalculation() {
        val totalBytes = 100 * 1024 * 1024L // 100 MB
        val downloadedBytes = 50 * 1024 * 1024L // 50 MB
        val speed = 5 * 1024 * 1024L // 5 MB/s

        val remainingBytes = totalBytes - downloadedBytes
        val eta = remainingBytes / speed
        assertEquals(10L, eta) // 10 seconds remaining

        val task = DownloadTask(
            id = "task-1",
            url = "https://example.com/video.mp4",
            filename = "video.mp4",
            mimeType = "video/mp4",
            totalBytes = totalBytes,
            downloadedBytes = downloadedBytes,
            status = DownloadStatus.DOWNLOADING,
            progress = 0.5f,
            speed = speed,
            createdAt = System.currentTimeMillis(),
            etaSeconds = eta,
            mediaType = MediaType.VIDEO
        )

        assertEquals(DownloadStatus.DOWNLOADING, task.status)
        assertEquals(0.5f, task.progress, 0.01f)
        assertEquals(10L, task.etaSeconds)
    }

    @Test
    fun testTaskStatusTransitions() {
        var status = DownloadStatus.QUEUED
        assertEquals(DownloadStatus.QUEUED, status)

        status = DownloadStatus.DOWNLOADING
        assertEquals(DownloadStatus.DOWNLOADING, status)

        status = DownloadStatus.PAUSED
        assertEquals(DownloadStatus.PAUSED, status)

        status = DownloadStatus.WAITING_FOR_WIFI
        assertEquals(DownloadStatus.WAITING_FOR_WIFI, status)

        status = DownloadStatus.COMPLETED
        assertEquals(DownloadStatus.COMPLETED, status)
    }
}
