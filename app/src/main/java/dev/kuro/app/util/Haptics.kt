package dev.kuro.app.util

import android.content.Context
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

/** Microinteraction: button tap haptic. */
fun View.kuroTapHaptic() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    } else {
        @Suppress("DEPRECATION")
        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }
}

/** Greeting for hero section. hour 0-23. */
fun greetingForHour(hour: Int): String = when (hour) {
    in 5..11 -> "Good morning"
    in 12..17 -> "Good afternoon"
    else -> "Good evening"
}

fun isReducedMotion(context: Context): Boolean {
    return android.provider.Settings.Global.getFloat(
        context.contentResolver,
        android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    ) == 0f
}
