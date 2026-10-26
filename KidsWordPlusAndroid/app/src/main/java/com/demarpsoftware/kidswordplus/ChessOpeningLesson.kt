package com.demarpsoftware.kidswordplus

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.demarpsoftware.kidswordplus.chess.*
import kotlin.math.roundToInt

// Pending moves are deliberately transient: backgrounding or leaving cancels them.
private data class OpeningMotion(val count: Int, val practice: Boolean, val move: ChessMove)

@Composable internal fun ChessOpeningLesson(state: AppState, model: AppModel, opening: ChessOpening, tablet: Boolean, back: () -> Unit) {
    val adBreak=LocalGameAdBreak.current
    val tr = state.ui == "tr"
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    val copy by openingCopy(state.ui)
    val audio = LocalChessAudio.current
    ScreenTitle(if (tr) opening.tr else opening.en)
    var practice by rememberSaveable(opening.id) { mutableStateOf(false) }
    var watchCount by rememberSaveable(opening.id) { mutableIntStateOf(0) }
    val count = if (practice) state.chessOpeningProgress.count(opening.id) else watchCount
    val position = remember(opening.id, count) { opening.position(count) }
    val step = opening.steps.getOrNull(count)
    val finished = step == null
    var selected by remember(opening.id, practice, count) { mutableStateOf<Int?>(null) }
    var hint by remember(opening.id, practice, count) { mutableStateOf(false) }
    var retry by remember(opening.id, practice, count) { mutableStateOf(false) }
    var pending by remember(opening.id) { mutableStateOf<OpeningMotion?>(null) }
    val progress = remember(opening.id) { Animatable(0f) }
    val motionEnabled = LocalKidMotion.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var active by remember(lifecycle) { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, _ ->
            active = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            if (!active) { pending = null; audio?.stop() }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); audio?.stop() }
    }
    val latestCount by rememberUpdatedState(count)
    val latestPractice by rememberUpdatedState(practice)
    val helpOpen=LocalPageHelp.current?.showing==true
    val enabled = active && !helpOpen && !state.busy && !state.chessOpeningSaveError
    ChessStopSpeechOnChange(opening.id, count, practice)
    fun advance() {
        if (!enabled || pending != null || step == null) return
        audio?.stop(); retry = false; selected = null; hint = false
        pending = OpeningMotion(count, practice, step.move)
    }
    LaunchedEffect(pending, motionEnabled, active,helpOpen) {
        val move = pending ?: return@LaunchedEffect
        if (!active || helpOpen) { pending = null; return@LaunchedEffect }
        progress.snapTo(0f)
        if (motionEnabled) progress.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        // Re-check after suspension so an obsolete frame cannot advance a new mode/step.
        if (pending == move && move.count == latestCount && move.practice == latestPractice && active) {
            if (move.practice) model.playOpening(opening.id, move.count, move.move)
            else watchCount = move.count + 1
            pending = null
        }
    }
    fun reset() {
        pending = null; selected = null; hint = false; retry = false; audio?.stop()
        if (practice) model.rewindOpening(opening.id, restart = true) else watchCount = 0
    }
    @Composable fun Header() {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ChessPageNavigation({pending=null;back()})
            Spacer(Modifier.weight(1f))
            TextButton(enabled = !state.busy, onClick = ::reset) { Text(t("Baştan", "Reset")) }
        }
        Text(t("Kısa örnek dizi", "Short example line"), Modifier.fillMaxWidth(), color = KidSecondary, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        Row(Modifier.fillMaxWidth().background(Color(0xFFECEAF0), RoundedCornerShape(14.dp)).padding(3.dp)) {
            listOf(false to t("İzle", "Watch"), true to t("Sıra sende", "Your turn")).forEach { (value, title) ->
                Box(Modifier.weight(1f).clip(RoundedCornerShape(11.dp)).background(if (practice == value) Color.White else Color.Transparent)
                    .semantics { this.selected = practice == value }.clickable(enabled = !state.busy, role = Role.RadioButton) {
                        pending = null; audio?.stop(); selected = null; hint = false; retry = false
                        practice = value
                    }.padding(horizontal = 10.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = KidHeading)
                }
            }
        }
        ChessAppearancePicker(tr)
        if (practice) Text(t("Sırası gelen taşı seç, sonra gideceği kareye dokun.", "Select the next piece, then tap its destination."), Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = KidSecondary, style = MaterialTheme.typography.bodyMedium)
        if (state.chessOpeningSaveError) Text(t("Açılış ilerlemesi okunamadı; mevcut kayıt korunuyor.", "Opening progress could not be read; the existing record has been preserved."), color = MaterialTheme.colorScheme.error)
    }
    @Composable fun Board(limit: Dp = if (tablet) 760.dp else 420.dp) {
        Row(Modifier.fillMaxWidth()) {
            Text("$count / ${opening.steps.size}", Modifier.weight(1f), fontWeight = FontWeight.Bold)
            if (!finished) Text(if (position.whiteTurn) t("Beyaz", "White") else t("Siyah", "Black"), fontWeight = FontWeight.Bold)
        }
        OpeningMotionBoard(position, pending?.move, { progress.value }, opening.steps.getOrNull(count-1)?.move, selected,
            if (practice && hint) step?.move else null, enabled && practice && pending == null && !finished, tablet, tr, limit) { index ->
            if (selected == index) { selected = null; return@OpeningMotionBoard }
            if (step != null && selected == step.move.from && index == step.move.to) { advance(); return@OpeningMotionBoard }
            if (position.board[index]?.white == position.whiteTurn) { selected = index; retry = index != step?.move?.from }
            else retry = true
        }
    }
    @Composable fun Actions() {
        if (retry) Text(t("Bir daha dene. Bu derste gösterilen hamleyi bul.", "Try again. Find the move shown in this lesson."), color = KidHeading)
        when {
            finished -> {
                Text(t("Örnek dizi tamamlandı!", "Example line complete!"), color = KidSuccess, style = MaterialTheme.typography.titleLarge)
                KidAction(if (practice) t("Yeniden çalış", "Practise again") else t("Baştan alıştırma yap", "Practise from the start"), enabled && pending == null, tone = 2) {
                    adBreak { pending = null; practice = true; model.rewindOpening(opening.id, restart = true) }
                }
            }
            practice -> {
                KidAction(if (hint) t("İpucunu gizle", "Hide hint") else t("İpucu", "Hint"), enabled && pending == null, tone = 1) { hint = !hint; retry = false }
                if (hint) Text("${squareName(step.move.from)} → ${squareName(step.move.to)}", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
            }
            else -> {
                Text(opening.notation, Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                KidAction(t("Sonraki hamle", "Next move"), enabled && pending == null, onClick = ::advance)
            }
        }
        if (count > 0 && !practice) KidAction(t("Önceki hamle", "Previous move"), enabled && pending == null, tone = 1) { audio?.stop(); watchCount-- }
        if (count > 0 && practice) Text(t("Alıştırma ilerlemen kaydedildi.", "Your practice progress is saved."), color = KidSuccess, style = MaterialTheme.typography.bodyMedium)
    }
    @Composable fun Explanation() {
        ChessListenButton(state.ui, "opening.${opening.id}.body")
        KidSurface { Text(copy["opening.${opening.id}.body"].orEmpty()) }
        if (count > 0) {
            val previous = opening.steps[count-1]
            KidSurface {
                Text("${squareName(previous.move.from)} → ${squareName(previous.move.to)}", style = MaterialTheme.typography.titleMedium)
                Text(copy["opening.step.${previous.idea}"].orEmpty())
                ChessListenButton(state.ui, "opening.step.${previous.idea}")
            }
        }
        ChessAudioStatus(state.ui)
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (tablet && maxWidth > maxHeight) {
            val sidebarWidth = minOf(320.dp, maxWidth * .34f)
            val boardLimit = maxOf(180.dp, maxHeight - 56.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(Modifier.width(sidebarWidth).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) { Header(); Actions(); Explanation() }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) { Board(boardLimit) }
            }
        } else {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) { Header(); Board(); Actions(); Explanation() }
        }
    }
}

