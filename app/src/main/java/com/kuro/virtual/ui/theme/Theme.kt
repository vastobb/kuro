package com.kuro.virtual.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Kuro palette: white + black + vermilion. No gradients, no AI-purple.
val KuroRed = Color(0xFFE53935)
val KuroRedDark = Color(0xFFEF5350)
val KuroBlack = Color(0xFF111111)
val KuroWhite = Color(0xFFFFFFFF)
val KuroSurfaceLight = Color(0xFFF4F4F5)
val KuroSurfaceDark = Color(0xFF1A1A1E)

private val LightScheme = lightColorScheme(
    primary = KuroBlack,
    onPrimary = KuroWhite,
    secondary = KuroRed,
    onSecondary = KuroWhite,
    tertiary = KuroRed,
    background = KuroWhite,
    onBackground = KuroBlack,
    surface = KuroWhite,
    onSurface = KuroBlack,
    surfaceVariant = KuroSurfaceLight,
    onSurfaceVariant = KuroBlack,
    error = KuroRed,
)

private val DarkScheme = darkColorScheme(
    primary = KuroWhite,
    onPrimary = KuroBlack,
    secondary = KuroRedDark,
    onSecondary = KuroBlack,
    tertiary = KuroRedDark,
    background = KuroBlack,
    onBackground = KuroWhite,
    surface = KuroBlack,
    onSurface = KuroWhite,
    surfaceVariant = KuroSurfaceDark,
    onSurfaceVariant = KuroWhite,
    error = KuroRedDark,
)

@Composable
fun KuroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content,
    )
}
