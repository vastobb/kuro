package dev.kuro.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.kuro.app.data.AppCategory
import dev.kuro.app.data.AppStatus
import dev.kuro.app.data.PreviewData
import dev.kuro.app.ui.components.AppCard
import dev.kuro.app.ui.components.KuroEmptyState
import dev.kuro.app.ui.components.KuroFilterChip
import dev.kuro.app.ui.components.KuroScreenTitle
import dev.kuro.app.ui.components.KuroSearch
import dev.kuro.app.ui.components.KuroSheet
import dev.kuro.app.ui.theme.KuroSpacing

/**
 * Vault — all virtualized apps. Filter chips + animated search + realtime sorting.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VaultScreen(
    onOpenApp: (String) -> Unit,
    onImport: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<AppStatus?>(null) }
    var categoryFilter by remember { mutableStateOf<AppCategory?>(null) }
    var sheetAppId by remember { mutableStateOf<String?>(null) }

    val filtered = PreviewData.apps
        .filter { query.isBlank() || it.name.contains(query, true) || it.packageName.contains(query, true) }
        .filter { statusFilter == null || it.status == statusFilter }
        .filter { categoryFilter == null || it.category == categoryFilter }
        .sortedWith(compareBy({ it.status != AppStatus.RUNNING }, { it.name }))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = KuroSpacing.s20, bottom = KuroSpacing.s32),
        verticalArrangement = Arrangement.spacedBy(KuroSpacing.s12)
    ) {
        item {
            KuroScreenTitle("Vault", "${PreviewData.apps.size} virtual apps · isolated")
        }
        item {
            Column(Modifier.padding(horizontal = KuroSpacing.s20)) {
                KuroSearch(query = query, onQuery = { query = it })
                Spacer(Modifier.height(KuroSpacing.s12))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(KuroSpacing.s8),
                    verticalArrangement = Arrangement.spacedBy(KuroSpacing.s8)
                ) {
                    KuroFilterChip("Running", statusFilter == AppStatus.RUNNING) {
                        statusFilter = if (statusFilter == AppStatus.RUNNING) null else AppStatus.RUNNING
                    }
                    KuroFilterChip("Paused", statusFilter == AppStatus.PAUSED) {
                        statusFilter = if (statusFilter == AppStatus.PAUSED) null else AppStatus.PAUSED
                    }
                    KuroFilterChip("Updated", statusFilter == AppStatus.UPDATING) {
                        statusFilter = if (statusFilter == AppStatus.UPDATING) null else AppStatus.UPDATING
                    }
                    KuroFilterChip("Games", categoryFilter == AppCategory.GAMES) {
                        categoryFilter = if (categoryFilter == AppCategory.GAMES) null else AppCategory.GAMES
                    }
                    KuroFilterChip("Tools", categoryFilter == AppCategory.TOOLS) {
                        categoryFilter = if (categoryFilter == AppCategory.TOOLS) null else AppCategory.TOOLS
                    }
                    KuroFilterChip("Social", categoryFilter == AppCategory.SOCIAL) {
                        categoryFilter = if (categoryFilter == AppCategory.SOCIAL) null else AppCategory.SOCIAL
                    }
                }
            }
        }
        if (filtered.isEmpty()) {
            item {
                KuroEmptyState(
                    headline = "No Virtual Apps Yet",
                    description = "Import your first app to begin.",
                    actionLabel = "Import app",
                    onAction = onImport,
                    glyph = {
                        Icon(
                            Icons.Filled.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        } else {
            items(filtered, key = { it.id }) { app ->
                AppCard(
                    app = app,
                    onLaunch = {},
                    onOpen = { onOpenApp(app.id) },
                    onLongPress = { sheetAppId = app.id },
                    modifier = Modifier.padding(horizontal = KuroSpacing.s20)
                )
            }
        }
    }

    val sheetApp = sheetAppId?.let { id -> PreviewData.apps.firstOrNull { it.id == id } }
    if (sheetApp != null) {
        KuroSheet(title = sheetApp.name, onDismiss = { sheetAppId = null }) {
            Text("Launch", style = MaterialTheme.typography.bodyLarge)
            Text("Clone", style = MaterialTheme.typography.bodyLarge)
            Text("Export", style = MaterialTheme.typography.bodyLarge)
            Text("Settings", style = MaterialTheme.typography.bodyLarge)
            Text(
                "Delete",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
