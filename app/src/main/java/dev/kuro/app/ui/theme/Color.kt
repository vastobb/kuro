package dev.kuro.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Kuro color system — exact spec values.
 * Glow effects: purple + cyan only.
 */
object KuroColors {
    const val PrimaryHex = 0xFF7C4DFF
    const val PrimaryContainerHex = 0xFF9A7DFF
    const val SecondaryHex = 0xFF00D4FF
    const val AccentHex = 0xFF33E1B5
    const val SuccessHex = 0xFF22C55E
    const val WarningHex = 0xFFF59E0B
    const val ErrorHex = 0xFFEF4444

    const val DarkBgHex = 0xFF0B0D13
    const val DarkSecondaryBgHex = 0xFF12151E
    const val DarkCardHex = 0xFF161A23
    const val DarkElevatedHex = 0xFF1C2230
    const val DarkBorderHex = 0xFF2A3142

    const val TextPrimaryHex = 0xFFFFFFFF
    const val TextSecondaryHex = 0xFFB3BACB
    const val TextMutedHex = 0xFF70788D
}

val KuroPrimary = Color(KuroColors.PrimaryHex)
val KuroPrimaryContainer = Color(KuroColors.PrimaryContainerHex)
val KuroSecondary = Color(KuroColors.SecondaryHex)
val KuroAccent = Color(KuroColors.AccentHex)
val KuroSuccess = Color(KuroColors.SuccessHex)
val KuroWarning = Color(KuroColors.WarningHex)
val KuroError = Color(KuroColors.ErrorHex)

val KuroDarkBg = Color(KuroColors.DarkBgHex)
val KuroDarkSecondaryBg = Color(KuroColors.DarkSecondaryBgHex)
val KuroDarkCard = Color(KuroColors.DarkCardHex)
val KuroDarkElevated = Color(KuroColors.DarkElevatedHex)
val KuroDarkBorder = Color(KuroColors.DarkBorderHex)

val KuroTextPrimary = Color(KuroColors.TextPrimaryHex)
val KuroTextSecondary = Color(KuroColors.TextSecondaryHex)
val KuroTextMuted = Color(KuroColors.TextMutedHex)

// Light mode — designed separately, not inverted. Warm paper, same purple/cyan accents.
val KuroLightBg = Color(0xFFF7F7FB)
val KuroLightSecondaryBg = Color(0xFFFFFFFF)
val KuroLightCard = Color(0xFFFFFFFF)
val KuroLightElevated = Color(0xFFFFFFFF)
val KuroLightBorder = Color(0xFFE3E6EF)
val KuroLightTextPrimary = Color(0xFF0E1120)
val KuroLightTextSecondary = Color(0xFF4A5168)
val KuroLightTextMuted = Color(0xFF8A91A8)
