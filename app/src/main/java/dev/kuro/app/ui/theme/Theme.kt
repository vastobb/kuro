package dev.kuro.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val KuroDarkScheme = darkColorScheme(
    primary = KuroPrimary,
    onPrimary = KuroTextPrimary,
    primaryContainer = KuroPrimaryContainer,
    onPrimaryContainer = KuroDarkBg,
    secondary = KuroSecondary,
    onSecondary = KuroDarkBg,
    tertiary = KuroAccent,
    onTertiary = KuroDarkBg,
    background = KuroDarkBg,
    onBackground = KuroTextPrimary,
    surface = KuroDarkSecondaryBg,
    onSurface = KuroTextPrimary,
    surfaceVariant = KuroDarkCard,
    onSurfaceVariant = KuroTextSecondary,
    surfaceContainer = KuroDarkCard,
    surfaceContainerHigh = KuroDarkElevated,
    surfaceContainerHighest = KuroDarkElevated,
    outline = KuroDarkBorder,
    outlineVariant = KuroDarkBorder,
    error = KuroError,
    onError = KuroTextPrimary
)

private val KuroLightScheme = lightColorScheme(
    primary = KuroPrimary,
    onPrimary = KuroTextPrimary,
    primaryContainer = KuroPrimaryContainer,
    onPrimaryContainer = KuroLightTextPrimary,
    secondary = KuroSecondary,
    onSecondary = KuroLightTextPrimary,
    tertiary = KuroAccent,
    onTertiary = KuroLightTextPrimary,
    background = KuroLightBg,
    onBackground = KuroLightTextPrimary,
    surface = KuroLightSecondaryBg,
    onSurface = KuroLightTextPrimary,
    surfaceVariant = KuroLightCard,
    onSurfaceVariant = KuroLightTextSecondary,
    surfaceContainer = KuroLightCard,
    surfaceContainerHigh = KuroLightElevated,
    surfaceContainerHighest = KuroLightElevated,
    outline = KuroLightBorder,
    outlineVariant = KuroLightBorder,
    error = KuroError,
    onError = KuroTextPrimary
)

/**
 * Kuro theme — dark-first AMOLED, separate light design, dynamic-color aware.
 * AMOLED: pure #0B0D13 background in dark mode.
 */
@Composable
fun KuroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> KuroDarkScheme
        else -> KuroLightScheme
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = KuroTypography,
        shapes = KuroShapes,
        content = content
    )
}
