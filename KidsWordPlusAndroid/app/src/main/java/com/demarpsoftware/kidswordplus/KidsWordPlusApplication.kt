package com.demarpsoftware.kidswordplus

import android.app.Application

class KidsWordPlusApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppTelemetry.initialize(this)
        InterfaceStrings.load(this)
        AppNotifications.initialize(this)
    }
}
