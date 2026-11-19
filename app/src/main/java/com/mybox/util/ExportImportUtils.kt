package com.mybox.util

import android.content.Context
import com.google.gson.Gson
import com.mybox.data.model.*
import com.mybox.data.database.AppDatabase
import com.mybox.data.dao.PasswordDao
import com.mybox.data.dao.EventDao
import com.mybox.data.dao.MasterPasswordDao
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object ExportImportUtils {

    private const val APP_NAME = "MyBox"
    private const val EXPORT_VERSION = "1.0"
    private const val SALT_LENGTH = 32
    private const val IV_LENGTH = 12
    private const val TAG_LENGTH = 128
    private const val ITERATION_COUNT = 100000
    private const val KEY_LENGTH = 256

    fun exportData(
        passwords: List<Password>,
        events: List<Event>
    ): String {
        val exportPasswords = passwords.map { password ->
            ExportPassword(
                id = password.id,
                title = password.title,
                account = password.account,
                password = EncryptUtils.decrypt(password.password),
                remark = password.remark,
                createdAt = password.createdAt,
                updatedAt = password.updatedAt
            )
        }

        val exportEvents = events.map { event ->
            ExportEvent(
                id = event.id,
                date = event.date,
                description = event.description,
                color = event.color,
                createdAt = event.createdAt,
                updatedAt = event.updatedAt
            )
        }

        val exportData = ExportData(
            appName = APP_NAME,
            version = EXPORT_VERSION,
            exportDate = System.currentTimeMillis(),
            passwords = exportPasswords,
            events = exportEvents
        )

        return Gson().toJson(exportData)
    }

    suspend fun importData(
        database: AppDatabase,
        jsonData: String
    ): ImportResult {
        // 使用事务包裹，保证导入原子性：全部成功或全部回滚
        // withTransaction 是 RoomDatabase 的扩展函数，必须通过 database 实例调用
        return database.withTransaction {
            importData(
                database.passwordDao(),
                database.eventDao(),
                jsonData
            )
        }
    }

    suspend fun importData(
        passwordDao: PasswordDao,
        eventDao: EventDao,
        jsonData: String
    ): ImportResult {
        return withContext(Dispatchers.IO) {
            try {
                val exportData = Gson().fromJson(jsonData, ExportData::class.java)

                if (exportData.appName != APP_NAME) {
                    return@withContext ImportResult.Error("不支持的应用数据")
                }

                if (exportData.version != EXPORT_VERSION) {
                    return@withContext ImportResult.Error("不兼容的导出版本")
                }

                var passwordCount = 0
                for (exportPassword in exportData.passwords) {
                    val existingPassword = passwordDao.getPasswordById(exportPassword.id)
                    val encryptedPassword = EncryptUtils.encrypt(exportPassword.password)

                    val password = Password(
                        id = exportPassword.id,
                        title = exportPassword.title,
                        account = exportPassword.account,
                        password = encryptedPassword,
                        remark = exportPassword.remark,
                        createdAt = exportPassword.createdAt,
                        updatedAt = exportPassword.updatedAt
                    )

                    if (existingPassword != null) {
                        passwordDao.updatePassword(password)
                    } else {
                        passwordDao.insertPassword(password)
                    }
                    passwordCount++
                }

                var eventCount = 0
                for (exportEvent in exportData.events) {
                    val existingEvent = eventDao.getEventById(exportEvent.id)

                    val event = Event(
                        id = exportEvent.id,
                        date = exportEvent.date,
                        description = exportEvent.description,
                        color = exportEvent.color ?: "#FFFFFF",
                        createdAt = exportEvent.createdAt,
                        updatedAt = exportEvent.updatedAt
                    )

                    if (existingEvent != null) {
                        eventDao.updateEvent(event)
                    } else {
                        eventDao.insertEvent(event)
                    }
                    eventCount++
                }

                ImportResult.Success(passwordCount, eventCount)
            } catch (e: com.google.gson.JsonSyntaxException) {
                ImportResult.Error("文件格式错误")
            } catch (e: Exception) {
                ImportResult.Error("导入失败: ${e.message}")
            }
        }
    }

    fun encryptExportData(jsonData: String, password: String): String {
        val salt = ByteArray(SALT_LENGTH).apply {
            SecureRandom().nextBytes(this)
        }

        val key = deriveKey(password, salt)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(IV_LENGTH).apply {
            SecureRandom().nextBytes(this)
        }
        val spec = GCMParameterSpec(TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)

        val encrypted = cipher.doFinal(jsonData.toByteArray(Charsets.UTF_8))

        val saltBase64 = android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP)
        val ivBase64 = android.util.Base64.encodeToString(iv, android.util.Base64.NO_WRAP)
        val encryptedBase64 = android.util.Base64.encodeToString(encrypted, android.util.Base64.NO_WRAP)

        return "$saltBase64:$ivBase64:$encryptedBase64"
    }

    fun decryptExportData(encryptedData: String, password: String): String {
        try {
            val parts = encryptedData.split(":")
            if (parts.size != 3) {
                throw IllegalArgumentException("无效的文件格式")
            }

            val salt = android.util.Base64.decode(parts[0], android.util.Base64.NO_WRAP)
            val iv = android.util.Base64.decode(parts[1], android.util.Base64.NO_WRAP)
            val encrypted = android.util.Base64.decode(parts[2], android.util.Base64.NO_WRAP)

            val key = deriveKey(password, salt)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)

            return String(cipher.doFinal(encrypted), Charsets.UTF_8)
        } catch (e: javax.crypto.BadPaddingException) {
            throw IllegalArgumentException("解密密码错误")
        } catch (e: javax.crypto.IllegalBlockSizeException) {
            throw IllegalArgumentException("解密密码错误")
        } catch (e: Exception) {
            throw IllegalArgumentException("解密失败: ${e.message}")
        }
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    sealed class ImportResult {
        data class Success(val passwordCount: Int, val eventCount: Int) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
}