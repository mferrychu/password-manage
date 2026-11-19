package com.mybox.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.mybox.ui.theme.ThemeType

/**
 * 应用偏好设置管理
 */
class AppPreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "mybox_prefs"
        private const val KEY_AUTO_LOCK_ENABLED = "auto_lock_enabled"
        private const val KEY_THEME_TYPE = "theme_type"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_BIOMETRIC_PROMPT_SHOWN = "biometric_prompt_shown"
    }
    
    /**
     * 获取自动锁定是否启用
     */
    fun isAutoLockEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_LOCK_ENABLED, false) // 默认关闭
    }
    
    /**
     * 设置自动锁定是否启用
     */
    fun setAutoLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_LOCK_ENABLED, enabled).apply()
    }

    /**
     * 获取生物识别解锁是否启用
     */
    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false) // 默认关闭
    }

    /**
     * 设置生物识别解锁是否启用
     */
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    /**
     * 是否已经弹过"启用生物识别"引导（避免重复打扰）
     */
    fun isBiometricPromptShown(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_PROMPT_SHOWN, false)
    }

    /**
     * 标记已弹过引导
     */
    fun setBiometricPromptShown(shown: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_PROMPT_SHOWN, shown).apply()
    }

    /**
     * 获取当前主题类型
     */
    fun getThemeType(): ThemeType {
        val themeName = prefs.getString(KEY_THEME_TYPE, ThemeType.PURPLE.name)
        return try {
            ThemeType.valueOf(themeName ?: ThemeType.PURPLE.name)
        } catch (e: Exception) {
            ThemeType.PURPLE
        }
    }

    /**
     * 设置主题类型
     */
    fun setThemeType(themeType: ThemeType) {
        prefs.edit().putString(KEY_THEME_TYPE, themeType.name).apply()
    }
}