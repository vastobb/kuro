package dev.kuro.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.kuro.app.ui.theme.KuroCardShape
import dev.kuro.app.ui.theme.KuroMotion
import dev.kuro.app.ui.theme.KuroSpacing

/**
 * Shimmer + skeleton cards. Replaces spinning progress bars per spec.
 */
@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(KuroMotion.Premium, easing = KuroMotion.Standard),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )
    return Brush.linearGradient(
        colors = listOf(
            Color.Gray.copy(alpha = 0.18f),
            Color.Gray.copy(alpha = 0.32f),
            Color.Gray.copy(alpha = 0.18f)
        ),
        start = Offset(x - 400f, 0f),
        end = Offset(x, 200f)
    )
}

@Composable
fun SkeletonLine(modifier: Modifier = Modifier, heightDp: Int = 14) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(shimmerBrush())
    )
}

@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(KuroCardShape)
            .background(Color.Gray.copy(alpha = 0.08f))
            .padding(KuroSpacing.s16)
    ) {
        SkeletonLine(Modifier.fillMaxWidth(0.5f))
        Spacer(Modifier.height(KuroSpacing.s8))
        SkeletonLine()
        Spacer(Modifier.height(KuroSpacing.s8))
        SkeletonLine(Modifier.fillMaxWidth(0.7f))
    }
}
