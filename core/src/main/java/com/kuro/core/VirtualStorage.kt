package com.kuro.core

import java.io.File

/**
 * Per-app isolated storage inside the vault.
 *
 * Each vault package gets its own data dir. Clearing it never touches the
 * host app or other vault entries. Real private-data virtualization of the
 * target app (file-system hooks) is Phase 2; this layer manages Kuro-side
 * state (prefs, caches, imported APK bookkeeping).
 */
object VirtualStorage {

    fun dataDirFor(packageName: String): File =
        VirtualCore.appDataDir(packageName).apply { mkdirs() }

    fun clearData(packageName: String): Boolean =
        try {
            VirtualCore.appDataDir(packageName).deleteRecursively()
        } catch (_: Exception) {
            false
        }

    fun vaultSizeBytes(): Long = dirSize(VirtualCore.vaultDir())

    private fun dirSize(dir: File): Long {
        if (!dir.exists()) return 0L
        if (dir.isFile) return dir.length()
        var total = 0L
        dir.listFiles()?.forEach { total += dirSize(it) }
        return total
    }
}