@Composable private fun OpeningMotionBoard(position: ChessPosition, motion: ChessMove?, fraction: () -> Float, last: ChessMove?, selected: Int?, hint: ChessMove?, enabled: Boolean, tablet: Boolean, tr: Boolean, limit: Dp, tap: (Int) -> Unit) {
    val density = LocalDensity.current
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        val width = minOf(maxWidth, limit, if (tablet) 760.dp else 420.dp)
        val cell = width / 8
        Column(Modifier.width(width), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))) {
                Column {
                    repeat(8) { row -> Row {
                        repeat(8) { column ->
                            val index = row*8+column
                            val actor = position.board[index]
                            val marked = index == selected || index == (motion ?: last)?.from || index == (motion ?: last)?.to
                            val target = index == hint?.from || index == hint?.to
                            val label = squareName(index) + (actor?.let { ", " + (if(it.white) { InterfaceStrings.text(tr,"Beyaz ","White ") } else { InterfaceStrings.text(tr,"Siyah ","Black ") }) + pieceTitle(it.kind,tr) } ?: InterfaceStrings.text(tr,", boş",", empty"))
                            Box(Modifier.size(cell).background(if((row+column)%2==0)Color(0xFFF7E8C9) else Color(0xFF704F9E)).semantics { contentDescription = label }
                                .clickable(enabled=enabled,role=Role.Button){tap(index)}) {
                                if (actor != null && motion?.from != index) ChessPieceImage(actor, inset = cell * .05f)
                                if (marked || target) Box(Modifier.matchParentSize().padding(2.dp).border(if(target)4.dp else 3.dp,if(target)KidSuccess else Color.Cyan))
                                if (column == 0) Text("${8-row}",Modifier.background(Color.White.copy(alpha=.8f)).padding(2.dp),fontSize=9.sp,lineHeight=10.sp,color=Color.Black,fontWeight=FontWeight.Bold)
                            }
                        }
                    } }
                }
                motion?.let { move -> position.board[move.from]?.let { actor ->
                    Box(Modifier.offset {
                        val f = fraction()
                        val px = with(density){cell.toPx()}
                        IntOffset(((move.from%8+(move.to%8-move.from%8)*f)*px).roundToInt(),((move.from/8+(move.to/8-move.from/8)*f)*px).roundToInt())
                    }.size(cell)) { ChessPieceImage(actor,inset=cell*.05f) }
                } }
            }
            Row { repeat(8) { index -> Text(('a'+index).toString(),Modifier.weight(1f),textAlign=TextAlign.Center,fontSize=12.sp,fontWeight=FontWeight.Bold,color=KidBody) } }
        }
    }
}
