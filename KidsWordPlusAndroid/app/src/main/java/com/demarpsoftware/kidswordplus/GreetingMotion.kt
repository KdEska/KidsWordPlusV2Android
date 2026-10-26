package com.demarpsoftware.kidswordplus

import android.database.ContentObserver
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.demarpsoftware.kidswordplus.family.Companions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

internal val LocalKidMotion = staticCompositionLocalOf { true }

@Composable internal fun KidMotionProvider(appEnabled:Boolean=true,content: @Composable () -> Unit) {
    val resolver = LocalContext.current.contentResolver
    fun readPreference() = Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    var enabled by remember(resolver) { mutableStateOf(readPreference()) }
    DisposableEffect(resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { enabled = readPreference() }
        }
        resolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE), false, observer)
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    CompositionLocalProvider(LocalKidMotion provides (enabled&&appEnabled), content = content)
}

private object GreetingFrames {
    val poses = listOf("wave-out", "wave-middle", "wave-in", "wink")
    // Decode only the selected companion; keep at most three complete sets.
    val cache = object : LruCache<String, List<ImageBitmap>>(12 * 1024 * 1024) {
        override fun sizeOf(key: String, value: List<ImageBitmap>) = value.sumOf { it.width * it.height * 4 }
    }
}

internal fun loadGreetingFrames(context:android.content.Context,character:String):List<ImageBitmap>? {
    if(Companions.all.none {it.id==character})return null
    return GreetingFrames.cache.get(character) ?: runCatching {
        GreetingFrames.poses.map {pose ->
            context.assets.open("ui/greetings/$character/$pose.webp").use {checkNotNull(BitmapFactory.decodeStream(it)).asImageBitmap()}
        }.also {GreetingFrames.cache.put(character,it)}
    }.getOrNull()
}

@Composable internal fun HomeGreeting(character: String, modifier: Modifier = Modifier, onComplete:(()->Unit)?=null, onWave:()->Unit={}) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val motion = LocalKidMotion.current
    var active by remember(lifecycle) { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, _ -> active = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    val frames by key(character) { produceState<List<ImageBitmap>?>(GreetingFrames.cache.get(character), character) {
        value = withContext(Dispatchers.IO) { loadGreetingFrames(context,character) }
    }
    }
    val complete by rememberUpdatedState(onComplete)
    val wave by rememberUpdatedState(onWave)
    var pose by remember(character) { mutableIntStateOf(0) }
    LaunchedEffect(character, active, motion, frames) {
        pose = 0
        if (!active) return@LaunchedEffect
        if (!motion || frames == null) {
            if(complete!=null){delay(if(!motion)650 else 1300);complete?.invoke()}
            return@LaunchedEffect
        }
        // Same 8-second sequence as CompanionWelcomeFrame.play / CompanionHomeGreeting.
        while (true) {
            pose = 0; delay(350)
            wave()
            repeat(2) { for (next in listOf(1, 2, 1, 0)) { pose = next; delay(160) } }
            delay(220); pose = 3; delay(400); pose = 0; delay(500)
            if(complete!=null){complete?.invoke();return@LaunchedEffect}
            delay(5250)
        }
    }
    val current = frames?.getOrNull(if (active && motion) pose else 0)
    if (current != null) Image(current, null, modifier)
    else DesignArt("companions/$character.png", modifier, decodeLimit = 512)
}
