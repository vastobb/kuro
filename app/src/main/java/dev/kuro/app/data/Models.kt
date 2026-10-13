package dev.kuro.app.data

import androidx.compose.runtime.Immutable

/** Environment an app runs in. */
@Immutable
data class KuroEnvironment(
    val id: String,
    val name: String,
    val androidVersion: String = "Android 14",
    val isolated: Boolean = true
)

/** Status of a virtualized app. */
enum class AppStatus { RUNNING, PAUSED, STOPPED, UPDATING }

/** Category for vault filtering. */
enum class AppCategory { GAMES, TOOLS, SOCIAL, OTHER }

/** A virtualized app entry. UI-foundation uses preview data; engine wires in later. */
@Immutable
data class VirtualApp(
    val id: String,
    val name: String,
    val packageName: String,
    val version: String,
    val environment: String,
    val status: AppStatus,
    val category: AppCategory,
    val storageMb: Int,
    val iconTint: Long = 0xFF7C4DFF,
    val pinned: Boolean = false,
    val cpuPct: Int = 0,
    val memMb: Int = 0
)

/** Live running environment row for Activity screen. */
@Immutable
data class RunningEnv(
    val app: VirtualApp,
    val uptimeMin: Int,
    val cpuPct: Int,
    val memMb: Int
)

/** Import wizard state (5 steps). */
@Immutable
data class ImportUiState(
    val step: Int = 0,
    val source: ImportSource = ImportSource.APK,
    val packageName: String = "",
    val envName: String = "Default Workspace",
    val isolateStorage: Boolean = true,
    val allowNotifications: Boolean = true
)

enum class ImportSource { APK, INSTALLED }

object PreviewData {
    val envs = listOf(
        KuroEnvironment("default", "Default Workspace"),
        KuroEnvironment("work", "Work Profile", androidVersion = "Android 14"),
        KuroEnvironment("gaming", "Gaming Sandbox")
    )

    val apps = listOf(
        VirtualApp("1", "Pixel Gallery", "com.example.gallery", "4.2.1", "Default Workspace", AppStatus.RUNNING, AppCategory.TOOLS, 184, pinned = true, cpuPct = 6, memMb = 212),
        VirtualApp("2", "Nebula Chat", "com.example.nebula", "12.8.0", "Default Workspace", AppStatus.RUNNING, AppCategory.SOCIAL, 342, iconTint = 0xFF00D4FF, pinned = true, cpuPct = 11, memMb = 388),
        VirtualApp("3", "Orbit Racer", "com.example.orbit", "2.3.4", "Gaming Sandbox", AppStatus.PAUSED, AppCategory.GAMES, 1024, iconTint = 0xFF33E1B5, cpuPct = 0, memMb = 0),
        VirtualApp("4", "Ledger Notes", "com.example.ledger", "1.9.2", "Work Profile", AppStatus.STOPPED, AppCategory.TOOLS, 96, iconTint = 0xFF9A7DFF),
        VirtualApp("5", "Wave Music", "com.example.wave", "8.1.0", "Default Workspace", AppStatus.UPDATING, AppCategory.OTHER, 512, iconTint = 0xFF7C4DFF),
        VirtualApp("6", "Drift Maps", "com.example.drift", "6.0.3", "Default Workspace", AppStatus.STOPPED, AppCategory.TOOLS, 268, iconTint = 0xFF00D4FF)
    )

    val running: List<RunningEnv> = apps
        .filter { it.status == AppStatus.RUNNING }
        .map { RunningEnv(it, uptimeMin = 42, cpuPct = it.cpuPct, memMb = it.memMb) }
}
