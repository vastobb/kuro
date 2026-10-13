package com.kuro.virtual.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kuro.core.ProfileManager
import com.kuro.virtual.KuroDeviceAdmin
import com.kuro.virtual.data.VaultEntry
import com.kuro.virtual.data.VaultRepository
import com.kuro.virtual.data.VaultStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Vault dashboard. Tapping an entry opens its action sheet — nothing launches
 * an original app without an explicit choice. Clones are the primary action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    snackbar: SnackbarHostState,
    vaultTick: Int,
    onOpenLab: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { VaultRepository(context.applicationContext) }

    var entries by remember(vaultTick) { mutableStateOf(VaultStore.load(context)) }
    var query by rememberSaveable { mutableStateOf("") }
    var sheetEntry by remember { mutableStateOf<VaultEntry?>(null) }

    fun reload() {
        entries = VaultStore.load(context)
    }

    fun persist(updated: VaultEntry) {
        VaultStore.save(
            context,
            VaultStore.load(context).map { if (it.packageName == updated.packageName) updated else it },
        )
        reload()
        if (sheetEntry?.packageName == updated.packageName) sheetEntry = updated
    }

    val shown = entries.filter {
        query.isBlank() ||
            it.label.contains(query, ignoreCase = true) ||
            it.packageName.contains(query, ignoreCase = true)
    }
    val clones = shown.filter { it.inProfile }
    val device = shown.filter { !it.inProfile }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            "Vault",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp),
        )
        Text(
            "${entries.size} apps · ${clones.size} clones",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search") },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        )
        if (shown.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Nothing here yet", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Add apps in the Clone lab, then make separate clones that open from here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onOpenLab) { Text("Open Clone lab") }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (clones.isNotEmpty()) {
                    item { SectionTitle("Clones — separate instances") }
                    items(clones, key = { it.packageName }) { entry ->
                        VaultRow(entry = entry, onTap = { sheetEntry = entry })
                    }
                }
                if (device.isNotEmpty()) {
                    item { SectionTitle("On this device") }
                    items(device, key = { it.packageName }) { entry ->
                        VaultRow(entry = entry, onTap = { sheetEntry = entry })
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }

    sheetEntry?.let { entry ->
        AppSheet(
            entry = entry,
            snackbar = snackbar,
            onUpdate = { persist(it) },
            onDismiss = { sheetEntry = null },
            onOpenSettings = {
                sheetEntry = null
                onOpenSettings()
            },
            onRemoved = {
                sheetEntry = null
                reload()
            },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 20.dp, top = 8.dp, end = 20.dp),
    )
}

@Composable
private fun VaultRow(entry: VaultEntry, onTap: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onTap),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = entry.label.firstOrNull()?.uppercase() ?: "?",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    entry.label,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    entry.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            InstancePill(clone = entry.inProfile)
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InstancePill(clone: Boolean) {
    Surface(
        color = if (clone) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        shape = RoundedCornerShape(50),
    ) {
        Text(
            if (clone) "Clone" else "Device",
            style = MaterialTheme.typography.labelSmall,
            color = if (clone) {
                MaterialTheme.colorScheme.onSecondary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppSheet(
    entry: VaultEntry,
    snackbar: SnackbarHostState,
    onUpdate: (VaultEntry) -> Unit,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    onRemoved: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { VaultRepository(context.applicationContext) }
    val sheetState = rememberModalBottomSheetState()

    var current by remember(entry) { mutableStateOf(entry) }
    var installed by remember(entry) { mutableStateOf<Boolean?>(null) }
    var profileReady by remember(entry) { mutableStateOf(false) }
    var working by remember { mutableStateOf<String?>(null) }

    val provisionLauncher = rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
    ) {
        scope.launch(Dispatchers.IO) {
            val ready = ProfileManager.workProfile(context) != null
            withContext(Dispatchers.Main) { profileReady = ready }
        }
    }

    LaunchedEffect(entry) {
        val outcome = withContext(Dispatchers.IO) {
            Triple(
                repo.isInstalled(entry.packageName),
                ProfileManager.workProfile(context) != null,
                repo.refreshCloneState(entry),
            )
        }
        installed = outcome.first
        profileReady = outcome.second
        if (outcome.third != current) {
            current = outcome.third
            onUpdate(outcome.third)
        }
    }

    fun runJob(label: String, job: suspend () -> String) {
        scope.launch {
            working = label
            try {
                val message = withContext(Dispatchers.IO) { job() }
                val refreshed = withContext(Dispatchers.IO) { repo.refreshCloneState(current) }
                if (refreshed != current) {
                    current = refreshed
                    onUpdate(refreshed)
                }
                snackbar.showSnackbar(message)
            } catch (e: Exception) {
                snackbar.showSnackbar(e.message ?: "Failed")
            } finally {
                working = null
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = current.label.firstOrNull()?.uppercase() ?: "?",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        current.label,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        current.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                InstancePill(clone = current.inProfile)
            }

            Spacer(Modifier.height(8.dp))
            Text(
                if (current.inProfile) {
                    "Opens the separate clone instance with its own data."
                } else {
                    "No clone yet — opening now runs the device copy."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))

            if (working != null) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                Text(working ?: "", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }

            val busy = working != null
            when {
                current.inProfile -> {
                    SheetPrimary(
                        text = "Open clone",
                        enabled = !busy,
                        onClick = {
                            scope.launch {
                                val res = repo.launch(current)
                                if (!res.ok) snackbar.showSnackbar(res.reason ?: "Launch failed")
                            }
                        },
                    )
                }
                current.fromApkFile && installed == false -> {
                    SheetPrimary(
                        text = "Install from vault",
                        enabled = !busy,
                        onClick = {
                            runJob("Installing…") {
                                repo.installVaultApk(current).fold(
                                    { "Installed. Open it from here." },
                                    { throw IllegalStateException(it.message) },
                                )
                            }
                        },
                    )
                }
                installed == true && profileReady -> {
                    SheetPrimary(
                        text = "Make clone",
                        enabled = !busy,
                        onClick = {
                            runJob("Cloning…") {
                                repo.cloneToProfile(current).fold(
                                    { it },
                                    { throw IllegalStateException(it.message) },
                                )
                            }
                        },
                    )
                }
                installed == true && !profileReady -> {
                    SheetPrimary(
                        text = "Set up work profile",
                        enabled = !busy,
                        onClick = {
                            val intent = ProfileManager.provisioningIntent(
                                context,
                                KuroDeviceAdmin.component(context),
                            )
                            if (intent == null) {
                                scope.launch { snackbar.showSnackbar("Work-profile setup unavailable on this device") }
                            } else {
                                try {
                                    provisionLauncher.launch(intent)
                                } catch (_: Exception) {
                                    scope.launch { snackbar.showSnackbar("Could not start setup") }
                                }
                            }
                        },
                    )
                    Text(
                        "Clones need a work profile (one-time setup). The clone gets its own data.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(6.dp))
                }
                else -> {
                    Text(
                        "This app is no longer on the device and no vault APK exists.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider()
            if (installed == true && !current.inProfile) {
                SheetRow(text = "Open device copy", enabled = !busy, onClick = {
                    scope.launch {
                        val res = repo.launch(current)
                        if (!res.ok) snackbar.showSnackbar(res.reason ?: "Launch failed")
                    }
                })
            }
            if (current.inProfile) {
                SheetRow(text = "Uninstall clone", enabled = !busy, onClick = {
                    runJob("Removing clone…") {
                        repo.uninstallClone(current).fold({ it }, { throw IllegalStateException(it.message) })
                    }
                })
            }
            SheetRow(text = "Clear vault data", enabled = !busy, onClick = {
                scope.launch {
                    val ok = withContext(Dispatchers.IO) { repo.clearAppData(current.packageName) }
                    snackbar.showSnackbar(if (ok) "Vault data cleared" else "Nothing to clear")
                }
            })
            SheetRow(text = "Remove from vault", enabled = !busy, onClick = {
                VaultStore.save(
                    context,
                    VaultStore.load(context).filterNot { it.packageName == current.packageName },
                )
                onRemoved()
            })
            SheetRow(text = "Work profile settings", enabled = !busy, onClick = onOpenSettings)
        }
    }
}

@Composable
private fun SheetPrimary(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
    ) {
        Text(text, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun SheetRow(text: String, enabled: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
