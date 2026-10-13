package com.kuro.virtual

import android.app.Application
import com.kuro.core.VirtualCore

class KuroApp : Application() {
    override fun onCreate() {
        super.onCreate()
        VirtualCore.init(this)
    }
}
