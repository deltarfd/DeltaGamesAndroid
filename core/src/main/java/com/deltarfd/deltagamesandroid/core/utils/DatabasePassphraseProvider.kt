package com.deltarfd.deltagamesandroid.core.utils

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Provides a secure database passphrase backed by Android Keystore.
 *
 * On first launch, generates a cryptographically random 32-byte passphrase, encrypts it
 * with an AES-256 key from the hardware-backed Keystore, and stores the ciphertext in
 * SharedPreferences. On subsequent launches, retrieves and decrypts the same passphrase.
 *
 * Security properties:
 * - The AES key never leaves the Keystore (hardware-backed via TEE/StrongBox)
 * - User authentication (device credential) required for key access
 * - The passphrase is unique per device/install — generated via [SecureRandom]
 * - If the key is invalidated (lock screen change, etc.), gracefully regenerates
 */
object DatabasePassphraseProvider {

    private const val TAG = "DBPassphraseProvider"
    private const val PREFS_FILE_NAME = "delta_games_secure_prefs"
    private const val KEY_DB_PASSPHRASE = "db_passphrase"
    private const val KEYSTORE_ALIAS = "delta_games_db_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val GCM_IV_LENGTH_BYTES = 12
    private const val PASSPHRASE_LENGTH_BYTES = 32
    private const val AUTH_VALIDITY_SECONDS = 30

    fun getPassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        val encryptedValue = prefs.getString(KEY_DB_PASSPHRASE, null)

        if (encryptedValue != null) {
            try {
                return decryptPassphrase(encryptedValue)
            } catch (e: Exception) {
                // Keystore key was invalidated (lock screen change, backup/restore, etc.)
                // Clear stale data and fall through to generate a new passphrase.
                Log.w(TAG, "Failed to decrypt stored passphrase, regenerating.", e)
                prefs.edit { remove(KEY_DB_PASSPHRASE) }
                deleteKeystoreKey()
            }
        }

        val passphrase = ByteArray(PASSPHRASE_LENGTH_BYTES).also {
            SecureRandom().nextBytes(it)
        }
        val encrypted = encryptPassphrase(passphrase)
        prefs.edit { putString(KEY_DB_PASSPHRASE, encrypted) }
        return passphrase
    }

    private fun encryptPassphrase(passphrase: ByteArray): String {
        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(passphrase)
        val combined = iv + ciphertext
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decryptPassphrase(encryptedValue: String): ByteArray {
        val combined = Base64.decode(encryptedValue, Base64.NO_WRAP)
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH_BYTES)
        val ciphertext = combined.copyOfRange(GCM_IV_LENGTH_BYTES, combined.size)
        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(ciphertext)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = keyStore.getKey(KEYSTORE_ALIAS, null) as? SecretKey
        if (existing != null) return existing

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        keyGenerator.init(buildKeySpec())
        return keyGenerator.generateKey()
    }

    private fun buildKeySpec(): KeyGenParameterSpec {
        val builder = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setUserAuthenticationParameters(
                AUTH_VALIDITY_SECONDS,
                KeyProperties.AUTH_DEVICE_CREDENTIAL
            )
        } else {
            @Suppress("DEPRECATION")
            builder.setUserAuthenticationValidityDurationSeconds(AUTH_VALIDITY_SECONDS)
        }

        return builder.build()
    }

    private fun deleteKeystoreKey() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            keyStore.deleteEntry(KEYSTORE_ALIAS)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete old keystore entry.", e)
        }
    }
}
