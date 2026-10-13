package com.kuro.core

import android.content.Context
import android.os.Build
import java.io.File

/**
 * Entry point of the Kuro isolation engine.
 *
 * Owns the vault directory layout and device ABI facts. Call [init] once from
 * the host Application; all helpers below are idempotent and thread-safe.
 */
object VirtualCore {

    @Volatile
    var initialized: Boolean = false
        private set

    private var hostContext: Context? = null

    @Synchronized
    fun init(context: Context) {
        if (initialized) return
        hostContext = context.applicationContext
        vaultDir().mkdirs()
        apkDir().mkdirs()
        initialized = true
    }

    fun requireContext(): Context =
        checkNotNull(hostContext) { "VirtualCore.init() was not called" }

    /** Root of the vault: apks + per-app data. Never stores host secrets. */
    fun vaultDir(): File = File(requireContext().filesDir, "vault")

    /** Imported APK files live here, named `<package>.apk`. */
    fun apkDir(): File = File(vaultDir(), "apks")

    /** Isolated data dir for one vault package. Created on demand. */
    fun appDataDir(packageName: String): File =
        File(vaultDir(), "data" + File.separator + packageName)

    /** True when the device offers at least one 64-bit ABI. */
    val is64BitDevice: Boolean
        get() = Build.SUPPORTED_64_BIT_ABIS.isNotEmpty()

    fun deviceAbis(): List<String> = Build.SUPPORTED_ABIS.toList()
}
