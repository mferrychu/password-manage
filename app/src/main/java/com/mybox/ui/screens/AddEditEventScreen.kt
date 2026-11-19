package com.mybox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mybox.data.model.Event
import java.text.SimpleDateFormat
import java.util.*

val EVENT_COLORS = listOf(
    "#FFFFFF",
    "#D1F0EA",
    "#F5ECCF",
    "#F9E0E5",
    "#DCE7F5"
)

/**
 * 添加/修改事件页
 * 添加事件页和修改事件页复用此页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEventScreen(
    title: String,
    existingEvent: Event? = null,
    isSubmitting: Boolean,
    onSave: (String, String, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var eventDate by remember { mutableStateOf(existingEvent?.date ?: getCurrentDate()) }
    var description by remember { mutableStateOf(existingEvent?.description ?: "") }
    var selectedColor by remember { mutableStateOf(existingEvent?.color ?: "#FFFFFF") }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    val isSaveEnabled = eventDate.isNotEmpty() && description.trim().isNotEmpty() && !isSubmitting
    
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
            // 日期选择（必填）
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = eventDate,
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("* 日期")
                    },
                    placeholder = {
                        Text("请选择日期")
                    },
                    singleLine = true,
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = "选择日期"
                            )
                        }
                    },
                    colors = com.mybox.ui.components.whiteTextFieldColors(),
                    shape = MaterialTheme.shapes.medium
                )
                
                // 农历显示
                val lunarDate = remember(eventDate) {
                    com.mybox.ui.components.getLunarDate(eventDate)
                }
                if (lunarDate.isNotEmpty()) {
                    Text(
                        text = lunarDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                    )
                }
            }
            
            // 事件说明输入框（必填，多行）
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                label = {
                    Text("* 事件说明")
                },
                placeholder = {
                    Text("请输入事件说明")
                },
                minLines = 4,
                maxLines = 6,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Default
                ),
                colors = com.mybox.ui.components.whiteTextFieldColors(),
                shape = MaterialTheme.shapes.medium
            )
            
            // 色块选择
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "选择卡片颜色",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    EVENT_COLORS.forEach { colorHex ->
                        val color = Color(android.graphics.Color.parseColor(colorHex))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(color)
                                .then(
                                    if (selectedColor == colorHex) {
                                        Modifier.border(
                                            width = 2.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = MaterialTheme.shapes.small
                                        )
                                    } else {
                                        Modifier.border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outline,
                                            shape = MaterialTheme.shapes.small
                                        )
                                    }
                                )
                                .clickable { selectedColor = colorHex }
                        ) {
                            if (selectedColor == colorHex) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "选中",
                                    modifier = Modifier.align(Alignment.Center).size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
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
                    onSave(eventDate, description.trim(), selectedColor)
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
    
    // 日期选择对话框
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = parseDateToMillis(eventDate)
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            eventDate = formatDateFromMillis(millis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * 获取当前日期（YYYY-MM-DD格式）
 */
private fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

/**
 * 解析日期字符串为毫秒值
 */
private fun parseDateToMillis(dateStr: String): Long? {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.parse(dateStr)?.time
    } catch (e: Exception) {
        null
    }
}

/**
 * 将毫秒值格式化为日期字符串
 */
private fun formatDateFromMillis(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(millis))
}