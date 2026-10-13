package com.kuro.core

import android.content.pm.PackageManager
import android.os.Build
import java.io.File

/** Base APK plus any split APKs belonging to one package. */
data class ApkSet(
    val packageName: String,
    val basePath: String,
    val splitPaths: List<String>,
)

/** Reads installed APK files straight from disk (world-readable, no root). */
object ApkExtractor {

    fun fromInstalled(pm: PackageManager, packageName: String): ApkSet? {
        return try {
            val app = if (Build.VERSION.SDK_INT >= 33) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(packageName, 0)
            }
            val base = app.sourceDir
            if (base.isNullOrEmpty() || !File(base).canRead()) return null
            val splits = app.splitSourceDirs
                ?.filter { !it.isNullOrEmpty() && File(it).canRead() }
                .orEmpty()
            ApkSet(packageName, base, splits)
        } catch (_: Exception) {
            null
        }
    }
}
