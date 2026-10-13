package dev.kuro.app.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import dev.kuro.app.data.PreviewData
import dev.kuro.app.ui.components.KuroCard
import dev.kuro.app.ui.components.KuroMeter
import dev.kuro.app.ui.components.KuroScreenTitle
import dev.kuro.app.ui.components.StatusIndicator
import dev.kuro.app.ui.theme.KuroSpacing

/**
 * Running apps — modern activity monitor: CPU / memory / storage,
 * animated charts, live indicators.
 */
@Composable
fun ActivityScreen() {
    val pulse = rememberInfiniteTransition(label = "live")
    val alpha by pulse.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "liveAlpha"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = KuroSpacing.s20, bottom = KuroSpacing.s32),
        verticalArrangement = Arrangement.spacedBy(KuroSpacing.s12)
    ) {
        item {
            KuroScreenTitle("Activity", "${PreviewData.running.size} live environments", eyebrow = "Monitor")
        }
        item {
            KuroCard(Modifier.padding(horizontal = KuroSpacing.s20).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("●", Modifier.alpha(alpha), color = MaterialTheme.colorScheme.primary)
                    Text("  Live", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    Text("60fps · spring", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(Modifier.height(KuroSpacing.s8))
                KuroMeter("CPU", "18%", 0.18f)
                Spacer(Modifier.height(KuroSpacing.s8))
                KuroMeter("Memory", "600 MB", 0.30f)
            }
        }
        items(PreviewData.running, key = { it.app.id }) { env ->
            KuroCard(Modifier.padding(horizontal = KuroSpacing.s20).fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(env.app.name, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.weight(1f))
                        StatusIndicator(env.app.status, pulsing = true)
                    }
                    Text(
                        "Up ${env.uptimeMin} min · ${env.app.environment}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(KuroSpacing.s8))
                    KuroMeter("CPU", "${env.cpuPct}%", env.cpuPct / 100f)
                    Spacer(Modifier.height(KuroSpacing.s8))
                    KuroMeter("Memory", "${env.memMb} MB", env.memMb / 2048f)
                }
            }
        }
    }
}
