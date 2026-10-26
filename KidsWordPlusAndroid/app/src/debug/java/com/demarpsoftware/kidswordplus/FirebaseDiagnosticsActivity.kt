package com.demarpsoftware.kidswordplus

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

/** Emulator-only setup verification; never packaged in release. */
class FirebaseDiagnosticsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!android.os.Build.MODEL.contains("sdk_gphone")) { finish(); return }
        if (intent.getBooleanExtra("test_reminder", false)) { AppNotifications.scheduleDeveloperReminder(); finish(); return }
        AppTelemetry.screen("firebase_diagnostics")
        if (intent.getBooleanExtra("test_crash", false)) {
            Handler(Looper.getMainLooper()).postDelayed({
                throw IllegalStateException("KidsWordPlus Firebase integration test crash")
            }, 1500)
        } else {
            AppTelemetry.operationFailed(IllegalStateException("Firebase integration test"))
            finish()
        }
    }
}
