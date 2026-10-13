package dev.kuro.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Kuro motion — spec: fast 120ms / normal 250ms / premium 400ms.
 * Spring-first, 60fps+. No spinning progress bars (use shimmer/skeleton).
 */
object KuroMotion {
    const val Fast = 120
    const val Normal = 250
    const val Premium = 400

    val Emphasized = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val Standard = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    fun <T> fast() = tween<T>(Fast, easing = Standard)
    fun <T> normal() = tween<T>(Normal, easing = Emphasized)
    fun <T> premium() = tween<T>(Premium, easing = Emphasized)

    fun <T> springy() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    fun <T> gentleSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
}
