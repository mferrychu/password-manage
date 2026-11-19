package com.mybox.ui.screens

import android.widget.Toast
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.fragment.app.FragmentActivity
import com.mybox.util.BiometricHelper

@Composable
fun UnlockScreen(
    onUnlockAttempt: (String) -> Boolean,
    biometricEnabled: Boolean = false,
    onBiometricUnlock: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val shakeAnimation = remember { Animatable(0f) }
    var isShaking by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    // 设备是否支持生物识别（强认证）且已录入
    val biometricAvailable = remember {
        BiometricHelper.isBiometricAvailable(context)
    }
    // 是否实际启用指纹入口：偏好开启 且 设备支持
    val showBiometric = biometricEnabled && biometricAvailable

    // 发起生物识别验证
    val triggerBiometric = {
        val activity = context as? FragmentActivity
        if (activity != null) {
            BiometricHelper.authenticate(
                activity = activity,
                title = "解锁 MyBox",
                subtitle = "请验证指纹或面部",
                description = "使用生物识别快捷解锁应用",
                onSuccess = { onBiometricUnlock() },
                onError = { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                },
                onCancel = { /* 用户取消，保留密码输入流程 */ }
            )
        }
    }

    // 进入页面自动触发一次指纹验证
    LaunchedEffect(showBiometric) {
        if (showBiometric) {
            triggerBiometric()
        }
    }

    val attemptUnlock = {
        if (password.isNotEmpty()) {
            keyboardController?.hide()
            if (onUnlockAttempt(password)) {
                password = ""
            } else {
                errorMessage = "密码错误，请重试"
                password = ""
                isShaking = true
            }
        }
    }

    LaunchedEffect(isShaking) {
        if (isShaking) {
            shakeAnimation.animateTo(
                targetValue = 1f,
                animationSpec = repeatable(
                    iterations = 3,
                    animation = tween(100, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            shakeAnimation.animateTo(0f)
            isShaking = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = (shakeAnimation.value * 10).dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .offset(x = 3.dp, y = 3.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f)
            )
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "锁",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "MyBox",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "请输入主密码解锁",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("请输入主密码") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { attemptUnlock() }
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                    )
                }
            },
            isError = errorMessage.isNotEmpty(),
            supportingText = if (errorMessage.isNotEmpty()) {
                { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
            } else null,
            colors = com.mybox.ui.components.whiteTextFieldColors(isError = errorMessage.isNotEmpty()),
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = attemptUnlock,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = password.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.outlineVariant
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "解锁",
                style = MaterialTheme.typography.labelLarge
            )
        }

        // 指纹快捷入口：仅当启用且设备支持时显示
        if (showBiometric) {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { triggerBiometric() }) {
                Icon(
                    imageVector = Icons.Filled.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "使用指纹/面部解锁",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
