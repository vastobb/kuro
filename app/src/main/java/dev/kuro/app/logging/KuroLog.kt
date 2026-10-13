package dev.kuro.app.logging

import android.content.Context
import android.util.Log as ALog
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Locale
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Production diagnostic logger.
 *
 * - Async single-thread writes, BufferedWriter, never blocks the UI thread.
 * - Session file: kuro_yyyy-MM-dd_HH-mm-ss.log (external app files/logs,
 *   fallback internal files/logs).
 * - Keeps the last [MAX_RECENT] lines in memory for crash reports.
 * - Mirrors everything to logcat under [TAG].
 */
object KuroLog {
    const val TAG = "Kuro"
    const val MAX_RECENT = 200

    private val initialized = AtomicBoolean(false)
    private val themeLogged = AtomicBoolean(false)
    private val navLogged = AtomicBoolean(false)

    @Volatile private var appContext: Context? = null
    @Volatile private var sessionFile: File? = null
    @Volatile private var launchTimeMs: Long = 0L
    @Volatile private var currentScreen: String = "?"

    private val lock = Any()
    private val recent = ArrayDeque<String>(MAX_RECENT + 16)
    // Confined to [lock]: SimpleDateFormat is not thread-safe.
    private val lineFmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    private val io = Executors.newSingleThreadExecutor { r ->
        Thread(r, "KuroLog").apply { isDaemon = true }
    }
    @Volatile private var writer: BufferedWriter? = null

    fun fileNameFor(whenMs: Long): String =
        "kuro_" + SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date(whenMs)) + ".log"

    fun crashNameFor(whenMs: Long): String =
        "crash_" + SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date(whenMs)) + ".log"

    /** App-specific external storage on Android 10+, else internal files. No permission needed. */
    fun resolveLogDir(context: Context): File {
        val ext = runCatching { context.getExternalFilesDir(null) }.getOrNull()
        return if (ext != null) File(ext, "logs") else File(context.filesDir, "logs")
    }

    /** Production entry: call once from Application.onCreate. Never throws. */
    fun init(context: Context) {
        if (!initialized.compareAndSet(false, true)) return
        runCatching {
            val app = context.applicationContext
            appContext = app
            val now = System.currentTimeMillis()
            val dir = resolveLogDir(app).apply { mkdirs() }
            attach(File(dir, fileNameFor(now)), now)
            event("APP_LAUNCH", "package=${app.packageName}")
            event("DEVICE", DeviceInfo.summary(app))
        }
    }

    /** JVM-test seam: attach to a plain directory, no Context required. */
    internal fun attachForTest(dir: File, nowMs: Long) {
        if (!initialized.compareAndSet(false, true)) return
        dir.mkdirs()
        attach(File(dir, fileNameFor(nowMs)), nowMs)
        event("APP_LAUNCH", "test")
    }

    private fun attach(file: File, nowMs: Long) {
        file.createNewFile()
        launchTimeMs = nowMs
        sessionFile = file
        synchronized(lock) { writer = BufferedWriter(FileWriter(file, true), 8192) }
    }

    internal fun resetForTest() {
        runCatching { synchronized(lock) { writer?.close() } }
        writer = null
        sessionFile = null
        appContext = null
        synchronized(lock) { recent.clear() }
        currentScreen = "?"
        themeLogged.set(false)
        navLogged.set(false)
        initialized.set(false)
    }

    // ---- public event API (all async, non-blocking) ----

    fun event(name: String, detail: String = "") =
        write("I", if (detail.isEmpty()) name else "$name $detail")

    fun warn(name: String, detail: String = "") =
        write("W", if (detail.isEmpty()) name else "$name $detail")

    fun error(name: String, detail: String = "") =
        write("E", if (detail.isEmpty()) name else "$name $detail")

    fun screen(name: String) {
        currentScreen = name
        event("SCREEN", name)
    }

    fun themeInit(dark: Boolean, dynamic: Boolean) {
        if (themeLogged.compareAndSet(false, true)) event("THEME_INIT", "dark=$dark dynamic=$dynamic")
    }

    fun navInit() {
        if (navLogged.compareAndSet(false, true)) event("NAV_INIT", "start=splash")
    }

    fun splashStart() = event("SPLASH_START", "")
    fun splashEnd() = event("SPLASH_END", "->Home")

    fun importStart(source: String) = event("IMPORT_START", "source=$source")
    fun importStep(step: Int, total: Int, label: String) =
        event("IMPORT_STEP", "${step + 1}/$total $label")

    fun settingsChange(key: String, value: String) = event("SETTINGS", "$key=$value")

    fun appClose() {
        val dur = System.currentTimeMillis() - launchTimeMs
        event("APP_CLOSE", "durationMs=$dur screen=$currentScreen")
    }

    // ---- internals ----

    private fun write(level: String, msg: String) {
        val line = synchronized(lock) {
            val l = "${lineFmt.format(Date())} $level/$TAG: $msg"
            if (recent.size >= MAX_RECENT) recent.removeFirst()
            recent.addLast(l)
            l
        }
        when (level) {
            "W" -> ALog.w(TAG, msg)
            "E" -> ALog.e(TAG, msg)
            else -> ALog.i(TAG, msg)
        }
        io.execute {
            runCatching {
                val w = synchronized(lock) { writer } ?: return@execute
                w.write(line)
                w.newLine()
                w.flush()
            }
        }
    }

    /** Snapshot of recent lines for crash reports. */
    internal fun drainRecent(): List<String> = synchronized(lock) { recent.toList() }

    internal fun recentSizeForTest(): Int = synchronized(lock) { recent.size }

    internal fun appContextOrNull(): Context? = appContext

    internal fun sessionFileOrNull(): File? = sessionFile

    /** Block (bounded) until queued writes are flushed. Used by tests only. */
    internal fun flushSync(timeoutMs: Long = 10_000L) {
        runCatching {
            io.submit(Callable {
                runCatching { synchronized(lock) { writer?.flush() } }
            }).get(timeoutMs, TimeUnit.MILLISECONDS)
        }
    }
}
