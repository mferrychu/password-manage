package com.mybox.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import java.util.*

/**
 * 从阳历日期计算农历和生肖
 * @param dateStr 阳历日期字符串，格式：YYYY-MM-DD
 * @return 农历字符串，如 "马年五月初七"，解析失败返回空字符串
 */
fun getLunarDate(dateStr: String): String {
    return try {
        val parts = dateStr.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt()
        
        val lunarYears = arrayOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")
        val lunarMonths = arrayOf("正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊")
        val lunarDays = arrayOf(
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
        )
        val weekDays = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
        
        val chineseCalendar = android.icu.util.ChineseCalendar()
        val gregorianCalendar = java.util.Calendar.getInstance()
        gregorianCalendar.clear()
        gregorianCalendar.set(year, month - 1, day)
        chineseCalendar.time = gregorianCalendar.time
        
        val lunarMonth = chineseCalendar.get(android.icu.util.ChineseCalendar.MONTH) + 1
        val lunarDay = chineseCalendar.get(android.icu.util.ChineseCalendar.DAY_OF_MONTH)
        
        val yearIndex = (year - 4) % 12
        val normalizedYearIndex = if (yearIndex < 0) yearIndex + 12 else yearIndex
        val yearStr = "${lunarYears[normalizedYearIndex]}年"
        
        val isLeapMonth = chineseCalendar.get(android.icu.util.ChineseCalendar.IS_LEAP_MONTH) == 1
        val monthStr = if (isLeapMonth) "闰${lunarMonths[lunarMonth - 1]}" else lunarMonths[lunarMonth - 1]
        
        val dayIndex = lunarDay - 1
        val dayStr = if (dayIndex >= 0 && dayIndex < lunarDays.size) lunarDays[dayIndex] else "第${lunarDay}天"
        
        val weekDayIndex = gregorianCalendar.get(java.util.Calendar.DAY_OF_WEEK) - 1
        val weekDayStr = weekDays[weekDayIndex]
        
        "${yearStr}${monthStr}月${dayStr} $weekDayStr"
    } catch (e: Exception) {
        ""
    }
}

/**
 * 白色背景无边框的TextField颜色配置
 */
@Composable
fun whiteTextFieldColors(
    isError: Boolean = false
): TextFieldColors {
    return TextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedIndicatorColor = if (isError) MaterialTheme.colorScheme.error else Color.Transparent,
        unfocusedIndicatorColor = if (isError) MaterialTheme.colorScheme.error else Color.Transparent,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        errorIndicatorColor = MaterialTheme.colorScheme.error
    )
}

/**
 * 搜索框组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索"
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "搜索",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "清除",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { /* 触发搜索 */ }
        ),
        colors = whiteTextFieldColors(),
        shape = MaterialTheme.shapes.medium
    )
}