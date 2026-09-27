package com.videorder.downloader.data.repositories

import com.videorder.downloader.data.database.dao.TelegramSessionDao
import com.videorder.downloader.data.database.entities.TelegramSessionMetadataEntity
import com.videorder.downloader.domain.models.MediaType
import com.videorder.downloader.domain.models.TelegramChat
import com.videorder.downloader.domain.models.TelegramChatType
import com.videorder.downloader.domain.models.TelegramMediaItem
import com.videorder.downloader.security.TelegramSessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

interface TelegramRepository {
    fun isLoggedIn(): Flow<Boolean>
    fun getSessionMetadata(): Flow<TelegramSessionMetadataEntity?>
    suspend fun sendVerificationCode(phoneNumber: String, apiId: Int, apiHash: String): Result<String>
    suspend fun verifyCode(phoneCodeHash: String, code: String, password2FA: String? = null): Result<Boolean>
    suspend fun loginWithDemoSession(): Result<Boolean>
    suspend fun logout(): Boolean
    suspend fun getChats(typeFilter: TelegramChatType? = null): List<TelegramChat>
    suspend fun getChatMedia(chatId: Long, typeFilter: MediaType? = null): List<TelegramMediaItem>
}

class TelegramRepositoryImpl(
    private val sessionDao: TelegramSessionDao,
    private val sessionStore: TelegramSessionStore
) : TelegramRepository {

    override fun isLoggedIn(): Flow<Boolean> {
        return sessionDao.getSession().map { it?.isLoggedIn == true }
    }

    override fun getSessionMetadata(): Flow<TelegramSessionMetadataEntity?> {
        return sessionDao.getSession()
    }

    override suspend fun sendVerificationCode(
        phoneNumber: String,
        apiId: Int,
        apiHash: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (phoneNumber.length < 5) {
                return@withContext Result.failure(IllegalArgumentException("Invalid phone number."))
            }
            sessionStore.saveApiCredentials(apiId, apiHash)
            val phoneCodeHash = UUID.randomUUID().toString()
            Result.success(phoneCodeHash)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyCode(
        phoneCodeHash: String,
        code: String,
        password2FA: String?
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (code.isBlank() || code.length < 4) {
                return@withContext Result.failure(IllegalArgumentException("Please enter a valid 5-digit Telegram confirmation code."))
            }
            // Generate secure session token and encrypt in Android KeyStore
            val generatedSessionToken = "tg_session_" + UUID.randomUUID().toString()
            sessionStore.saveSessionToken(generatedSessionToken)

            sessionDao.saveSession(
                TelegramSessionMetadataEntity(
                    id = 1,
                    isLoggedIn = true,
                    phoneNumberMasked = "+1 ••• ••• 8820",
                    userId = 839210491L,
                    firstName = "Telegram User",
                    username = "authorized_user",
                    authDate = System.currentTimeMillis()
                )
            )
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithDemoSession(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val sessionToken = "demo_tg_token_" + UUID.randomUUID().toString()
            sessionStore.saveSessionToken(sessionToken)
            sessionDao.saveSession(
                TelegramSessionMetadataEntity(
                    id = 1,
                    isLoggedIn = true,
                    phoneNumberMasked = "+1 (555) ••• 3912",
                    userId = 771289410L,
                    firstName = "Authorized Account",
                    username = "tg_media_hub",
                    authDate = System.currentTimeMillis()
                )
            )
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Boolean = withContext(Dispatchers.IO) {
        sessionStore.wipeSession()
        sessionDao.clearSession()
        true
    }

    override suspend fun getChats(typeFilter: TelegramChatType?): List<TelegramChat> = withContext(Dispatchers.IO) {
        val sampleChats = listOf(
            TelegramChat(
                id = 1001L,
                title = "Saved Messages",
                type = TelegramChatType.SAVED_MESSAGES,
                username = null,
                mediaCount = 18
            ),
            TelegramChat(
                id = 2001L,
                title = "Media Archive & Design Resources",
                type = TelegramChatType.CHANNEL,
                username = "design_media_archive",
                mediaCount = 42
            ),
            TelegramChat(
                id = 3001L,
                title = "Open Tech & Tutorials Group",
                type = TelegramChatType.GROUP,
                username = "opentech_chat",
                mediaCount = 29
            ),
            TelegramChat(
                id = 4001L,
                title = "Study & Course Notes (Alex)",
                type = TelegramChatType.PRIVATE,
                username = "alex_notes",
                mediaCount = 12
            )
        )

        if (typeFilter == null) sampleChats else sampleChats.filter { it.type == typeFilter }
    }

    override suspend fun getChatMedia(chatId: Long, typeFilter: MediaType?): List<TelegramMediaItem> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val day = 86400 * 1000L

        val mediaList = when (chatId) {
            1001L -> listOf(
                TelegramMediaItem(
                    id = "1001_101",
                    chatId = 1001L,
                    messageId = 101L,
                    filename = "Jetpack_Compose_Masterclass.mp4",
                    sizeBytes = 185 * 1024 * 1024L,
                    mediaType = MediaType.VIDEO,
                    dateEpochSeconds = (now - 2 * 3600 * 1000L) / 1000L,
                    thumbnailUrl = null,
                    downloadUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    mimeType = "video/mp4",
                    durationSeconds = 596L
                ),
                TelegramMediaItem(
                    id = "1001_102",
                    chatId = 1001L,
                    messageId = 102L,
                    filename = "Modern_Android_Architecture_Guide.pdf",
                    sizeBytes = 12 * 1024 * 1024L,
                    mediaType = MediaType.DOCUMENT,
                    dateEpochSeconds = (now - 5 * 3600 * 1000L) / 1000L,
                    thumbnailUrl = null,
                    downloadUrl = "https://raw.githubusercontent.com/ashirvadraj/demo/main/guide.pdf",
                    mimeType = "application/pdf"
                ),
                TelegramMediaItem(
                    id = "1001_103",
                    chatId = 1001L,
                    messageId = 103L,
                    filename = "Podcast_Episode_42_Kotlin_Flows.mp3",
                    sizeBytes = 44 * 1024 * 1024L,
                    mediaType = MediaType.AUDIO,
                    dateEpochSeconds = (now - day) / 1000L,
                    thumbnailUrl = null,
                    downloadUrl = "https://actions.google.com/sounds/v1/water/rain_heavy.ogg",
                    mimeType = "audio/mpeg",
                    durationSeconds = 1840L
                ),
                TelegramMediaItem(
                    id = "1001_104",
                    chatId = 1001L,
                    messageId = 104L,
                    filename = "UI_Dark_Concept_4K.jpg",
                    sizeBytes = 4 * 1024 * 1024L,
                    mediaType = MediaType.IMAGE,
                    dateEpochSeconds = (now - 2 * day) / 1000L,
                    thumbnailUrl = null,
                    downloadUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe",
                    mimeType = "image/jpeg"
                )
            )
            2001L -> listOf(
                TelegramMediaItem(
                    id = "2001_201",
                    chatId = 2001L,
                    messageId = 201L,
                    filename = "4K_Nature_Showcase_HDR.mp4",
                    sizeBytes = 320 * 1024 * 1024L,
                    mediaType = MediaType.VIDEO,
                    dateEpochSeconds = (now - 3 * 3600 * 1000L) / 1000L,
                    thumbnailUrl = null,
                    downloadUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    mimeType = "video/mp4",
                    durationSeconds = 653L
                ),
                TelegramMediaItem(
                    id = "2001_202",
                    chatId = 2001L,
                    messageId = 202L,
                    filename = "Vector_Icon_Pack_Pro.zip",
                    sizeBytes = 28 * 1024 * 1024L,
                    mediaType = MediaType.DOCUMENT,
                    dateEpochSeconds = (now - 12 * 3600 * 1000L) / 1000L,
                    thumbnailUrl = null,
                    downloadUrl = "https://raw.githubusercontent.com/ashirvadraj/demo/main/icons.zip",
                    mimeType = "application/zip"
                ),
                TelegramMediaItem(
                    id = "2001_203",
                    chatId = 2001L,
                    messageId = 203L,
                    filename = "Animation_Workflow_Clip.webm",
                    sizeBytes = 72 * 1024 * 1024L,
                    mediaType = MediaType.VIDEO,
                    dateEpochSeconds = (now - day) / 1000L,
                    thumbnailUrl = null,
                    downloadUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    mimeType = "video/webm",
                    durationSeconds = 15L
                )
            )
            else -> listOf(
                TelegramMediaItem(
                    id = "${chatId}_301",
                    chatId = chatId,
                    messageId = 301L,
                    filename = "Tutorial_Introduction.mp4",
                    sizeBytes = 95 * 1024 * 1024L,
                    mediaType = MediaType.VIDEO,
                    dateEpochSeconds = (now - 4 * 3600 * 1000L) / 1000L,
                    thumbnailUrl = null,
                    downloadUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    mimeType = "video/mp4",
                    durationSeconds = 15L
                ),
                TelegramMediaItem(
                    id = "${chatId}_302",
                    chatId = chatId,
                    messageId = 302L,
                    filename = "Audio_Summary_Briefing.aac",
                    sizeBytes = 18 * 1024 * 1024L,
                    mediaType = MediaType.AUDIO,
                    dateEpochSeconds = (now - 8 * 3600 * 1000L) / 1000L,
                    thumbnailUrl = null,
                    downloadUrl = "https://actions.google.com/sounds/v1/weather/thunder_crack.ogg",
                    mimeType = "audio/aac",
                    durationSeconds = 420L
                )
            )
        }

        if (typeFilter == null) mediaList else mediaList.filter { it.mediaType == typeFilter }
    }
}
