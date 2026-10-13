package dev.kuro.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import dev.kuro.app.logging.KuroLog
import dev.kuro.app.ui.components.KuroCard
import dev.kuro.app.ui.components.KuroScreenTitle
import dev.kuro.app.ui.theme.KuroSpacing

private data class SettingRow(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val hasSwitch: Boolean = false
)

/**
 * Settings — grouped cards. No endless boring lists.
 * Sections: Appearance / Virtualization / Performance / Storage /
 * Security / Developer / Backup / About.
 */
@Composable
fun SettingsScreen(
    darkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onOpenDebug: () -> Unit = {}
) {
    var perfMode by remember { mutableStateOf(true) }
    var saver by remember { mutableStateOf(true) }
    var aboutTaps by remember { mutableStateOf(0) }

    val groups: List<Pair<String, List<SettingRow>>> = listOf(
        "Appearance" to listOf(
            SettingRow(Icons.Filled.Palette, "Theme", "AMOLED dark · light designed", hasSwitch = true)
        ),
        "Virtualization" to listOf(
            SettingRow(Icons.Filled.PhoneAndroid, "Default environment", "Android 14 · isolated"),
            SettingRow(Icons.Filled.Security, "Isolation level", "Storage + permissions")
        ),
        "Performance" to listOf(
            SettingRow(Icons.Filled.Memory, "Performance mode", "Prefer 60fps springs", hasSwitch = true)
        ),
        "Storage" to listOf(
            SettingRow(Icons.Filled.Storage, "Vault usage", "2.1 GB · saver on", hasSwitch = true)
        ),
        "Security" to listOf(
            SettingRow(Icons.Filled.Security, "App lock", "Biometric for Vault")
        ),
        "Developer" to listOf(
            SettingRow(Icons.Filled.DeveloperMode, "Developer options", "Logs · reduced motion")
        ),
        "Backup" to listOf(
            SettingRow(Icons.Filled.Backup, "Backup", "Export vault index")
        ),
        "About" to listOf(
            SettingRow(Icons.Filled.Info, "Kuro 0.1.0", "UI foundation milestone")
        )
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = KuroSpacing.s20, bottom = KuroSpacing.s32),
        verticalArrangement = Arrangement.spacedBy(KuroSpacing.s16)
    ) {
        item { KuroScreenTitle("Settings", "Tuned for calm and speed", eyebrow = "Preferences") }
        groups.forEach { (title, rows) ->
            item {
                Column(Modifier.padding(horizontal = KuroSpacing.s20)) {
                    Text(
                        title.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = KuroSpacing.s8)
                    )
                    KuroCard(
                        Modifier.fillMaxWidth().then(
                            if (title == "About") {
                                Modifier.clickable {
                                    aboutTaps++
                                    if (aboutTaps >= 5) {
                                        aboutTaps = 0
                                        KuroLog.event("DEBUG_OPEN", "via About taps")
                                        onOpenDebug()
                                    }
                                }
                            } else Modifier
                        )
                    ) {
                        rows.forEach { row ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = KuroSpacing.s8)
                            ) {
                                Icon(
                                    row.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column(Modifier.weight(1f).padding(start = KuroSpacing.s12)) {
                                    Text(row.title, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        row.subtitle,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                when {
                                    row.title == "Theme" -> Switch(
                                        checked = darkTheme,
                                        onCheckedChange = onToggleTheme
                                    )
                                    row.title == "Performance mode" -> Switch(
                                        checked = perfMode,
                                        onCheckedChange = {
                                            perfMode = it
                                            KuroLog.settingsChange("perfMode", it.toString())
                                        }
                                    )
                                    row.title == "Vault usage" -> Switch(
                                        checked = saver,
                                        onCheckedChange = {
                                            saver = it
                                            KuroLog.settingsChange("storageSaver", it.toString())
                                        }
                                    )
                                    else -> Icon(Icons.Filled.ChevronRight, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
