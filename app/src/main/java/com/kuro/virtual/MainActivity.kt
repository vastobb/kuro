package com.kuro.virtual

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kuro.virtual.ui.KuroRoot
import com.kuro.virtual.ui.theme.KuroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KuroTheme {
                KuroRoot()
            }
        }
    }
}
