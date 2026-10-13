package dev.kuro.app

import android.app.Application
import dev.kuro.app.logging.CrashHandler
import dev.kuro.app.logging.KuroLog

/** App entry point. Initializes diagnostics first, before anything else. */
class KuroApp : Application() {
    override fun onCreate() {
        super.onCreate()
        KuroLog.init(this)
        CrashHandler.install()
    }
}
