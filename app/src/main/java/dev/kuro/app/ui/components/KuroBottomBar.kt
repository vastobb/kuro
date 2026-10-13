package dev.kuro.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class KuroNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

/** Bottom nav items — Import is center-elevated FAB, not a plain tab. */
fun kuroNavItems(home: String, vault: String, activity: String, settings: String) = listOf(
    KuroNavItem(home, "Home", Icons.Filled.Home),
    KuroNavItem(vault, "Vault", Icons.Filled.Inventory2),
    KuroNavItem(activity, "Activity", Icons.Filled.Timeline),
    KuroNavItem(settings, "Settings", Icons.Filled.Settings)
)

@Composable
fun KuroBottomBar(
    currentRoute: String?,
    onHome: () -> Unit,
    onVault: () -> Unit,
    onImport: () -> Unit,
    onActivity: () -> Unit,
    onSettings: () -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        KuroTab(
            selected = currentRoute == "home",
            label = "Home",
            icon = Icons.Filled.Home,
            onClick = onHome
        )
        KuroTab(
            selected = currentRoute == "vault",
            label = "Vault",
            icon = Icons.Filled.Inventory2,
            onClick = onVault
        )
        // Center elevated Import
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(
                onClick = onImport,
                modifier = Modifier.size(56.dp).offset(y = (-12).dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Import")
            }
        }
        KuroTab(
            selected = currentRoute == "activity",
            label = "Activity",
            icon = Icons.Filled.Timeline,
            onClick = onActivity
        )
        KuroTab(
            selected = currentRoute == "settings",
            label = "Settings",
            icon = Icons.Filled.Settings,
            onClick = onSettings
        )
    }
}

@Composable
private fun RowScope.KuroTab(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        )
    )
}
