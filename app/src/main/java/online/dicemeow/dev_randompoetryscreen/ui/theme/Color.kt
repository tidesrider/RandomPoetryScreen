package online.dicemeow.dev_randompoetryscreen.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import online.dicemeow.dev_randompoetryscreen.data.model.ColorSchemeStyle

val GeekBlack = Color(0xFF0A0A0A)
val GeekBgDark = Color(0xFF0D0D0D)
val GeekSurface = Color(0xFF141414)
val GeekSurfaceVariant = Color(0xFF1E1E1E)

val TerminalGreen = Color(0xFF00FF41)
val TerminalGreenDim = Color(0xFF008F11)
val TerminalGreenDark = Color(0xFF003B00)

val TerminalAmber = Color(0xFFFFB000)
val TerminalCyan = Color(0xFF00FFFF)
val TerminalRed = Color(0xFFFF0040)
val TerminalWhite = Color(0xFFE0E0E0)
val TerminalGray = Color(0xFF808080)

data class ColorSchemeColors(
    val primary: Color,
    val primaryDim: Color,
    val primaryDark: Color
)

fun getColorSchemeColors(style: ColorSchemeStyle): ColorSchemeColors {
    return when (style) {
        ColorSchemeStyle.TERMINAL_GREEN -> ColorSchemeColors(
            primary = TerminalGreen,
            primaryDim = TerminalGreenDim,
            primaryDark = TerminalGreenDark
        )
        ColorSchemeStyle.MONOCHROME -> ColorSchemeColors(
            primary = TerminalWhite,
            primaryDim = TerminalGray,
            primaryDark = Color(0xFF404040)
        )
        ColorSchemeStyle.CYAN_BLUE -> ColorSchemeColors(
            primary = TerminalCyan,
            primaryDim = Color(0xFF0088AA),
            primaryDark = Color(0xFF003344)
        )
        ColorSchemeStyle.AMBER_ORANGE -> ColorSchemeColors(
            primary = TerminalAmber,
            primaryDim = Color(0xFFAA7700),
            primaryDark = Color(0xFF442200)
        )
    }
}

fun getMaterialColorScheme(style: ColorSchemeStyle): ColorScheme {
    return when (style) {
        ColorSchemeStyle.TERMINAL_GREEN -> darkColorScheme(
            primary = TerminalGreen,
            onPrimary = GeekBlack,
            primaryContainer = TerminalGreenDark,
            onPrimaryContainer = TerminalGreen,
            secondary = TerminalAmber,
            onSecondary = GeekBlack,
            secondaryContainer = GeekSurfaceVariant,
            onSecondaryContainer = TerminalAmber,
            tertiary = TerminalCyan,
            onTertiary = GeekBlack,
            background = GeekBgDark,
            onBackground = TerminalGreen,
            surface = GeekSurface,
            onSurface = TerminalGreen,
            surfaceVariant = GeekSurfaceVariant,
            onSurfaceVariant = TerminalGreenDim,
            error = TerminalRed,
            onError = GeekBlack,
            outline = TerminalGreenDim,
            outlineVariant = GeekSurfaceVariant
        )
        ColorSchemeStyle.MONOCHROME -> darkColorScheme(
            primary = TerminalWhite,
            onPrimary = GeekBlack,
            primaryContainer = Color(0xFF404040),
            onPrimaryContainer = TerminalWhite,
            secondary = TerminalGray,
            onSecondary = GeekBlack,
            secondaryContainer = GeekSurfaceVariant,
            onSecondaryContainer = TerminalGray,
            tertiary = TerminalGray,
            onTertiary = GeekBlack,
            background = GeekBgDark,
            onBackground = TerminalWhite,
            surface = GeekSurface,
            onSurface = TerminalWhite,
            surfaceVariant = GeekSurfaceVariant,
            onSurfaceVariant = TerminalGray,
            error = TerminalRed,
            onError = GeekBlack,
            outline = TerminalGray,
            outlineVariant = GeekSurfaceVariant
        )
        ColorSchemeStyle.CYAN_BLUE -> darkColorScheme(
            primary = TerminalCyan,
            onPrimary = GeekBlack,
            primaryContainer = Color(0xFF003344),
            onPrimaryContainer = TerminalCyan,
            secondary = Color(0xFF00AAFF),
            onSecondary = GeekBlack,
            secondaryContainer = GeekSurfaceVariant,
            onSecondaryContainer = Color(0xFF00AAFF),
            tertiary = TerminalGreen,
            onTertiary = GeekBlack,
            background = GeekBgDark,
            onBackground = TerminalCyan,
            surface = GeekSurface,
            onSurface = TerminalCyan,
            surfaceVariant = GeekSurfaceVariant,
            onSurfaceVariant = Color(0xFF0088AA),
            error = TerminalRed,
            onError = GeekBlack,
            outline = Color(0xFF0088AA),
            outlineVariant = GeekSurfaceVariant
        )
        ColorSchemeStyle.AMBER_ORANGE -> darkColorScheme(
            primary = TerminalAmber,
            onPrimary = GeekBlack,
            primaryContainer = Color(0xFF442200),
            onPrimaryContainer = TerminalAmber,
            secondary = Color(0xFFFF6600),
            onSecondary = GeekBlack,
            secondaryContainer = GeekSurfaceVariant,
            onSecondaryContainer = Color(0xFFFF6600),
            tertiary = TerminalGreen,
            onTertiary = GeekBlack,
            background = GeekBgDark,
            onBackground = TerminalAmber,
            surface = GeekSurface,
            onSurface = TerminalAmber,
            surfaceVariant = GeekSurfaceVariant,
            onSurfaceVariant = Color(0xFFAA7700),
            error = TerminalRed,
            onError = GeekBlack,
            outline = Color(0xFFAA7700),
            outlineVariant = GeekSurfaceVariant
        )
    }
}
