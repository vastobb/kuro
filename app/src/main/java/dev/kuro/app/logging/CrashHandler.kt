package dev.kuro.app.logging

import android.os.Process
import java.io.File
import java.io.FileWriter
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

/**
 * Global crash handler.
 *
 * - Writes crash_yyyy-MM-dd_HH-mm-ss.log FIRST (synchronously, own writer —
 *   never touches the async session writer).
 * - Report: device/app identity, thread, full exception + cause chain +
 *   recent application events.
 * - Then delegates to the previous default handler so the app terminates
 *   normally (system crash dialog, process kill).
 */
object CrashHandler : Thread.UncaughtExceptionHandler {
    private val installed = AtomicBoolean(false)
    @Volatile private var previous: Thread.UncaughtExceptionHandler? = null

    /** Install as default handler. Returns true when active. Never throws. */
    fun install(): Boolean {
        if (!installed.compareAndSet(false, true)) return true
        return runCatching {
            previous = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(this)
            KuroLog.event("CRASH_HANDLER", "registered")
            true
        }.getOrDefault(false)
    }

    fun isInstalled(): Boolean = Thread.getDefaultUncaughtExceptionHandler() === this

    internal fun resetForTest() {
        installed.set(false)
        previous = null
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        runCatching { writeReport(t, e) }
        val p = previous
        if (p != null && p !== this) {
            runCatching { p.uncaughtException(t, e) }
        } else {
            runCatching { Process.killProcess(Process.myPid()) }
            exitProcess(10)
        }
    }

    private fun writeReport(t: Thread, e: Throwable) {
        val now = System.currentTimeMillis()
        val ctx = KuroLog.appContextOrNull()
        val dir = ctx?.let { KuroLog.resolveLogDir(it).apply { mkdirs() } }
            ?: KuroLog.sessionFileOrNull()?.parentFile
            ?: return
        val (app, device) = ctx?.let { runCatching { DeviceInfo.collect(it) }.getOrNull() }
            ?: (AppMeta("?", "?", -1) to DeviceMeta("?", "?", "?", -1))
        val report = buildCrashReport(now, app, device, t.name, e, KuroLog.drainRecent())
        FileWriter(File(dir, KuroLog.crashNameFor(now)), false).use { w ->
            w.write(report)
            w.flush()
        }
    }

    /** Pure report builder — covered by JVM unit tests. */
    fun buildCrashReport(
        timestampMs: Long,
        app: AppMeta,
        device: DeviceMeta,
        threadName: String,
        e: Throwable,
        recentEvents: List<String>
    ): String {
        val sb = StringBuilder(8192)
        sb.append("*** KURO CRASH REPORT ***\n")
        sb.append("timestamp=").append(Date(timestampMs)).append('\n')
        sb.append("app=").append(app.packageName)
            .append(' ').append(app.versionName)
            .append('(').append(app.versionCode).append(')').append('\n')
        sb.append("device=").append(device.manufacturer)
            .append(' ').append(device.model).append('\n')
        sb.append("android=").append(device.androidRelease)
            .append("(sdk ").append(device.sdkInt).append(')').append('\n')
        sb.append("thread=").append(threadName).append('\n')
        sb.append("--- exception ---\n")
        appendChain(sb, e)
        sb.append("--- recent events (last ").append(recentEvents.size).append(") ---\n")
        recentEvents.forEach { sb.append(it).append('\n') }
        return sb.toString()
    }

    private fun appendChain(sb: StringBuilder, root: Throwable) {
        var cur: Throwable? = root
        var depth = 0
        while (cur != null && depth < 25) {
            if (depth == 0) {
                sb.append(cur.toString()).append('\n')
            } else {
                sb.append("Caused by: ").append(cur.toString()).append('\n')
            }
            cur.stackTrace.forEach { sb.append("\tat ").append(it.toString()).append('\n') }
            cur = cur.cause
            depth++
        }
    }
}
