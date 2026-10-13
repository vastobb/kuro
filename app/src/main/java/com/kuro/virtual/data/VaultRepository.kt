package com.kuro.virtual.data

import android.content.ClipData
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.kuro.core.AbiGate
import com.kuro.core.ApkExtractor
import com.kuro.core.ApkSet
import com.kuro.core.ProfileManager
import com.kuro.core.VirtualCore
import com.kuro.core.VirtualLauncher
import com.kuro.core.VirtualPackage
import com.kuro.core.VirtualStorage
import com.kuro.core.VaultInstaller
import com.kuro.virtual.KuroDeviceAdmin
import com.kuro.virtual.ProfileInstallActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class VaultRepository(context: Context) {

    private val appContext = context.applicationContext
    private val pm: PackageManager = appContext.packageManager
    private val admin = ComponentName(appContext, KuroDeviceAdmin::class.java)

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

    fun isInstalled(packageName: String): Boolean =
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, 0)
            }
            true
        } catch (_: Exception) {
            false
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

    /**
     * Installs a vault APK entry onto the device through a user-confirmed
     * PackageInstaller session. Needed when the package is not installed yet:
     * only installed packages can be launched.
     */
    suspend fun installVaultApk(entry: VaultEntry): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(entry.apkPath)
            require(file.isFile && file.canRead()) { "Vault APK file missing" }
            val parsed = VirtualPackage.fromApk(pm, file.absolutePath)
                ?: throw IllegalStateException("Not a valid APK")
            val parts = VaultInstaller.fileParts(ApkSet(parsed.packageName, file.absolutePath, emptyList()))
            val status = VaultInstaller.startInstall(appContext, parsed.packageName, parts).get()
            if (status != PackageInstaller.STATUS_SUCCESS) {
                throw IllegalStateException("Installer rejected the package (status $status)")
            }
        }
    }

    /**
     * Clones an entry into Kuro's work profile: stages base + split APKs in
     * the vault, then hands them to the profile-side installer. The system
     * asks once in the work context; the clone gets separate data and is
     * launched from Kuro afterwards.
     */
    suspend fun cloneToProfile(entry: VaultEntry): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            ProfileManager.workProfile(appContext)
                ?: throw IllegalStateException("No work profile. Set one up in Settings first.")
            ProfileManager.ensureHandoffFilter(appContext, admin)
            val apkSet = ApkExtractor.fromInstalled(pm, entry.packageName)
                ?: entry.apkPath.takeIf { File(it).isFile }?.let { ApkSet(entry.packageName, it, emptyList()) }
                ?: throw IllegalStateException("No readable APK source to clone")
            val stage = File(VirtualCore.apkDir(), "handoff-" + entry.packageName).apply { mkdirs() }
            val staged = mutableListOf<File>()
            staged += File(stage, "base.apk").also { copyFile(File(apkSet.basePath), it) }
            apkSet.splitPaths.forEachIndexed { index, path ->
                staged += File(stage, "split_$index.apk").also { copyFile(File(path), it) }
            }
            val uris = staged.map { file ->
                FileProvider.getUriForFile(appContext, appContext.packageName + ".vault", file)
            }
            val intent = Intent(VaultInstaller.ACTION_PROFILE_INSTALL).apply {
                component = ComponentName(appContext, ProfileInstallActivity::class.java)
                type = VaultInstaller.APK_MIME
                putExtra(VaultInstaller.EXTRA_PACKAGE, entry.packageName)
                putExtra(VaultInstaller.EXTRA_LABEL, entry.label)
                putExtra(
                    VaultInstaller.EXTRA_SPLITS,
                    uris.drop(1).map { it.toString() }.toTypedArray(),
                )
                data = uris.first()
                clipData = ClipData(
                    "kuro-apk",
                    arrayOf(VaultInstaller.APK_MIME),
                    ClipData.Item(uris.first()),
                ).apply {
                    uris.drop(1).forEach { addItem(ClipData.Item(it)) }
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                appContext.startActivity(intent)
            } catch (_: Exception) {
                throw IllegalStateException("Work-profile handoff blocked on this device")
            }
            "Sent to the work profile. Confirm the install there, then launch the clone from Kuro."
        }
    }

    /**
     * Removes a work-profile clone through the profile-side installer.
     * The device copy is never touched.
     */
    suspend fun uninstallClone(entry: VaultEntry): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            ProfileManager.workProfile(appContext)
                ?: throw IllegalStateException("No work profile.")
            val intent = Intent(VaultInstaller.ACTION_PROFILE_INSTALL).apply {
                component = ComponentName(appContext, ProfileInstallActivity::class.java)
                putExtra(VaultInstaller.EXTRA_MODE, VaultInstaller.MODE_UNINSTALL)
                putExtra(VaultInstaller.EXTRA_PACKAGE, entry.packageName)
                putExtra(VaultInstaller.EXTRA_LABEL, entry.label)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                appContext.startActivity(intent)
            } catch (_: Exception) {
                throw IllegalStateException("Work-profile handoff blocked on this device")
            }
            "Opening uninstall in the work profile."
        }
    }

    /**
     * Launches a vault entry. Clones open their work-profile instance;
     * plain entries open the device copy.
     */
    fun launch(entry: VaultEntry): VirtualLauncher.LaunchResult {
        if (entry.inProfile) {
            val user = ProfileManager.workProfile(appContext)
            if (user != null) {
                val component = ProfileManager.resolveInProfile(appContext, entry.packageName)
                if (component != null && ProfileManager.launchInProfile(appContext, component, user)) {
                    return VirtualLauncher.LaunchResult(true)
                }
                // Clone gone (removed in profile?) — fall through to device copy.
            }
        }
        return VirtualLauncher.launch(appContext, entry.packageName)
    }

    /** Re-checks whether the clone exists in the profile; returns updated entry. */
    fun refreshCloneState(entry: VaultEntry): VaultEntry {
        val cloned = ProfileManager.isCloned(appContext, entry.packageName)
        return if (cloned == entry.inProfile) entry else entry.copy(inProfile = cloned)
    }

    fun clearAppData(packageName: String): Boolean =
        VirtualStorage.clearData(packageName)

    private fun copyFile(source: File, dest: File) {
        require(source.isFile && source.canRead()) { "Cannot read " + source.path }
        if (dest.exists()) dest.delete()
        source.inputStream().use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
    }
}
