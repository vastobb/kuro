package com.kuro.virtual.ui.add

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.kuro.core.VirtualPackage
import com.kuro.virtual.data.VaultEntry
import com.kuro.virtual.data.VaultRepository
import com.kuro.virtual.data.VaultStore
import kotlinx.coroutines.launch

@Composable
fun AddAppScreen(
    modifier: Modifier = Modifier,
    snackbar: SnackbarHostState,
    onVaultChanged: () -> Unit,
) {
    var tab by remember { mutableStateOf(0) }
    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Installed") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("APK file") })
        }
        if (tab == 0) {
            InstalledTab(snackbar = snackbar, onVaultChanged = onVaultChanged)
        } else {
            ApkTab(snackbar = snackbar, onVaultChanged = onVaultChanged)
        }
    }
}

@Composable
private fun InstalledTab(
    snackbar: SnackbarHostState,
    onVaultChanged: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { VaultRepository(context.applicationContext) }

    var loading by remember { mutableStateOf(true) }
    var apps by remember { mutableStateOf<List<VirtualPackage>>(emptyList()) }
    var query by rememberSaveable { mutableStateOf("") }
    var owned by remember { mutableStateOf(VaultStore.load(context).map { it.packageName }.toSet()) }

    LaunchedEffect(Unit) {
        apps = repo.installedLaunchable()
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search installed apps") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val shown = apps.filter {
                query.isBlank() ||
                    it.appLabel.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(shown, key = { it.packageName }) { app ->
                    val added = app.packageName in owned
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = app.appLabel.firstOrNull()?.uppercase() ?: "?",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                app.appLabel,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                app.packageName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        TextButton(
                            enabled = !added,
                            onClick = {
                                scope.launch {
                                    val current = VaultStore.load(context).toMutableList()
                                    if (current.none { it.packageName == app.packageName }) {
                                        current += VaultEntry(
                                            packageName = app.packageName,
                                            label = app.appLabel,
                                            apkPath = app.apkPath,
                                            fromApkFile = false,
                                        )
                                        VaultStore.save(context, current)
                                        owned = current.map { it.packageName }.toSet()
                                        onVaultChanged()
                                        snackbar.showSnackbar("Added ${app.appLabel}")
                                    }
                                }
                            },
                        ) { Text(if (added) "Added" else "Add") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApkTab(
    snackbar: SnackbarHostState,
    onVaultChanged: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { VaultRepository(context.applicationContext) }

    var busy by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            busy = true
            try {
                val pkg = repo.importApk(uri).getOrThrow()
                val current = VaultStore.load(context).toMutableList()
                current.removeAll { it.packageName == pkg.packageName }
                current += VaultEntry(
                    packageName = pkg.packageName,
                    label = pkg.appLabel,
                    apkPath = pkg.apkPath,
                    fromApkFile = true,
                )
                VaultStore.save(context, current)
                onVaultChanged()
                note = "Imported ${pkg.appLabel} (${pkg.versionName ?: "?"})"
                snackbar.showSnackbar("Imported ${pkg.appLabel}")
            } catch (e: Exception) {
                note = "Import failed: ${e.message}"
                snackbar.showSnackbar("Import failed: ${e.message}")
            } finally {
                busy = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(Modifier.height(24.dp))
        Button(
            enabled = !busy,
            onClick = { picker.launch(arrayOf("application/vnd.android.package-archive")) },
        ) { Text("Choose APK") }
        Spacer(Modifier.height(16.dp))
        if (busy) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
        }
        note?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        Spacer(Modifier.height(16.dp))
        Text(
            "APKs are copied into the vault and ABI-checked: packages without " +
                "64-bit native libraries for this device are rejected.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
