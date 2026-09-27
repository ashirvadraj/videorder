package com.videorder.downloader.presentation.home

import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.videorder.downloader.domain.models.DownloadStatus
import com.videorder.downloader.domain.models.DownloadTask
import com.videorder.downloader.domain.models.MediaItem
import com.videorder.downloader.domain.models.MediaQuality
import com.videorder.downloader.domain.usecases.DetectMediaUseCase
import com.videorder.downloader.domain.usecases.StartDownloadUseCase
import com.videorder.downloader.data.repositories.DownloadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val urlInput: String = "",
    val isDetecting: Boolean = false,
    val detectedMedia: MediaItem? = null,
    val selectedQuality: MediaQuality? = null,
    val errorMessage: String? = null,
    val activeTasks: List<DownloadTask> = emptyList(),
    val completedTasks: List<DownloadTask> = emptyList()
)

class HomeViewModel(
    private val detectMediaUseCase: DetectMediaUseCase,
    private val startDownloadUseCase: StartDownloadUseCase,
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeDownloads()
    }

    private fun observeDownloads() {
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

    fun onUrlChange(newUrl: String) {
        _uiState.value = _uiState.value.copy(urlInput = newUrl, errorMessage = null)
    }

    fun pasteFromClipboard(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text?.toString() ?: ""
            if (text.isNotBlank()) {
                onUrlChange(text)
            }
        }
    }

    fun detectMedia(targetUrl: String? = null) {
        val url = targetUrl ?: _uiState.value.urlInput
        if (url.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid URL.")
            return
        }

        _uiState.value = _uiState.value.copy(isDetecting = true, errorMessage = null)

        viewModelScope.launch {
            val result = detectMediaUseCase(url)
            result.fold(
                onSuccess = { mediaItem ->
                    _uiState.value = _uiState.value.copy(
                        isDetecting = false,
                        detectedMedia = mediaItem,
                        selectedQuality = mediaItem.qualities.firstOrNull(),
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isDetecting = false,
                        detectedMedia = null,
                        errorMessage = error.message ?: "Failed to detect media streams."
                    )
                }
            )
        }
    }

    fun selectQuality(quality: MediaQuality) {
        _uiState.value = _uiState.value.copy(selectedQuality = quality)
    }

    fun confirmDownload(customFilename: String? = null) {
        val media = _uiState.value.detectedMedia ?: return
        val quality = _uiState.value.selectedQuality ?: media.qualities.firstOrNull() ?: return

        startDownloadUseCase(media, quality, customFilename)
        _uiState.value = _uiState.value.copy(
            detectedMedia = null,
            selectedQuality = null,
            urlInput = ""
        )
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(
            detectedMedia = null,
            selectedQuality = null,
            errorMessage = null
        )
    }
}
