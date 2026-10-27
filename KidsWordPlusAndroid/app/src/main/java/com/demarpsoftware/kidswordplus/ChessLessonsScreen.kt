package com.demarpsoftware.kidswordplus

import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import com.demarpsoftware.kidswordplus.chess.*

@Composable internal fun ChessLessonsScreen(state: AppState, model: AppModel, tablet: Boolean, initialPractice: Boolean = false, back: () -> Unit) {
    val campaignPrefs=androidx.compose.ui.platform.LocalContext.current.getSharedPreferences("chess_campaigns_v1",android.content.Context.MODE_PRIVATE)
    val tr = state.ui == "tr"
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    var training by rememberSaveable { mutableStateOf(initialPractice) }
    var detail by rememberSaveable { mutableStateOf<String?>(null) }
    var tasksMode by rememberSaveable { mutableStateOf(true) }
    var freePiece by rememberSaveable { mutableStateOf(PieceKind.KING.name) }
    var detailTaskId by rememberSaveable { mutableStateOf(ChessLessons.baseTasks.first().id) }
    val roundSaver=remember { listSaver<ChessLessonRound,String>(save={it.save()},restore={ChessLessonRound.restore(it)}) }
    var round by rememberSaveable(stateSaver=roundSaver) { mutableStateOf(campaignPrefs.getString("training",null)?.split("|")?.let(ChessLessonRound::restore) ?: ChessLessonRound.create()) }
    LaunchedEffect(round){campaignPrefs.edit().putString("training",round.save().joinToString("|")).apply()}
    val task=if(training && tasksMode)round.task else ChessLessons.byId.getValue(detailTaskId)
    val taskId=task.id
    var selected by rememberSaveable(taskId) { mutableStateOf(false) }
    val solved=round.solved
    val audio=LocalChessAudio.current
    var feedback by rememberSaveable(taskId) { mutableStateOf(0) }
    var hint by rememberSaveable(taskId) { mutableStateOf(false) }
    fun startRound() { round=ChessLessonRound.create(excluding=round.tasks.map {it.id}.toSet());selected=false;feedback=0;hint=false }
    fun chooseMode(index: Int) { if(index==1 && !tasksMode)startRound();tasksMode=index==1 }
    fun finish(from: Int,to: Int) {
        if (round.solved) return
        val next=round.finish(from,to)
        if(!next.solved) {feedback=2;return}
        round=next;selected=false;feedback=3;audio?.celebrate(state.ui);model.finishChessLesson(task.id,from,to);audio?.touch()
    }
    PageHelp(if(training)"practice"else if(detail!=null)"piece"else "pieces")
    ScreenTitle(if(training)t("Alıştırma Yap","Practise") else detail?.let {pieceTitle(PieceKind.valueOf(it),tr)} ?: t("Taşları Tanı","Meet the Pieces"))
    BackHandler(training || detail != null) { if(training){if(initialPractice&&detail==null)back()else training=false}else detail=null }
    ChessPageNavigation({if(training){if(initialPractice&&detail==null)back()else training=false}else if(detail!=null)detail=null else back()},hideTabs=training||detail!=null)
    if(!training) {
        Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(20.dp)) {
            if(detail==null) {
                ChessLanguagePill(tr)
                ChessAppearancePicker(tr)
                val fontScale = LocalDensity.current.fontScale
                BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment=Alignment.TopCenter) {
                val gridWidth = minOf(maxWidth, if(tablet) 820.dp else 600.dp)
                val minimum = if(fontScale >= 1.6f) 280.dp else 145.dp
                val columns = ((gridWidth + 16.dp) / (minimum + 16.dp)).toInt().coerceAtLeast(1)
                Column(Modifier.width(gridWidth), verticalArrangement=Arrangement.spacedBy(16.dp)) {
                PieceKind.entries.chunked(columns).forEach {row->Row(horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                    row.forEach {kind->Card(onClick={detail=kind.name;detailTaskId=ChessLessons.baseTasks.first {it.kind==kind}.id},modifier=Modifier.weight(1f),shape=RoundedCornerShape(26.dp),colors=CardDefaults.cardColors(containerColor=Color.White),border=androidx.compose.foundation.BorderStroke(2.dp,pieceCardColor(kind).copy(alpha=.3f))) {
                        Column(Modifier.padding(vertical=20.dp,horizontal=10.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.fillMaxWidth().height(125.dp),contentAlignment=Alignment.Center){ChessPieceImage(Piece(kind,true))}
                            Text(pieceTitle(kind,tr),style=MaterialTheme.typography.titleLarge,color=KidHeading)
                            Canvas(Modifier.size(28.dp)) {
                                val color=pieceCardColor(kind)
                                drawCircle(color)
                                val w=size.width
                                drawLine(Color.White,Offset(w*.26f,w*.5f),Offset(w*.72f,w*.5f),w*.09f,StrokeCap.Round)
                                drawLine(Color.White,Offset(w*.52f,w*.3f),Offset(w*.72f,w*.5f),w*.09f,StrokeCap.Round)
                                drawLine(Color.White,Offset(w*.52f,w*.7f),Offset(w*.72f,w*.5f),w*.09f,StrokeCap.Round)
                            }
                        }
                    }}
                    repeat(columns-row.size) { Spacer(Modifier.weight(1f)) }
                }}
                }
                }
            } else {
                val kind=PieceKind.valueOf(detail!!)
                Box(Modifier.fillMaxWidth().height(150.dp),contentAlignment=Alignment.Center){ChessPieceImage(Piece(kind,true))}
                Text(pieceTitle(kind,tr),style=MaterialTheme.typography.headlineLarge,color=KidHeading)
                ChessListenButton(state.ui,kind.name.lowercase()+".name")
                KidSurface {
                    Text(t("Nasıl hareket eder?","How does it move?"),style=MaterialTheme.typography.titleLarge,color=KidHeading)
                    IosChessBoard(task.position.board,tablet,tr,legal=task.position.movesFrom(task.origin).map{it.to}.toSet(),enabled=false){}
                    KidAction(t("Sıra sende!","Your turn!"),tone=2){freePiece=kind.name;tasksMode=false;training=true}
                    Text(lessonText(kind,tr))
                    ChessListenButton(state.ui,kind.name.lowercase()+".move")
                }

            }
        }
        return
    }
    ChessStopSpeechOnChange(taskId, state.ui, tasksMode)
    if (!tasksMode) {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement=Arrangement.spacedBy(12.dp)) {
            ChessSegmented(listOf(t("Serbest", "Free"), t("Görevler", "Tasks")), 0) { chooseMode(it) }
            ChessFreePracticeContent(PieceKind.valueOf(freePiece), tablet, state.ui) { freePiece=it.name }
        }
        return
    }
    val position = remember(taskId) { task.position }
    val legal = remember(taskId) { position.movesFrom(task.origin).map { it.to }.toSet() }
    val board = if (solved) position.board.toMutableList().apply { this[task.target] = this[task.origin]; this[task.origin] = null } else position.board
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ChessSegmented(listOf(t("Serbest", "Free"), t("Görevler", "Tasks")), 1) { chooseMode(it) }
        Text(InterfaceStrings.render(tr,"Bölüm {0} / 10 · Görev {1} / 10","Stage {0} / 10 · Task {1} / 10",round.block.toString(),round.blockTask.toString()),style=MaterialTheme.typography.titleMedium,color=KidHeading)
        ChessProgress(round.blockCompleted,10,tr)
        Text("${round.completed} / 100",color=KidSecondary)
        val adBreak=LocalGameAdBreak.current
        if(solved) KidAction(if(round.finished)t("Yeni tur","New round")else if(round.blockFinished)t("Sonraki 10 göreve geç","Next 10 tasks")else t("Sonraki hedef","Next goal"),tone=2,enabled=!state.busy) {
            adBreak { if(round.finished)startRound() else round=round.advance() }
        }
        ChessListenButton(state.ui, if (task.capture) "training.capture" else "training.target", mapOf("piece" to task.kind.name.lowercase(), "square" to squareName(task.target)), label = t("Hedefi dinle", "Listen to the goal"))
        ChessAudioStatus(state.ui)
        if (task.id.startsWith("jump")) Text(t("At, önündeki taşların üzerinden atlayabilir.", "The knight can jump over the pieces in its way."))
        ChessInstruction(if (task.capture) InterfaceStrings.render(tr, "{0} ile {1} karesindeki rakip taşı al.", "Use the White {0} to capture the Black piece on {1}.", (pieceTitle(task.kind,tr)).toString(), (squareName(task.target)).toString())
            else InterfaceStrings.render(tr, "{0} taşını {1} karesine götür.", "Move the White {0} to {1}.", (pieceTitle(task.kind,tr)).toString(), (squareName(task.target)).toString()))
        ChessAppearancePicker(tr)
        IosChessBoard(board,tablet,tr,if(selected)task.origin else null,if(hint||solved)task.target else null,legal=if(selected)legal else emptySet(),enabled=!solved&&!state.busy,drag={from,to ->
            finish(from,to)
        }) {index ->
            when {
                index==task.origin->{selected=!selected;feedback=0;audio?.touch()}
                !selected->feedback=1
                task.accepts(task.origin,index)->finish(task.origin,index)
                else->feedback=2
            }
        }
        Text(when (feedback) {
            1 -> t("Önce Beyaz taşı seç.", "Select the White piece first.")
            2 -> InterfaceStrings.render(tr, "Tekrar dene. Hedef {0}; taşın hareket kuralına dikkat et.", "Try again. The goal is {0}; follow the piece's movement rule.", (squareName(task.target)).toString())
            3 -> t("Harika, hedefe ulaştın!", "You reached the goal!")
            else -> t("Bu bir hareket alıştırmasıdır; sıra değişmez. Rok ve geçerken alma bu derste yoktur.", "This is a movement lesson; turns do not alternate. Castling and en passant are not part of this lesson.")
        })
        if (solved && task.id !in state.chessLessonCompleted) {
            Text(t("Hedef henüz kaydedilmedi.", "The goal has not been saved yet."))
            OutlinedButton(enabled = !state.busy, onClick = { model.finishChessLesson(task.id, task.origin, task.target) }) { Text(t("Kaydı tekrar dene", "Retry saving")) }
        }
        if (!solved) KidAction(if(hint)t("İpucunu gizle","Hide hint")else t("Hedefi göster","Show target"),enabled=!state.busy,tone=1,icon="hint"){hint=!hint}
        if(round.finished) KidSurface {
            Text(t("Harika! 100 görevin hepsini tamamladın.","Well done! You completed all 100 goals."),style=MaterialTheme.typography.titleMedium,color=KidSuccess)
            FamilyShareButton(t("Alıştırma Yap","Practise"),t("Tamamlandı · 100 görev","Completed · 100 goals"),tr)
        }
        

    }
}
private fun lessonText(kind: PieceKind, tr: Boolean): String = when (kind) {
    PieceKind.KING -> InterfaceStrings.text(tr,"Şah her yönde bir kare ilerler. Gerçek oyunda tehdit altındaki kareye gidemez.","The king moves one square in any direction. In a match it cannot move into check.")
    PieceKind.QUEEN -> InterfaceStrings.text(tr,"Vezir yatay, dikey ve çapraz ilerler. Taşların üzerinden atlayamaz.","The queen moves along ranks, files and diagonals. It cannot jump over pieces.")
    PieceKind.ROOK -> InterfaceStrings.text(tr,"Kale yatay ve dikey ilerler. Önündeki taşların üzerinden atlayamaz.","The rook moves along ranks and files. It cannot jump over pieces.")
    PieceKind.BISHOP -> InterfaceStrings.text(tr,"Fil çapraz ilerler ve hep aynı renkteki karelerde kalır. Taşların üzerinden atlayamaz.","The bishop moves diagonally and stays on the same square colour. It cannot jump over pieces.")
    PieceKind.KNIGHT -> InterfaceStrings.text(tr,"At iki kare bir yöne, bir kare yana gider: L çizer. Taşların üzerinden atlayabilir.","The knight moves two squares one way and one sideways, making an L. It can jump over pieces.")
    PieceKind.PAWN -> InterfaceStrings.text(tr,"Beyaz piyon bir kare ileri gider; başlangıç karesinden önü boşsa iki kare gidebilir. Taşı bir kare çapraz ilerleyerek alır, geri gidemez.","A White pawn moves one square forward, or two from its starting rank if clear. It captures one square diagonally forward and cannot move backwards.")
}

