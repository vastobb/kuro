package dev.kuro.app.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.kuro.app.logging.KuroLog
import dev.kuro.app.ui.theme.KuroMotion
import dev.kuro.app.ui.theme.KuroSecondary
import dev.kuro.app.ui.theme.KuroSpacing
import kotlinx.coroutines.delay

/**
 * Splash — black bg, animated logo, subtle purple pulse, tiny cyan particles.
 * Fades in, pulse expands, transitions to dashboard. Max 1.2s.
 */
@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        KuroLog.splashStart()
        delay(1150)
        onDone()
    }

    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = KuroMotion.Emphasized),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val glowAlpha by pulse.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Purple pulse halo
        Box(
            Modifier
                .size(220.dp)
                .scale(scale)
                .alpha(glowAlpha)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Logo mark
            Box(
                Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "K",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White
                )
            }
            Spacer(Modifier.height(KuroSpacing.s16))
            Text("Kuro", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text(
                "Isolated workspace",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
            // Cyan particles
            Spacer(Modifier.height(KuroSpacing.s20))
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                repeat(5) { i ->
                    Box(
                        Modifier
                            .offset(y = ((-4 + i * 2)).dp)
                            .size((3 + i).dp)
                            .clip(CircleShape)
                            .background(KuroSecondary.copy(alpha = 0.7f))
                    )
                }
            }
        }
    }
}
