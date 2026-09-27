package com.videorder.downloader

import com.videorder.downloader.data.database.dao.TelegramSessionDao
import com.videorder.downloader.data.database.entities.TelegramSessionMetadataEntity
import com.videorder.downloader.data.repositories.TelegramRepositoryImpl
import com.videorder.downloader.security.TelegramSessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TelegramAuthTest {

    private lateinit var fakeDao: FakeTelegramSessionDao
    private lateinit var fakeStorage: FakeTelegramSessionStorage
    private lateinit var repository: TelegramRepositoryImpl

    @Before
    fun setup() {
        fakeDao = FakeTelegramSessionDao()
        fakeStorage = FakeTelegramSessionStorage()
        repository = TelegramRepositoryImpl(fakeDao, fakeStorage)
    }

    @Test
    fun testSaveWebSessionPersistsSession() = runBlocking {
        val result = repository.saveWebSession(
            accountLabel = "Official Telegram Web (+1 ••• ••• 4410)",
            userId = 987654321L,
            username = "official_telegram_user"
        )
        assertTrue("Web session save should return true", result)

        val session = fakeDao.currentSession
        assertNotNull("Session must be saved in database", session)
        assertTrue("Session must be logged in", session?.isLoggedIn == true)
        assertEquals(987654321L, session?.userId)
        assertEquals("official_telegram_user", session?.username)
        assertTrue("Session store must have encrypted token", fakeStorage.savedToken != null)
    }

    @Test
    fun testSendCodeRejectsInvalidPhone() = runBlocking {
        val result = repository.sendVerificationCode("123", 123456, "abcdef0123456789")
        assertTrue("Short phone number must fail validation", result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun testSendCodeDirectsPersonalAccountsToOfficialTelegramWeb() = runBlocking {
        val result = repository.sendVerificationCode("+12025550143", 123456, "default_hash")
        assertTrue("Plain sendCode should direct user to Official Telegram Web", result.isFailure)
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(
            "Exception should inform user to use Official Telegram Web",
            ex?.message?.contains("Official Telegram Web") == true
        )
    }

    @Test
    fun testBotTokenValidationRejectsInvalidTokens() = runBlocking {
        val invalidTokenResult = repository.loginWithBotToken("short_token")
        assertTrue("Malformed bot token should fail validation", invalidTokenResult.isFailure)
    }

    @Test
    fun testDemoSessionLoginSucceeds() = runBlocking {
        val demoResult = repository.loginWithDemoSession()
        assertTrue("Demo login should succeed", demoResult.isSuccess)
        assertTrue(fakeDao.currentSession?.isLoggedIn == true)
    }

    @Test
    fun testLogoutClearsSessionAndStorage() = runBlocking {
        repository.loginWithDemoSession()
        assertTrue(fakeDao.currentSession?.isLoggedIn == true)

        val logoutResult = repository.logout()
        assertTrue(logoutResult)
        assertEquals(false, fakeDao.currentSession?.isLoggedIn)
        assertNull(fakeStorage.savedToken)
    }

    // Fakes for testing
    private class FakeTelegramSessionDao : TelegramSessionDao {
        var currentSession: TelegramSessionMetadataEntity? = null
        private val state = MutableStateFlow<TelegramSessionMetadataEntity?>(null)

        override fun getSession(): Flow<TelegramSessionMetadataEntity?> = state.asStateFlow()

        override suspend fun getSessionSync(): TelegramSessionMetadataEntity? = currentSession

        override suspend fun saveSession(session: TelegramSessionMetadataEntity) {
            currentSession = session
            state.value = session
        }

        override suspend fun clearSession() {
            currentSession = currentSession?.copy(isLoggedIn = false)
            state.value = currentSession
        }
    }

    private class FakeTelegramSessionStorage : TelegramSessionStorage {
        var savedToken: String? = null
        private var storedApiId: Int = 0
        private var storedApiHash: String? = null

        override fun saveSessionToken(sessionToken: String) {
            savedToken = sessionToken
        }

        override fun getSessionToken(): String? = savedToken

        override fun saveApiCredentials(apiId: Int, apiHash: String) {
            this.storedApiId = apiId
            this.storedApiHash = apiHash
        }

        override fun getApiId(): Int = storedApiId

        override fun getApiHash(): String? = storedApiHash

        override fun hasValidSession(): Boolean = !savedToken.isNullOrBlank()

        override fun wipeSession() {
            savedToken = null
            storedApiHash = null
        }
    }
}
