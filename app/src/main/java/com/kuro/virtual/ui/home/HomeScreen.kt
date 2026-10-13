package com.kuro.virtual.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kuro.virtual.data.VaultEntry
import com.kuro.virtual.data.VaultRepository
import com.kuro.virtual.data.VaultStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    snackbar: SnackbarHostState,
    vaultTick: Int,
    onAdd: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { VaultRepository(context.applicationContext) }

    var entries by remember(vaultTick) { mutableStateOf(VaultStore.load(context)) }
    var query by rememberSaveable { mutableStateOf("") }
    var menuEntry by remember { mutableStateOf<VaultEntry?>(null) }

    fun reload() {
        entries = VaultStore.load(context)
    }

    val shown = entries.filter {
        query.isBlank() ||
            it.label.contains(query, ignoreCase = true) ||
            it.packageName.contains(query, ignoreCase = true)
    }

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search vault") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        if (shown.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Filled.Apps,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                Text("Vault is empty", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Add an installed app or import an APK file to run it here in isolation.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onAdd) { Text("Add app") }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(shown, key = { it.packageName }) { entry ->
                    VaultCard(
                        entry = entry,
                        onLaunch = {
                            scope.launch {
                                val res = repo.launch(entry.packageName)
                                if (!res.ok) {
                                    snackbar.showSnackbar(res.reason ?: "Launch failed")
                                }
                            }
                        },
                        onMenu = { menuEntry = entry },
                    )
                }
            }
        }
    }

    menuEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { menuEntry = null },
            title = { Text(entry.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            text = { Text(entry.packageName) },
            confirmButton = {
                TextButton(
                    onClick = {
                        menuEntry = null
                        scope.launch {
                            val res = repo.launch(entry.packageName)
                            if (!res.ok) snackbar.showSnackbar(res.reason ?: "Launch failed")
                        }
                    },
                ) { Text("Launch") }
            },
            dismissButton = {
                Column(horizontalAlignment = Alignment.End) {
                    TextButton(
                        onClick = {
                            menuEntry = null
                            scope.launch {
                                val ok = repo.clearAppData(entry.packageName)
                                snackbar.showSnackbar(if (ok) "Vault data cleared" else "Nothing to clear")
                            }
                        },
                    ) { Text("Clear data") }
                    TextButton(
                        onClick = {
                            val updated = VaultStore.load(context)
                                .filterNot { it.packageName == entry.packageName }
                            VaultStore.save(context, updated)
                            menuEntry = null
                            reload()
                            scope.launch { snackbar.showSnackbar("Removed from vault") }
                        },
                    ) { Text("Remove") }
                }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VaultCard(
    entry: VaultEntry,
    onLaunch: () -> Unit,
    onMenu: () -> Unit,
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.combinedClickable(onClick = onLaunch, onLongClick = onMenu),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = entry.label.firstOrNull()?.uppercase() ?: "?",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                entry.label,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (entry.fromApkFile) "APK" else "Installed",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}
