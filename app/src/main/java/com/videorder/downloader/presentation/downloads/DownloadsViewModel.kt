package com.videorder.downloader.presentation.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.videorder.downloader.domain.models.DownloadTask
import com.videorder.downloader.domain.usecases.ManageDownloadUseCase
import com.videorder.downloader.data.repositories.DownloadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DownloadsUiState(
    val selectedTab: Int = 0, // 0 = Active/Queue, 1 = Completed
    val activeTasks: List<DownloadTask> = emptyList(),
    val completedTasks: List<DownloadTask> = emptyList()
)

class DownloadsViewModel(
    private val downloadRepository: DownloadRepository,
    private val manageDownloadUseCase: ManageDownloadUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    init {
        observeTasks()
    }

    private fun observeTasks() {
        viewModelScope.launch {
            downloadRepository.getActiveTasks().collect { active ->
                _uiState.value = _uiState.value.copy(activeTasks = active)
            }
        }
        viewModelScope.launch {
            downloadRepository.getCompletedTasks().collect { completed ->
                _uiState.value = _uiState.value.copy(completedTasks = completed)
            }
        }
    }

    fun selectTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tabIndex)
    }

    fun pauseTask(taskId: String) = manageDownloadUseCase.pause(taskId)
    fun resumeTask(taskId: String) = manageDownloadUseCase.resume(taskId)
    fun cancelTask(taskId: String) = manageDownloadUseCase.cancel(taskId)
    fun deleteTask(taskId: String, deleteFile: Boolean = false) =
        manageDownloadUseCase.deleteTask(taskId, deleteFile)

    fun pauseAll() = manageDownloadUseCase.pauseAll()
    fun resumeAll() = manageDownloadUseCase.resumeAll()
    fun cancelAll() = manageDownloadUseCase.cancelAll()
}
