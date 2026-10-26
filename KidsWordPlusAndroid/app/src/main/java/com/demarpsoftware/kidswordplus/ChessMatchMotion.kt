package com.demarpsoftware.kidswordplus

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.demarpsoftware.kidswordplus.chess.*
import kotlinx.coroutines.delay

internal data class ChessMatchAnimation(val visual: ChessMoveVisual,val progress: Animatable<Float,AnimationVector1D>)
internal data class ChessMatchMotion(val animation: ChessMatchAnimation?,val foreground: Boolean)

/** Cosmetic only: cancellation never applies, undoes or repeats a saved move. */
@Composable internal fun rememberChessMatchMotion(game: ChessGame?, visible: Boolean, flipped: Boolean, enabled:Boolean=true): ChessMatchMotion {
    val motion=LocalKidMotion.current && enabled
    val appearance=LocalChessAppearance.current?.value
    var displayed by remember(game?.id,visible,flipped,appearance) { mutableStateOf(game) }
    val latestGame by rememberUpdatedState(game)
    val owner=LocalLifecycleOwner.current
    var foreground by remember(owner) { mutableStateOf(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) }
    DisposableEffect(owner) {
        val observer=LifecycleEventObserver {_,event ->
            when(event) {
                Lifecycle.Event.ON_STOP -> {foreground=false;displayed=latestGame}
                Lifecycle.Event.ON_START -> {foreground=true;displayed=latestGame}
                else -> Unit
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose {owner.lifecycle.removeObserver(observer)}
    }
    val visual=remember(displayed?.id,displayed?.history,game?.id,game?.history,visible,motion,foreground) {
        if(visible && motion && foreground)ChessMoveVisual.between(displayed,game) else null
    }
    val progress=remember(visual) {Animatable(0f)}
    LaunchedEffect(visual,game?.id,game?.history,visible,motion,foreground,flipped,appearance) {
        if(visual!=null) {
            delay(450)
            progress.animateTo(1f,tween(900,easing=FastOutSlowInEasing))
        }
        displayed=latestGame
    }
    return ChessMatchMotion(visual?.let {ChessMatchAnimation(it,progress)},foreground)
}