private fun pieceCardColor(kind: PieceKind): Color = when(kind) {
    PieceKind.KING -> Color(0xFFAF52DE)
    PieceKind.QUEEN -> Color(0xFFFF2D55)
    PieceKind.ROOK -> Color(0xFF007AFF)
    PieceKind.BISHOP -> Color(0xFF30B0C7)
    PieceKind.KNIGHT -> Color(0xFFFF9500)
    PieceKind.PAWN -> Color(0xFF34C759)
}

@Composable private fun ChessFreePracticeContent(piece: PieceKind, tablet: Boolean, language: String, choose: (PieceKind) -> Unit) {
    val tr=language=="tr"
    var currentKind by rememberSaveable(piece) { mutableStateOf(piece.name) }
    var location by rememberSaveable(piece) { mutableStateOf(ChessFreePractice.start(piece).location) }
    var selected by rememberSaveable(piece) { mutableStateOf(false) }
    var feedback by rememberSaveable(piece) { mutableStateOf(0) }
    val audio=LocalChessAudio.current
    val position=ChessFreePractice(PieceKind.valueOf(currentKind),location)
    ChessStopSpeechOnChange(piece, language)
    ChessPieceStrip(piece,true,tr,choose=choose)
    ChessInstruction(InterfaceStrings.text(tr,"Taşınla hareketleri keşfet. Bu bir hareket alıştırmasıdır.","Explore how your piece moves. This is a movement exercise."))
    ChessListenButton(language,"training.free")
    ChessAudioStatus(language)
    ChessAppearancePicker(tr)
    IosChessBoard(position.board,tablet,tr,selected=if(selected)location else null,legal=if(selected)position.destinations else emptySet(),drag={from,to ->
        if(from==location) {
            val next=position.move(to)
            if(next==null) feedback=2 else {
                feedback=if(position.kind==PieceKind.PAWN && next.kind==PieceKind.QUEEN) 3 else 0
                currentKind=next.kind.name;location=next.location;selected=true;audio?.touch()
            }
        }
    }) { target ->
        if(target==location) { selected=true;feedback=0;audio?.touch() }
        else if(!selected) feedback=1
        else {
            val next=position.move(target)
            if(next==null) feedback=2 else {
                feedback=if(position.kind==PieceKind.PAWN && next.kind==PieceKind.QUEEN) 3 else 0
                currentKind=next.kind.name;location=next.location;audio?.touch()
            }
        }
    }
    Text(when(feedback) {
        1 -> InterfaceStrings.text(tr,"Önce Beyaz taşı seç.","Select the White piece first.")
        2 -> InterfaceStrings.text(tr,"Tekrar dene. Taşın hareket kuralına dikkat et.","Try again. Follow the piece's movement rule.")
        3 -> InterfaceStrings.text(tr,"Piyon son sıraya ulaştı ve bu alıştırmada vezire dönüştü.","The pawn reached the last rank and became a queen in this exercise.")
        else -> InterfaceStrings.text(tr,"Taşı seç; gidebileceğin kareleri gör.","Select the piece to see where it can move.")
    },color=KidBody)
    ChessListenButton(language,currentKind.lowercase()+".move",label=InterfaceStrings.text(tr,"Hareket kuralını dinle","Listen to the movement rule"))
    OutlinedButton(onClick={currentKind=piece.name;location=ChessFreePractice.start(piece).location;selected=false;feedback=0}) {
        Text(InterfaceStrings.text(tr,"Sıfırla","Reset"))
    }
}
