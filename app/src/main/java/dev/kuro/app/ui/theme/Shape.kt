package dev.kuro.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Kuro corner radius — spec:
 * small 12 / cards 20 / dialogs 24 / sheets 28 top / buttons 18 / hero 28.
 */
val KuroShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

val KuroButtonShape = RoundedCornerShape(18.dp)
val KuroCardShape = RoundedCornerShape(20.dp)
val KuroHeroShape = RoundedCornerShape(28.dp)
val KuroSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
