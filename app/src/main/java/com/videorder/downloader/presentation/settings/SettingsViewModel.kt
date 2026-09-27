package com.videorder.downloader.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.videorder.downloader.data.repositories.HistoryRepository
import com.videorder.downloader.data.repositories.SettingsRepository
import com.videorder.downloader.data.repositories.TelegramRepository
import com.videorder.downloader.domain.models.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: AppSettings = AppSettings(downloadLocation = "Downloads/Videorder"),
    val isTelegramLoggedIn: Boolean = false,
    val telegramAccountMasked: String? = null,
    val freeStorageBytes: Long = 0L,
    val videorderStorageBytes: Long = 0L
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val telegramRepository: TelegramRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
        observeTelegram()
        calculateStorage()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { current ->
                _uiState.value = _uiState.value.copy(settings = current)
            }
        }
    }

    private fun observeTelegram() {
        viewModelScope.launch {
            telegramRepository.isLoggedIn().collect { loggedIn ->
                _uiState.value = _uiState.value.copy(isTelegramLoggedIn = loggedIn)
            }
        }
        viewModelScope.launch {
            telegramRepository.getSessionMetadata().collect { meta ->
                _uiState.value = _uiState.value.copy(
                    telegramAccountMasked = meta?.phoneNumberMasked ?: meta?.username
                )
            }
        }
    }

    private fun calculateStorage() {
        viewModelScope.launch {
            val free = android.os.Environment.getExternalStorageDirectory().freeSpace
            _uiState.value = _uiState.value.copy(freeStorageBytes = free)
        }
    }

    fun setDownloadLocation(path: String) {
        viewModelScope.launch {
            settingsRepository.updateDownloadLocation(path)
        }
    }

    fun setMaxSimultaneous(count: Int) {
        viewModelScope.launch {
            settingsRepository.updateMaxSimultaneous(count)
        }
    }

    fun setWifiOnly(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateWifiOnly(enabled)
        }
    }

    fun setTheme(themeMode: String) {
        viewModelScope.launch {
            settingsRepository.updateTheme(themeMode)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory(deleteLocalFiles = false)
        }
    }

    fun logoutTelegram() {
        viewModelScope.launch {
            telegramRepository.logout()
        }
    }
}
