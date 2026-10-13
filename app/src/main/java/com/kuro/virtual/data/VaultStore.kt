package com.kuro.virtual.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class VaultEntry(
    val packageName: String,
    val label: String,
    val apkPath: String,
    val fromApkFile: Boolean,
    val addedAt: Long = System.currentTimeMillis(),
)

/** Minimal SharedPreferences persistence (org.json only, no extra deps). */
object VaultStore {

    private const val PREFS = "kuro_vault"
    private const val KEY_ENTRIES = "entries_v1"

    fun load(context: Context): MutableList<VaultEntry> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_ENTRIES, "[]") ?: "[]"
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
                    .put("addedAt", it.addedAt),
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_ENTRIES, arr.toString()).apply()
    }
}
