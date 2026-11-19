package com.mybox.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * 主题类型枚举
 */
enum class ThemeType {
    PURPLE, // 紫色（默认）
    BLUE,   // 科技蓝
    GREEN,  // 绿色
    PINK,   // 玫瑰粉
    YELLOW  // 芥末黄
}

/**
 * Material Design 3 紫色系配色（默认主题）
 */
object PurpleTheme {
    // 主色
    val Primary = Color(0xFF6750A4)
    val PrimaryContainer = Color(0xFFEADDFF)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnPrimaryContainer = Color(0xFF21005D)

    // 辅助色
    val Secondary = Color(0xFF625B71)
    val SecondaryContainer = Color(0xFFE8DEF8)
    val OnSecondary = Color(0xFFFFFFFF)
    val OnSecondaryContainer = Color(0xFF1D192B)

    // 第三色
    val Tertiary = Color(0xFF7D5260)
    val TertiaryContainer = Color(0xFFFFD8E4)
    val OnTertiary = Color(0xFFFFFFFF)
    val OnTertiaryContainer = Color(0xFF31111D)

    // 背景色
    val Background = Color(0xFFEADDFF)
    val OnBackground = Color(0xFF1C1B1F)

    // 表面色
    val Surface = Color(0xFFEADDFF)
    val SurfaceVariant = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFF1C1B1F)
    val OnSurfaceVariant = Color(0xFF49454F)

    // 错误色
    val Error = Color(0xFFB3261E)
    val ErrorContainer = Color(0xFFF9DEDC)
    val OnError = Color(0xFFFFFFFF)
    val OnErrorContainer = Color(0xFF410E0B)

    // 其他
    val Outline = Color(0xFF79747E)
    val OutlineVariant = Color(0xFFCAC4D0)

    val colorScheme: ColorScheme = lightColorScheme(
        primary = Primary,
        primaryContainer = PrimaryContainer,
        onPrimary = OnPrimary,
        onPrimaryContainer = OnPrimaryContainer,
        secondary = Secondary,
        secondaryContainer = SecondaryContainer,
        onSecondary = OnSecondary,
        onSecondaryContainer = OnSecondaryContainer,
        tertiary = Tertiary,
        tertiaryContainer = TertiaryContainer,
        onTertiary = OnTertiary,
        onTertiaryContainer = OnTertiaryContainer,
        background = Background,
        onBackground = OnBackground,
        surface = Surface,
        surfaceVariant = SurfaceVariant,
        onSurface = OnSurface,
        onSurfaceVariant = OnSurfaceVariant,
        error = Error,
        errorContainer = ErrorContainer,
        onError = OnError,
        onErrorContainer = OnErrorContainer,
        outline = Outline,
        outlineVariant = OutlineVariant
    )
}

/**
 * 科技蓝主题（Tech Blue）
 * 主色：#1976D2，深邃 · 未来 · 智能
 */
object BlueTheme {
    val Primary = Color(0xFF1976D2)
    val PrimaryContainer = Color(0xFFBBDEFB)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnPrimaryContainer = Color(0xFF0D47A1)

    val Secondary = Color(0xFF42A5F5)
    val SecondaryContainer = Color(0xFF90CAF9)
    val OnSecondary = Color(0xFFFFFFFF)
    val OnSecondaryContainer = Color(0xFF0D47A1)

    val Tertiary = Color(0xFF2196F3)
    val TertiaryContainer = Color(0xFF64B5F6)
    val OnTertiary = Color(0xFFFFFFFF)
    val OnTertiaryContainer = Color(0xFF0D47A1)

    val Background = Color(0xFFF5F9FC)
    val OnBackground = Color(0xFF1C1B1F)

    val Surface = Color(0xFFF5F9FC)
    val SurfaceVariant = Color(0xFFE3F2FD)
    val OnSurface = Color(0xFF1C1B1F)
    val OnSurfaceVariant = Color(0xFF37474F)

