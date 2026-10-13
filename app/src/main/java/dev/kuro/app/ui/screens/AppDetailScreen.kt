package dev.kuro.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.kuro.app.data.PreviewData
import dev.kuro.app.ui.components.EnvironmentBadge
import dev.kuro.app.ui.components.KuroCard
import dev.kuro.app.ui.components.KuroMeter
import dev.kuro.app.ui.components.KuroPrimaryButton
import dev.kuro.app.ui.components.KuroSecondaryButton
import dev.kuro.app.ui.components.StatusIndicator
import dev.kuro.app.ui.components.VersionBadge
import dev.kuro.app.ui.theme.KuroSpacing

/**
 * App detail — large header, icon/name/version/env, storage graph,
 * permissions, runtime status, launch/clone/settings, animated stats.
 */
@Composable
fun AppDetailScreen(appId: String, onBack: () -> Unit) {
    val app = PreviewData.apps.firstOrNull { it.id == appId } ?: PreviewData.apps.first()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(top = KuroSpacing.s20, bottom = KuroSpacing.s32)
    ) {
        // Header
        Column(Modifier.padding(horizontal = KuroSpacing.s20)) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(app.iconTint).copy(alpha = 0.16f)
                )
            ) {
                Icon(
                    Icons.Filled.Apps,
                    contentDescription = null,
                    tint = Color(app.iconTint),
                    modifier = Modifier.size(84.dp).padding(20.dp)
                )
            }
            Spacer(Modifier.height(KuroSpacing.s16))
            Text(
                app.name,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                app.packageName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(KuroSpacing.s8))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EnvironmentBadge(app.environment)
                VersionBadge("v${app.version}")
            }
            Spacer(Modifier.height(KuroSpacing.s8))
            StatusIndicator(app.status, pulsing = true)
        }
        Spacer(Modifier.height(KuroSpacing.s16))
        KuroCard(Modifier.padding(horizontal = KuroSpacing.s20).fillMaxWidth()) {
            Text("Storage", style = MaterialTheme.typography.titleMedium)
            KuroMeter("App data", "${app.storageMb} MB", app.storageMb / 2048f)
            Spacer(Modifier.height(KuroSpacing.s8))
            KuroMeter("Cache", "38 MB", 0.04f)
        }
        Spacer(Modifier.height(KuroSpacing.s12))
        KuroCard(Modifier.padding(horizontal = KuroSpacing.s20).fillMaxWidth()) {
            Text("Permissions", style = MaterialTheme.typography.titleMedium)
            Text("Camera · Location · Storage (sandboxed)", style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.height(KuroSpacing.s12))
        KuroCard(Modifier.padding(horizontal = KuroSpacing.s20).fillMaxWidth()) {
            Text("Runtime", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("CPU ${app.cpuPct}% · MEM ${app.memMb} MB", Modifier.weight(1f))
                StatusIndicator(app.status)
            }
        }
        Spacer(Modifier.height(KuroSpacing.s20))
        Row(
            Modifier.padding(horizontal = KuroSpacing.s20).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KuroSpacing.s12)
        ) {
            KuroPrimaryButton("Launch", {}, Modifier.weight(1f))
            KuroSecondaryButton("Clone", {}, Modifier.weight(1f))
        }
        Spacer(Modifier.height(KuroSpacing.s12))
        Row(
            Modifier.padding(horizontal = KuroSpacing.s20).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KuroSpacing.s12)
        ) {
            androidx.compose.material3.OutlinedButton(
                onClick = {},
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Filled.Settings, contentDescription = null)
                Text("  Settings")
            }
            androidx.compose.material3.OutlinedButton(
                onClick = {},
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null)
                Text("  Export")
            }
        }
        Spacer(Modifier.height(KuroSpacing.s12))
        KuroSecondaryButton("Back", onBack, Modifier.padding(horizontal = KuroSpacing.s20).fillMaxWidth())
    }
}
