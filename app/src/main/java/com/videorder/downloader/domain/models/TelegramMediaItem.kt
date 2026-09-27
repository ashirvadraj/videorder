package com.videorder.downloader.domain.models

data class TelegramMediaItem(
    val id: String, // chat_id + message_id
    val chatId: Long,
    val messageId: Long,
    val filename: String,
    val sizeBytes: Long,
    val mediaType: MediaType,
    val dateEpochSeconds: Long,
    val thumbnailUrl: String? = null,
    val downloadUrl: String,
    val mimeType: String,
    val durationSeconds: Long? = null
)
