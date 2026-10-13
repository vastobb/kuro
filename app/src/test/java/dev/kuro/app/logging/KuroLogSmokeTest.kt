package dev.kuro.app.logging

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Startup smoke test (JVM, no device):
 * - logger initializes and creates the session file,
 * - log file creation + write succeeds,
 * - crash handler registration succeeds,
 * - crash reports carry the full cause chain + recent events.
 */
class KuroLogSmokeTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private var savedHandler: Thread.UncaughtExceptionHandler? = null

    @Before
    fun setUp() {
        savedHandler = Thread.getDefaultUncaughtExceptionHandler()
        KuroLog.resetForTest()
        CrashHandler.resetForTest()
    }

    @After
    fun tearDown() {
        Thread.setDefaultUncaughtExceptionHandler(savedHandler)
        KuroLog.resetForTest()
        CrashHandler.resetForTest()
    }

    @Test
    fun loggerInitializes_andCreatesSessionFile() {
        val dir = tmp.newFolder("logs")
        KuroLog.attachForTest(dir, 1_700_000_000_000L)

        val files = dir.listFiles().orEmpty()
        assertEquals(1, files.size)
        assertTrue(files[0].name.matches(Regex("kuro_\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}\\.log")))
    }

    @Test
    fun logFileCreationSucceeds_andContainsEvent() {
        val dir = tmp.newFolder("logs")
        KuroLog.attachForTest(dir, 1_700_000_000_000L)
        KuroLog.event("SMOKE", "hello-world")
        KuroLog.flushSync()

        val file = dir.listFiles().orEmpty().single()
        val text = file.readText()
        assertTrue(text.contains("SMOKE hello-world"))
        assertTrue(text.contains("APP_LAUNCH"))
    }

    @Test
    fun crashHandlerRegistrationSucceeds() {
        assertTrue(CrashHandler.install())
        assertTrue(CrashHandler.isInstalled())
    }

    @Test
    fun crashReportContainsFullChainAndRecentEvents() {
        val cause = IllegalStateException("root-cause")
        val middle = IllegalArgumentException("mid-level", cause)
        val top = RuntimeException("top-failure", middle)
        val events = listOf(
            "12:00:01 I/Kuro: SCREEN Splash",
            "12:00:02 I/Kuro: SCREEN Home",
            "12:00:15 I/Kuro: SCREEN Vault"
        )

        val report = CrashHandler.buildCrashReport(
            timestampMs = 1_700_000_000_000L,
            app = AppMeta("dev.kuro.app", "0.1.0", 1),
            device = DeviceMeta("Google", "Pixel", "14", 34),
            threadName = "main",
            e = top,
            recentEvents = events
        )

        assertTrue(report.contains("KURO CRASH REPORT"))
        assertTrue(report.contains("dev.kuro.app 0.1.0(1)"))
        assertTrue(report.contains("Google Pixel"))
        assertTrue(report.contains("sdk 34"))
        assertTrue(report.contains("top-failure"))
        assertTrue(report.contains("Caused by: java.lang.IllegalArgumentException: mid-level"))
        assertTrue(report.contains("Caused by: java.lang.IllegalStateException: root-cause"))
        assertTrue(report.contains("\tat "))
        events.forEach { assertTrue(report.contains(it)) }
    }

    @Test
    fun fileNamesMatchContract() {
        assertTrue(KuroLog.fileNameFor(1_700_000_000_000L).startsWith("kuro_"))
        assertTrue(KuroLog.crashNameFor(1_700_000_000_000L).startsWith("crash_"))
        assertTrue(KuroLog.fileNameFor(1_700_000_000_000L).endsWith(".log"))
        assertTrue(KuroLog.crashNameFor(1_700_000_000_000L).endsWith(".log"))
    }

    @Test
    fun recentEventsAreBounded() {
        val dir = tmp.newFolder("logs")
        KuroLog.attachForTest(dir, 1_700_000_000_000L)
        repeat(KuroLog.MAX_RECENT + 50) { KuroLog.event("FILL", "$it") }
        assertEquals(KuroLog.MAX_RECENT, KuroLog.recentSizeForTest())
    }
}
