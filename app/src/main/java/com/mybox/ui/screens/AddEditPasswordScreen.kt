package com.mybox.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mybox.data.model.Password
import com.mybox.util.EncryptUtils

/**
 * 添加/修改密码页
 * 添加密码页和修改密码页复用此页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPasswordScreen(
    title: String,
    existingPassword: Password? = null,
    isSubmitting: Boolean,
    onSave: (String, String, String, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordTitle by remember { mutableStateOf(existingPassword?.title ?: "") }
    var account by remember { mutableStateOf(existingPassword?.account ?: "") }
    var password by remember { mutableStateOf(existingPassword?.let { EncryptUtils.decrypt(it.password) } ?: "") }
    var remark by remember { mutableStateOf(existingPassword?.remark ?: "") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    
    val isSaveEnabled = passwordTitle.trim().isNotEmpty() && password.isNotBlank() && !isSubmitting
    
    Column(modifier = modifier.fillMaxSize()) {
        // 顶部栏
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = title,
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
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题输入框（必填）
            OutlinedTextField(
                value = passwordTitle,
                onValueChange = { passwordTitle = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("* 请输入标题")
                },
                placeholder = {
                    Text("请输入标题")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = com.mybox.ui.components.whiteTextFieldColors(),
                shape = MaterialTheme.shapes.medium
            )
            
            // 账号输入框（选填）
            OutlinedTextField(
                value = account,
                onValueChange = { account = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("账号")
                },
                placeholder = {
                    Text("请输入账号")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = com.mybox.ui.components.whiteTextFieldColors(),
                shape = MaterialTheme.shapes.medium
            )
            
            // 密码输入框（必填）
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("* 请输入密码")
                },
                placeholder = {
                    Text("请输入密码")
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                },
                colors = com.mybox.ui.components.whiteTextFieldColors(),
                shape = MaterialTheme.shapes.medium
            )
            
            // 备注输入框（选填，多行）
            OutlinedTextField(
                value = remark,
                onValueChange = { remark = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                label = {
                    Text("备注")
                },
                placeholder = {
                    Text("请输入备注")
                },
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Default
                ),
                colors = com.mybox.ui.components.whiteTextFieldColors(),
                shape = MaterialTheme.shapes.medium
            )
        }
        
        // 底部按钮区域
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 保存按钮（初始禁用，验证通过后激活）
            Button(
                onClick = {
                    onSave(passwordTitle.trim(), account.trim(), password, remark.trim())
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