package com.demarpsoftware.kidswordplus

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.util.Locale

/** Fixed screen/event codes only: never pass names, photos, word text or profile IDs. */
internal object AppTelemetry {
    private var analytics: FirebaseAnalytics? = null
    fun initialize(context: Context) {
        analytics = FirebaseAnalytics.getInstance(context).apply {
            setConsent(mapOf(
                FirebaseAnalytics.ConsentType.AD_STORAGE to FirebaseAnalytics.ConsentStatus.DENIED,
                FirebaseAnalytics.ConsentType.AD_USER_DATA to FirebaseAnalytics.ConsentStatus.DENIED,
                FirebaseAnalytics.ConsentType.AD_PERSONALIZATION to FirebaseAnalytics.ConsentStatus.DENIED
            ))
        }
        FirebaseCrashlytics.getInstance().setCustomKey("build_type", if (BuildConfig.DEBUG) "debug" else "release")
    }
    fun screen(code: String) {
        analytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, code)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, "KidsWordPlus")
        })
        FirebaseCrashlytics.getInstance().setCustomKey("screen", code)
    }
    fun packageInstalled(language: String) = event("language_pack_installed", "language", language)
    fun setupCompleted() = event("tutorial_complete")
    fun gameStarted(kind: String) = event("learning_game_started", "game_kind", kind.lowercase(Locale.ROOT))
    fun chessStarted(computer: Boolean) = event("chess_game_started", "opponent", if (computer) "computer" else "local")
    private fun event(name: String, key: String? = null, value: String? = null) {
        analytics?.logEvent(name, Bundle().apply { if (key != null) putString(key, value) })
    }
    fun operationFailed(error: Exception) {
        // Drop original messages and causes, which can contain paths or profile data.
        val sanitized = IllegalStateException("Content operation failed: ${error.javaClass.simpleName}")
        sanitized.stackTrace = error.stackTrace
        FirebaseCrashlytics.getInstance().recordException(sanitized)
    }
}
