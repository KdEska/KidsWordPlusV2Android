package com.demarpsoftware.kidswordplus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.demarpsoftware.kidswordplus.chess.*

@Composable internal fun ChessPuzzlesScreen(state: AppState, model: AppModel, tablet: Boolean, initialMate: Boolean = false, back: () -> Unit) {
    val campaignPrefs=androidx.compose.ui.platform.LocalContext.current.getSharedPreferences("chess_campaigns_v1",android.content.Context.MODE_PRIVATE)
    val tr = state.ui == "tr"
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    val mateMode=initialMate
    PageHelp(if(mateMode)"mate"else "endings")
    ScreenTitle(if(mateMode)t("Tek Hamlede Mat","Mate in One") else t("Mat ve Pat","Checkmate and Stalemate"))
    val endingSaver=remember {listSaver<ChessPuzzleRound,String>(save={it.save()},restore={ChessPuzzleRound.restore(it,ChessPuzzles.endingPool)})}
    val mateSaver=remember {listSaver<ChessPuzzleRound,String>(save={it.save()},restore={ChessPuzzleRound.restore(it,ChessPuzzles.matePool)})}
    var endingRound by rememberSaveable(stateSaver=endingSaver) {mutableStateOf(campaignPrefs.getString("ending",null)?.split("|")?.let{ChessPuzzleRound.restore(it,ChessPuzzles.endingPool)} ?: ChessPuzzleRound.create(ChessPuzzles.endingPool))}
    var mateRound by rememberSaveable(stateSaver=mateSaver) {mutableStateOf(campaignPrefs.getString("mate",null)?.split("|")?.let{ChessPuzzleRound.restore(it,ChessPuzzles.matePool)} ?: ChessPuzzleRound.create(ChessPuzzles.matePool))}
    LaunchedEffect(endingRound,mateRound){campaignPrefs.edit().putString("ending",endingRound.save().joinToString("|")).putString("mate",mateRound.save().joinToString("|")).apply()}
    val round=if(mateMode)mateRound else endingRound
    val ending=ChessPuzzles.endingById.getValue(endingRound.taskId)
    val mate=ChessPuzzles.mateById.getValue(mateRound.taskId)
    val id=round.taskId
    fun newRound() {if(mateMode)mateRound=ChessPuzzleRound.create(ChessPuzzles.matePool,mateRound.ids.toSet())else endingRound=ChessPuzzleRound.create(ChessPuzzles.endingPool,endingRound.ids.toSet())}
    val adBreak=LocalGameAdBreak.current
    fun next() {adBreak { if(round.finished)newRound()else if(mateMode)mateRound=mateRound.advance()else endingRound=endingRound.advance() }}
    ChessStopSpeechOnChange(id, state.ui)
    var selected by rememberSaveable(mateRound.taskId) { mutableStateOf<Int?>(null) }
    var solvedFrom by rememberSaveable(mateRound.taskId) { mutableStateOf<Int?>(null) }
    var solvedTo by rememberSaveable(mateRound.taskId) { mutableStateOf<Int?>(null) }
    var feedback by rememberSaveable(id) { mutableStateOf(0) }
    val solvedMove = if (solvedFrom != null && solvedTo != null) ChessMove(solvedFrom!!, solvedTo!!) else null
    val solved=round.solved
    val after = remember(id, solvedMove) { solvedMove?.let(mate.position::play) }
    val position = if (mateMode) after ?: mate.position else ending.position
    fun save() {
        if (mateMode && solvedMove != null) model.solveChessMate(id, solvedMove)
        else if (!mateMode && solved) model.answerChessEnding(id, ending.answer)
    }
    val resetAudio=LocalChessAudio.current
    fun attempt(from: Int, to: Int) {
        if(mateRound.solved)return
        val move=ChessMove(from,to)
        when {
            mate.position.play(move)==null -> feedback=2
            mate.accepts(move) -> {selected=null;solvedFrom=from;solvedTo=to;mateRound=mateRound.finish(id);feedback=4;resetAudio?.celebrate(state.ui);model.solveChessMate(id,move)}
            else -> feedback=3
        }
    }
    val nextLabel=if(round.finished)t("Yeni 100 görev","100 new tasks")else if(round.blockFinished)t("Sonraki 10 göreve geç","Next 10 tasks")else t("Sonraki görev","Next task")
    val shareTitle=if(mateMode)t("Tek Hamlede Mat","Mate in One")else t("Mat ve Pat","Checkmate and Stalemate")
    fun completion()=t("Harika! 100 görevin hepsini tamamladın.","Well done! You completed all 100 goals.")
    ChessPageNavigation(back,reset=if(mateMode){{if(!solved){resetAudio?.stop();selected=null;feedback=0}}}else null)
    Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.TopCenter) {
    Column(Modifier.widthIn(max=if(tablet)820.dp else 600.dp).fillMaxWidth().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(if(mateMode)14.dp else 16.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
            Text(InterfaceStrings.render(tr,"Bölüm {0} / 10 · Görev {1} / 10","Stage {0} / 10 · Task {1} / 10",round.block.toString(),round.blockTask.toString()),style=MaterialTheme.typography.titleMedium)
            Text("✓ ${round.completed} / ${ChessPuzzleRound.COUNT}",style=MaterialTheme.typography.titleMedium,color=KidHeading)
        }
        LinearProgressIndicator(progress={round.blockCompleted/10f},modifier=Modifier.fillMaxWidth(),color=KidHeading,drawStopIndicator={})
        if(mateMode) {
            if(solved) {
                Text(t("Harika! Siyah şah tehdit altında ve kurtulabileceği geçerli hamle yok. Mat!","Great! The black king is attacked and has no legal escape. Checkmate!"),color=KidSuccess,style=MaterialTheme.typography.titleMedium,textAlign=TextAlign.Center)
                if(round.finished){Text(completion());FamilyShareButton(shareTitle,t("Tamamlandı · 100","Completed · 100"),tr)}
                KidAction(nextLabel,enabled=!state.busy,tone=2){next()}
            }else KidSurface {
                Text(t("Sıra beyazda. Beyaz taşını seç ve siyah şahı tek hamlede mat et.","White to move. Select a white piece and checkmate the black king in one move."),style=MaterialTheme.typography.titleMedium)
                ChessListenButton(state.ui,"mate.prompt")
            }
        }else Text(t("Sıra beyazda. Beyaz şahın durumunu seç.","White to move. Choose the white king’s situation."),style=MaterialTheme.typography.titleMedium,textAlign=TextAlign.Center)
        ChessAudioStatus(state.ui)
        ChessAppearancePicker(tr)
        if(!mateMode)key(ending.id){ChessEndingBoard(ending,solved,tablet,tr)}
        else IosChessBoard(position.board,tablet,tr,selected,if(solved)solvedTo else null,
            legal=if(!solved)selected?.let{mate.position.movesFrom(it).map(ChessMove::to).toSet()}.orEmpty()else emptySet(),
            enabled=!solved&&!state.busy,drag={from,to ->attempt(from,to)}) {index ->
            val piece=mate.position.board[index]
            if(piece?.white==true){selected=if(selected==index)null else index;feedback=0}
            else if(selected==null)feedback=1
            else attempt(selected!!,index)
        }
        if(!mateMode) {
            if(solved) {
                Text(t("Harika! Görevi tamamladın.","Great! You completed the task."),color=KidSuccess,style=MaterialTheme.typography.titleMedium)
                ChessInstruction(endingExplanation(ending.answer,tr))
                ChessListenButton(state.ui,"ending."+when(ending.answer){ChessEnding.CHECK->"check";ChessEnding.MATE->"mate";ChessEnding.STALEMATE->"stalemate"}+".body")
                if(round.finished){Text(completion());FamilyShareButton(shareTitle,t("Tamamlandı · 100","Completed · 100"),tr)}
                KidAction(nextLabel,enabled=!state.busy,tone=2){next()}
            }else {
                listOf(ChessEnding.MATE,ChessEnding.STALEMATE,ChessEnding.CHECK).forEach {choice ->
                    KidAction(endingTitle(choice,tr),enabled=!state.busy,tone=1){
                        if(ending.accepts(choice)){endingRound=endingRound.finish(id);feedback=4;resetAudio?.celebrate(state.ui);model.answerChessEnding(id,choice)}else feedback=5
                    }
                }
                if(feedback>0)Text(t("Tekrar bak: Şah tehdit altında mı? Geçerli hamlesi var mı?","Look again: Is the king attacked? Is there a legal move?"),style=MaterialTheme.typography.titleMedium,textAlign=TextAlign.Center)
            }
        }else if(!solved&&feedback>0)ChessInstruction(t("Bu hamle mat yapmıyor. Başka bir hamle dene.","This move does not give checkmate. Try another move."))
        if(solved&&id !in state.chessPuzzleCompleted) {
            Text(t("Hedef henüz kaydedilmedi.","The goal has not been saved yet."))
            OutlinedButton(enabled=!state.busy,onClick={save()}){Text(t("Kaydı tekrar dene","Retry saving"))}
        }
    }
    }
}

private fun endingTitle(ending: ChessEnding, tr: Boolean) = when (ending) {
    ChessEnding.CHECK -> InterfaceStrings.text(tr,"Şah","Check")
    ChessEnding.MATE -> InterfaceStrings.text(tr,"Mat","Checkmate")
    ChessEnding.STALEMATE -> InterfaceStrings.text(tr,"Pat","Stalemate")
}
private fun endingExplanation(ending: ChessEnding, tr: Boolean) = when (ending) {
    ChessEnding.CHECK -> InterfaceStrings.text(tr,"Şah tehdit altında ama güvenli bir hamle var. Oyun devam eder.","The king is attacked but has a safe move. The game continues.")
    ChessEnding.MATE -> InterfaceStrings.text(tr,"Şah tehdit altında ve geçerli hamle yok. Mat! Oyun biter.","The king is attacked and has no legal move. Checkmate! The game ends.")
    ChessEnding.STALEMATE -> InterfaceStrings.text(tr,"Şah tehdit altında değil ama geçerli hamle yok. Pat! Oyun berabere biter.","The king is not attacked but has no legal move. Stalemate! The game is a draw.")
}
