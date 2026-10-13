package com.kuro.core

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import java.io.File
import java.io.InputStream
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * Completes with PackageInstaller status codes (SUCCESS == [PackageInstaller.STATUS_SUCCESS]).
 * Filled by the host app's install-result receiver; the installer never blocks the UI.
 */
object InstallEvents {

    private val pending = ConcurrentHashMap<String, CompletableFuture<Int>>()

    fun expect(packageName: String): CompletableFuture<Int> {
        val future = CompletableFuture<Int>()
        pending[packageName] = future
        return future
    }

    fun complete(packageName: String, status: Int) {
        pending.remove(packageName)?.complete(status)
    }

    fun fail(packageName: String, error: Throwable) {
        pending.remove(packageName)?.completeExceptionally(error)
    }
}

/**
 * Installs APKs through public PackageInstaller sessions.
 *
 * Sessions commit with user confirmation (system prompt), so no privileged
 * permission is needed. Split APKs ride as child sessions of one parent.
 */
object VaultInstaller {

    const val ACTION_INSTALL_RESULT = "com.kuro.virtual.INSTALL_RESULT"
    const val ACTION_PROFILE_INSTALL = "com.kuro.virtual.PROFILE_INSTALL"
    const val EXTRA_PACKAGE = "com.kuro.virtual.EXTRA_PACKAGE"
    const val EXTRA_LABEL = "com.kuro.virtual.EXTRA_LABEL"
    const val EXTRA_SPLITS = "com.kuro.virtual.EXTRA_SPLITS"
    const val APK_MIME = "application/vnd.android.package-archive"

    /** Receiver component inside the host app (explicit intent, no reflection). */
    private const val RESULT_RECEIVER = "com.kuro.virtual.InstallResultReceiver"

    data class ApkPart(val name: String, val length: Long, val open: () -> InputStream?)

    fun fileParts(apkSet: ApkSet): List<ApkPart> {
        val parts = mutableListOf(
            ApkPart("base.apk", File(apkSet.basePath).length()) { fileStream(apkSet.basePath) },
        )
        apkSet.splitPaths.forEachIndexed { index, path ->
            parts += ApkPart("split_$index.apk", File(path).length()) { fileStream(path) }
        }
        return parts
    }

    private fun fileStream(path: String): InputStream? =
        try {
            File(path).inputStream()
        } catch (_: Exception) {
            null
        }

    /**
     * Starts the install in the caller's user and returns a future completed
     * with the PackageInstaller status code.
     */
    fun startInstall(
        context: Context,
        packageName: String,
        parts: List<ApkPart>,
    ): CompletableFuture<Int> {
        val future = InstallEvents.expect(packageName)
        try {
            require(parts.isNotEmpty()) { "No APK parts to install" }
            val installer = context.packageManager.packageInstaller
            val params = PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL,
            ).apply {
                setAppPackageName(packageName)
                if (parts.size > 1) setMultiPackage()
            }
            val sessionId = installer.createSession(params)
            installer.openSession(sessionId).use { session ->
                parts.forEachIndexed { index, part ->
                    if (index == 0) {
                        writePart(session, part)
                    } else {
                        val childId = installer.createSession(params)
                        installer.openSession(childId).use { child -> writePart(child, part) }
                        session.addChildSessionId(childId)
                    }
                }
                val result = Intent(ACTION_INSTALL_RESULT).apply {
                    setClassName(context.packageName, RESULT_RECEIVER)
                    putExtra(EXTRA_PACKAGE, packageName)
                }
                var flags = PendingIntent.FLAG_UPDATE_CURRENT
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    flags = flags or PendingIntent.FLAG_MUTABLE
                }
                val sender = PendingIntent.getBroadcast(context, sessionId, result, flags)
                session.commit(sender.intentSender)
            }
        } catch (e: Exception) {
            InstallEvents.fail(packageName, e)
        }
        return future
    }

    private fun writePart(session: PackageInstaller.Session, part: ApkPart) {
        val stream = part.open() ?: throw IllegalStateException("Cannot read " + part.name)
        stream.use { input ->
            session.openWrite(part.name, 0, part.length).use { out ->
                input.copyTo(out)
                session.fsync(out)
            }
        }
    }
}
