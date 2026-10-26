package com.demarpsoftware.kidswordplus

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.SystemClock
import androidx.compose.runtime.staticCompositionLocalOf

internal val LocalInteractionSound = staticCompositionLocalOf<(String)->Unit> {{}}

/** One quiet cue at a time. Spoken audio always has priority. */
internal class InteractionAudio(context:Context,private val allowed:()->Boolean) {
    private val pool=SoundPool.Builder().setMaxStreams(1).setAudioAttributes(
        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build()
    private val sounds=mutableMapOf<String,Int>()
    private val loaded=mutableSetOf<Int>()
    private var stream=0
    private var pending:String?=null
    private var lastPlay=-1000L
    private var protectedUntil=0L
    init {
        pool.setOnLoadCompleteListener {_,id,status ->
            if(status==0){loaded.add(id);pending?.takeIf {sounds[it]==id}?.let {pending=null;play(it)}}
        }
        listOf("tap","page","welcome","start","success","retry","train","category","success2","success3","celebration","unlock").forEach {name ->
            context.assets.openFd("interaction/$name.wav").use {sounds[name]=pool.load(it,1)}
        }
    }
    fun play(name:String) {
        if(!allowed())return
        val now=SystemClock.elapsedRealtime()
        val important=name in listOf("welcome","start","success","retry","celebration","unlock","train")
        if(!important && (now<protectedUntil || now-lastPlay<180))return
        val cue=if(name=="success")listOf("success","success2","success3").random() else name
        val id=sounds[cue] ?: return
        if(id !in loaded){pending=cue;return}
        stop();lastPlay=now
        protectedUntil=if(important)now+600 else now
        stream=pool.play(id,.55f,.55f,1,0,1f)
        if(BuildConfig.DEBUG)android.util.Log.d("KwpFeedback","Playing $cue stream=$stream")
    }
    fun stop() {if(stream!=0)pool.stop(stream);stream=0}
    fun release() {pool.release()}
}
