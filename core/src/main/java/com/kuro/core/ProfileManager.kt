package com.kuro.core

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserHandle

/**
 * Work-profile clone backend, built only on public APIs (no root, no NDK).
 *
 * Clones are separate OS-level instances with separate data: Kuro provisions
 * a managed profile as profile owner, installs the package there (confirmed
 * once in the work context), and launches it cross-profile with LauncherApps.
 */
object ProfileManager {

    fun dpm(context: Context): DevicePolicyManager =
        context.getSystemService(DevicePolicyManager::class.java)

    fun launcherApps(context: Context): LauncherApps =
        context.getSystemService(LauncherApps::class.java)

    fun myUser(): UserHandle = Process.myUserHandle()

    fun isProfileOwner(context: Context): Boolean =
        try {
            dpm(context).isProfileOwnerApp(context.packageName)
        } catch (_: Exception) {
            false
        }

    /** Null when provisioning is unavailable or already done. */
    fun provisioningIntent(context: Context, admin: ComponentName): Intent? {
        return try {
            if (isProfileOwner(context) || workProfile(context) != null) return null
            if (!dpm(context).isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)) {
                return null
            }
            Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE).apply {
                putExtra(
                    DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                    admin,
                )
            }.takeIf { it.resolveActivity(context.packageManager) != null }
        } catch (_: Exception) {
            null
        }
    }

    /** The managed-profile handle, or null when none exists. */
    fun workProfile(context: Context): UserHandle? {
        return try {
            val me = myUser()
            launcherApps(context).profiles.firstOrNull { it != me }
        } catch (_: Exception) {
            null
        }
    }

    fun profileApps(context: Context, user: UserHandle): List<LauncherActivityInfo> =
        try {
            launcherApps(context).getActivityList(null, user)
        } catch (_: Exception) {
            emptyList()
        }

    fun isCloned(context: Context, packageName: String): Boolean {
        val user = workProfile(context) ?: return false
        return try {
            profileApps(context, user).any { it.componentName.packageName == packageName }
        } catch (_: Exception) {
            false
        }
    }

    fun resolveInProfile(context: Context, packageName: String): ComponentName? {
        val user = workProfile(context) ?: return null
        return try {
            val launch = context.packageManager.getLaunchIntentForPackage(packageName)
                ?: return null
            launcherApps(context).resolveActivity(launch, user)?.componentName
        } catch (_: Exception) {
            null
        }
    }

    fun launchInProfile(context: Context, component: ComponentName, user: UserHandle): Boolean {
        return try {
            launcherApps(context).startMainActivity(component, user, null, null)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Lets the personal and profile sides exchange installer handoffs.
     * Idempotent; only succeeds while Kuro is profile owner.
     */
    fun ensureHandoffFilter(context: Context, admin: ComponentName): Boolean {
        return try {
            val filter = IntentFilter(VaultInstaller.ACTION_PROFILE_INSTALL).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                addDataScheme("content")
                try {
                    addDataType(VaultInstaller.APK_MIME)
                } catch (_: IntentFilter.MalformedMimeTypeException) {
                    return false
                }
            }
            dpm(context).addCrossProfileIntentFilter(
                admin,
                filter,
                DevicePolicyManager.FLAG_PARENT_CAN_ACCESS_MANAGED or
                    DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT,
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    fun clearProfileOwner(context: Context, admin: ComponentName): Boolean =
        try {
            dpm(context).clearProfileOwner(admin)
            true
        } catch (_: Exception) {
            false
        }
}
