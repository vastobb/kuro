package com.kuro.core

import android.content.Context
import android.content.Intent

/**
 * Launches vault packages with isolation flags.
 *
 * Every target runs in its own Android sandbox process already; Kuro adds
 * task-stack isolation ([Intent.FLAG_ACTIVITY_MULTIPLE_TASK]) plus a marker
 * extra so future engine versions can attach virtualization shims. A crash
 * inside the launched app cannot kill Kuro: [launch] is exception-safe and
 * returns a result instead of throwing.
 */
object VirtualLauncher {

    const val EXTRA_ISOLATED = "com.kuro.virtual.ISOLATED"
    const val EXTRA_VAULT_PACKAGE = "com.kuro.virtual.VAULT_PACKAGE"

    data class LaunchResult(val ok: Boolean, val reason: String? = null)

    fun launch(context: Context, packageName: String): LaunchResult {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                ?: return LaunchResult(false, "No launchable activity for $packageName")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            intent.putExtra(EXTRA_ISOLATED, true)
            intent.putExtra(EXTRA_VAULT_PACKAGE, packageName)
            // Isolated task affinity keeps this app's windows grouped together.
            context.startActivity(intent)
            LaunchResult(true)
        } catch (e: Exception) {
            LaunchResult(false, e.message ?: e.javaClass.simpleName)
        }
    }
}
