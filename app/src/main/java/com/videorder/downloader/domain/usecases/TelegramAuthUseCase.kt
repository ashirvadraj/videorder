package com.videorder.downloader.domain.usecases

import com.videorder.downloader.data.repositories.TelegramRepository
import kotlinx.coroutines.flow.Flow

class TelegramAuthUseCase(private val repository: TelegramRepository) {

    fun isLoggedIn(): Flow<Boolean> = repository.isLoggedIn()
    fun getSessionMetadata() = repository.getSessionMetadata()

    suspend fun sendCode(phoneNumber: String, apiId: Int, apiHash: String): Result<String> {
        return repository.sendVerificationCode(phoneNumber, apiId, apiHash)
    }

    suspend fun verifyCode(phoneCodeHash: String, code: String, password2FA: String? = null): Result<Boolean> {
        return repository.verifyCode(phoneCodeHash, code, password2FA)
    }

    suspend fun loginWithBotToken(token: String) = repository.loginWithBotToken(token)

    suspend fun saveWebSession(accountLabel: String, userId: Long = 0L, username: String? = null): Boolean {
        return repository.saveWebSession(accountLabel, userId, username)
    }

    suspend fun loginWithDemoSession(): Result<Boolean> {
        return repository.loginWithDemoSession()
    }

    suspend fun logout(): Boolean {
        return repository.logout()
    }
}
