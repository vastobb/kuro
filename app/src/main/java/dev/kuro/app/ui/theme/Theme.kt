package dev.kuro.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import dev.kuro.app.logging.KuroLog

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
 * Kuro theme — static palettes only, by startup-safety policy.
 * No dynamic color (OEM monet resources can be absent and kill first frame),
 * no runtime resolution: a pure synchronous scheme pick.
 */
@Composable
fun KuroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    KuroLog.themeInit(darkTheme, false)
    MaterialTheme(
        colorScheme = if (darkTheme) KuroDarkScheme else KuroLightScheme,
        typography = KuroTypography,
        shapes = KuroShapes,
        content = content
    )
}
