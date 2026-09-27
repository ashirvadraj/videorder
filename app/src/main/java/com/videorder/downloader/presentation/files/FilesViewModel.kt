package com.videorder.downloader.presentation.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.videorder.downloader.data.repositories.FileRepository
import com.videorder.downloader.domain.models.FileItem
import com.videorder.downloader.domain.models.MediaType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FilesUiState(
    val selectedCategory: MediaType? = null, // null = All
    val files: List<FileItem> = emptyList(),
    val selectedFileForDetails: FileItem? = null,
    val showRenameDialog: Boolean = false,
    val fileToRename: FileItem? = null,
    val renameInput: String = ""
)

class FilesViewModel(
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FilesUiState())
    val uiState: StateFlow<FilesUiState> = _uiState.asStateFlow()

    init {
        refreshFiles()
    }

    fun refreshFiles() {
        viewModelScope.launch {
            fileRepository.syncLocalDirectory()
            loadCategory(_uiState.value.selectedCategory)
        }
    }

    fun selectCategory(type: MediaType?) {
        _uiState.value = _uiState.value.copy(selectedCategory = type)
        loadCategory(type)
    }

    private fun loadCategory(type: MediaType?) {
        viewModelScope.launch {
            val flow = if (type == null) {
                fileRepository.getAllFiles()
            } else {
                fileRepository.getFilesByType(type)
            }
            flow.collect { list ->
                _uiState.value = _uiState.value.copy(files = list)
            }
        }
    }

    fun openFileDetails(file: FileItem) {
        _uiState.value = _uiState.value.copy(selectedFileForDetails = file)
    }

    fun closeFileDetails() {
        _uiState.value = _uiState.value.copy(selectedFileForDetails = null)
    }

    fun startRename(file: FileItem) {
        _uiState.value = _uiState.value.copy(
            showRenameDialog = true,
            fileToRename = file,
            renameInput = file.filename
        )
    }

    fun onRenameInputChange(name: String) {
        _uiState.value = _uiState.value.copy(renameInput = name)
    }

    fun confirmRename() {
        val file = _uiState.value.fileToRename ?: return
        val newName = _uiState.value.renameInput.trim()
        if (newName.isNotBlank()) {
            viewModelScope.launch {
                fileRepository.renameFile(file.id, newName)
                _uiState.value = _uiState.value.copy(showRenameDialog = false, fileToRename = null)
                refreshFiles()
            }
        }
    }

    fun cancelRename() {
        _uiState.value = _uiState.value.copy(showRenameDialog = false, fileToRename = null)
    }

    fun deleteFile(file: FileItem) {
        viewModelScope.launch {
            fileRepository.deleteFile(file.id)
            if (_uiState.value.selectedFileForDetails?.id == file.id) {
                _uiState.value = _uiState.value.copy(selectedFileForDetails = null)
            }
            refreshFiles()
        }
    }
}
