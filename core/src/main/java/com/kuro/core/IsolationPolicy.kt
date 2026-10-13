package com.kuro.core

/**
 * What "isolation" means in each phase.
 *
 * Phase 1 (shipped): managed vault entries, isolated task stacks, per-app
 * vault storage, ABI gating, crash-safe launches. Installed-app entries are
 * managed shortcuts to real packages; APK entries are vault-stored packages
 * installed through the system installer flow.
 *
 * Phase 2 (roadmap, interfaces reserved): install-free execution via
 * ActivityManager/PackageManager virtualization and file-system hooks, in
 * the spirit of the engine studied in NewBlackbox — reimplemented here from
 * scratch when it lands. Nothing in Phase 2 is claimed before it exists.
 */
object IsolationPolicy {
    const val PHASE = 1
    const val MIN_SDK = 29
    const val PREFERRED_ABI = "arm64-v8a"
}
