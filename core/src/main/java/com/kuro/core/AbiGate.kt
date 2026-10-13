package com.kuro.core

import java.util.zip.ZipFile

/**
 * 64-bit gate for APK imports.
 *
 * Reads the APK as a plain zip (no NDK, no PackageParser native code) and
 * collects native library entries from the APK lib folders. Pure Dalvik/ART
 * packages have no native libs and always pass; packages with native libs
 * pass when at least one of their ABIs is in [android.os.Build.SUPPORTED_ABIS].
 */
object AbiGate {

    fun apkNativeAbis(apkPath: String): Set<String> {
        val abis = mutableSetOf<String>()
        try {
            ZipFile(apkPath).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val name = entries.nextElement().name
                    if (name.startsWith("lib/") && name.endsWith(".so")) {
                        val abi = name.removePrefix("lib/").substringBefore("/")
                        if (abi.isNotEmpty() && abi != "lib") abis += abi
                    }
                }
            }
        } catch (_: Exception) {
            // Unreadable zip: caller treats empty set as "unknown, allow with care".
        }
        return abis
    }

    fun isSupportedOnDevice(apkPath: String, supportedAbis: Set<String>): Boolean {
        val needed = apkNativeAbis(apkPath)
        if (needed.isEmpty()) return true
        return needed.any { it in supportedAbis }
    }

    fun isSupportedOnDevice(apkPath: String): Boolean =
        isSupportedOnDevice(apkPath, android.os.Build.SUPPORTED_ABIS.toSet())

    fun describe(apkPath: String): String {
        val abis = apkNativeAbis(apkPath)
        if (abis.isEmpty()) return "no native libs (pure bytecode)"
        return "native ABIs: " + abis.sorted().joinToString(", ")
    }
}