    val Error = Color(0xFFB3261E)
    val ErrorContainer = Color(0xFFF9DEDC)
    val OnError = Color(0xFFFFFFFF)
    val OnErrorContainer = Color(0xFF410E0B)

    val Outline = Color(0xFF90CAF9)
    val OutlineVariant = Color(0xFFBBDEFB)

    val colorScheme: ColorScheme = lightColorScheme(
        primary = Primary,
        primaryContainer = PrimaryContainer,
        onPrimary = OnPrimary,
        onPrimaryContainer = OnPrimaryContainer,
        secondary = Secondary,
        secondaryContainer = SecondaryContainer,
        onSecondary = OnSecondary,
        onSecondaryContainer = OnSecondaryContainer,
        tertiary = Tertiary,
        tertiaryContainer = TertiaryContainer,
        onTertiary = OnTertiary,
        onTertiaryContainer = OnTertiaryContainer,
        background = Background,
        onBackground = OnBackground,
        surface = Surface,
        surfaceVariant = SurfaceVariant,
        onSurface = OnSurface,
        onSurfaceVariant = OnSurfaceVariant,
        error = Error,
        errorContainer = ErrorContainer,
        onError = OnError,
        onErrorContainer = OnErrorContainer,
        outline = Outline,
        outlineVariant = OutlineVariant
    )
}

/**
 * Material Design 3 绿色系配色
 */
object GreenTheme {
    val Primary = Color(0xFF006E1C)
    val PrimaryContainer = Color(0xFFA4F692)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnPrimaryContainer = Color(0xFF002104)

    val Secondary = Color(0xFF535F50)
    val SecondaryContainer = Color(0xFFD7E4CC)
    val OnSecondary = Color(0xFFFFFFFF)
    val OnSecondaryContainer = Color(0xFF101C10)

    val Tertiary = Color(0xFF39656A)
    val TertiaryContainer = Color(0xFFBCEAEE)
    val OnTertiary = Color(0xFFFFFFFF)
    val OnTertiaryContainer = Color(0xFF001F23)

    val Background = Color(0xFFFAFDF5)
    val OnBackground = Color(0xFF191C18)

    val Surface = Color(0xFFFAFDF5)
    val SurfaceVariant = Color(0xFFDDE5D4)
    val OnSurface = Color(0xFF191C18)
    val OnSurfaceVariant = Color(0xFF3F493E)

    val Error = Color(0xFFB3261E)
    val ErrorContainer = Color(0xFFF9DEDC)
    val OnError = Color(0xFFFFFFFF)
    val OnErrorContainer = Color(0xFF410E0B)

    val Outline = Color(0xFF6F7A6B)
    val OutlineVariant = Color(0xFFBEC9B9)

    val colorScheme: ColorScheme = lightColorScheme(
        primary = Primary,
        primaryContainer = PrimaryContainer,
        onPrimary = OnPrimary,
        onPrimaryContainer = OnPrimaryContainer,
        secondary = Secondary,
        secondaryContainer = SecondaryContainer,
        onSecondary = OnSecondary,
        onSecondaryContainer = OnSecondaryContainer,
        tertiary = Tertiary,
        tertiaryContainer = TertiaryContainer,
        onTertiary = OnTertiary,
        onTertiaryContainer = OnTertiaryContainer,
        background = Background,
        onBackground = OnBackground,
        surface = Surface,
        surfaceVariant = SurfaceVariant,
        onSurface = OnSurface,
        onSurfaceVariant = OnSurfaceVariant,
        error = Error,
        errorContainer = ErrorContainer,
        onError = OnError,
        onErrorContainer = OnErrorContainer,
        outline = Outline,
        outlineVariant = OutlineVariant
    )
}

/**
 * 玫瑰粉主题（Rose Pink）
 * 主色：#E8829A，温柔而不甜腻
 */
object PinkTheme {
    val Primary = Color(0xFFE8829A)
    val PrimaryContainer = Color(0xFFF4A0B0)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnPrimaryContainer = Color(0xFFA65368)

