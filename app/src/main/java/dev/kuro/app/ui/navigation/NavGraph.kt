package dev.kuro.app.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.kuro.app.logging.KuroLog
import dev.kuro.app.ui.components.KuroBottomBar
import dev.kuro.app.ui.screens.ActivityScreen
import dev.kuro.app.ui.screens.AppDetailScreen
import dev.kuro.app.ui.screens.DebugLogsScreen
import dev.kuro.app.ui.screens.HomeScreen
import dev.kuro.app.ui.screens.ImportFlowScreen
import dev.kuro.app.ui.screens.ImportScreen
import dev.kuro.app.ui.screens.SettingsScreen
import dev.kuro.app.ui.theme.KuroTheme

/** Short display name for navigation tracing. */
private fun traceName(route: String): String = when {
    route == Routes.HOME -> "Home"
    route == Routes.VAULT -> "Vault"
    route == Routes.IMPORT -> "Import"
    route.startsWith("import_flow") -> "ImportFlow"
    route == Routes.ACTIVITY -> "Activity"
    route == Routes.SETTINGS -> "Settings"
    route == Routes.DEBUG_LOGS -> "DebugLogs"
    route.startsWith("app_detail") -> "AppDetail"
    else -> route
}

/**
 * Kuro nav — startup-safety policy: start destination is Home, no splash,
 * no launch-time navigation. Bottom bar: Home / Vault / Import* / Activity /
 * Settings (*Import is center-elevated).
 */
@Composable
fun KuroNavGraph() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    var darkTheme by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) { KuroLog.navInit() }
    LaunchedEffect(route) { route?.let { KuroLog.screen(traceName(it)) } }

    KuroTheme(darkTheme = darkTheme) {
        val showBar = route != null &&
            route != Routes.DEBUG_LOGS &&
            !route.startsWith("import_flow") &&
            !route.startsWith("app_detail")

        Scaffold(
            bottomBar = {
                if (showBar) {
                    KuroBottomBar(
                        currentRoute = route,
                        onHome = { nav.navigate(Routes.HOME) { launchSingleTop = true } },
                        onVault = { nav.navigate(Routes.VAULT) { launchSingleTop = true } },
                        onImport = { nav.navigate(Routes.IMPORT) { launchSingleTop = true } },
                        onActivity = { nav.navigate(Routes.ACTIVITY) { launchSingleTop = true } },
                        onSettings = { nav.navigate(Routes.SETTINGS) { launchSingleTop = true } }
                    )
                }
            }
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = Routes.HOME,
                modifier = Modifier.padding(padding)
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        onOpenApp = { nav.navigate(Routes.appDetail(it)) },
                        onImport = { nav.navigate(Routes.IMPORT) },
                        onSeeAll = { nav.navigate(Routes.VAULT) }
                    )
                }
                composable(Routes.VAULT) {
                    dev.kuro.app.ui.screens.VaultScreen(
                        onOpenApp = { nav.navigate(Routes.appDetail(it)) },
                        onImport = { nav.navigate(Routes.IMPORT) }
                    )
                }
                composable(Routes.IMPORT) {
                    ImportScreen(
                        onImportApk = { nav.navigate(Routes.importFlow("apk")) },
                        onImportInstalled = { nav.navigate(Routes.importFlow("installed")) }
                    )
                }
                composable(
                    route = Routes.IMPORT_FLOW,
                    arguments = listOf(navArgument("source") { type = NavType.StringType; defaultValue = "apk" })
                ) { entry ->
                    ImportFlowScreen(
                        initialSource = entry.arguments?.getString("source") ?: "apk",
                        onDone = { nav.navigate(Routes.VAULT) { launchSingleTop = true } },
                        onBack = { nav.popBackStack() }
                    )
                }
                composable(Routes.ACTIVITY) { ActivityScreen() }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        darkTheme = darkTheme,
                        onToggleTheme = {
                            darkTheme = it
                            KuroLog.settingsChange("theme", if (it) "dark" else "light")
                        },
                        onOpenDebug = { nav.navigate(Routes.DEBUG_LOGS) }
                    )
                }
                composable(Routes.DEBUG_LOGS) {
                    DebugLogsScreen(onBack = { nav.popBackStack() })
                }
                composable(
                    route = Routes.APP_DETAIL,
                    arguments = listOf(navArgument("appId") { type = NavType.StringType })
                ) { entry ->
                    AppDetailScreen(
                        appId = entry.arguments?.getString("appId") ?: "",
                        onBack = { nav.popBackStack() }
                    )
                }
            }
        }
    }
}

/** System default holder for previews/tests. */
@Composable
fun KuroNavGraphPreviewHost() {
    KuroTheme(darkTheme = isSystemInDarkTheme()) { }
}
