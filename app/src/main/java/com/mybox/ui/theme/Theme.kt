package com.mybox.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * MyBox应用主题
 */
@Composable
fun MyBoxTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = false,
    themeType: ThemeType = ThemeType.PURPLE,
    content: @Composable () -> Unit
) {
    val colorScheme = getColorScheme(themeType)
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MyBoxTypography,
        content = content
    )
}