    val Secondary = Color(0xFFF4A0B0)
    val SecondaryContainer = Color(0xFFFDE2E8)
    val OnSecondary = Color(0xFFFFFFFF)
    val OnSecondaryContainer = Color(0xFFA65368)

    val Tertiary = Color(0xFFA65368)
    val TertiaryContainer = Color(0xFFE2CED4)
    val OnTertiary = Color(0xFFFFFFFF)
    val OnTertiaryContainer = Color(0xFF402B30)

    val Background = Color(0xFFFDF8F5)
    val OnBackground = Color(0xFF402B30)

    val Surface = Color(0xFFFDF8F5)
    val SurfaceVariant = Color(0xFFFFFBFA)
    val OnSurface = Color(0xFF402B30)
    val OnSurfaceVariant = Color(0xFF735B62)

    val Error = Color(0xFFB3261E)
    val ErrorContainer = Color(0xFFF9DEDC)
    val OnError = Color(0xFFFFFFFF)
    val OnErrorContainer = Color(0xFF410E0B)

    val Outline = Color(0xFFE2CED4)
    val OutlineVariant = Color(0xFFF3E6EA)

    val colorScheme: ColorScheme = lightColorScheme(
        primary = Primary,
        primaryContainer = PrimaryContainer,
        onPrimary = OnPrimary,
        onPrimaryContainer = OnPrimaryContainer,
        secondary = Secondary,
        secondaryContainer = SecondaryContainer,
        onSecondary = OnSecondary,
        onSecondaryContainer = OnSecondaryContainer,
        tertiary = Tertiary,
        tertiaryContainer = TertiaryContainer,
        onTertiary = OnTertiary,
        onTertiaryContainer = OnTertiaryContainer,
        background = Background,
        onBackground = OnBackground,
        surface = Surface,
        surfaceVariant = SurfaceVariant,
        onSurface = OnSurface,
        onSurfaceVariant = OnSurfaceVariant,
        error = Error,
        errorContainer = ErrorContainer,
        onError = OnError,
        onErrorContainer = OnErrorContainer,
        outline = Outline,
        outlineVariant = OutlineVariant
    )
}

/**
 * 芥末黄主题（Mustard Yellow）
 * 主色：#E8C547，阳光温暖，低饱和高亲和力
 */
object YellowTheme {
    val Primary = Color(0xFFE8C547)
    val PrimaryContainer = Color(0xFFF5E6B8)
    val OnPrimary = Color(0xFF362F1C)
    val OnPrimaryContainer = Color(0xFF362F1C)

    val Secondary = Color(0xFFF5E6B8)
    val SecondaryContainer = Color(0xFFFCF3DC)
    val OnSecondary = Color(0xFF362F1C)
    val OnSecondaryContainer = Color(0xFF362F1C)

    val Tertiary = Color(0xFFB8962E)
    val TertiaryContainer = Color(0xFFF5E6B8)
    val OnTertiary = Color(0xFFFFFFFF)
    val OnTertiaryContainer = Color(0xFF362F1C)

    val Background = Color(0xFFFCF9F0)
    val OnBackground = Color(0xFF362F1C)

    val Surface = Color(0xFFFCF9F0)
    val SurfaceVariant = Color(0xFFF5E6B8)
    val OnSurface = Color(0xFF362F1C)
    val OnSurfaceVariant = Color(0xFF71673F)

    val Error = Color(0xFFB3261E)
    val ErrorContainer = Color(0xFFF9DEDC)
    val OnError = Color(0xFFFFFFFF)
    val OnErrorContainer = Color(0xFF410E0B)

    val Outline = Color(0xFFE6DDC0)
    val OutlineVariant = Color(0xFFF3EDD9)

