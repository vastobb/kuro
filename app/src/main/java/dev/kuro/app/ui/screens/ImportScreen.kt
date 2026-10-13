package dev.kuro.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.kuro.app.ui.components.KuroHeroCard
import dev.kuro.app.ui.components.KuroScreenTitle
import dev.kuro.app.ui.theme.KuroHeroShape
import dev.kuro.app.ui.theme.KuroMotion
import dev.kuro.app.ui.theme.KuroSpacing

/**
 * Import — hero + two large floating cards (APK / Installed App)
 * with duotone icon tiles and press physics.
 */
@Composable
fun ImportScreen(
    onImportApk: () -> Unit,
    onImportInstalled: () -> Unit
) {
    var pressed by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(top = KuroSpacing.s24, bottom = KuroSpacing.s32)
    ) {
        KuroScreenTitle(
            title = "Import Applications",
            subtitle = "Bring any APK or installed app into an isolated workspace.",
            eyebrow = "Vault intake"
        )
        Spacer(Modifier.height(KuroSpacing.s20))
        KuroHeroCard(Modifier.padding(horizontal = KuroSpacing.s20).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.PhoneAndroid,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                Column(Modifier.weight(1f).padding(start = KuroSpacing.s12)) {
                    Text("Isolated by default", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Storage, permissions and runtime stay sandboxed.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(KuroSpacing.s16))
        ImportHeroCard(
            title = "Import APK",
            subtitle = "Pick a file · analyze · configure",
            pressed = pressed == "apk",
            onPress = { pressed = "apk"; onImportApk() },
            icon = Icons.Filled.Add,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(KuroSpacing.s12))
        ImportHeroCard(
            title = "Import Installed App",
            subtitle = "Clone from this device safely",
            pressed = pressed == "installed",
            onPress = { pressed = "installed"; onImportInstalled() },
            icon = Icons.Filled.PhoneAndroid,
            tint = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun ImportHeroCard(
    title: String,
    subtitle: String,
    pressed: Boolean,
    onPress: () -> Unit,
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color
) {
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = KuroMotion.springy(),
        label = "importPress"
    )
    Card(
        onClick = onPress,
        modifier = Modifier.padding(horizontal = KuroSpacing.s20).fillMaxWidth().scale(scale),
        shape = KuroHeroShape,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = if (pressed) 2.dp else 8.dp)
    ) {
        Row(
            Modifier.padding(KuroSpacing.s20),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KuroSpacing.s16)
        ) {
            Box(
                Modifier.size(56.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    .background(tint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null)
        }
    }
}
