package com.kuro.core

import android.content.pm.PackageManager
import android.os.Build

/**
 * Vault-facing view of one package, built either from an installed app or
 * from an APK file on disk. Parsing uses only public PackageManager APIs
 * with version branches for API 29-35.
 */
data class VirtualPackage(
    val packageName: String,
    val appLabel: String,
    val versionName: String?,
    val versionCode: Long,
    val apkPath: String,
    val is64BitCompatible: Boolean,
    val targetSdk: Int,
    val minSdk: Int,
) {
    companion object {

        fun fromInstalled(pm: PackageManager, packageName: String): VirtualPackage? {
            return try {
                val info = if (Build.VERSION.SDK_INT >= 33) {
                    pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(packageName, 0)
                }
                val app = info.applicationInfo ?: return null
                VirtualPackage(
                    packageName = packageName,
                    appLabel = app.loadLabel(pm)?.toString() ?: packageName,
                    versionName = info.versionName,
                    versionCode = info.longVersionCode,
                    apkPath = app.sourceDir,
                    // Installed apps already run on this device's ABI.
                    is64BitCompatible = true,
                    targetSdk = app.targetSdkVersion,
                    minSdk = app.minSdkVersion,
                )
            } catch (_: Exception) {
                null
            }
        }

        fun fromApk(pm: PackageManager, apkPath: String): VirtualPackage? {
            return try {
                val info = if (Build.VERSION.SDK_INT >= 33) {
                    pm.getPackageArchiveInfo(apkPath, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageArchiveInfo(apkPath, 0)
                } ?: return null
                val app = info.applicationInfo ?: return null
                app.sourceDir = apkPath
                app.publicSourceDir = apkPath
                VirtualPackage(
                    packageName = info.packageName,
                    appLabel = app.loadLabel(pm)?.toString() ?: info.packageName,
                    versionName = info.versionName,
                    versionCode = info.longVersionCode,
                    apkPath = apkPath,
                    is64BitCompatible = AbiGate.isSupportedOnDevice(apkPath),
                    targetSdk = app.targetSdkVersion,
                    minSdk = app.minSdkVersion,
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}
