package com.videorder.downloader.downloader.network

import com.videorder.downloader.downloader.core.Downloader
import com.videorder.downloader.downloader.core.NetworkInterruptedException
import com.videorder.downloader.domain.models.DownloadTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.util.concurrent.TimeUnit

class HttpDownloader(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) : Downloader {

    override val name: String = "HttpDownloader"

    override fun canHandle(task: DownloadTask): Boolean {
        val url = task.url.lowercase()
        return url.startsWith("http://") || url.startsWith("https://")
    }

    override suspend fun download(
        task: DownloadTask,
        destinationFile: File,
        onProgress: suspend (downloadedBytes: Long, totalBytes: Long, speedBytesPerSec: Long) -> Unit,
        isCancelled: () -> Boolean,
        isPaused: () -> Boolean
    ): File = withContext(Dispatchers.IO) {
        val parentDir = destinationFile.parentFile
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs()
        }

        var existingBytes = if (destinationFile.exists()) destinationFile.length() else 0L

        // If existing file is already complete, return it
        if (task.totalBytes > 0 && existingBytes >= task.totalBytes) {
            onProgress(existingBytes, existingBytes, 0L)
            return@withContext destinationFile
        }

        val requestBuilder = Request.Builder()
            .url(task.url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")

        // Resume support: send Range header if partial content exists
        if (existingBytes > 0) {
            requestBuilder.header("Range", "bytes=$existingBytes-")
        }

        val request = requestBuilder.build()
        val response = try {
            client.newCall(request).execute()
        } catch (e: IOException) {
            throw NetworkInterruptedException("Network error: ${e.message}")
        }

        if (!response.isSuccessful && response.code != 206) {
            response.close()
            // If range request was rejected (e.g. 416 Range Not Satisfiable), restart from 0
            if (response.code == 416 || existingBytes > 0) {
                if (destinationFile.exists()) destinationFile.delete()
                existingBytes = 0L
                val freshRequest = Request.Builder().url(task.url).build()
                val freshResponse = client.newCall(freshRequest).execute()
                if (!freshResponse.isSuccessful) {
                    freshResponse.close()
                    throw NetworkInterruptedException("HTTP Error ${freshResponse.code}: ${freshResponse.message}")
                }
                return@withContext streamToFile(
                    freshResponse,
                    destinationFile,
                    0L,
                    freshResponse.body?.contentLength() ?: task.totalBytes,
                    onProgress,
                    isCancelled,
                    isPaused
                )
            }
            throw NetworkInterruptedException("HTTP Error ${response.code}: ${response.message}")
        }

        val isPartial = response.code == 206
        val startingOffset = if (isPartial) existingBytes else 0L
        if (!isPartial && existingBytes > 0) {
            // Server did not honor Range header; restart file
            destinationFile.delete()
        }

        val contentLength = response.body?.contentLength() ?: -1L
        val totalBytes = if (contentLength > 0) {
            if (isPartial) startingOffset + contentLength else contentLength
        } else {
            task.totalBytes
        }

        streamToFile(
            response,
            destinationFile,
            startingOffset,
            totalBytes,
            onProgress,
            isCancelled,
            isPaused
        )
    }

    private suspend fun streamToFile(
        response: okhttp3.Response,
        destinationFile: File,
        startingOffset: Long,
        totalBytes: Long,
        onProgress: suspend (downloaded: Long, total: Long, speed: Long) -> Unit,
        isCancelled: () -> Boolean,
        isPaused: () -> Boolean
    ): File {
        val body = response.body ?: throw NetworkInterruptedException("Empty response body.")
        var inputStream: InputStream? = null
        var randomAccessFile: RandomAccessFile? = null

        try {
            inputStream = body.byteStream()
            randomAccessFile = RandomAccessFile(destinationFile, "rw")
            randomAccessFile.seek(startingOffset)

            val buffer = ByteArray(32 * 1024)
            var bytesRead: Int
            var totalDownloaded = startingOffset

            var lastUpdateTime = System.currentTimeMillis()
            var bytesInWindow = 0L
            var currentSpeed = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                if (isCancelled()) {
                    response.close()
                    return destinationFile
                }
                if (isPaused()) {
                    response.close()
                    return destinationFile
                }

                randomAccessFile.write(buffer, 0, bytesRead)
                totalDownloaded += bytesRead
                bytesInWindow += bytesRead

                val now = System.currentTimeMillis()
                val elapsed = now - lastUpdateTime
                if (elapsed >= 500) {
                    currentSpeed = (bytesInWindow * 1000L) / elapsed
                    bytesInWindow = 0L
                    lastUpdateTime = now
                    onProgress(totalDownloaded, totalBytes, currentSpeed)
                }
            }

            // Final progress update on completion
            onProgress(totalDownloaded, totalBytes, 0L)
            return destinationFile
        } finally {
            try { inputStream?.close() } catch (ignored: Exception) {}
            try { randomAccessFile?.close() } catch (ignored: Exception) {}
            response.close()
        }
    }
}
