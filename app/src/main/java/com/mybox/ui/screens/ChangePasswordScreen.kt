package com.mybox.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * 修改主密码页
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    currentPasswordHash: String,
    onBack: () -> Unit,
    onSave: (String) -> Unit, // 新密码
    modifier: Modifier = Modifier
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var lengthError by remember { mutableStateOf("") }
    
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    
    // 按钮是否可用：原密码正确且两次新密码一致
    val isSaveEnabled = oldPassword.isNotBlank() &&
                        newPassword.isNotBlank() &&
                        confirmPassword.isNotBlank() &&
                        newPassword == confirmPassword &&
                        errorMessage.isEmpty() &&
                        lengthError.isEmpty()
    
    Column(modifier = modifier.fillMaxSize()) {
        // 顶部栏
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "修改主密码",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "返回"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        
        // 表单内容
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 原密码输入框
            OutlinedTextField(
                value = oldPassword,
                onValueChange = { 
                    oldPassword = it
                    errorMessage = "" // 清空错误提示
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("请输入原密码") },
                singleLine = true,
                visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                trailingIcon = {
                    IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                        Icon(
                            imageVector = if (oldPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (oldPasswordVisible) "隐藏密码" else "显示密码"
                        )
                    }
                },
                isError = errorMessage.contains("原密码"),
                colors = com.mybox.ui.components.whiteTextFieldColors(isError = errorMessage.contains("原密码")),
                shape = MaterialTheme.shapes.medium
            )
            
            // 新密码输入框
            OutlinedTextField(
                value = newPassword,
                onValueChange = { 
                    newPassword = it
                    lengthError = if (it.isNotEmpty() && it.length < 4) "新密码至少需要 4 位" else ""
                    // 实时验证两次密码是否一致
                    if (confirmPassword.isNotEmpty() && it != confirmPassword) {
                        errorMessage = "两次密码不一致"
                    } else {
                        errorMessage = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("请输入新密码") },
                singleLine = true,
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                trailingIcon = {
                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                        Icon(
                            imageVector = if (newPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (newPasswordVisible) "隐藏密码" else "显示密码"
                        )
                    }
                },
                isError = lengthError.isNotEmpty(),
                supportingText = if (lengthError.isNotEmpty()) {
                    { Text(lengthError, color = MaterialTheme.colorScheme.error) }
                } else null,
                colors = com.mybox.ui.components.whiteTextFieldColors(isError = lengthError.isNotEmpty()),
                shape = MaterialTheme.shapes.medium
            )
            
            // 确认新密码输入框
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    // 实时验证两次密码是否一致
                    if (newPassword.isNotEmpty() && it != newPassword) {
                        errorMessage = "两次密码不一致"
                    } else {
                        errorMessage = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("请再次输入新密码") },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (isSaveEnabled) {
                            verifyAndSave(
                                oldPassword = oldPassword,
                                newPassword = newPassword,
                                currentPasswordHash = currentPasswordHash,
                                onSave = onSave,
                                onError = { msg -> errorMessage = msg }
                            )
                        }
                    }
                ),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (confirmPasswordVisible) "隐藏密码" else "显示密码"
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
        }
        
        // 底部按钮区域
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 保存按钮（初始禁用，验证通过后激活）
            Button(
                onClick = {
                    verifyAndSave(
                        oldPassword = oldPassword,
                        newPassword = newPassword,
                        currentPasswordHash = currentPasswordHash,
                        onSave = onSave,
                        onError = { msg -> errorMessage = msg }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = isSaveEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.outlineVariant
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Filled.Save,
                    contentDescription = "保存",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("保存")
            }
            
            // 取消按钮
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "取消",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("取消")
            }
        }
    }
}

/**
 * 验证原密码并保存新密码
 */
private fun verifyAndSave(
    oldPassword: String,
    newPassword: String,
    currentPasswordHash: String,
    onSave: (String) -> Unit,
    onError: (String) -> Unit
) {
    // 验证原密码
    if (!com.mybox.util.EncryptUtils.verify(oldPassword, currentPasswordHash)) {
        onError("原密码错误")
        return
    }
    
    // 保存新密码
    onSave(newPassword)
}