package com.kuro.virtual.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class VaultEntry(
    val packageName: String,
    val label: String,
    val apkPath: String,
    val fromApkFile: Boolean,
    /** True when a separate instance lives in Kuro's work profile. */
    val inProfile: Boolean = false,
    val addedAt: Long = System.currentTimeMillis(),
)

/** Minimal SharedPreferences persistence (org.json only, no extra deps). */
object VaultStore {

    private const val PREFS = "kuro_vault"
    private const val KEY_ENTRIES = "entries_v2"

    fun load(context: Context): MutableList<VaultEntry> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        // Migrate v1 entries (no profile flag) on first run.
        val raw = prefs.getString(KEY_ENTRIES, null)
            ?: prefs.getString("entries_v1", "[]") ?: "[]"
        val out = mutableListOf<VaultEntry>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val pkg = o.optString("package")
                if (pkg.isNotEmpty()) {
                    out += VaultEntry(
                        packageName = pkg,
                        label = o.optString("label", pkg),
                        apkPath = o.optString("apk", ""),
                        fromApkFile = o.optBoolean("fromApk", false),
                        inProfile = o.optBoolean("profile", false),
                        addedAt = o.optLong("addedAt", 0L),
                    )
                }
            }
        } catch (_: Exception) {
            // Corrupt prefs: start empty rather than crash.
        }
        return out
    }

    fun save(context: Context, entries: List<VaultEntry>) {
        val arr = JSONArray()
        entries.forEach {
            arr.put(
                JSONObject()
                    .put("package", it.packageName)
                    .put("label", it.label)
                    .put("apk", it.apkPath)
                    .put("fromApk", it.fromApkFile)
                    .put("profile", it.inProfile)
                    .put("addedAt", it.addedAt),
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_ENTRIES, arr.toString()).apply()
    }
}
