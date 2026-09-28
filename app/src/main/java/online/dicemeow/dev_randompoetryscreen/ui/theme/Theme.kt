package online.dicemeow.dev_randompoetryscreen.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import online.dicemeow.dev_randompoetryscreen.data.model.ColorSchemeStyle

@Composable
fun Dev_RandomPoetryScreenTheme(
    colorSchemeStyle: ColorSchemeStyle = ColorSchemeStyle.TERMINAL_GREEN,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = getMaterialColorScheme(colorSchemeStyle),
        typography = PixelTypography,
        content = content
    )
}