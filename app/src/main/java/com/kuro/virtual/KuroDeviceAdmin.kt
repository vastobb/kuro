package com.kuro.virtual

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.kuro.core.ProfileManager

/** Profile owner for Kuro's managed work profile. No lock/wipe policies used. */
class KuroDeviceAdmin : android.app.admin.DeviceAdminReceiver() {

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        try {
            ProfileManager.ensureHandoffFilter(context, ComponentName(context, KuroDeviceAdmin::class.java))
        } catch (_: Exception) {
            // Filters are re-applied on every clone handoff; safe to skip here.
        }
    }

    companion object {
        fun component(context: Context): ComponentName =
            ComponentName(context, KuroDeviceAdmin::class.java)

        @Suppress("unused")
        fun manager(context: Context): DevicePolicyManager =
            context.getSystemService(DevicePolicyManager::class.java)
    }
}
