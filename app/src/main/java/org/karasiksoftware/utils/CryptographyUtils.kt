package org.karasiksoftware.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec

class CryptographyUtils(
    private val context: Context
) {
    private var secretKeySpec: SecretKeySpec? = null

    init {
        val secureRandom: SecureRandom = SecureRandom.getInstance("SHA1PRNG")
        secureRandom.setSeed(getKey().toByteArray())

        val keyGenerator: KeyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(128, secureRandom)

        secretKeySpec = SecretKeySpec(keyGenerator.generateKey().encoded, "AES")
    }
    @SuppressLint("HardwareIds")
    private fun getKey(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun encrypt(text: String): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)

        return cipher.doFinal(text.toByteArray())
    }

    fun decrypt(bytes: ByteArray): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        return String(cipher.doFinal(bytes))
    }
}