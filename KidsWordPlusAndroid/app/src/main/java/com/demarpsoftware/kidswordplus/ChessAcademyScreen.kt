package com.demarpsoftware.kidswordplus

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.demarpsoftware.kidswordplus.chess.*

@Composable internal fun ChessAcademyScreen(state: AppState, model: AppModel, tablet: Boolean, page: String, back: () -> Unit) {
    val campaignPrefs=androidx.compose.ui.platform.LocalContext.current.getSharedPreferences("chess_campaigns_v1",android.content.Context.MODE_PRIVATE)
    val tr = state.ui == "tr"
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    if(page=="setup"){ChessSetupScreen(state,model,tablet,back);return}
    val setup = page == "setup"
    PageHelp(if(setup)"setup"else "safety")
    ScreenTitle(if(setup)t("Tahtayı Hazırla","Set Up the Board") else t("Şahını Koru","Protect Your King"))
    val saver=remember {listSaver<ChessPuzzleRound,String>(save={it.save()},restore={ChessPuzzleRound.restore(it,ChessSafety.pool)})}
    var round by rememberSaveable(stateSaver=saver) {mutableStateOf(campaignPrefs.getString("safety",null)?.split("|")?.let{ChessPuzzleRound.restore(it,ChessSafety.pool)} ?: ChessPuzzleRound.create(ChessSafety.pool))}
    LaunchedEffect(round){campaignPrefs.edit().putString("safety",round.save().joinToString("|")).apply()}
    val taskId=round.taskId
    val task=ChessSafety.byId.getValue(taskId)
    fun newRound() {round=ChessPuzzleRound.create(ChessSafety.pool,round.ids.toSet())}
    var chosen by rememberSaveable(page, taskId, state.chessSetupCount) { mutableStateOf<String?>(null) }
    var selected by rememberSaveable(page, taskId) { mutableStateOf<Int?>(null) }
    val solved=round.solved
    val audio=LocalChessAudio.current
    var hint by rememberSaveable(page, taskId, state.chessSetupCount) { mutableStateOf(false) }
    var feedback by rememberSaveable(page, taskId, state.chessSetupCount) { mutableStateOf(0) }
    var confirmRestart by remember { mutableStateOf(false) }
    fun finishSafety(from:Int?,to:Int) {
        if(round.solved)return
        selected=null;round=round.finish(taskId);feedback=3
        audio?.celebrate(state.ui)
        model.finishChessSafety(task.id,from,to)
    }
    ChessStopSpeechOnChange(page, taskId, state.chessSetupCount, state.ui)
    val next = ChessBoardSetup.placements.getOrNull(state.chessSetupCount)
    val target = if (setup) next?.first else task.target
    val board = if (setup) ChessBoardSetup.board(state.chessSetupCount) else if (solved) task.after().board else task.position.board
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ChessPageNavigation(back)
        if (setup) {
            ChessProgress(state.chessSetupCount,32,tr)
            Text(t("Sağ alt köşe Beyaz karedir. Vezir kendi renginde başlar: Beyaz vezir d1, Siyah vezir d8.", "The bottom-right square is light. Each queen starts on her own colour: White on d1, Black on d8."))
            if (next != null) {
                Text(t("${if (next.second.white) "Beyaz" else "Siyah"} ${pieceTitle(next.second.kind, tr)} taşını seç ve ${squareName(next.first)} karesine yerleştir.", "Choose the ${if (next.second.white) "White" else "Black"} ${pieceTitle(next.second.kind, tr)} and place it on ${squareName(next.first)}."))
                ChessPieceStrip(chosen?.let(PieceKind::valueOf),next.second.white,tr,!state.busy){chosen=it.name;feedback=0}

            } else {
                Text(t("Tahta hazır! Tüm taşları doğru yerleştirdin.", "The board is ready! Every piece is in the right place."), style = MaterialTheme.typography.titleLarge)
                FamilyShareButton(t("Tahtayı Hazırla","Set Up the Board"),t("Tamamlandı","Completed"),tr,board,symbol=FamilyShareSymbol.BOARD)
            }
        } else {
            ChessPuzzleRoundHeader(round,tr,state.busy){if(round.finished)newRound()else round=round.advance()}
            ChessInstruction(safetyPrompt(task,tr))
            if (!solved) Text(t("Beyaz şah tehdit altında: şah!", "The White king is under attack: check!"), color = MaterialTheme.colorScheme.error)
        }
        if (setup && next != null) ChessListenButton(state.ui, "setup.place", mapOf("piece" to next.second.kind.name.lowercase(), "square" to squareName(next.first)))
        else if (setup) ChessListenButton(state.ui, "setup.complete")
        else ChessListenButton(state.ui,task.audioKey,label=if(task.id==task.group || task.source==null)t("Dinle","Listen")else t("Hareket kuralını dinle","Listen to the movement rule"))
        ChessAudioStatus(state.ui)
        ChessAppearancePicker(tr)
        AcademyBoard(board, tablet, tr, selected, if (hint || solved) target else null, !state.busy && (if (setup) next != null else !solved),drag=if(!setup && task.source!=null) {from,to ->
            if(task.accepts(from,to)) {finishSafety(from,to)} else feedback=2
        } else null) { index ->
            if (setup) {
                val kind = chosen?.let(PieceKind::valueOf)
                when {
                    kind == null -> feedback = 1
                    ChessBoardSetup.place(state.chessSetupCount, kind, index) == state.chessSetupCount -> feedback = 2
                    else -> model.placeChessPiece(state.chessSetupCount, kind, index)
                }
            } else {
                val from = if (task.source == null) null else selected
                when {
                    task.accepts(from, index) -> { finishSafety(from,index) }
                    task.source != null && board[index]?.white == true -> { selected = index; feedback = 0 }
                    else -> feedback = 2
                }
            }
        }
        if (feedback > 0) Text(when (feedback) {
            1 -> t("Önce yerleştireceğin taşı seç.", "Choose a piece first.")
            3 -> if (task.source == null) t("Doğru! Şahı tehdit eden taşı buldun.", "Correct! You found the piece attacking the king.") else t("Doğru! Beyaz şah artık güvende.", "Correct! The White king is safe now.")
            else -> if (setup) t("Tekrar dene: istenen taşı ve kareyi kontrol et.", "Try again: check the requested piece and square.") else t("Tekrar dene. Bu hedefte ${if (task.source == null) "${squareName(task.target)} karesindeki saldıran taşı bulmalısın" else "${squareName(task.source)} karesindeki taşı ${squareName(task.target)} karesine götürmelisin"}.", "Try again. For this goal, ${if (task.source == null) "find the attacker on ${squareName(task.target)}" else "move the piece on ${squareName(task.source)} to ${squareName(task.target)}"}.")
        })
        if (!setup && solved && task.id !in state.chessSafetyCompleted) {
            Text(t("Hedef henüz kaydedilmedi.", "The goal has not been saved yet."))
            OutlinedButton(enabled = !state.busy, onClick = { model.finishChessSafety(task.id, task.source, task.target) }) { Text(t("Kaydı tekrar dene", "Retry saving")) }
        }
        if ((setup && next != null) || (!setup && !solved)) OutlinedButton(onClick = { hint = !hint }) {
            Text(if (hint) t("İpucunu gizle", "Hide hint") else t("Hedefi göster", "Show target"))
        }
        if(!setup && round.finished) KidSurface {
            Text(t("Harika! 100 görevin hepsini tamamladın.","Well done! You completed all 100 goals."),color=KidSuccess)
            FamilyShareButton(t("Şahını Koru","Protect Your King"),t("Tamamlandı · 100 görev","Completed · 100 goals"),tr)
        }
        
        if (setup && state.chessSetupCount > 0) TextButton(enabled = !state.busy, onClick = { confirmRestart = true }) { Text(t("Dizime yeniden başla", "Restart setup")) }
    }
    if (confirmRestart) AlertDialog(onDismissRequest = { confirmRestart = false }, title = { Text(t("Dizime yeniden başlansın mı?", "Restart board setup?")) },
        text = { Text(t("Yerleştirdiğin taşlar temizlenecek; dizime ilk taştan başlayacaksın.", "The placed pieces will be cleared and setup will start from the first piece.")) },
        confirmButton = { TextButton(onClick = { confirmRestart = false; model.restartChessSetup() }) { Text(t("Yeniden başla", "Restart")) } },
        dismissButton = { TextButton(onClick = { confirmRestart = false }) { Text(t("Vazgeç", "Cancel")) } })
}

