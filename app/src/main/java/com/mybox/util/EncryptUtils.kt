package com.mybox.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * 加密工具类
 * 使用Android Keystore + AES-256-GCM加密
 */
object EncryptUtils {
    
    private const val KEY_ALIAS = "mybox_master_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val TAG_LENGTH = 128

    // 主密码哈希参数（PBKDF2 + 随机盐，与导出功能加密算法保持一致）
    private const val PBKDF2_ITERATION_COUNT = 100000
    private const val PBKDF2_KEY_LENGTH = 256
    private const val SALT_LENGTH = 32
    
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }
    
    /**
     * 获取或创建密钥
     */
    private fun getOrCreateKey(): SecretKey {
        return if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
            entry.secretKey
        } else {
            createKey()
        }
    }
    
    /**
     * 创建新密钥
     */
    private fun createKey(): SecretKey {
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        
        return KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        ).apply { init(spec) }.generateKey()
    }
    
    /**
     * 加密数据
     */
    fun encrypt(data: String): String {
        if (data.isEmpty()) return data
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        
        // 将IV和加密数据组合后Base64编码
        val combined = cipher.iv + encrypted
        return android.util.Base64.encodeToString(combined, android.util.Base64.NO_WRAP)
    }
    
    /**
     * 解密数据
     */
    fun decrypt(encryptedData: String): String {
        if (encryptedData.isEmpty()) return encryptedData
        
        val combined = android.util.Base64.decode(encryptedData, android.util.Base64.NO_WRAP)
        
        // 从组合数据中提取IV和加密数据
        // GCM IV长度为12字节
        val iv = combined.copyOfRange(0, 12)
        val encrypted = combined.copyOfRange(12, combined.size)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)
        
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }
    
    /**
     * 生成加盐的密码哈希（PBKDF2 + 随机盐）
     * 返回格式：saltBase64:hashBase64
     * 同一密码每次调用结果都不同（随机盐），需用 verify() 校验
     */
    fun hash(input: String): String {
        val salt = ByteArray(SALT_LENGTH).apply {
            java.security.SecureRandom().nextBytes(this)
        }
        val hashBytes = deriveKeyWithPbkdf2(input, salt)
        val saltBase64 = android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP)
        val hashBase64 = android.util.Base64.encodeToString(hashBytes, android.util.Base64.NO_WRAP)
        return "$saltBase64:$hashBase64"
    }

    /**
     * 验证密码是否匹配存储的哈希
     * 兼容性：若 storedHash 不符合 "salt:hash" 格式直接返回 false
     */
    fun verify(input: String, storedHash: String): Boolean {
        if (storedHash.isEmpty()) return false
        val parts = storedHash.split(":")
        if (parts.size != 2) return false
        return try {
            val salt = android.util.Base64.decode(parts[0], android.util.Base64.NO_WRAP)
            val storedHashBytes = android.util.Base64.decode(parts[1], android.util.Base64.NO_WRAP)
            val computedHash = deriveKeyWithPbkdf2(input, salt)
            computedHash.contentEquals(storedHashBytes)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * PBKDF2WithHmacSHA256 派生密钥
     */
    private fun deriveKeyWithPbkdf2(password: String, salt: ByteArray): ByteArray {
        val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = javax.crypto.spec.PBEKeySpec(
            password.toCharArray(), salt, PBKDF2_ITERATION_COUNT, PBKDF2_KEY_LENGTH
        )
        return factory.generateSecret(spec).encoded
    }
}