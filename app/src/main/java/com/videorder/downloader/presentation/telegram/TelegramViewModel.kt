package com.videorder.downloader.presentation.telegram

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.videorder.downloader.data.database.entities.TelegramSessionMetadataEntity
import com.videorder.downloader.domain.models.MediaType
import com.videorder.downloader.domain.models.TelegramChat
import com.videorder.downloader.domain.models.TelegramChatType
import com.videorder.downloader.domain.models.TelegramMediaItem
import com.videorder.downloader.domain.usecases.TelegramAuthUseCase
import com.videorder.downloader.domain.usecases.TelegramBrowseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TelegramUiState(
    val isLoggedIn: Boolean = false,
    val sessionMetadata: TelegramSessionMetadataEntity? = null,
    val isLoading: Boolean = false,
    val chats: List<TelegramChat> = emptyList(),
    val selectedChatType: TelegramChatType? = null,
    val selectedChat: TelegramChat? = null,
    val mediaItems: List<TelegramMediaItem> = emptyList(),
    val selectedMediaType: MediaType? = null,
    val selectedMediaIds: Set<String> = emptySet(),
    val showLoginDialog: Boolean = false,
    val authStep: Int = 0, // 0 = Phone, 1 = Verification Code, 2 = 2FA Password
    val phoneInput: String = "",
    val codeInput: String = "",
    val phoneCodeHash: String = "",
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class TelegramViewModel(
    private val authUseCase: TelegramAuthUseCase,
    private val browseUseCase: TelegramBrowseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TelegramUiState())
    val uiState: StateFlow<TelegramUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authUseCase.isLoggedIn().collect { loggedIn ->
                _uiState.value = _uiState.value.copy(isLoggedIn = loggedIn)
                if (loggedIn) {
                    loadChats()
                }
            }
        }
        viewModelScope.launch {
            authUseCase.getSessionMetadata().collect { meta ->
                _uiState.value = _uiState.value.copy(sessionMetadata = meta)
            }
        }
    }

    fun openLoginDialog() {
        _uiState.value = _uiState.value.copy(
            showLoginDialog = true,
            authStep = 0,
            errorMessage = null,
            phoneInput = "",
            codeInput = ""
        )
    }

    fun closeLoginDialog() {
        _uiState.value = _uiState.value.copy(showLoginDialog = false, errorMessage = null)
    }

    fun onPhoneChange(phone: String) {
        _uiState.value = _uiState.value.copy(phoneInput = phone, errorMessage = null)
    }

    fun onCodeChange(code: String) {
        _uiState.value = _uiState.value.copy(codeInput = code, errorMessage = null)
    }

    fun requestVerificationCode(apiId: Int = 2040, apiHash: String = "b18441a29bd63e") {
        val phone = _uiState.value.phoneInput.trim()
        if (phone.length < 5) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid phone number with country code.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = authUseCase.sendCode(phone, apiId, apiHash)
            result.fold(
                onSuccess = { hash ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        authStep = 1,
                        phoneCodeHash = hash,
                        infoMessage = "Official confirmation code sent to your Telegram app."
                    )
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = err.message ?: "Failed to send code."
                    )
                }
            )
        }
    }

    fun submitCode(password2FA: String? = null) {
        val code = _uiState.value.codeInput.trim()
        val hash = _uiState.value.phoneCodeHash

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = authUseCase.verifyCode(hash, code, password2FA)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showLoginDialog = false,
                        errorMessage = null
                    )
                    loadChats()
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = err.message ?: "Invalid code."
                    )
                }
            )
        }
    }

    fun loginDemoMode() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = authUseCase.loginWithDemoSession()
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showLoginDialog = false
                    )
                    loadChats()
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = err.message
                    )
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authUseCase.logout()
            _uiState.value = _uiState.value.copy(
                isLoggedIn = false,
                selectedChat = null,
                mediaItems = emptyList(),
                selectedMediaIds = emptySet()
            )
        }
    }

    fun loadChats(typeFilter: TelegramChatType? = null) {
        _uiState.value = _uiState.value.copy(isLoading = true, selectedChatType = typeFilter)
        viewModelScope.launch {
            val chats = browseUseCase.getChats(typeFilter)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                chats = chats
            )
            if (_uiState.value.selectedChat == null && chats.isNotEmpty()) {
                selectChat(chats.first())
            }
        }
    }

    fun selectChat(chat: TelegramChat) {
        _uiState.value = _uiState.value.copy(
            selectedChat = chat,
            isLoading = true,
            selectedMediaIds = emptySet()
        )
        viewModelScope.launch {
            val media = browseUseCase.getChatMedia(chat.id, _uiState.value.selectedMediaType)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                mediaItems = media
            )
        }
    }

    fun filterMediaType(type: MediaType?) {
        _uiState.value = _uiState.value.copy(selectedMediaType = type)
        val chat = _uiState.value.selectedChat ?: return
        viewModelScope.launch {
            val media = browseUseCase.getChatMedia(chat.id, type)
            _uiState.value = _uiState.value.copy(mediaItems = media)
        }
    }

    fun toggleMediaSelection(id: String) {
        val current = _uiState.value.selectedMediaIds.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _uiState.value = _uiState.value.copy(selectedMediaIds = current)
    }

    fun selectAllMedia() {
        val allIds = _uiState.value.mediaItems.map { it.id }.toSet()
        _uiState.value = _uiState.value.copy(selectedMediaIds = allIds)
    }

    fun clearMediaSelection() {
        _uiState.value = _uiState.value.copy(selectedMediaIds = emptySet())
    }

    fun downloadSingle(item: TelegramMediaItem) {
        browseUseCase.enqueueMediaItem(item)
    }

    fun downloadSelected() {
        val selectedIds = _uiState.value.selectedMediaIds
        val itemsToDownload = _uiState.value.mediaItems.filter { selectedIds.contains(it.id) }
        browseUseCase.enqueueMultiple(itemsToDownload)
        clearMediaSelection()
    }
}
