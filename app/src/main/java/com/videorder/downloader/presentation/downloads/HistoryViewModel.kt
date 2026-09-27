package com.videorder.downloader.presentation.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.videorder.downloader.data.database.entities.DownloadHistoryEntity
import com.videorder.downloader.data.repositories.HistoryRepository
import com.videorder.downloader.utils.Formatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val searchQuery: String = "",
    val historyItems: List<DownloadHistoryEntity> = emptyList(),
    val groupedHistory: Map<String, List<DownloadHistoryEntity>> = emptyMap(),
    val showClearDialog: Boolean = false,
    val alsoDeleteFiles: Boolean = false
)

class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            historyRepository.getAllHistory().collect { list ->
                val grouped = list.groupBy { Formatters.getDateBucket(it.completedAt) }
                _uiState.value = _uiState.value.copy(
                    historyItems = list,
                    groupedHistory = grouped
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        viewModelScope.launch {
            val flow = if (query.isBlank()) {
                historyRepository.getAllHistory()
            } else {
                historyRepository.searchHistory(query)
            }
            flow.collect { list ->
                val grouped = list.groupBy { Formatters.getDateBucket(it.completedAt) }
                _uiState.value = _uiState.value.copy(
                    historyItems = list,
                    groupedHistory = grouped
                )
            }
        }
    }

    fun openClearDialog() {
        _uiState.value = _uiState.value.copy(showClearDialog = true, alsoDeleteFiles = false)
    }

    fun closeClearDialog() {
        _uiState.value = _uiState.value.copy(showClearDialog = false)
    }

    fun toggleAlsoDeleteFiles(value: Boolean) {
        _uiState.value = _uiState.value.copy(alsoDeleteFiles = value)
    }

    fun confirmClearHistory() {
        val deleteFiles = _uiState.value.alsoDeleteFiles
        viewModelScope.launch {
            historyRepository.clearHistory(deleteFiles)
            _uiState.value = _uiState.value.copy(showClearDialog = false)
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            historyRepository.deleteHistory(id, deleteLocalFile = false)
        }
    }
}
