package com.kuro.virtual.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.kuro.virtual.ui.add.AddAppScreen
import com.kuro.virtual.ui.home.HomeScreen
import com.kuro.virtual.ui.settings.SettingsScreen

enum class KuroDestination { Home, Add, Settings }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KuroRoot() {
    var destination by remember { mutableStateOf(KuroDestination.Home) }
    var vaultTick by remember { mutableStateOf(0) }
    val snackbar = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Kuro") },
                navigationIcon = {
                    if (destination != KuroDestination.Home) {
                        IconButton(onClick = { destination = KuroDestination.Home }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (destination == KuroDestination.Home) {
                        IconButton(onClick = { destination = KuroDestination.Settings }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (destination == KuroDestination.Home) {
                FloatingActionButton(onClick = { destination = KuroDestination.Add }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add app")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        AnimatedContent(targetState = destination, label = "kuro-nav") { dest ->
            when (dest) {
                KuroDestination.Home -> HomeScreen(
                    modifier = Modifier.padding(padding),
                    snackbar = snackbar,
                    vaultTick = vaultTick,
                    onAdd = { destination = KuroDestination.Add },
                )
                KuroDestination.Add -> AddAppScreen(
                    modifier = Modifier.padding(padding),
                    snackbar = snackbar,
                    onVaultChanged = { vaultTick += 1 },
                )
                KuroDestination.Settings -> SettingsScreen(
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}
