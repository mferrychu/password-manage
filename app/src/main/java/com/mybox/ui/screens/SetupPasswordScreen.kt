package com.mybox.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mybox.util.EncryptUtils

@Composable
fun SetupPasswordScreen(
    onPasswordSet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var lengthError by remember { mutableStateOf("") }
    
    val focusManager = LocalFocusManager.current
    
    val isButtonEnabled = password.isNotBlank() &&
                          confirmPassword.isNotBlank() &&
                          password == confirmPassword &&
                          errorMessage.isEmpty() &&
                          lengthError.isEmpty()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(80.dp)) {
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
            text = "首次使用，需要设置主密码对数据进行加密和保护。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                lengthError = if (it.isNotEmpty() && it.length < 4) "主密码至少需要 4 位" else ""
                if (confirmPassword.isNotEmpty() && it != confirmPassword) {
                    errorMessage = "两次输入的密码不一致"
                } else {
                    errorMessage = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            label = { Text("请输入主密码") },
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
                        contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
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
        
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it
                if (password.isNotEmpty() && it != password) {
                    errorMessage = "两次输入的密码不一致"
                } else {
                    errorMessage = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            label = { Text("请再次输入主密码") },
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (isButtonEnabled) {
                        onPasswordSet(password)
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
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { onPasswordSet(password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = isButtonEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.outlineVariant
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "开始使用",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
