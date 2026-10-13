package dev.kuro.app.screens

import app.cash.paparazzi.DeviceConfig.Companion.PIXEL_6
import app.cash.paparazzi.Paparazzi
import dev.kuro.app.ui.screens.ActivityScreen
import dev.kuro.app.ui.screens.AppDetailScreen
import dev.kuro.app.ui.screens.DebugLogsScreen
import dev.kuro.app.ui.screens.HomeScreen
import dev.kuro.app.ui.screens.ImportFlowScreen
import dev.kuro.app.ui.screens.ImportScreen
import dev.kuro.app.ui.screens.SettingsScreen
import dev.kuro.app.ui.screens.VaultScreen
import dev.kuro.app.ui.theme.KuroTheme
import org.junit.Rule
import org.junit.Test

/**
 * JVM-rendered screenshots (Paparazzi, no device). Run:
 * ./gradlew :app:recordPaparazziDebug — PNGs land in src/test/snapshots.
 */
class KuroScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = PIXEL_6)

    @Test
    fun homeDark() {
        paparazzi.snapshot {
            KuroTheme(darkTheme = true) {
                HomeScreen(onOpenApp = {}, onImport = {}, onSeeAll = {})
            }
        }
    }

    @Test
    fun homeLight() {
        paparazzi.snapshot {
            KuroTheme(darkTheme = false) {
                HomeScreen(onOpenApp = {}, onImport = {}, onSeeAll = {})
            }
        }
    }

    @Test
    fun vault() {
        paparazzi.snapshot {
            KuroTheme(darkTheme = true) {
                VaultScreen(onOpenApp = {}, onImport = {})
            }
        }
    }

    @Test
    fun import() {
        paparazzi.snapshot {
            KuroTheme(darkTheme = true) {
                ImportScreen(onImportApk = {}, onImportInstalled = {})
            }
        }
    }

    @Test
    fun importFlow() {
        paparazzi.snapshot {
            KuroTheme(darkTheme = true) {
                ImportFlowScreen(initialSource = "apk", onDone = {}, onBack = {})
            }
        }
    }

    @Test
    fun activity() {
        paparazzi.snapshot {
            KuroTheme(darkTheme = true) {
                ActivityScreen()
            }
        }
    }

    @Test
    fun appDetail() {
        paparazzi.snapshot {
            KuroTheme(darkTheme = true) {
                AppDetailScreen(appId = "1", onBack = {})
            }
        }
    }

    @Test
    fun settings() {
        paparazzi.snapshot {
            KuroTheme(darkTheme = true) {
                SettingsScreen(darkTheme = true, onToggleTheme = {}, onOpenDebug = {})
            }
        }
    }

    @Test
    fun debugLogs() {
        paparazzi.snapshot {
            KuroTheme(darkTheme = true) {
                DebugLogsScreen(onBack = {})
            }
        }
    }
}
