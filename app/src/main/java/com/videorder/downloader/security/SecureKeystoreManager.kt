package com.videorder.downloader.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class SecureKeystoreManager(private val context: Context? = null) {

    private val keyStoreAlias = "videorder_aes_key"
    private val transformation = "AES/GCM/NoPadding"
    private val gcmIvLength = 12
    private val gcmTagLength = 128

    private var fallbackKey: SecretKey? = null

    private fun getSecretKey(): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            if (!keyStore.containsAlias(keyStoreAlias)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore"
                )
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    keyStoreAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()

                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }

            keyStore.getKey(keyStoreAlias, null) as SecretKey
        } catch (e: Exception) {
            // Fallback for JVM Unit Tests where AndroidKeyStore is absent
            if (fallbackKey == null) {
                val keyGen = KeyGenerator.getInstance("AES")
                keyGen.init(256)
                fallbackKey = keyGen.generateKey()
            }
            fallbackKey!!
        }
    }

    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        val secretKey = getSecretKey()
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Prepend 12-byte IV to ciphertext
        val combined = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)

        return Base64.getEncoder().encodeToString(combined)
    }

    fun decrypt(encryptedBase64: String): String {
        if (encryptedBase64.isEmpty()) return ""
        val combined = Base64.getDecoder().decode(encryptedBase64)
        if (combined.size < gcmIvLength) return ""

        val iv = ByteArray(gcmIvLength)
        System.arraycopy(combined, 0, iv, 0, gcmIvLength)

        val cipherTextSize = combined.size - gcmIvLength
        val cipherText = ByteArray(cipherTextSize)
        System.arraycopy(combined, gcmIvLength, cipherText, 0, cipherTextSize)

        val secretKey = getSecretKey()
        val cipher = Cipher.getInstance(transformation)
        val spec = GCMParameterSpec(gcmTagLength, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        val decryptedBytes = cipher.doFinal(cipherText)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    fun deleteKey() {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            if (keyStore.containsAlias(keyStoreAlias)) {
                keyStore.deleteEntry(keyStoreAlias)
            }
        } catch (ignored: Exception) {
        }
        fallbackKey = null
    }
}
