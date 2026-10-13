package com.kuro.virtual

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import com.kuro.core.InstallEvents
import com.kuro.core.VaultInstaller

/** Completes [InstallEvents] futures for PackageInstaller sessions. */
class InstallResultReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != VaultInstaller.ACTION_INSTALL_RESULT) return
        val pkg = intent.getStringExtra(VaultInstaller.EXTRA_PACKAGE) ?: return
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            val confirm = if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_INTENT)
            }
            confirm?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)?.let {
                try {
                    context.startActivity(it)
                } catch (_: Exception) {
                    InstallEvents.fail(pkg, IllegalStateException("Install confirmation blocked"))
                }
            } ?: InstallEvents.fail(pkg, IllegalStateException("Install confirmation missing"))
            return
        }
        InstallEvents.complete(pkg, status)
    }
}
