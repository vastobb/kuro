package com.kuro.virtual.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kuro.core.VirtualCore
import com.kuro.core.VirtualStorage
import com.kuro.virtual.BuildConfig
import com.kuro.virtual.data.VaultStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var vaultBytes by remember { mutableStateOf(-1L) }
    var confirmClear by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vaultBytes = withContext(Dispatchers.IO) {
            try {
                VirtualStorage.vaultSizeBytes()
            } catch (_: Exception) {
                -1L
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        SettingRow("App", "Kuro ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        SettingRow("Android", "API ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})")
        SettingRow(
            "Device",
            if (VirtualCore.is64BitDevice) "64-bit" else "32-bit",
        )
        SettingRow("ABIs", VirtualCore.deviceAbis().joinToString(", "))
        SettingRow("Vault size", formatBytes(vaultBytes))
        SettingRow("Theme", "Follows system (white / black + vermilion)")
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { confirmClear = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) { Text("Clear vault") }
        Spacer(Modifier.height(8.dp))
        Text(
            "Phase 1: managed vault with isolated task stacks and per-app storage. " +
                "Install-free engine execution is on the roadmap, not claimed yet.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear vault?") },
            text = { Text("Removes every vault entry, imported APK and per-app data.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClear = false
                        try {
                            VirtualCore.vaultDir().deleteRecursively()
                            VirtualCore.apkDir().mkdirs()
                        } catch (_: Exception) {
                        }
                        VaultStore.save(context, emptyList())
                        vaultBytes = 0L
                    },
                ) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SettingRow(title: String, value: String) {
    Text(title, style = MaterialTheme.typography.labelLarge)
    Text(
        value,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(4.dp))
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 0) return "…"
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format(Locale.US, "%.1f MB", mb)
    return String.format(Locale.US, "%.2f GB", mb / 1024.0)
}
