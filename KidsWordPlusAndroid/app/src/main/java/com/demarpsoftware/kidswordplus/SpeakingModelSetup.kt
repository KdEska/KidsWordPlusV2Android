package com.demarpsoftware.kidswordplus

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.*
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import com.demarpsoftware.kidswordplus.speech.*

/** Model metadata and explicit parent-requested downloads; never opens the microphone. */
internal class SpeakingModelSetup(context:Context,private val authorized:()->Boolean) {
    enum class Status { CHECKING, READY, MISSING, PENDING, UNSUPPORTED, UNKNOWN, LEGACY, UNAVAILABLE, DOWNLOADING, FAILED, PERMISSION }
    private val app=context.applicationContext
    private val handler=Handler(Looper.getMainLooper())
    private var engine:SpeechRecognizer?=null
    private var generation=0
    var status by mutableStateOf(Status.CHECKING); private set
    var locale by mutableStateOf(""); private set
    var progress by mutableStateOf<Int?>(null); private set
    val busy get()=status==Status.CHECKING || status==Status.DOWNLOADING
    val canDownload get()=Build.VERSION.SDK_INT>=33 && status in listOf(Status.MISSING,Status.UNKNOWN,Status.FAILED)
    fun close() {
        generation++;handler.removeCallbacksAndMessages(null)
        val old=engine;engine=null;runCatching {old?.destroy()}
    }
    private fun finish(value:Status) {close();status=value}
    private fun intent(locale:String)=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE,locale)
        .putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true)
    @android.annotation.TargetApi(33) // New APIs below are guarded by the SDK checks.
    fun check(language:String) {
        if(!authorized())return
        close();progress=null
        locale=SpeakingRules.locales[language].orEmpty()
        if(locale.isEmpty()){status=Status.UNSUPPORTED;return}
        if(Build.VERSION.SDK_INT<31 || !runCatching {SpeechRecognizer.isOnDeviceRecognitionAvailable(app)}.getOrDefault(false)) {
            status=Status.UNAVAILABLE;return
        }
        if(Build.VERSION.SDK_INT<33){status=Status.LEGACY;return}
        status=Status.CHECKING
        val ticket=generation
        try {
            val recognizer=SpeechRecognizer.createOnDeviceSpeechRecognizer(app)
            engine=recognizer
            handler.postDelayed({if(ticket==generation)finish(Status.UNKNOWN)},12_000)
            recognizer.checkRecognitionSupport(intent(locale),app.mainExecutor,object:RecognitionSupportCallback {
                @RequiresApi(33)
                override fun onSupportResult(support:RecognitionSupport) {
                    if(ticket!=generation)return
                    val plan=SpeakingSupportPolicy.plan(support.installedOnDeviceLanguages,support.supportedOnDeviceLanguages,support.pendingOnDeviceLanguages,locale)
                    locale=plan.locale
                    finish(when(plan.availability) {
                        SpeakingModelAvailability.READY->Status.READY
                        SpeakingModelAvailability.DOWNLOADABLE->Status.MISSING
                        SpeakingModelAvailability.PENDING->Status.PENDING
                        SpeakingModelAvailability.UNSUPPORTED->Status.UNSUPPORTED
                        SpeakingModelAvailability.UNKNOWN->Status.UNKNOWN
                    })
                }
                override fun onError(error:Int) {
                    if(ticket!=generation)return
                    finish(when(error) {
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS->Status.PERMISSION
                        SpeechRecognizer.ERROR_CANNOT_CHECK_SUPPORT->Status.UNKNOWN
                        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED->Status.UNSUPPORTED
                        SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE->Status.MISSING
                        else->Status.FAILED
                    })
                }
            })
        } catch (_:SecurityException){finish(Status.PERMISSION)} catch (_:Exception){finish(Status.UNKNOWN)}
    }
    fun download(language:String) {
        if(!authorized() || !canDownload || Build.VERSION.SDK_INT<33)return
        val requested=locale.ifEmpty {SpeakingRules.locales[language].orEmpty()}
        if(requested.isEmpty())return
        close();locale=requested;progress=null;status=Status.DOWNLOADING
        val ticket=generation
        try {
            val recognizer=SpeechRecognizer.createOnDeviceSpeechRecognizer(app)
            engine=recognizer
            // Scheduling or a stalled callback is not proof that the model is installed.
            handler.postDelayed({if(ticket==generation)finish(Status.PENDING)},90_000)
            if(Build.VERSION.SDK_INT>=34)recognizer.triggerModelDownload(intent(locale),app.mainExecutor,object:ModelDownloadListener {
                override fun onProgress(completedPercent:Int) {
                    if(ticket==generation)progress=completedPercent.coerceIn(0,100)
                }
                override fun onSuccess(){if(ticket==generation)finish(Status.READY)}
                override fun onScheduled(){if(ticket==generation)finish(Status.PENDING)}
                override fun onError(error:Int){if(ticket==generation)finish(if(error==SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS)Status.PERMISSION else Status.FAILED)}
            }) else {
                recognizer.triggerModelDownload(intent(locale))
                // Android 13 has no completion callback; require an explicit support recheck.
                handler.removeCallbacksAndMessages(null)
                status=Status.PENDING
            }
        } catch (_:SecurityException){finish(Status.PERMISSION)} catch (_:Exception){finish(Status.FAILED)}
    }
}
