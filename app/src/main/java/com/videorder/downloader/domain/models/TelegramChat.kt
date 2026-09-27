package com.videorder.downloader.domain.models

enum class TelegramChatType {
    SAVED_MESSAGES,
    PRIVATE,
    GROUP,
    CHANNEL
}

data class TelegramChat(
    val id: Long,
    val title: String,
    val type: TelegramChatType,
    val username: String? = null,
    val mediaCount: Int = 0,
    val avatarUrl: String? = null
)
