package com.kuro.virtual.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.kuro.core.AbiGate
import com.kuro.core.VirtualCore
import com.kuro.core.VirtualLauncher
import com.kuro.core.VirtualPackage
import com.kuro.core.VirtualStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class VaultRepository(context: Context) {

    private val appContext = context.applicationContext
    private val pm: PackageManager = appContext.packageManager

    /** All launchable installed apps except Kuro itself, sorted by label. */
    suspend fun installedLaunchable(): List<VirtualPackage> = withContext(Dispatchers.IO) {
        val probe = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = if (Build.VERSION.SDK_INT >= 33) {
            pm.queryIntentActivities(probe, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(probe, 0)
        }
        resolved.mapNotNull { it.activityInfo?.packageName }
            .distinct()
            .filter { it != appContext.packageName && it != appContext.packageName + ".debug" }
            .mapNotNull { VirtualPackage.fromInstalled(pm, it) }
            .sortedBy { it.appLabel.lowercase() }
    }

    /**
     * Copies an APK from a content [source] Uri into the vault, parses it and
     * enforces the 64-bit ABI gate. Returns the parsed package on success.
     */
    suspend fun importApk(source: Uri): Result<VirtualPackage> = withContext(Dispatchers.IO) {
        runCatching {
            VirtualCore.apkDir().mkdirs()
            val tmp = File(VirtualCore.apkDir(), "import-" + System.currentTimeMillis() + ".apk")
            appContext.contentResolver.openInputStream(source)?.use { input ->
                tmp.outputStream().use { output -> input.copyTo(output) }
            } ?: throw IllegalStateException("Cannot read the selected file")
            val parsed = VirtualPackage.fromApk(pm, tmp.absolutePath)
            if (parsed == null) {
                tmp.delete()
                throw IllegalStateException("Not a valid APK")
            }
            if (!parsed.is64BitCompatible) {
                val detail = AbiGate.describe(tmp.absolutePath)
                tmp.delete()
                throw IllegalStateException("No 64-bit native libraries for this device ($detail)")
            }
            val dest = File(VirtualCore.apkDir(), parsed.packageName + ".apk")
            if (dest.exists()) dest.delete()
            if (!tmp.renameTo(dest)) throw IllegalStateException("Cannot store APK in vault")
            parsed.copy(apkPath = dest.absolutePath)
        }
    }

    fun launch(packageName: String): VirtualLauncher.LaunchResult =
        VirtualLauncher.launch(appContext, packageName)

    fun clearAppData(packageName: String): Boolean =
        VirtualStorage.clearData(packageName)
}
