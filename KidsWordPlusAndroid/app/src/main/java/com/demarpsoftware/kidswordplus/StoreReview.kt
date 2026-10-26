package com.demarpsoftware.kidswordplus

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.play.core.review.ReviewManagerFactory
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId

internal val LocalStoreReviewTest=staticCompositionLocalOf<StoreReviewCoordinator?> {null}
internal val LocalCompletionReview=staticCompositionLocalOf<(Boolean)->Unit> {{}}
internal data class ReviewAttempt(val time:Long,val version:String)
internal data class ReviewHistory(val firstSeen:Long,val completed:Int=0,val days:Set<String> = emptySet(),
    val attempts:List<ReviewAttempt> = emptyList(),val lastOfferedCompletion:Int=0) {
    fun observe(total:Int,now:Long):ReviewHistory {
        if(total<=completed)return this
        val day=Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate().toString()
        return copy(completed=total,days=(days+day).sorted().takeLast(90).toSet())
    }
    fun eligible(now:Long,version:String):Boolean = now>=firstSeen && now-firstSeen>=7*DAY &&
        completed>=5 && days.size>=3 && completed-lastOfferedCompletion>=5 &&
        attempts.none {it.version==version} && attempts.none {now<it.time || now-it.time<120*DAY} &&
        attempts.count {now-it.time<365*DAY}<3
    fun reserve(now:Long,version:String)=copy(attempts=attempts+ReviewAttempt(now,version),lastOfferedCompletion=completed)
    fun encode():String=JSONObject().put("firstSeen",firstSeen).put("completed",completed).put("days",JSONArray(days.toList()))
        .put("lastOfferedCompletion",lastOfferedCompletion).put("attempts",JSONArray().apply {
            attempts.forEach {put(JSONObject().put("time",it.time).put("version",it.version))}
        }).toString()
    companion object {
        const val DAY=86_400_000L
        fun decode(text:String):ReviewHistory {
            val j=JSONObject(text);val days=j.getJSONArray("days");val attempts=j.getJSONArray("attempts")
            return ReviewHistory(j.getLong("firstSeen"),j.getInt("completed"),(0 until days.length()).map{days.getString(it)}.toSet(),
                (0 until attempts.length()).map{attempts.getJSONObject(it).let {a->ReviewAttempt(a.getLong("time"),a.getString("version"))}},j.getInt("lastOfferedCompletion"))
        }
    }
}

internal class StoreReviewCoordinator(private val activity:Activity) {
    private val preferences=activity.getSharedPreferences("store-review-v1",Context.MODE_PRIVATE)
    private var history=preferences.getString("history",null)?.let {runCatching {ReviewHistory.decode(it)}.getOrNull()} ?: ReviewHistory(System.currentTimeMillis())
    var needsParent by mutableStateOf(false);private set
    private var launching=false
    private var testing=false
    private var testTr=true
    var testStatus by mutableStateOf<String?>(null);private set
    fun test(tr:Boolean) {
        if(!BuildConfig.DEBUG || needsParent || launching)return
        testTr=tr;testing=true
        testStatus=InterfaceStrings.text(tr,"Test başladı. Ebeveyn doğrulamasını tamamlayın.","Test started. Complete parent verification.")
        needsParent=true
    }
    private fun status(tr:String,en:String){testStatus=InterfaceStrings.text(testTr,tr,en)}
    init {save()}
    private fun save()=preferences.edit().putString("history",history.encode()).commit()
    fun completion(total:Int,offer:Boolean) {
        history=history.observe(total,System.currentTimeMillis())
        if(!save() || !offer || needsParent || launching || !history.eligible(System.currentTimeMillis(),BuildConfig.VERSION_NAME))return
        val reserved=history.reserve(System.currentTimeMillis(),BuildConfig.VERSION_NAME)
        if(!preferences.edit().putString("history",reserved.encode()).commit())return
        history=reserved;needsParent=true
    }
    fun dismiss(){
        if(testing){status("Test iptal edildi.","Test cancelled.");testing=false}
        needsParent=false
    }
    fun parentVerified() {
        val isTest=testing;testing=false
        needsParent=false;launching=true
        if(isTest)status("Mağazadan değerlendirme penceresi isteniyor…","Requesting the store review dialog…")
        // Let the parent-gate window close before requesting the untouched store card.
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if(activity.isFinishing || activity.isDestroyed || !activity.hasWindowFocus()){
                launching=false
                if(isTest)status("Test kesildi. Uygulama açıkken yeniden deneyin.","Test interrupted. Try again while the app is open.")
                return@postDelayed
            }
            val manager=ReviewManagerFactory.create(activity)
            manager.requestReviewFlow().addOnCompleteListener {request ->
                if(request.isSuccessful && !activity.isFinishing && !activity.isDestroyed && activity.hasWindowFocus()) {
                    manager.launchReviewFlow(activity,request.result).addOnCompleteListener {
                        launching=false
                        if(isTest)status("Mağaza isteği tamamlandı. Pencerenin gösterilmesini Google Play belirler; bu, puan verildiği anlamına gelmez.","Store request completed. Google Play decides whether to show the dialog; this does not mean a rating was submitted.")
                    }
                } else {
                    launching=false
                    if(isTest)status("Bu kurulumda mağaza isteği açılamadı. Gerçek pencereyi Google Play iç test sürümünde deneyin.","The store request could not open on this installation. Test the real dialog using a Google Play internal test build.")
                }
            }
        },400)
    }
}

