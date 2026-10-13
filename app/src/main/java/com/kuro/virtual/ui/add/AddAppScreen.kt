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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
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

/**
 * Clone lab: bring apps into the vault. Cloning itself happens from each
 * vault entry's sheet, where the instance type is explicit.
 */
@Composable
fun AddAppScreen(
    modifier: Modifier = Modifier,
    snackbar: SnackbarHostState,
    onVaultChanged: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { VaultRepository(context.applicationContext) }

    var loading by remember { mutableStateOf(true) }
    var apps by remember { mutableStateOf<List<VirtualPackage>>(emptyList()) }
    var query by rememberSaveable { mutableStateOf("") }
    var owned by remember { mutableStateOf(VaultStore.load(context).map { it.packageName }.toSet()) }
    var busyApk by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        apps = repo.installedLaunchable()
        loading = false
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            busyApk = true
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
                owned = current.map { it.packageName }.toSet()
                onVaultChanged()
                snackbar.showSnackbar("Imported ${pkg.appLabel}")
            } catch (e: Exception) {
                snackbar.showSnackbar("Import failed: ${e.message}")
            } finally {
                busyApk = false
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            "Clone lab",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp),
        )
        Text(
            "Add apps first. Open an entry in the Vault to install it or make its clone.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("APK file", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Imports are ABI-checked for 64-bit before they enter the vault.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(10.dp))
                if (busyApk) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                } else {
                    Button(onClick = { picker.launch(arrayOf("application/vnd.android.package-archive")) }) {
                        Text("Choose APK")
                    }
                }
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search installed apps") },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
        )

        TextButton(onClick = onOpenSettings) { Text("Clones need a work profile — open settings") }

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
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(shown, key = { it.packageName }) { app ->
                    val added = app.packageName in owned
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(15.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = app.appLabel.firstOrNull()?.uppercase() ?: "?",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.titleMedium,
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
                                            snackbar.showSnackbar("Added ${app.appLabel} — open it in the Vault to clone")
                                        }
                                    }
                                },
                            ) { Text(if (added) "Added" else "Add") }
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}
