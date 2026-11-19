package com.mybox.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * 生物识别工具类
 * 封装 BiometricPrompt 的可用性检测与验证发起。
 *
 * 安全策略：使用 BIOMETRIC_STRONG（仅 Class 3 指纹/3D 面部），
 * 密码管理类 App 安全性优先于兼容性。
 */
object BiometricHelper {

    /**
     * 设备是否支持生物识别（强认证）且已录入指纹/面部
     *
     * @return true 表示可发起 BiometricPrompt 验证
     */
    fun isBiometricAvailable(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * 发起生物识别验证
     *
     * @param activity 宿主 FragmentActivity（BiometricPrompt 硬性要求）
     * @param title 对话框标题
     * @param subtitle 副标题
     * @param description 描述
     * @param onSuccess 验证成功回调
     * @param onError 验证错误回调（code, msg）
     * @param onCancel 用户取消回调
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String = "",
        description: String = "",
        onSuccess: () -> Unit,
        onError: (Int, String) -> Unit,
        onCancel: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // 区分"用户主动取消"与"真实错误"
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_CANCELED
                ) {
                    onCancel()
                } else {
                    onError(errorCode, errString.toString())
                }
            }

            // 验证失败（指纹不匹配等）：不回调，由系统自动重试，超过次数后系统会调 onError
            override fun onAuthenticationFailed() {
                // 系统会自动给用户重试机会，这里不干预
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText("取消") // BIOMETRIC_STRONG 模式下不能用 setAllowedAuthenticators + setNegativeButtonText 同时配置
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setConfirmationRequired(false)
            .build()

        prompt.authenticate(info)
    }
}
