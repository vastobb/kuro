package dev.kuro.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.kuro.app.data.PreviewData
import dev.kuro.app.ui.components.AppCard
import dev.kuro.app.ui.components.KuroCard
import dev.kuro.app.ui.components.KuroHeroCard
import dev.kuro.app.ui.components.KuroMeter
import dev.kuro.app.ui.components.SectionHeader
import dev.kuro.app.ui.components.StatusIndicator
import dev.kuro.app.ui.theme.KuroMotion
import dev.kuro.app.ui.theme.KuroSpacing
import dev.kuro.app.util.greetingForHour
import java.util.Calendar

/**
 * Home dashboard — hero greeting + workspace status + memory + counts,
 * then Recent / Running / Pinned / Recommended / Features.
 * Cards animate on viewport entry.
 */
@Composable
fun HomeScreen(
    onOpenApp: (String) -> Unit,
    onImport: () -> Unit,
    onSeeAll: () -> Unit
) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    val apps = PreviewData.apps
    val running = apps.count { it.status == dev.kuro.app.data.AppStatus.RUNNING }
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(KuroSpacing.s16),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            top = KuroSpacing.s20,
            bottom = KuroSpacing.s32
        )
    ) {
        item {
            AnimatedVisibility(
                visible = entered,
                enter = fadeIn(animationSpec = KuroMotion.normal()) +
                    slideInVertically(animationSpec = KuroMotion.normal()) { it / 3 }
            ) {
                Column(Modifier.padding(horizontal = KuroSpacing.s20)) {
                    Text(
                        "${greetingForHour(hour)}.",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        "Workspace healthy · $running running · ${apps.size} imported",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            AnimatedVisibility(visible = entered, enter = fadeIn(KuroMotion.normal())) {
                KuroHeroCard(Modifier.padding(horizontal = KuroSpacing.s20)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Memory, contentDescription = null)
                        Text(
                            "  Memory  ·  612 MB / 2 GB",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.weight(1f))
                        StatusIndicator(dev.kuro.app.data.AppStatus.RUNNING, pulsing = true)
                    }
                    Spacer(Modifier.height(KuroSpacing.s12))
                    KuroMeter(label = "Workspace memory", valueLabel = "31%", fraction = 0.31f)
                    Spacer(Modifier.height(KuroSpacing.s8))
                    KuroMeter(label = "Storage", valueLabel = "2.1 GB / 8 GB", fraction = 0.27f)
                }
            }
        }
        item { SectionHeader("Recent Apps", "See all", onAction = { onSeeAll() }) }
        items(apps.take(3), key = { it.id }) { app ->
            AppCard(
                app = app,
                onLaunch = {},
                onOpen = { onOpenApp(app.id) },
                onLongPress = { onOpenApp(app.id) },
                modifier = Modifier.padding(horizontal = KuroSpacing.s20)
            )
        }
        item { SectionHeader("Running Now", actionLabel = null) }
        item {
            KuroCard(Modifier.padding(horizontal = KuroSpacing.s20).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Text("  $running environments active", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        item { SectionHeader("Pinned") }
        items(apps.filter { it.pinned }, key = { it.id }) { app ->
            AppCard(
                app = app,
                onLaunch = {},
                onOpen = { onOpenApp(app.id) },
                onLongPress = { onOpenApp(app.id) },
                modifier = Modifier.padding(horizontal = KuroSpacing.s20)
            )
        }
        item { SectionHeader("Recommended Actions") }
        item {
            KuroCard(
                modifier = Modifier.padding(horizontal = KuroSpacing.s20).fillMaxWidth(),
                onClick = onImport
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Import your first APK", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Isolated in seconds.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }
        }
        item {
            KuroCard(Modifier.padding(horizontal = KuroSpacing.s20).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Storage, contentDescription = null)
                    Text(
                        "  Storage saver is on",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