@Composable internal fun ReviewParentGate(tr:Boolean,close:()->Unit,verified:()->Unit) {
    var gate by remember {mutableStateOf(com.demarpsoftware.kidswordplus.family.ParentGate())}
    var fraction by remember {mutableFloatStateOf(0f)}
    val owner=LocalLifecycleOwner.current
    DisposableEffect(owner){
        val observer=LifecycleEventObserver {_,event->if(event==Lifecycle.Event.ON_STOP)close()}
        owner.lifecycle.addObserver(observer);onDispose{owner.lifecycle.removeObserver(observer)}
    }
    LaunchedEffect(gate.started) {
        if(gate.started==null){fraction=0f;return@LaunchedEffect}
        while(true) {
            val now=android.os.SystemClock.elapsedRealtime()
            fraction=gate.fraction(now)
            gate=gate.tick(now)
            if(gate.unlocked){verified();return@LaunchedEffect}
            kotlinx.coroutines.delay(20)
        }
    }
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    AlertDialog(onDismissRequest=close,title={Text(t("Ebeveyn doğrulaması","Parent verification"))},text={
        Column(verticalArrangement=Arrangement.spacedBy(16.dp),horizontalAlignment=androidx.compose.ui.Alignment.CenterHorizontally) {
            DesignArt("ui/parent/parent-lock.webp",Modifier.size(80.dp),decodeLimit=192)
            Text(t("Mağaza değerlendirmesi yetişkinler içindir. Devam etmek için bir ebeveyninden yardım iste.","Store ratings are for adults. Ask a parent to help you continue."))
            Text(t("Düğmeye 3 saniye basılı tutun.","Press and hold the button for 3 seconds."))
            LinearProgressIndicator(progress={fraction},modifier=Modifier.fillMaxWidth(),drawStopIndicator={})
            Text(t("Basılı tut","Press and hold"),Modifier.fillMaxWidth()
                .background(KidHeading,androidx.compose.foundation.shape.RoundedCornerShape(24.dp)).padding(20.dp)
                .semantics {role=androidx.compose.ui.semantics.Role.Button}
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down=awaitFirstDown(requireUnconsumed=false)
                        down.consume();gate=gate.begin(android.os.SystemClock.elapsedRealtime())
                        try {
                            while(true) {
                                val event=awaitPointerEvent()
                                val pointer=event.changes.find {it.id==down.id} ?: break
                                if(!pointer.pressed || (pointer.position-down.position).getDistance()>20.dp.toPx())break
                                pointer.consume()
                            }
                        } finally {gate=gate.release()}
                    }
                },color=androidx.compose.ui.graphics.Color.White,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
        }
    },confirmButton={},dismissButton={TextButton(onClick=close){Text(t("Şimdi değil","Not now"))}})
}
