package dev.kuro.app.logging

import android.content.Context
import android.os.Build

/** App + device identity captured in session headers and crash reports. */
data class AppMeta(
    val packageName: String,
    val versionName: String,
    val versionCode: Long
)

data class DeviceMeta(
    val manufacturer: String,
    val model: String,
    val androidRelease: String,
    val sdkInt: Int
)

object DeviceInfo {
    fun collect(context: Context): Pair<AppMeta, DeviceMeta> {
        val pm = context.packageManager
        val pkg = context.packageName
        @Suppress("DEPRECATION")
        val pi = pm.getPackageInfo(pkg, 0)
        val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pi.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            pi.versionCode.toLong()
        }
        return AppMeta(pkg, pi.versionName ?: "?", code) to
            DeviceMeta(Build.MANUFACTURER ?: "?", Build.MODEL ?: "?", Build.VERSION.RELEASE ?: "?", Build.VERSION.SDK_INT)
    }

    fun summary(context: Context): String = runCatching {
        val (app, dev) = collect(context)
        "app=${app.packageName} ${app.versionName}(${app.versionCode}) " +
            "device=${dev.manufacturer} ${dev.model} android=${dev.androidRelease}(sdk ${dev.sdkInt})"
    }.getOrDefault("app=? device=?")
}
