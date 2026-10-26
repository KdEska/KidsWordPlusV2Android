package com.demarpsoftware.kidswordplus

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.demarpsoftware.kidswordplus.chess.*

/** A teaching preview only; never writes game or task progress. */
@Composable internal fun ChessEndingBoard(task:ChessEndingPuzzle,solved:Boolean,tablet:Boolean,tr:Boolean) {
    val motion=LocalKidMotion.current
    val helpOpen=LocalPageHelp.current?.showing==true
    val appearance=LocalChessAppearance.current?.value
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    var active by remember {mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))}
    var preview by remember {mutableStateOf<ChessMove?>(null)}
    var request by remember {mutableIntStateOf(0)}
    var settled by remember {mutableStateOf(false)}
    var selected by remember {mutableStateOf<Int?>(null)}
    val progress=remember {Animatable(0f)}
    DisposableEffect(lifecycle) {
        val observer=LifecycleEventObserver {_,_ ->
            active=lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            if(!active){preview=null;settled=false;selected=null}
        }
        lifecycle.addObserver(observer)
        onDispose {lifecycle.removeObserver(observer)}
    }
    LaunchedEffect(helpOpen,appearance,solved) {preview=null;settled=false;selected=null}
    val moves=if(solved)task.escapeMoves else emptyList()
    fun show(move:ChessMove) {
        if(move !in moves || !active || helpOpen)return
        preview=move;settled=false;selected=null;request++
    }
    LaunchedEffect(request,preview,motion,active,helpOpen,appearance) {
        val move=preview ?: return@LaunchedEffect
        if(!active || helpOpen)return@LaunchedEffect
        progress.snapTo(0f)
        if(motion)progress.animateTo(1f,tween(900,easing=FastOutSlowInEasing))
        if(preview==move)settled=true
    }
    val after=preview?.let(task.position::play)
    val visual=preview?.let {move -> task.position.board[move.from]?.let {piece ->
        ChessMoveVisual(task.position.board,listOf(ChessMovingPiece(move.from,move.to,piece)))
    }}
    val targets=if(preview==null)moves.filter {selected==null || it.from==selected}.map {it.to}.toSet() else emptySet()
    IosChessBoard(if(settled)after?.board ?: task.position.board else task.position.board,tablet,tr,
        selected=selected,target=preview?.to,legal=targets,enabled=moves.isNotEmpty(),
        animation=if(motion && !settled && visual!=null)ChessMatchAnimation(visual,progress)else null) {square ->
        if(preview!=null){preview=null;settled=false}
        val move=moves.firstOrNull {it.to==square && (selected==null || it.from==selected)}
        if(move!=null)show(move)
        else if(moves.any {it.from==square})selected=if(selected==square)null else square
    }
    if(moves.isNotEmpty()) KidSurface {
        Text(InterfaceStrings.text(tr,"Şahtan kurtuluş hamleleri","Legal escapes from check"),style=MaterialTheme.typography.titleMedium,color=KidHeading)
        Text(InterfaceStrings.text(tr,"Bir hamleye veya işaretli kareye dokun ve şahın nasıl kurtulduğunu gör.","Tap a move or a marked square to see how the king escapes."))
        moves.chunked(if(tablet)3 else 2).forEach {row ->
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                row.forEach {move ->
                    OutlinedButton(onClick={show(move)},modifier=Modifier.weight(1f)) {
                        Text("${squareName(move.from)} → ${squareName(move.to)}"+(if(preview==move)" ✓"else ""))
                    }
                }
            }
        }
        if(preview!=null)TextButton(onClick={preview=null;settled=false;selected=null}){Text(InterfaceStrings.text(tr,"İlk konuma dön","Reset position"))}
    }
}
