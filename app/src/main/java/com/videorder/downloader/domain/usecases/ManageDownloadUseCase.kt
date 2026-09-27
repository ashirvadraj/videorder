package com.videorder.downloader.domain.usecases

import com.videorder.downloader.data.repositories.DownloadRepository

class ManageDownloadUseCase(private val downloadRepository: DownloadRepository) {

    fun pause(taskId: String) = downloadRepository.pause(taskId)
    fun resume(taskId: String) = downloadRepository.resume(taskId)
    fun cancel(taskId: String) = downloadRepository.cancel(taskId)
    fun pauseAll() = downloadRepository.pauseAll()
    fun resumeAll() = downloadRepository.resumeAll()
    fun cancelAll() = downloadRepository.cancelAll()
    fun deleteTask(taskId: String, deleteFile: Boolean = false) =
        downloadRepository.deleteTask(taskId, deleteFile)
}
