package dev.kuro.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.kuro.app.logging.KuroLog
import dev.kuro.app.logging.LogFiles
import dev.kuro.app.ui.components.KuroCard
import dev.kuro.app.ui.components.KuroScreenTitle
import dev.kuro.app.ui.theme.KuroSpacing
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Hidden debug screen (5 taps on Settings → About): view / copy / share /
 * export session logs and crash reports. All file IO stays off the UI thread.
 */
@Composable
fun DebugLogsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var files by remember { mutableStateOf<List<File>>(emptyList()) }
    var selected by remember { mutableStateOf<File?>(null) }
    var content by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    fun refresh() {
        scope.launch {
            val list = withContext(Dispatchers.IO) { LogFiles.listAll(context) }
            files = list
            val sel = selected
            if (sel != null && list.none { it.absolutePath == sel.absolutePath }) {
                selected = null
                content = ""
            }
        }
    }

    LaunchedEffect(Unit) {
        KuroLog.event("SCREEN", "DebugLogs")
        refresh()
    }
    LaunchedEffect(selected) {
        val sel = selected
        content = if (sel == null) "" else withContext(Dispatchers.IO) { LogFiles.readCapped(sel) }
    }

    val crashes = files.filter { it.name.startsWith("crash_") }
    val sessions = files.filter { it.name.startsWith("kuro_") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = KuroSpacing.s20, bottom = KuroSpacing.s32),
        verticalArrangement = Arrangement.spacedBy(KuroSpacing.s12)
    ) {
        item {
            Row(
                Modifier.padding(horizontal = KuroSpacing.s20).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { refresh() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                }
            }
            KuroScreenTitle("Debug Logs", "${files.size} files", eyebrow = "Diagnostics")
        }
        if (status.isNotEmpty()) {
            item {
                KuroCard(Modifier.padding(horizontal = KuroSpacing.s20).fillMaxWidth()) {
                    Text(status, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        if (crashes.isNotEmpty()) {
            item {
                Text(
                    "CRASH REPORTS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = KuroSpacing.s20)
                )
            }
            items(crashes, key = { it.absolutePath }) { f ->
                LogRow(f, selected == f, onSelect = { selected = f })
            }
        }
        if (sessions.isNotEmpty()) {
            item {
                Text(
                    "SESSION LOGS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = KuroSpacing.s20)
                )
            }
            items(sessions, key = { it.absolutePath }) { f ->
                LogRow(f, selected == f, onSelect = { selected = f })
            }
        }
        if (files.isEmpty()) {
            item {
                KuroCard(Modifier.padding(horizontal = KuroSpacing.s20).fillMaxWidth()) {
                    Text("No log files yet.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        val sel = selected
        if (sel != null) {
            item {
                KuroCard(Modifier.padding(horizontal = KuroSpacing.s20).fillMaxWidth()) {
                    Text(sel.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${sel.length() / 1024} KB",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(KuroSpacing.s8))
                    Row(horizontalArrangement = Arrangement.spacedBy(KuroSpacing.s8)) {
                        ViewerAction(Icons.Filled.ContentCopy, "Copy") {
                            scope.launch {
                                val n = withContext(Dispatchers.IO) {
                                    LogFiles.copyToClipboard(context, sel)
                                }
                                status = "Copied $n chars."
                            }
                        }
                        ViewerAction(Icons.Filled.Share, "Share") {
                            status = runCatching {
                                LogFiles.shareFile(context, sel)
                                "Sharing ${sel.name}…"
                            }.getOrElse { "Share failed: ${it.message}" }
                        }
                        ViewerAction(Icons.Filled.Description, "Export") {
                            scope.launch {
                                status = runCatching {
                                    val dest = withContext(Dispatchers.IO) {
                                        LogFiles.exportToDocuments(context, sel)
                                    }
                                    "Exported to ${dest.absolutePath}"
                                }.getOrElse { "Export failed: ${it.message}" }
                            }
                        }
                        ViewerAction(Icons.Filled.Delete, "Delete") {
                            scope.launch {
                                withContext(Dispatchers.IO) { LogFiles.delete(sel) }
                                selected = null
                                content = ""
                                status = "Deleted ${sel.name}."
                                refresh()
                            }
                        }
                    }
                    Spacer(Modifier.height(KuroSpacing.s8))
                    SelectionContainer {
                        Text(
                            content,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogRow(file: File, isSelected: Boolean, onSelect: () -> Unit) {
    KuroCard(
        modifier = Modifier.padding(horizontal = KuroSpacing.s20).fillMaxWidth(),
        onClick = onSelect
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Description,
                contentDescription = null,
                tint = if (file.name.startsWith("crash_")) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
            Column(Modifier.weight(1f).padding(start = KuroSpacing.s12)) {
                Text(file.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${file.length() / 1024} KB" + if (isSelected) " · open" else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ViewerAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = label)
        }
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
