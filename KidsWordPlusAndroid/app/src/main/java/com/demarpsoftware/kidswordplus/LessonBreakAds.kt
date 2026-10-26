package com.demarpsoftware.kidswordplus

import android.app.Activity
import android.os.SystemClock
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

internal val LocalGameAdBreak = androidx.compose.runtime.staticCompositionLocalOf<((() -> Unit) -> Unit)> {
    { action -> action() }
}

internal class LessonBreakAds(private val activity: Activity) {
    private var ready: InterstitialAd? = null
    private var active = false
    private var disposed = false
    private var loading = false
    private var showing = false
    private var loadedAt = 0L
    private var lastShown = SystemClock.elapsedRealtime()

    fun start() = ChildDirectedAds.initialize(activity) {
        activity.runOnUiThread { if (!disposed) { active = true; load() } }
    }

    private fun load() {
        if (!active || disposed || loading || ready != null || activity.isDestroyed) return
        loading = true
        InterstitialAd.load(activity, BuildConfig.ADMOB_INTERSTITIAL_ID, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    loading = false
                    if (!disposed) { ready = ad; loadedAt = SystemClock.elapsedRealtime() }
                }
                override fun onAdFailedToLoad(error: LoadAdError) { loading = false }
            })
    }

    fun atBreak(navigate: () -> Unit) {
        if (showing) return
        val now = SystemClock.elapsedRealtime()
        if (now - loadedAt > 55 * 60_000L) ready = null
        val ad = ready
        if (!active || disposed || ad == null || now - lastShown < 5 * 60_000L || activity.isFinishing || activity.isDestroyed) {
            navigate(); load(); return
        }
        ready = null
        showing = true
        var finished = false
        fun finish() {
            if (finished) return
            finished = true
            showing = false
            if (!disposed) { navigate(); load() }
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() { lastShown = SystemClock.elapsedRealtime() }
            override fun onAdDismissedFullScreenContent() = finish()
            override fun onAdFailedToShowFullScreenContent(error: AdError) = finish()
        }
        ad.show(activity)
    }

    fun dispose() { disposed = true; ready = null }
}
