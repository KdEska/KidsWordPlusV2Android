package com.demarpsoftware.kidswordplus

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.speech.*
import androidx.compose.runtime.*
import com.demarpsoftware.kidswordplus.speech.SpeakingRules
import com.demarpsoftware.kidswordplus.speech.SpeakingSupportPolicy
import com.demarpsoftware.kidswordplus.speech.SpeakingModelAvailability

/** Main-thread only. Uses the on-device factory exclusively; never falls back to a server. */
internal class SpeakingSession(context: Context) {
    enum class Phase { READY, CHECKING, LISTENING, RECOGNIZING, MATCHED, DIFFERENT, NO_SPEECH, UNSUPPORTED, UNAVAILABLE, PERMISSION }
    private val app = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var generation = 0
    var phase by mutableStateOf(Phase.READY); private set
    var transcript by mutableStateOf(""); private set
    var level by mutableFloatStateOf(0f); private set
    var seconds by mutableFloatStateOf(0f); private set
    val busy get() = phase in listOf(Phase.CHECKING, Phase.LISTENING, Phase.RECOGNIZING)
    val available get() = Build.VERSION.SDK_INT >= 31 && runCatching { SpeechRecognizer.isOnDeviceRecognitionAvailable(app) }.getOrDefault(false)

    fun reset() {
        generation++
        handler.removeCallbacksAndMessages(null)
        val old = recognizer; recognizer = null
        runCatching { old?.cancel() }; runCatching { old?.destroy() }
        phase = Phase.READY; transcript = ""; level = 0f; seconds = 0f
    }
    fun stop() {
        if (phase != Phase.LISTENING) return
        phase = Phase.RECOGNIZING
        runCatching { recognizer?.stopListening() }.onFailure { finish(Phase.UNAVAILABLE) }
    }
    private fun finish(result: Phase, heard: String = "") {
        generation++
        handler.removeCallbacksAndMessages(null)
        val old = recognizer; recognizer = null
        runCatching { old?.destroy() }
        transcript = heard; phase = result; level = 0f
    }
    fun start(expected: String, language: String) {
        reset()
        if (!app.getSharedPreferences("profile",0).getBoolean("speech.practice.enabled",false)) {
            phase = Phase.PERMISSION; return
        }
        val locale = SpeakingRules.locales[language]
        if (Build.VERSION.SDK_INT < 31 || !available || locale == null) { phase = Phase.UNSUPPORTED; return }
        if (app.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            phase = Phase.PERMISSION; return
        }
        val ticket = generation
        phase = Phase.CHECKING
        try {
            val engine = SpeechRecognizer.createOnDeviceSpeechRecognizer(app)
            recognizer = engine
            engine.setRecognitionListener(object: RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    if (ticket != generation) return
                    phase = Phase.LISTENING
                    val began=SystemClock.elapsedRealtime()
                    val tick=object:Runnable {
                        override fun run() {
                            if(ticket!=generation || phase!=Phase.LISTENING)return
                            seconds=((SystemClock.elapsedRealtime()-began)/1000f).coerceAtMost(5f)
                            if(seconds>=5f)stop() else handler.postDelayed(this,100)
                        }
                    }
                    handler.post(tick)
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) { if (ticket == generation) level = ((rmsdB + 2) / 12).coerceIn(0f,1f) }
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { if (ticket == generation) phase = Phase.RECOGNIZING }
                override fun onError(error: Int) {
                    if (ticket != generation) return
                    finish(when(error) {
                        SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> Phase.NO_SPEECH
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> Phase.PERMISSION
                        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED, SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> Phase.UNSUPPORTED
                        else -> Phase.UNAVAILABLE
                    })
                }
                override fun onResults(results: Bundle?) {
                    if (ticket != generation) return
                    val heard = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                    finish(if (SpeakingRules.normalized(heard,language).isEmpty()) Phase.NO_SPEECH
                        else if (SpeakingRules.matches(heard,expected,language)) Phase.MATCHED else Phase.DIFFERENT, heard)
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
                .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                .putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            // A bounded attempt also covers services that never call onReady/onResults.
            handler.postDelayed({ if (ticket == generation) finish(Phase.UNAVAILABLE) }, 18_000)
            if (Build.VERSION.SDK_INT >= 33) {
                engine.checkRecognitionSupport(intent, app.mainExecutor, object: RecognitionSupportCallback {
                    override fun onSupportResult(support: RecognitionSupport) {
                        if (ticket != generation) return
                        val plan = SpeakingSupportPolicy.plan(support.installedOnDeviceLanguages,support.supportedOnDeviceLanguages,support.pendingOnDeviceLanguages,locale)
                        if (plan.availability in listOf(SpeakingModelAvailability.READY,SpeakingModelAvailability.UNKNOWN))
                            runCatching { engine.startListening(intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,plan.locale)) }.onFailure { finish(Phase.UNAVAILABLE) }
                        else finish(Phase.UNSUPPORTED)
                    }
                    override fun onError(error: Int) {
                        if (ticket != generation) return
                        if(SpeakingSupportPolicy.queryUnavailable(error)) {
                            runCatching {engine.startListening(intent)}.onFailure {finish(Phase.UNAVAILABLE)}
                        } else finish(if(error==SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED || error==SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE)Phase.UNSUPPORTED else Phase.UNAVAILABLE)
                    }
                })
            } else engine.startListening(intent)
        } catch (_: Exception) { finish(Phase.UNAVAILABLE) }
    }
}
