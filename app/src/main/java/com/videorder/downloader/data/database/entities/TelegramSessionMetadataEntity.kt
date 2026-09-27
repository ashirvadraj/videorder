package com.videorder.downloader.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "telegram_sessions")
data class TelegramSessionMetadataEntity(
    @PrimaryKey
    val id: Int = 1,
    val isLoggedIn: Boolean = false,
    val phoneNumberMasked: String? = null,
    val userId: Long? = null,
    val firstName: String? = null,
    val username: String? = null,
    val authDate: Long = 0L,
    // Android KeyStore alias pointing to AES-256 GCM encrypted token.
    // NEVER contains the plaintext authentication token.
    val keyStoreAlias: String = "tg_secure_session_key"
)
