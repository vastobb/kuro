package dev.kuro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.kuro.app.logging.KuroLog
import dev.kuro.app.ui.navigation.KuroNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KuroLog.event("ACTIVITY_LAUNCH", "MainActivity savedState=${savedInstanceState != null}")
        enableEdgeToEdge()
        setContent {
            KuroNavGraph()
        }
    }

    override fun onDestroy() {
        if (isFinishing) KuroLog.appClose()
        super.onDestroy()
    }
}
