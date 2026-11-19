package com.mybox.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.background
import androidx.fragment.app.FragmentActivity
import com.mybox.data.preferences.AppPreferences
import com.mybox.ui.theme.BlueTheme
import com.mybox.ui.theme.GreenTheme
import com.mybox.ui.theme.PinkTheme
import com.mybox.ui.theme.PurpleTheme
import com.mybox.ui.theme.YellowTheme
import com.mybox.ui.theme.ThemeType
import com.mybox.util.BiometricHelper
import com.mybox.viewmodel.SnackbarState
import com.mybox.viewmodel.SnackbarType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    appPreferences: AppPreferences,
    onBack: () -> Unit,
    onChangePassword: () -> Unit,
    onExport: (String) -> Unit,
    onImport: () -> Unit,
    onImportWithPassword: suspend (String) -> String,
    onImportCancelled: () -> Unit,
    importFileSelected: Boolean,
    onCloseApp: () -> Unit,
    onAbout: () -> Unit,
    onThemeChanged: (ThemeType) -> Unit = {},
    snackbarEvents: Flow<SnackbarState>,
    operationState: com.mybox.viewmodel.OperationState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var currentSnackbarType by remember { mutableStateOf(SnackbarType.SUCCESS) }
    var showCloseDialog by remember { mutableStateOf(false) }
    var autoLockEnabled by remember { mutableStateOf(appPreferences.isAutoLockEnabled()) }
    var currentThemeType by remember { mutableStateOf(appPreferences.getThemeType()) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var biometricEnabled by remember { mutableStateOf(appPreferences.isBiometricEnabled()) }
    val biometricAvailable = remember { BiometricHelper.isBiometricAvailable(context) }
    
    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordDialogType by remember { mutableStateOf<PasswordDialogType>(PasswordDialogType.EXPORT) }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    var isResultSuccess by remember { mutableStateOf(false) }

    val isLoading = operationState is com.mybox.viewmodel.OperationState.Exporting ||
            operationState is com.mybox.viewmodel.OperationState.Importing
    val loadingText = when (operationState) {
        is com.mybox.viewmodel.OperationState.Exporting -> "正在导出..."
        is com.mybox.viewmodel.OperationState.Importing -> "正在导入..."
        else -> ""
    }
    
    
    LaunchedEffect(importFileSelected) {
        if (importFileSelected) {
            passwordDialogType = PasswordDialogType.IMPORT
            passwordInput = ""
            passwordVisible = false
            showPasswordDialog = true
        }
    }

    LaunchedEffect(Unit) {
        snackbarEvents.collect { event ->
            currentSnackbarType = event.type
            snackbarHostState.showSnackbar(
                message = event.message,
                duration = SnackbarDuration.Short
            )
        }
    }
    
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    actionOnNewLine = false,
                    modifier = Modifier.padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 88.dp),
                    containerColor = if (currentSnackbarType == SnackbarType.SUCCESS) {
                        Color(0xFF34A853)
                    } else {
                        Color(0xFFEA4335)
                    },
                    contentColor = Color.White,
                    action = {
                        IconButton(onClick = { data.dismiss() }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "关闭",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (currentSnackbarType == SnackbarType.SUCCESS) {
                                Icons.Filled.Check
                            } else {
                                Icons.Filled.Close
                            },
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(data.visuals.message)
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "设置",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onBack,
                    enabled = !isLoading
                ) {
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
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 主题选择卡片
            Card(
                onClick = { showThemeDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "主题",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = getThemeDisplayName(currentThemeType),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // 主题颜色预览
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ThemeColorDot(PurpleTheme.Primary, currentThemeType == ThemeType.PURPLE)
                        ThemeColorDot(BlueTheme.Primary, currentThemeType == ThemeType.BLUE)
                        ThemeColorDot(GreenTheme.Primary, currentThemeType == ThemeType.GREEN)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "进入",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "自动锁定",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "应用切换到后台时自动锁定",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoLockEnabled,
                        onCheckedChange = {
                            autoLockEnabled = it
                            appPreferences.setAutoLockEnabled(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            // 生物识别解锁卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "生物识别解锁",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (biometricAvailable) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (biometricAvailable) "使用指纹/面部快捷解锁应用"
                                   else "当前设备不支持生物识别或未录入",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = biometricEnabled && biometricAvailable,
                        enabled = biometricAvailable,
                        onCheckedChange = { target ->
                            if (target) {
                                // 开启前先验证一次本人身份，防止他人开启
                                val activity = context as? FragmentActivity
                                if (activity != null) {
                                    BiometricHelper.authenticate(
                                        activity = activity,
                                        title = "启用生物识别解锁",
                                        subtitle = "请验证身份以开启",
                                        description = "开启后可使用指纹/面部快速解锁应用",
                                        onSuccess = {
                                            biometricEnabled = true
                                            appPreferences.setBiometricEnabled(true)
                                        },
                                        onError = { _, _ -> /* 不开启 */ },
                                        onCancel = { /* 用户取消，不开启 */ }
                                    )
                                }
                            } else {
                                biometricEnabled = false
                                appPreferences.setBiometricEnabled(false)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    )
                }
            }
            
            Card(
                onClick = onChangePassword,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "主密码",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "修改应用的主密码",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "进入",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Card(
                onClick = {
                    passwordDialogType = PasswordDialogType.EXPORT
                    passwordInput = ""
                    passwordVisible = false
                    showPasswordDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "导出",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "把所有内容导出到本地",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "进入",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Card(
                onClick = onImport,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "导入",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "把本地的文件导入进来",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "进入",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                onClick = onAbout,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "关于",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "关于MyBox的详细介绍",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "进入",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = { showCloseDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "关闭应用",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("关闭应用")
            }
        }
        
        }
    }
    
    // 主题选择对话框
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentThemeType,
            onThemeSelected = { themeType ->
                currentThemeType = themeType
                appPreferences.setThemeType(themeType)
                onThemeChanged(themeType)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
    
    if (showCloseDialog) {
        AlertDialog(
            onDismissRequest = { showCloseDialog = false },
            title = { Text("确认关闭") },
            text = { Text("确定要彻底关闭应用吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        showCloseDialog = false
                        onCloseApp()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("关闭")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCloseDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showPasswordDialog = false
                if (passwordDialogType == PasswordDialogType.IMPORT) {
                    onImportCancelled()
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(if (passwordDialogType == PasswordDialogType.EXPORT) "输入加密密码" else "输入解密密码") },
            text = {
                Column {
                    Text(
                        text = if (passwordDialogType == PasswordDialogType.EXPORT) "设置一个密码来加密导出文件" else "输入导出文件时设置的密码",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("密码") },
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = if (MaterialTheme.colorScheme.primary == Color(0xFF6750A4)) {
                            com.mybox.ui.components.whiteTextFieldColors()
                        } else {
                            TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (passwordInput.isNotBlank()) {
                            showPasswordDialog = false
                            if (passwordDialogType == PasswordDialogType.EXPORT) {
                                onExport(passwordInput)
                            } else {
                                scope.launch {
                                    val result = onImportWithPassword(passwordInput)
                                    android.widget.Toast.makeText(context, result, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    enabled = passwordInput.isNotBlank()
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showPasswordDialog = false
                        if (passwordDialogType == PasswordDialogType.IMPORT) {
                            onImportCancelled()
                        }
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
    
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = { Text(if (isResultSuccess) "操作成功" else "操作失败") },
            text = { Text(resultMessage) },
            confirmButton = {
                Button(onClick = { showResultDialog = false }) {
                    Text("确定")
                }
            }
        )
    }

    if (isLoading) {
        Dialog(
            onDismissRequest = { }
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = loadingText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

enum class PasswordDialogType {
    EXPORT, IMPORT
}

/**
 * 获取主题显示名称
 */
private fun getThemeDisplayName(themeType: ThemeType): String {
    return when (themeType) {
        ThemeType.PURPLE -> "经典紫 (默认)"
        ThemeType.BLUE -> "科技蓝"
        ThemeType.GREEN -> "森林绿"
        ThemeType.PINK -> "玫瑰粉"
        ThemeType.YELLOW -> "芥末黄"
    }
}

/**
 * 主题颜色圆点组件
 */
@Composable
private fun ThemeColorDot(color: Color, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                }
            )
    )
}

/**
 * 主题选择对话框
 */
@Composable
private fun ThemeSelectionDialog(
    currentTheme: ThemeType,
    onThemeSelected: (ThemeType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择主题") },
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ThemeOptionItem(
                    themeType = ThemeType.PURPLE,
                    color = PurpleTheme.Primary,
                    isSelected = currentTheme == ThemeType.PURPLE,
                    onClick = { onThemeSelected(ThemeType.PURPLE) }
                )
                ThemeOptionItem(
                    themeType = ThemeType.BLUE,
                    color = BlueTheme.Primary,
                    isSelected = currentTheme == ThemeType.BLUE,
                    onClick = { onThemeSelected(ThemeType.BLUE) }
                )
                ThemeOptionItem(
                    themeType = ThemeType.GREEN,
                    color = GreenTheme.Primary,
                    isSelected = currentTheme == ThemeType.GREEN,
                    onClick = { onThemeSelected(ThemeType.GREEN) }
                )
                ThemeOptionItem(
                    themeType = ThemeType.PINK,
                    color = PinkTheme.Primary,
                    isSelected = currentTheme == ThemeType.PINK,
                    onClick = { onThemeSelected(ThemeType.PINK) }
                )
                ThemeOptionItem(
                    themeType = ThemeType.YELLOW,
                    color = YellowTheme.Primary,
                    isSelected = currentTheme == ThemeType.YELLOW,
                    onClick = { onThemeSelected(ThemeType.YELLOW) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

/**
 * 主题选项组件
 */
@Composable
private fun ThemeOptionItem(
    themeType: ThemeType,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 颜色预览圆点
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    CircleShape
                )
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // 主题名称
        Text(
            text = getThemeDisplayName(themeType),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        
        // 选中图标
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "已选择",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
