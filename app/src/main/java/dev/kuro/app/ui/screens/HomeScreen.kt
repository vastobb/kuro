package dev.kuro.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.kuro.app.data.AppStatus
import dev.kuro.app.data.PreviewData
import dev.kuro.app.ui.components.AppCard
import dev.kuro.app.ui.components.BrandHeader
import dev.kuro.app.ui.components.KuroCard
import dev.kuro.app.ui.components.KuroHeroCard
import dev.kuro.app.ui.components.KuroMeter
import dev.kuro.app.ui.components.SectionHeader
import dev.kuro.app.ui.components.StatusIndicator
import dev.kuro.app.ui.theme.KuroCardShape
import dev.kuro.app.ui.theme.KuroSpacing
import dev.kuro.app.util.greetingForHour
import java.util.Calendar

/**
 * Home dashboard — static first frame by startup-safety policy
 * (no enter animations before launch completes).
 */
@Composable
fun HomeScreen(
    onOpenApp: (String) -> Unit,
    onImport: () -> Unit,
    onSeeAll: () -> Unit
) {
    val apps = PreviewData.apps
    val running = apps.count { it.status == AppStatus.RUNNING }
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(KuroSpacing.s16),
        contentPadding = PaddingValues(top = KuroSpacing.s24, bottom = KuroSpacing.s32)
    ) {
        item {
            BrandHeader(
                greeting = "${greetingForHour(hour)}.",
                statusLine = "Workspace healthy · $running running · ${apps.size} imported"
            )
        }
        item {
            Row(
                Modifier.padding(horizontal = KuroSpacing.s20).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(KuroSpacing.s12)
            ) {
                MiniStat(
                    value = "$running",
                    label = "Running",
                    dot = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                MiniStat(
                    value = "${apps.size}",
                    label = "Imported",
                    dot = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MiniStat(
                    value = "2.1G",
                    label = "Storage",
                    dot = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            KuroHeroCard(Modifier.padding(horizontal = KuroSpacing.s20)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(10.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary)
                    )
                    Text(
                        "  Workspace health",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.weight(1f))
                    StatusIndicator(AppStatus.RUNNING, pulsing = true)
                }
                Spacer(Modifier.height(KuroSpacing.s12))
                KuroMeter(label = "Memory", valueLabel = "612 MB / 2 GB", fraction = 0.31f)
                Spacer(Modifier.height(KuroSpacing.s8))
                KuroMeter(label = "Storage", valueLabel = "2.1 GB / 8 GB", fraction = 0.27f)
                Spacer(Modifier.height(KuroSpacing.s12))
                Text(
                    "$running environments active · storage saver on",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            SectionHeader("Recent Apps", "See all", onAction = { onSeeAll() })
        }
        items(apps.take(3), key = { it.id }) { app ->
            AppCard(
                app = app,
                onLaunch = {},
                onOpen = { onOpenApp(app.id) },
                onLongPress = { onOpenApp(app.id) },
                modifier = Modifier.padding(horizontal = KuroSpacing.s20).animateItem()
            )
        }
        item { SectionHeader("Pinned") }
        items(apps.filter { it.pinned }, key = { it.id }) { app ->
            AppCard(
                app = app,
                onLaunch = {},
                onOpen = { onOpenApp(app.id) },
                onLongPress = { onOpenApp(app.id) },
                modifier = Modifier.padding(horizontal = KuroSpacing.s20).animateItem()
            )
        }
        item { SectionHeader("Recommended") }
        item {
            Card(
                onClick = onImport,
                modifier = Modifier.padding(horizontal = KuroSpacing.s20).fillMaxWidth(),
                shape = KuroCardShape,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )
            ) {
                Row(
                    Modifier.padding(KuroSpacing.s20),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(Modifier.width(KuroSpacing.s12))
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
                    Text("  Storage saver is on", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun MiniStat(
    value: String,
    label: String,
    dot: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    KuroCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
            Text(
                "  $value",
                style = MaterialTheme.typography.titleLarge
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
