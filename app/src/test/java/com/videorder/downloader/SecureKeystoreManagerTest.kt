package com.videorder.downloader

import com.videorder.downloader.security.SecureKeystoreManager
import org.junit.Assert.*
import org.junit.Test

class SecureKeystoreManagerTest {

    @Test
    fun testEncryptionAndDecryptionRoundtrip() {
        val manager = SecureKeystoreManager()
        val plainText = "my_telegram_session_secret_12345"

        val encrypted = manager.encrypt(plainText)
        assertNotNull(encrypted)
        assertNotEquals(plainText, encrypted)
        assertTrue(encrypted.isNotBlank())

        val decrypted = manager.decrypt(encrypted)
        assertEquals(plainText, decrypted)
    }

    @Test
    fun testEmptyStringHandling() {
        val manager = SecureKeystoreManager()
        assertEquals("", manager.encrypt(""))
        assertEquals("", manager.decrypt(""))
    }

    @Test
    fun testKeyDeletion() {
        val manager = SecureKeystoreManager()
        val plainText = "session_token_to_wipe"
        val encrypted = manager.encrypt(plainText)

        // Wipe / delete key
        manager.deleteKey()

        // After deleting the key, attempting to decrypt should fail to produce the original plaintext
        try {
            val decrypted = manager.decrypt(encrypted)
            assertNotEquals(plainText, decrypted)
        } catch (e: Exception) {
            // Decryption failure with new key is expected
            assertTrue(true)
        }
    }
}
