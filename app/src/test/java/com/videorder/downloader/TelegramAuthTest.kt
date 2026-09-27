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
    fun testSendVerificationCodeGenerates5DigitOtp() = runBlocking {
        val result = repository.sendVerificationCode("+12025550143", 123456, "abcdef0123456789")
        assertTrue("OTP dispatch should succeed", result.isSuccess)
        val otp = result.getOrNull()
        assertNotNull(otp)
        assertEquals("OTP must be exactly 5 digits", 5, otp?.length)
        assertTrue("OTP must be numeric", otp?.all { it.isDigit() } == true)
    }

    @Test
    fun testInvalidPhoneNumberFailsValidation() = runBlocking {
        val result = repository.sendVerificationCode("123", 123456, "abcdef0123456789")
        assertTrue("Short phone number must fail", result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun testRandomOtpIsStrictlyRejected() = runBlocking {
        // Send real verification code
        val dispatchResult = repository.sendVerificationCode("+12025550143", 123456, "abcdef0123456789")
        assertTrue(dispatchResult.isSuccess)
        val realOtp = dispatchResult.getOrThrow()

        // Fabricate a random wrong OTP
        val randomWrongOtp = if (realOtp == "99999") "11111" else "99999"

        // Verify with the wrong OTP
        val verifyResult = repository.verifyCode("phone_hash_placeholder", randomWrongOtp)
        assertTrue("Entering a random OTP must fail verification", verifyResult.isFailure)
        val exception = verifyResult.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(
            "Exception message must mention invalid verification code",
            exception?.message?.contains("Invalid verification code") == true
        )
    }

    @Test
    fun testMatchingOtpSucceedsAndEstablishesSession() = runBlocking {
        val dispatchResult = repository.sendVerificationCode("+12025550143", 123456, "abcdef0123456789")
        val realOtp = dispatchResult.getOrThrow()

        // Verify with exact matching OTP
        val verifyResult = repository.verifyCode("phone_hash_placeholder", realOtp)
        assertTrue("Matching OTP must succeed", verifyResult.isSuccess)
        assertEquals(true, verifyResult.getOrNull())

        // Verify session was persisted
        val session = fakeDao.currentSession
        assertNotNull("Session must be stored in database", session)
        assertTrue("Session must be logged in", session?.isLoggedIn == true)
        assertTrue("Session store must have saved token", fakeStorage.savedToken != null)
    }

    @Test
    fun testCannotReuseOtpAfterSuccessfulLogin() = runBlocking {
        val dispatchResult = repository.sendVerificationCode("+12025550143", 123456, "abcdef0123456789")
        val realOtp = dispatchResult.getOrThrow()

        val firstVerify = repository.verifyCode("phone_hash_placeholder", realOtp)
        assertTrue(firstVerify.isSuccess)

        // Attempting to reuse the OTP must fail because activeDispatchedCode is cleared
        val secondVerify = repository.verifyCode("phone_hash_placeholder", realOtp)
        assertTrue("Reusing old OTP must fail", secondVerify.isFailure)
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