    val colorScheme: ColorScheme = lightColorScheme(
        primary = Primary,
        primaryContainer = PrimaryContainer,
        onPrimary = OnPrimary,
        onPrimaryContainer = OnPrimaryContainer,
        secondary = Secondary,
        secondaryContainer = SecondaryContainer,
        onSecondary = OnSecondary,
        onSecondaryContainer = OnSecondaryContainer,
        tertiary = Tertiary,
        tertiaryContainer = TertiaryContainer,
        onTertiary = OnTertiary,
        onTertiaryContainer = OnTertiaryContainer,
        background = Background,
        onBackground = OnBackground,
        surface = Surface,
        surfaceVariant = SurfaceVariant,
        onSurface = OnSurface,
        onSurfaceVariant = OnSurfaceVariant,
        error = Error,
        errorContainer = ErrorContainer,
        onError = OnError,
        onErrorContainer = OnErrorContainer,
        outline = Outline,
        outlineVariant = OutlineVariant
    )
}

/**
 * 根据主题类型获取对应的 ColorScheme
 */
fun getColorScheme(themeType: ThemeType): ColorScheme {
    return when (themeType) {
        ThemeType.PURPLE -> PurpleTheme.colorScheme
        ThemeType.BLUE -> BlueTheme.colorScheme
        ThemeType.GREEN -> GreenTheme.colorScheme
        ThemeType.PINK -> PinkTheme.colorScheme
        ThemeType.YELLOW -> YellowTheme.colorScheme
    }
}

/**
 * 根据主题类型获取对应的主色（用于状态栏等硬编码颜色）
 */
fun getPrimaryColor(themeType: ThemeType): Color {
    return when (themeType) {
        ThemeType.PURPLE -> PurpleTheme.Primary
        ThemeType.BLUE -> BlueTheme.Primary
        ThemeType.GREEN -> GreenTheme.Primary
        ThemeType.PINK -> PinkTheme.Primary
        ThemeType.YELLOW -> YellowTheme.Primary
    }
}

fun getPrimaryContainerColor(themeType: ThemeType): Color {
    return when (themeType) {
        ThemeType.PURPLE -> PurpleTheme.PrimaryContainer
        ThemeType.BLUE -> BlueTheme.PrimaryContainer
        ThemeType.GREEN -> GreenTheme.PrimaryContainer
        ThemeType.PINK -> PinkTheme.PrimaryContainer
        ThemeType.YELLOW -> YellowTheme.PrimaryContainer
    }
}

fun getBackgroundColor(themeType: ThemeType): Color {
    return when (themeType) {
        ThemeType.PURPLE -> PurpleTheme.Background
        ThemeType.BLUE -> BlueTheme.Background
        ThemeType.GREEN -> GreenTheme.Background
        ThemeType.PINK -> PinkTheme.Background
        ThemeType.YELLOW -> YellowTheme.Background
    }
}

/**
 * 为兼容旧代码保留的顶层属性（指向紫色主题）
 */
val Primary = PurpleTheme.Primary
val PrimaryContainer = PurpleTheme.PrimaryContainer
val OnPrimary = PurpleTheme.OnPrimary
val OnPrimaryContainer = PurpleTheme.OnPrimaryContainer
val Secondary = PurpleTheme.Secondary
val SecondaryContainer = PurpleTheme.SecondaryContainer
val OnSecondary = PurpleTheme.OnSecondary
val OnSecondaryContainer = PurpleTheme.OnSecondaryContainer
val Tertiary = PurpleTheme.Tertiary
val TertiaryContainer = PurpleTheme.TertiaryContainer
val OnTertiary = PurpleTheme.OnTertiary
val OnTertiaryContainer = PurpleTheme.OnTertiaryContainer
val Background = PurpleTheme.Background
val OnBackground = PurpleTheme.OnBackground
val Surface = PurpleTheme.Surface
val SurfaceVariant = PurpleTheme.SurfaceVariant
val OnSurface = PurpleTheme.OnSurface
val OnSurfaceVariant = PurpleTheme.OnSurfaceVariant
val Error = PurpleTheme.Error
val ErrorContainer = PurpleTheme.ErrorContainer
val OnError = PurpleTheme.OnError
val OnErrorContainer = PurpleTheme.OnErrorContainer
val Outline = PurpleTheme.Outline
val OutlineVariant = PurpleTheme.OutlineVariant