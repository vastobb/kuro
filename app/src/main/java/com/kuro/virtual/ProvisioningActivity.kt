package com.kuro.virtual

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import com.kuro.core.ProfileManager

/**
 * Minimal provisioning handlers so managed-profile setup succeeds on
 * Android 12+ role-holder flows: answers the provisioning mode and applies
 * the cross-profile handoff filter on compliance.
 */
class ProvisioningActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (intent?.action) {
            DevicePolicyManager.ACTION_GET_PROVISIONING_MODE -> {
                setResult(
                    RESULT_OK,
                    Intent().putExtra(
                        DevicePolicyManager.EXTRA_PROVISIONING_MODE,
                        DevicePolicyManager.PROVISIONING_MODE_MANAGED_PROFILE,
                    ),
                )
            }
            DevicePolicyManager.ACTION_ADMIN_POLICY_COMPLIANCE -> {
                try {
                    ProfileManager.ensureHandoffFilter(this, ComponentName(this, KuroDeviceAdmin::class.java))
                } catch (_: Exception) {
                }
                setResult(RESULT_OK, null)
            }
            else -> setResult(RESULT_CANCELED, null)
        }
        finish()
    }
}
