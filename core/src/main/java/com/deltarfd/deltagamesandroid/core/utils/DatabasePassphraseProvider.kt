package com.deltarfd.deltagamesandroid.core.utils

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import java.security.InvalidAlgorithmParameterException
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
 * - The AES key never leaves the Keystore (often hardware-backed via TEE/StrongBox)
 * - User authentication (device credential) required for key access on secure devices
 * - The passphrase is unique per device/install — generated via [SecureRandom]
 * - An attacker decompiling the APK only sees code that *retrieves* the passphrase,
 *   not the passphrase itself, which doesn't exist until first launch on the user's device
 */
object DatabasePassphraseProvider {

    private const val PREFS_FILE_NAME = "delta_games_secure_prefs"
    private const val KEY_DB_PASSPHRASE = "db_passphrase"
    private const val KEYSTORE_ALIAS = "delta_games_db_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val GCM_IV_LENGTH_BYTES = 12
    private const val PASSPHRASE_LENGTH_BYTES = 32

    /**
     * Authentication validity duration in seconds. After the user authenticates with their
     * device credential (lock screen unlock counts), the key is usable for this duration
     * without re-authenticating. Set to a long window so app lifecycle events don't
     * trigger biometric prompts during normal use.
     */
    private const val AUTH_VALIDITY_SECONDS = 60 * 60 * 24 * 30 // 30 days

    fun getPassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        val encryptedValue = prefs.getString(KEY_DB_PASSPHRASE, null)

        return if (encryptedValue != null) {
            decryptPassphrase(context, encryptedValue)
        } else {
            val passphrase = ByteArray(PASSPHRASE_LENGTH_BYTES).also {
                SecureRandom().nextBytes(it)
            }
            val encrypted = encryptPassphrase(context, passphrase)
            prefs.edit { putString(KEY_DB_PASSPHRASE, encrypted) }
            passphrase
        }
    }

    private fun encryptPassphrase(context: Context, passphrase: ByteArray): String {
        val secretKey = getOrCreateSecretKey(context)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(passphrase)
        val combined = iv + ciphertext
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decryptPassphrase(context: Context, encryptedValue: String): ByteArray {
        val combined = Base64.decode(encryptedValue, Base64.NO_WRAP)
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH_BYTES)
        val ciphertext = combined.copyOfRange(GCM_IV_LENGTH_BYTES, combined.size)
        val secretKey = getOrCreateSecretKey(context)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(ciphertext)
    }

    private fun getOrCreateSecretKey(context: Context): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = keyStore.getKey(KEYSTORE_ALIAS, null) as? SecretKey
        if (existing != null) return existing

        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)

        return if (keyguardManager.isDeviceSecure) {
            // Device has a lock screen — generate a key that requires user authentication
            keyGenerator.init(buildAuthenticatedKeySpec())
            keyGenerator.generateKey()
        } else {
            // Device has no lock screen — generate a key without auth requirement.
            // The key is still hardware-backed and the data is still encrypted.
            keyGenerator.init(buildUnauthenticatedKeySpec())
            keyGenerator.generateKey()
        }
    }

    /**
     * Builds a key spec that requires user authentication. Used when the device has a
     * lock screen configured. The user only needs to unlock their device once every
     * [AUTH_VALIDITY_SECONDS] for the key to remain usable.
     */
    private fun buildAuthenticatedKeySpec(): KeyGenParameterSpec {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            builder.setUnlockedDeviceRequired(true)
        }

        return builder.build()
    }

    /**
     * Fallback spec for devices without a lock screen. The key is still stored in the
     * hardware-backed Keystore, but doesn't require user authentication.
     */
    @Suppress("kotlin:S6288") // Acceptable here: device has no lock screen, no auth available
    private fun buildUnauthenticatedKeySpec(): KeyGenParameterSpec {
        return KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
    }
}
