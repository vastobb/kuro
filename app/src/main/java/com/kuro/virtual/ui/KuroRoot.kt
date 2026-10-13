package com.kuro.virtual.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.kuro.virtual.ui.add.AddAppScreen
import com.kuro.virtual.ui.home.HomeScreen
import com.kuro.virtual.ui.settings.SettingsScreen

private enum class KuroDestination(val label: String, val icon: ImageVector) {
    Vault("Vault", Icons.Filled.Apps),
    Lab("Clone lab", Icons.Filled.Add),
    Settings("Settings", Icons.Filled.Settings),
}

/** Kuro console: bottom navigation, screens own their headers. */
@Composable
fun KuroRoot() {
    var destination by remember { mutableStateOf(KuroDestination.Vault) }
    var vaultTick by remember { mutableStateOf(0) }
    val snackbar = remember { SnackbarHostState() }
    fun changed() {
        vaultTick += 1
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                KuroDestination.entries.forEach { dest ->
                    NavigationBarItem(
                        selected = destination == dest,
                        onClick = { destination = dest },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        AnimatedContent(targetState = destination, label = "kuro-nav") { dest ->
            when (dest) {
                KuroDestination.Vault -> HomeScreen(
                    modifier = Modifier.padding(padding),
                    snackbar = snackbar,
                    vaultTick = vaultTick,
                    onOpenLab = { destination = KuroDestination.Lab },
                    onOpenSettings = { destination = KuroDestination.Settings },
                )
                KuroDestination.Lab -> AddAppScreen(
                    modifier = Modifier.padding(padding),
                    snackbar = snackbar,
                    onVaultChanged = { changed() },
                    onOpenSettings = { destination = KuroDestination.Settings },
                )
                KuroDestination.Settings -> SettingsScreen(
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}
