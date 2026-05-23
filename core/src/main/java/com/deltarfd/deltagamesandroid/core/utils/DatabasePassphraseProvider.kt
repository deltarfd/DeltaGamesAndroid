package com.deltarfd.deltagamesandroid.core.utils

import android.content.Context
import android.security.keystore.KeyGenParameterSpec.Builder
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Generates and persists a database passphrase using Android Keystore (hardware-backed AES-256-GCM).
 */
object DatabasePassphraseProvider {

    private const val PREFS_NAME = "delta_games_secure_prefs"
    private const val PREF_KEY = "db_passphrase"
    private const val KEYSTORE_ALIAS = "delta_games_db_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_SIZE = 12
    private const val TAG_SIZE = 128
    private const val PASSPHRASE_SIZE = 32

    fun getPassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getString(PREF_KEY, null)

        if (stored != null) return decrypt(stored)

        val passphrase = ByteArray(PASSPHRASE_SIZE).also { SecureRandom().nextBytes(it) }
        prefs.edit { putString(PREF_KEY, encrypt(passphrase)) }
        return passphrase
    }

    private fun encrypt(data: ByteArray): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        return Base64.encodeToString(cipher.iv + cipher.doFinal(data), Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): ByteArray {
        val raw = Base64.decode(encoded, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_SIZE, raw, 0, IV_SIZE))
        return cipher.doFinal(raw, IV_SIZE, raw.size - IV_SIZE)
    }

    @Suppress("kotlin:S6288")
    private fun getOrCreateKey(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        ks.getKey(KEYSTORE_ALIAS, null)?.let { return it as SecretKey }

        val spec = Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            .apply { init(spec) }
            .generateKey()
    }
}
