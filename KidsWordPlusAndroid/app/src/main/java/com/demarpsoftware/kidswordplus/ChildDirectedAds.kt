package com.demarpsoftware.kidswordplus

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

/** Explicit initialization: invoke only after privacy prerequisites are satisfied. */
internal object ChildDirectedAds {
    private var started = false
    private var ready = false
    private val callbacks = mutableListOf<() -> Unit>()

    @Synchronized
    fun initialize(context: Context, onReady: () -> Unit) {
        if (BuildConfig.ADMOB_INTERSTITIAL_ID.isBlank()) return
        if (ready) { onReady(); return }
        callbacks += onReady
        if (started) return
        started = true
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                .setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE)
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                .setTestDeviceIds(if (BuildConfig.DEBUG) listOf("38B5B429C323114DDE1DE9E78CEF091A") else emptyList())
                .build()
        )
        MobileAds.initialize(context.applicationContext) {
            val pending = synchronized(this) {
                ready = true
                callbacks.toList().also { callbacks.clear() }
            }
            pending.forEach { it() }
        }
    }
}
