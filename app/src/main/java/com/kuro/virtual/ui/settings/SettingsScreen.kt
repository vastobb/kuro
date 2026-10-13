package com.kuro.virtual.ui.settings

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kuro.core.ProfileManager
import com.kuro.core.VirtualCore
import com.kuro.core.VirtualStorage
import com.kuro.virtual.BuildConfig
import com.kuro.virtual.KuroDeviceAdmin
import com.kuro.virtual.data.VaultStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var vaultBytes by remember { mutableStateOf(-1L) }
    var confirmClear by remember { mutableStateOf(false) }
    var confirmRemoveProfile by remember { mutableStateOf(false) }
    var profileState by remember { mutableStateOf("…") }
    var profileNote by remember { mutableStateOf<String?>(null) }

    fun refreshProfile() {
        profileState = when {
            ProfileManager.isProfileOwner(context) -> "Active — Kuro manages the work profile"
            ProfileManager.workProfile(context) != null -> "Present (managed by another app)"
            else -> "Not set up"
        }
    }

    LaunchedEffect(Unit) {
        vaultBytes = withContext(Dispatchers.IO) {
            try {
                VirtualStorage.vaultSizeBytes()
            } catch (_: Exception) {
                -1L
            }
        }
        refreshProfile()
    }

    val provisionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        refreshProfile()
        profileNote = "Returned from setup. Clones are made from each vault entry."
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp),
        )
        Spacer(Modifier.height(10.dp))

        SettingsCard(title = "Work profile clones") {
            SettingRow("Status", profileState)
            profileNote?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(6.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val intent = ProfileManager.provisioningIntent(context, KuroDeviceAdmin.component(context))
                        if (intent == null) {
                            profileNote = "Work-profile setup is not available on this device."
                        } else {
                            try {
                                provisionLauncher.launch(intent)
                            } catch (_: Exception) {
                                profileNote = "Could not start setup on this device."
                            }
                        }
                    },
                    enabled = profileState == "Not set up",
                ) { Text("Set up") }
                OutlinedButton(
                    onClick = { confirmRemoveProfile = true },
                    enabled = profileState.startsWith("Active"),
                ) { Text("Remove") }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Each clone is a separate instance with its own data. Tapping a plain vault " +
                    "entry still opens the device copy — make its clone first.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SettingsCard(title = "This device") {
            SettingRow("App", "Kuro ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) · dev.kuro.reu")
            SettingRow("Android", "API ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})")
            SettingRow("CPU", if (VirtualCore.is64BitDevice) "64-bit" else "32-bit")
            SettingRow("ABIs", VirtualCore.deviceAbis().joinToString(", "))
            SettingRow("Vault storage", formatBytes(vaultBytes))
            SettingRow("Theme", "Follows system")
        }

        SettingsCard(title = "Danger zone") {
            Button(
                onClick = { confirmClear = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) { Text("Clear vault") }
            Spacer(Modifier.height(4.dp))
            Text(
                "Removes every vault entry, staged APK and per-app vault data.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(24.dp))
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear vault?") },
            text = { Text("Removes every vault entry, staged APK and per-app vault data.") },
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

    if (confirmRemoveProfile) {
        AlertDialog(
            onDismissRequest = { confirmRemoveProfile = false },
            title = { Text("Remove work profile?") },
            text = { Text("Kuro stops managing the profile. Clones inside it go away with it.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmRemoveProfile = false
                        scope.launch(Dispatchers.IO) {
                            val ok = ProfileManager.clearProfileOwner(
                                context,
                                KuroDeviceAdmin.component(context),
                            )
                            withContext(Dispatchers.Main) {
                                profileNote = if (ok) "Profile ownership removed." else "Could not remove it."
                                refreshProfile()
                            }
                        }
                    },
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { confirmRemoveProfile = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            content()
        }
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
    Spacer(Modifier.height(6.dp))
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
