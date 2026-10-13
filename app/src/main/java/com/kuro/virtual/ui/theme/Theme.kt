package com.kuro.virtual.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Kuro palette: white + black + vermilion. Every surface role is pinned so no
// baseline-purple leaks through Cards, sheets or bars.
val KuroRed = Color(0xFFE53935)
val KuroRedDark = Color(0xFFEF5350)
val KuroRedPale = Color(0xFFFDECEA)
val KuroRedInk = Color(0xFF7F1D1B)
val KuroBlack = Color(0xFF111111)
val KuroWhite = Color(0xFFFFFFFF)
val KuroSurfaceLight = Color(0xFFF4F4F5)
val KuroSurfaceDark = Color(0xFF1A1A1E)
val KuroLine = Color(0xFFE3E5E9)
val KuroLineDark = Color(0xFF2A2A30)

private val LightScheme = lightColorScheme(
    primary = KuroBlack,
    onPrimary = KuroWhite,
    primaryContainer = KuroBlack,
    onPrimaryContainer = KuroWhite,
    secondary = KuroRed,
    onSecondary = KuroWhite,
    secondaryContainer = KuroRedPale,
    onSecondaryContainer = KuroRedInk,
    tertiary = KuroRed,
    onTertiary = KuroWhite,
    tertiaryContainer = KuroRedPale,
    onTertiaryContainer = KuroRedInk,
    background = KuroWhite,
    onBackground = KuroBlack,
    surface = KuroWhite,
    onSurface = KuroBlack,
    surfaceVariant = KuroSurfaceLight,
    onSurfaceVariant = KuroBlack,
    surfaceContainerLowest = KuroWhite,
    surfaceContainerLow = KuroWhite,
    surfaceContainer = KuroSurfaceLight,
    surfaceContainerHigh = KuroSurfaceLight,
    surfaceContainerHighest = KuroLine,
    outline = Color(0xFFC9CCD1),
    outlineVariant = KuroLine,
    error = KuroRed,
    errorContainer = KuroRedPale,
    onErrorContainer = KuroRedInk,
    scrim = KuroBlack,
)

private val DarkScheme = darkColorScheme(
    primary = KuroWhite,
    onPrimary = KuroBlack,
    primaryContainer = KuroWhite,
    onPrimaryContainer = KuroBlack,
    secondary = KuroRedDark,
    onSecondary = KuroBlack,
    secondaryContainer = Color(0xFF3A1412),
    onSecondaryContainer = Color(0xFFFFCDCA),
    tertiary = KuroRedDark,
    onTertiary = KuroBlack,
    tertiaryContainer = Color(0xFF3A1412),
    onTertiaryContainer = Color(0xFFFFCDCA),
    background = KuroBlack,
    onBackground = KuroWhite,
    surface = KuroBlack,
    onSurface = KuroWhite,
    surfaceVariant = KuroSurfaceDark,
    onSurfaceVariant = KuroWhite,
    surfaceContainerLowest = KuroBlack,
    surfaceContainerLow = KuroBlack,
    surfaceContainer = KuroSurfaceDark,
    surfaceContainerHigh = KuroSurfaceDark,
    surfaceContainerHighest = KuroLineDark,
    outline = Color(0xFF4A4A52),
    outlineVariant = KuroLineDark,
    error = KuroRedDark,
    errorContainer = Color(0xFF3A1412),
    onErrorContainer = Color(0xFFFFCDCA),
    scrim = KuroBlack,
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