@Composable internal fun AcademyBoard(board: List<Piece?>, tablet: Boolean, tr: Boolean, selected: Int?, target: Int?, enabled: Boolean, drag: ((Int,Int)->Unit)? = null, tap: (Int) -> Unit) {
    IosChessBoard(board,tablet,tr,selected,target,enabled=enabled,drag=drag,tap=tap)
}

private fun safetyPrompt(task: ChessSafetyTask,tr: Boolean): String {
    val from=task.source?.let(::squareName).orEmpty()
    val target=squareName(task.target)
    return when(task.group) {
        "threat" -> InterfaceStrings.text(tr,"Beyaz şahı tehdit eden Siyah taşa dokun.","Tap the Black piece attacking the White king.")
        "escape" -> InterfaceStrings.text(tr,"$from karesindeki Beyaz şahı güvenli $target karesine götür.","Move the White king from $from to the safe square $target.")
        "capture" -> InterfaceStrings.text(tr,"$from karesindeki Beyaz fil ile $target karesindeki saldıran taşı al.","Use the White bishop on $from to capture the attacking piece on $target.")
        else -> InterfaceStrings.text(tr,"$from karesindeki Beyaz kaleyi $target karesine getirerek şahını koru.","Move the White rook from $from to $target to block the attack.")
    }
}
