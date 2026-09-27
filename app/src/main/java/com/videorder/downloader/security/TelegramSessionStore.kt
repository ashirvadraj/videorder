package com.videorder.downloader.security

import android.content.Context
import android.content.SharedPreferences

interface TelegramSessionStorage {
    fun saveSessionToken(sessionToken: String)
    fun getSessionToken(): String?
    fun saveApiCredentials(apiId: Int, apiHash: String)
    fun getApiId(): Int
    fun getApiHash(): String?
    fun hasValidSession(): Boolean
    fun wipeSession()
}

class TelegramSessionStore(
    context: Context,
    private val keystoreManager: SecureKeystoreManager = SecureKeystoreManager(context)
) : TelegramSessionStorage {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "videorder_tg_secure_vault",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_ENCRYPTED_SESSION = "enc_tg_session_token"
        private const val KEY_ENCRYPTED_API_HASH = "enc_tg_api_hash"
        private const val KEY_API_ID = "tg_api_id"
    }

    override fun saveSessionToken(sessionToken: String) {
        val encrypted = keystoreManager.encrypt(sessionToken)
        prefs.edit().putString(KEY_ENCRYPTED_SESSION, encrypted).apply()
    }

    override fun getSessionToken(): String? {
        val encrypted = prefs.getString(KEY_ENCRYPTED_SESSION, null) ?: return null
        return try {
            keystoreManager.decrypt(encrypted).takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            null
        }
    }

    override fun saveApiCredentials(apiId: Int, apiHash: String) {
        val encryptedHash = keystoreManager.encrypt(apiHash)
        prefs.edit()
            .putInt(KEY_API_ID, apiId)
            .putString(KEY_ENCRYPTED_API_HASH, encryptedHash)
            .apply()
    }

    override fun getApiId(): Int {
        return prefs.getInt(KEY_API_ID, 0)
    }

    override fun getApiHash(): String? {
        val encrypted = prefs.getString(KEY_ENCRYPTED_API_HASH, null) ?: return null
        return try {
            keystoreManager.decrypt(encrypted).takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            null
        }
    }

    override fun hasValidSession(): Boolean {
        return !getSessionToken().isNullOrBlank()
    }

    override fun wipeSession() {
        prefs.edit().clear().apply()
        keystoreManager.deleteKey()
    }
}
