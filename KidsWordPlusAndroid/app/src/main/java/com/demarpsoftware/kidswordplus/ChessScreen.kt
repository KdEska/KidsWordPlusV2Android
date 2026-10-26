package com.demarpsoftware.kidswordplus

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.foundation.border
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import com.demarpsoftware.kidswordplus.chess.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun ColumnScope.ChessScreen(state: AppState, model: AppModel, tablet: Boolean, initialDestination:String="world") {
    val audio = LocalChessAudio.current
    ChessStopSpeechOnChange(state.ui)
    val adBreak = LocalGameAdBreak.current
    val tr = state.ui == "tr"
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    var boardPage by rememberSaveable { mutableStateOf(false) }
    var lessonPractice by rememberSaveable { mutableStateOf(false) }
    var lessonsPage by rememberSaveable { mutableStateOf(initialDestination=="pieces") }
    val guideContext = LocalContext.current
    val guidePreferences = remember(guideContext) { guideContext.getSharedPreferences("profile", 0) }
    var guideComplete by remember { mutableStateOf(guidePreferences.getBoolean("chess.guide.completed.v1", false)) }
    var dontShowGuide by remember { mutableStateOf(guidePreferences.getBoolean("chess.guide.dontShowAgain.v1", false)) }
    var showGuide by rememberSaveable { mutableStateOf(initialDestination=="world" && !guidePreferences.getBoolean("chess.guide.dontShowAgain.v1", false)) }
    var academyPage by rememberSaveable { mutableStateOf(if(initialDestination=="openings")"openings" else "") }
    var optionsPage by rememberSaveable { mutableStateOf(false) }
    var introPage by rememberSaveable { mutableStateOf(false) }
    val guideAtMenu = !boardPage && !lessonsPage && academyPage.isEmpty() && !optionsPage && !introPage
    LaunchedEffect(guideAtMenu) {
        if (guideAtMenu && !dontShowGuide) showGuide = true
    }
    if (showGuide) ChessFirstStepsGuide(state, dontShowAgain = dontShowGuide, setDontShowAgain = { hidden ->
        guidePreferences.edit().putBoolean("chess.guide.dontShowAgain.v1", hidden).apply()
        dontShowGuide = hidden
    }, dismiss = { showGuide = false }) { choice ->
        guidePreferences.edit().putBoolean("chess.guide.completed.v1", true).apply()
        guideComplete = true
        showGuide = false
        when (choice) {
            ChessGuideDestination.PIECES -> { lessonPractice = false; lessonsPage = true }
            ChessGuideDestination.PRACTICE -> { lessonPractice = true; lessonsPage = true }
            ChessGuideDestination.MENU -> Unit
        }
    }
    if (academyPage.isNotEmpty()) {
        BackHandler { academyPage = "" }
        if (academyPage == "custom") ChessCustomScreen(state, model, tablet, { academyPage = "" }) { academyPage = ""; boardPage = true }
        else if (academyPage == "openings") ChessOpeningsScreen(state, model, tablet) { academyPage = "" }
        else if (academyPage == "puzzles" || academyPage == "mate") ChessPuzzlesScreen(state, model, tablet, academyPage == "mate") { academyPage = "" }
        else ChessAcademyScreen(state, model, tablet, academyPage) { academyPage = "" }
        return
    }
    if (lessonsPage) {
        BackHandler { lessonsPage = false }
        ChessLessonsScreen(state, model, tablet, lessonPractice) { lessonsPage = false }
        return
    }
    BackHandler(optionsPage || introPage) { optionsPage = false; introPage = false }
    var difficultyName by rememberSaveable { mutableStateOf(ChessDifficulty.NORMAL.name) }
    val difficulty = ChessDifficulty.valueOf(difficultyName)
    var minutes by rememberSaveable { mutableStateOf(0) }
    var computer by rememberSaveable { mutableStateOf(true) }
    var playerWhite by rememberSaveable { mutableStateOf(true) }
    var confirmNew by remember { mutableStateOf(false) }
    val game = state.chess
    var flipped by rememberSaveable(game?.id) { mutableStateOf(game?.let { it.computer && !it.playerWhite } ?: false) }
    var settingsOpen by rememberSaveable {mutableStateOf(false)}
    val settingsPreferences=LocalContext.current.getSharedPreferences("chess-match-settings",0)
    var moveEffects by remember {mutableStateOf(settingsPreferences.getBoolean("moveEffects",true))}
    val matchMotion=rememberChessMatchMotion(game,boardPage,flipped,moveEffects)
    val matchAnimation=matchMotion.animation
    val animating=matchAnimation!=null
    var selected by remember(game?.id, game?.history?.size) { mutableStateOf<Int?>(null) }
    var promotion by remember(game?.id, game?.history?.size) { mutableStateOf<List<ChessMove>>(emptyList()) }
    var narratedCount by remember(game?.id) { mutableStateOf(game?.history?.size ?: 0) }
    LaunchedEffect(game?.id, game?.history?.size) {
        val count = game?.history?.size ?: 0
        if (boardPage && count > narratedCount && audio?.automatic == true && game != null) audio.playMove(state.ui, game, enqueue = true)
        if (count < narratedCount) audio?.stop()
        narratedCount = count
    }
    ChessStopSpeechOnChange(boardPage)
    val position = game?.position
    val outcome = game?.outcome
    val playerWon=game?.computer==true && outcome==(if(game.playerWhite)ChessOutcome.WHITE_WON else ChessOutcome.BLACK_WON)
    val victoryGate=remember {ChessVictoryGate()}
    var victoryOpen by rememberSaveable(game?.id){mutableStateOf(false)}
    LaunchedEffect(game?.id,outcome,boardPage) {
        val consumed=guidePreferences.getStringSet("chess.victory.seen.v1",emptySet()).orEmpty().toMutableSet()
        val celebrate=victoryGate.observe(game?.id,outcome?.name,playerWon,boardPage,consumed)
        guidePreferences.edit().putStringSet("chess.victory.seen.v1",consumed).apply()
        if(!boardPage || !playerWon)victoryOpen=false
        if(celebrate){victoryOpen=true;audio?.celebrate(state.ui)}
    }
    if(victoryOpen && boardPage && playerWon)androidx.compose.ui.window.Dialog(onDismissRequest={victoryOpen=false}) {
        KidSurface(Modifier.gameAnswerMotion(true,false)) {
            Text("🏆",Modifier.fillMaxWidth(),textAlign=androidx.compose.ui.text.style.TextAlign.Center,style=MaterialTheme.typography.displayLarge)
            HomeGreeting(state.companion,Modifier.fillMaxWidth().height(170.dp))
            Text(t("Şahane! Kazandın!","Wonderful! You won!"),Modifier.fillMaxWidth(),textAlign=androidx.compose.ui.text.style.TextAlign.Center,style=MaterialTheme.typography.headlineMedium,color=KidHeading)
            Text(t("Planını kurdun, taşlarını korudun ve bilgisayarı yendin!","You made a plan, protected your pieces and beat the computer!"),textAlign=androidx.compose.ui.text.style.TextAlign.Center)
            KidAction(t("Zaferimi gör","See my victory"),tone=2){victoryOpen=false}
            KidAction(t("Yeniden oyna","Play again"),tone=3){victoryOpen=false;boardPage=false;optionsPage=true}
        }
    }
    ScreenTitle(if(boardPage)t("Bilgisayara Karşı","Play the Computer")else if(optionsPage)t("Yeni oyun","New game")else if(introPage)t("Genel Bilgiler","General Information")else t("Satranç Dünyası","Chess World"))
    PageHelp(if(boardPage)"computer"else if(optionsPage)"newGame"else if(introPage)"general"else "chess")
    val helpOpen=LocalPageHelp.current?.showing==true
    DisposableEffect(boardPage, game?.id,helpOpen,settingsOpen) {
        model.chessBoardVisible(boardPage && game != null && !helpOpen && !settingsOpen)
        onDispose { model.chessBoardVisible(false) }
    }
    val choices = remember(position, selected) { selected?.let { position?.movesFrom(it) }.orEmpty() }
    BackHandler(boardPage) { boardPage = false }
    LaunchedEffect(boardPage, game?.id, game?.history?.size, state.busy, animating, matchMotion.foreground,helpOpen,settingsOpen) {
        if (boardPage && !helpOpen && !settingsOpen && matchMotion.foreground && !state.busy && !animating && game?.computerTurn == true) model.chessComputerMove()
    }
    if ((!boardPage || game == null) && !optionsPage) {
        if(introPage) {
            ChessIntroduction(tr,{introPage=false},{introPage=false;lessonsPage=true})
        } else Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(20.dp)) {
                if(tablet) Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(24.dp)) {
                    DesignArt("ui/chess/world.webp",Modifier.width(320.dp).height(230.dp).clip(RoundedCornerShape(24.dp)),decodeLimit=1200)
                    Column(verticalArrangement=Arrangement.spacedBy(14.dp)) {Text(t("Satranç maceran başlıyor!","Your chess adventure starts here!"),style=MaterialTheme.typography.headlineLarge,color=KidHeading);Text(t("Taşları tanımakla başla","Start by meeting the pieces"),style=MaterialTheme.typography.titleMedium);ChessLanguagePill(tr)}
                } else {
                    DesignArt("ui/chess/world.webp",Modifier.fillMaxWidth().aspectRatio(1.5f).clip(RoundedCornerShape(28.dp)),decodeLimit=1200)
                    Text(t("Satranç maceran başlıyor!","Your chess adventure starts here!"),style=MaterialTheme.typography.headlineLarge,color=KidHeading)
                    Text(t("Taşları tanımakla başla","Start by meeting the pieces"),style=MaterialTheme.typography.titleMedium)
                    ChessLanguagePill(tr)
                }
                if (!guideComplete) {
                    KidAction(InterfaceStrings.text(state.ui, "Birlikte öğrenelim", "Let’s learn together"), tone = 2) { showGuide = true }
                } else {
                    TextButton(onClick = { showGuide = true }) { Text(InterfaceStrings.text(state.ui, "İlk satranç adımlarım · Yeniden göster", "My first chess steps · Show again")) }
                }
                val entries=listOf("learn" to t("Genel Bilgiler","General Information"),"pieces" to t("Taşları Tanı","Meet the Pieces"),"setup" to t("Tahtayı Hazırla","Set Up the Board"),"practice" to t("Alıştırma Yap","Practise"),"openings" to t("Açılışları öğren","Learn openings"),"safety" to t("Şahını Koru","Protect Your King"),"endings" to t("Mat ve Pat","Checkmate and Stalemate"),"mate" to t("Tek Hamlede Mat","Mate in One"),"computer" to t("Bilgisayara Karşı","Play the Computer"),"custom" to t("Kendi Oyununu Kur","Create Your Game"))
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val density=LocalDensity.current
                    val measurer=rememberTextMeasurer()
                    val titleStyle=MaterialTheme.typography.titleMedium.copy(lineHeight=20.sp)
                    val cols=if(density.fontScale>=1.6f)1 else if(tablet)maxOf(2,((maxWidth.value+14)/234).toInt()) else 2
                    val labelWidth=with(density){((maxWidth-14.dp*(cols-1))/cols-42.dp).coerceAtLeast(20.dp).roundToPx()}
                    val measured=entries.maxOf{measurer.measure(it.second,titleStyle,constraints=Constraints(maxWidth=labelWidth)).size.height}
                    val labelHeight=maxOf(60.dp*density.fontScale,with(density){measured.toDp()+12.dp})
                    Column(verticalArrangement=Arrangement.spacedBy(16.dp)) {entries.chunked(cols).forEach {row->Row(horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                        row.forEach {(key,title)->
                            Card(onClick={when(key){"learn"->introPage=true;"pieces","practice"->{lessonPractice=key=="practice";lessonsPage=true};"computer"->{if(game!=null&&!state.chessSaveError){boardPage=true;optionsPage=false}else optionsPage=true};"endings"->academyPage="puzzles";else->academyPage=key}},modifier=Modifier.weight(1f),colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(22.dp),border=androidx.compose.foundation.BorderStroke(1.dp,KidHeading.copy(alpha=.14f)),elevation=CardDefaults.cardElevation(5.dp)) {
                                DesignArt("ui/chess/$key.webp",Modifier.fillMaxWidth().aspectRatio(1.15f),scale=ContentScale.Crop,decodeLimit=512)
                                Row(Modifier.padding(horizontal=12.dp).height(labelHeight),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)) {Text(title,Modifier.weight(1f),style=titleStyle,color=KidHeading);androidx.compose.foundation.Canvas(Modifier.size(12.dp)) {
                                    val a=androidx.compose.ui.geometry.Offset(size.width*.35f,size.height*.2f);val b=androidx.compose.ui.geometry.Offset(size.width*.65f,size.height*.5f);val c=androidx.compose.ui.geometry.Offset(size.width*.35f,size.height*.8f)
                                    drawLine(KidHeading,a,b,2.dp.toPx(),androidx.compose.ui.graphics.StrokeCap.Round);drawLine(KidHeading,b,c,2.dp.toPx(),androidx.compose.ui.graphics.StrokeCap.Round)
                                }}
                            }
                        };repeat(cols-row.size){Spacer(Modifier.weight(1f))}
                    }}}
                }
        }
    } else if (!boardPage || game == null) {
        Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            ChessPageNavigation({optionsPage=false},hideTabs=false)
            if (state.chessSaveError) Text(t("Oyun kaydı okunamadı; eski kayıt korunuyor. Ebeveyn alanından yedekleyip yeni kayıt başlatabilirsiniz.", "The saved game could not be read and has been preserved. A parent can back it up and start a new record in the parent area."))
            if (game != null) KidAction(t("Kaldığın oyuna dön", "Resume game"),!state.busy) { boardPage = true; optionsPage=false }
            ChessAppearancePicker(tr)
            ChessSegmented(listOf(t("Bilgisayarla","Computer"),t("İki kişi","Two players")),if(computer)0 else 1){computer=it==0}
            if(computer)ChessSidePicker(playerWhite,tr){playerWhite=it}
            if (computer) ChessDifficultyPicker(difficulty, tr) { difficultyName = it.name }
            ChessTimePicker(minutes, tr) { minutes = it }
            IosButton(enabled = !state.busy && !state.chessSaveError, onClick = {
                if (game != null && game.history.isNotEmpty() && outcome == null) confirmNew = true
                else { model.newChess(computer, playerWhite, minutes, difficulty); boardPage = true;optionsPage=false }
            }) { Text(t("Yeni oyuna başla", "Start new game")) }
            ChessStatisticsPanel(state.chessStats,tr)
        }
    } else {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if(outcome!=null) {
                DesignArt("ui/games/congrats-ribbon.webp",Modifier.fillMaxWidth().height(140.dp),decodeLimit=768)
                KidSurface{Text(if(game.clock?.timedOutWhite!=null)t("Süre doldu","Time expired")else outcomeTitle(outcome,tr),style=MaterialTheme.typography.headlineMedium,color=KidHeading);Text("${game.history.size} ${t("hamle","moves")}")
                    FamilyShareButton(t("Bilgisayara Karşı","Play the Computer"),(if(game.clock?.timedOutWhite!=null)t("Süre doldu","Time expired")else outcomeTitle(outcome,tr))+" · "+(if(game.computer)chessDifficultyTitle(game.difficulty,tr)else t("İki kişilik oyun","Two-player game")),tr,game.position.board,symbol=FamilyShareSymbol.FLAG)
                }
            }
            Text(outcome?.let { if (game.clock?.timedOutWhite != null) { if (game.clock.timedOutWhite == true) t("Süre doldu! Siyah kazandı.", "Time expired! Black wins.") else t("Süre doldu! Beyaz kazandı.", "Time expired! White wins.") } else outcomeTitle(it, tr) } ?: (if (position!!.whiteTurn) t("Sıra Beyazda", "White to move") else t("Sıra Siyahta", "Black to move")), style = MaterialTheme.typography.titleLarge)
            ChessAppearancePicker(tr)
            KidSurface {
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(44.dp)){ChessPieceImage(Piece(PieceKind.KING,game.playerWhite))}
                    Column(Modifier.weight(1f)){Text(if(game.computer)t("Sen ve bilgisayar","You and the computer")else t("İki kişilik oyun","Two-player game"),style=MaterialTheme.typography.titleMedium,color=KidHeading);Text(if(game.computer)chessDifficultyTitle(game.difficulty,tr)else t("Birlikte oynayın","Play together"),style=MaterialTheme.typography.bodyMedium)}
                    Text("✓ "+t("Kaydedildi","Saved"),fontSize=12.sp,color=KidSuccess)
                }
            }
            game.clock?.takeIf {game.clockEnabled}?.let { clock ->
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    listOf(true,false).forEach{side ->
                        Column(Modifier.weight(1f).background(Color.White,RoundedCornerShape(18.dp)).border(2.dp,if(position!!.whiteTurn==side)KidHeading else Color.Transparent,RoundedCornerShape(18.dp)).padding(14.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                            Text(if(side)t("Beyaz","White")else t("Siyah","Black"),fontWeight=FontWeight.Bold)
                            Text(chessClockText(if(side)clock.whiteMillis else clock.blackMillis),style=MaterialTheme.typography.titleLarge,color=KidHeading)
                        }
                    }
                }
                Text(t("Saat yalnız bu tahta açık ve uygulama öndeyken işler; menüde ve arka planda durur.", "The clock runs only with this board open and the app in front; it pauses in menus and the background."))
            }
            if (position!!.inCheck(position.whiteTurn) && outcome == null) Text(t("Şah!", "Check!"), color = MaterialTheme.colorScheme.error)
            if (game.computerTurn) Text(t("Bilgisayar düşünüyor…", "Computer is thinking…"))
            IosChessBoard(position!!.board,tablet,tr,selected,legal=choices.map{it.to}.toSet(),last=game.history.lastOrNull()?.let{setOf(it.from,it.to)}?:emptySet(),flipped=flipped,enabled=!state.busy&&!animating&&!game.computerTurn&&outcome==null,animation=matchAnimation,drag={from,to ->
                val candidates=position.movesFrom(from).filter{it.to==to}
                if(candidates.size>1)promotion=candidates
                else if(candidates.size==1)model.chessMove(candidates.single())
            }) {index ->
                val piece=position.board[index]
                val candidates=choices.filter{it.to==index}
                if(candidates.size>1)promotion=candidates
                else if(candidates.size==1)model.chessMove(candidates.single())
                else selected=if(piece?.white==position.whiteTurn&&selected!=index)index else null
            }
            game.history.lastOrNull()?.let {move ->
                Text("${squareName(move.from)} → ${squareName(move.to)}",style=MaterialTheme.typography.titleMedium)
            }
            ChessCapturedPanel(game,tr)
            ChessAudioStatus(state.ui)
            TextButton(enabled = game.history.isNotEmpty(), onClick = { audio?.playMove(state.ui, game, enqueue = false) }) { Text(t("Son hamleyi dinle", "Listen to last move")) }
            state.chessHint?.let { Text(t("İpucu: ", "Hint: ") + it.notation) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) { KidAction(t("Geri al", "Undo"),tone=1,icon="undo",enabled = !state.busy && !animating && game.clock?.timedOutWhite == null && game.history.isNotEmpty() && !(game.computer && game.startingWhite != game.playerWhite && game.history.size == 1), onClick = model::chessUndo) }
                Box(Modifier.weight(1f)) { KidAction(t("İpucu", "Hint"),tone=1,icon="hint",enabled = !state.busy && !animating && outcome == null && !game.computerTurn, onClick = model::chessHint) }
            }
            ChessPageNavigation({boardPage=false;optionsPage=false},settings={settingsOpen=true})
            KidAction(t("Yeni oyun","New game"),tone=2,icon="replay"){ if(outcome!=null) adBreak { boardPage=false;optionsPage=true } else { boardPage=false;optionsPage=true } }
            if(game.startFen!=null)KidAction(t("Konumu düzenle","Edit position"),tone=1,icon="edit"){boardPage=false;academyPage="custom"}
            ChessStatisticsPanel(state.chessStats,tr)
            ChessHistoryPanel(game,tr)
        }
    }
    if(settingsOpen) ModalBottomSheet(onDismissRequest={settingsOpen=false},sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),containerColor=Color.White) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text(t("Maç ayarları","Match settings"),Modifier.weight(1f),style=MaterialTheme.typography.titleLarge,color=KidHeading)
                TextButton(onClick={settingsOpen=false}){Text(t("Kapat","Close"))}
            }
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text(t("Hamleleri seslendir","Narrate moves"),Modifier.weight(1f))
                Switch(audio?.automatic==true,{audio?.automatic(it)})
            }
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text(t("Hamle animasyonları","Move animations"),Modifier.weight(1f))
                Switch(moveEffects,{value -> if(settingsPreferences.edit().putBoolean("moveEffects",value).commit())moveEffects=value})
            }
            Text(t("Animasyonlar ebeveyn ve cihazın hareket ayarlarına uyar.","Animations follow parent and device motion settings."),style=MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick={flipped=!flipped}){Text(t("Tahtayı çevir","Flip board"))}
            val clockMinutes=(game?.clock?.limitMillis?.div(60000L)?.toInt() ?: 5)
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text(t("Satranç saati","Chess clock"),Modifier.weight(1f))
                Switch(game?.clock!=null && game.clockEnabled,{value->model.configureChessClock(value,clockMinutes)},enabled=!state.busy&&game?.outcome==null)
            }
            if(game?.clock!=null && game.clockEnabled) {
                ChessTimePicker(clockMinutes,tr){value->if(value>0)model.configureChessClock(true,value)else model.configureChessClock(false,clockMinutes)}
                Text(t("Süre değişince iki tarafın saati yeniden başlar.","Changing the duration resets both clocks."),style=MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
    if (promotion.isNotEmpty()) AlertDialog(onDismissRequest = { promotion = emptyList() }, title = { Text(t("Piyonun hangi taş olsun?", "Choose a promotion piece")) }, text = {
        Column { promotion.forEach { move -> TextButton(onClick = { model.chessMove(move); promotion = emptyList() }) { Text(pieceTitle(move.promotion!!, tr)) } } }
    }, confirmButton = {})
    if (confirmNew) AlertDialog(onDismissRequest = { confirmNew = false }, title = { Text(t("Yeni oyun başlatılsın mı?", "Start a new game?")) }, text = { Text(t("Devam eden oyunun yerini yeni oyun alacak.", "This will replace the game in progress.")) }, confirmButton = {
        TextButton(onClick = { confirmNew = false; model.newChess(computer, playerWhite, minutes, difficulty); boardPage = true;optionsPage=false }) { Text(t("Yeni oyun", "New game")) }
    }, dismissButton = { TextButton(onClick = { confirmNew = false }) { Text(t("Vazgeç", "Cancel")) } })
}
private object ChessBitmaps { val images = HashMap<String, androidx.compose.ui.graphics.ImageBitmap>() }
@Composable internal fun ChessPieceImage(piece: Piece, inset: Dp = 3.dp, appearanceOverride: ChessAppearance? = null) {
    val appearance = appearanceOverride ?: LocalChessAppearance.current?.value ?: ChessAppearance.CLASSIC
    if (appearance == ChessAppearance.CLASSIC) {
        FlatChessPiece(piece, Modifier.fillMaxSize().padding(inset))
        return
    }
    val context = LocalContext.current
    val path = "chess/pieces/" + (if (piece.white) "" else "black/") + piece.kind.name.lowercase() + ".png"
    val image by key(path) { produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, path) {
        value = withContext(Dispatchers.IO) { synchronized(ChessBitmaps.images) {
            ChessBitmaps.images[path] ?: runCatching {
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.assets.open(path).use { BitmapFactory.decodeStream(it, null, options) }
                options.inSampleSize = 1
                while (options.outWidth / options.inSampleSize > 512 || options.outHeight / options.inSampleSize > 512) options.inSampleSize *= 2
                options.inJustDecodeBounds = false
                context.assets.open(path).use { BitmapFactory.decodeStream(it, null, options) }?.asImageBitmap()?.also { ChessBitmaps.images[path] = it }
            }.getOrNull()
        } }
    } }
    if (image != null) Image(image!!, null, Modifier.fillMaxSize().padding(inset))
    else FlatChessPiece(piece, Modifier.fillMaxSize().padding(inset))
}
internal fun pieceTitle(kind: PieceKind, tr: Boolean) = InterfaceStrings.text(tr,when (kind) {
    PieceKind.KING -> "Şah"; PieceKind.QUEEN -> "Vezir"; PieceKind.ROOK -> "Kale"; PieceKind.BISHOP -> "Fil"; PieceKind.KNIGHT -> "At"; PieceKind.PAWN -> "Piyon"
},kind.name.lowercase().replaceFirstChar {it.uppercase()})
private fun outcomeTitle(outcome: ChessOutcome, tr: Boolean) = when (outcome) {
    ChessOutcome.WHITE_WON -> InterfaceStrings.text(tr,"Şah mat! Beyaz kazandı.","Checkmate! White wins.")
    ChessOutcome.BLACK_WON -> InterfaceStrings.text(tr,"Şah mat! Siyah kazandı.","Checkmate! Black wins.")
    ChessOutcome.STALEMATE -> InterfaceStrings.text(tr,"Pat: beraberlik.","Stalemate: draw.")
    ChessOutcome.REPETITION -> InterfaceStrings.text(tr,"Üç konum tekrarı: beraberlik.","Threefold repetition: draw.")
    ChessOutcome.FIFTY_MOVES -> InterfaceStrings.text(tr,"50 hamle kuralı: beraberlik.","Fifty-move rule: draw.")
    ChessOutcome.INSUFFICIENT -> InterfaceStrings.text(tr,"Yetersiz taş: beraberlik.","Insufficient material: draw.")
}

@Composable internal fun ChessTimePicker(minutes: Int, tr: Boolean, select: (Int) -> Unit) {
    Text(InterfaceStrings.text(tr,"Taraf başına süre","Time per side"))
    listOf(0, 1, 5, 10, 15, 30).chunked(3).forEach { choices -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        choices.forEach { value -> FilterChip(minutes == value, { select(value) }, modifier = Modifier.weight(1f), label = {
            Text(if (value == 0) { InterfaceStrings.text(tr,"Süresiz","Untimed") } else "$value " + InterfaceStrings.text(tr,"dk","min"))
        }) }
    } }
}

internal fun chessDifficultyTitle(difficulty: ChessDifficulty, tr: Boolean): String = when (difficulty) {
    ChessDifficulty.EASY -> InterfaceStrings.text(tr,"Kolay","Easy")
    ChessDifficulty.NORMAL -> InterfaceStrings.text(tr,"Orta","Medium")
    ChessDifficulty.CHALLENGE -> InterfaceStrings.text(tr,"Zor","Hard")
}
@Composable internal fun ChessDifficultyPicker(difficulty: ChessDifficulty, tr: Boolean, select: (ChessDifficulty) -> Unit) {
    Text(InterfaceStrings.text(tr,"Bilgisayar seviyesi","Computer difficulty"),style=MaterialTheme.typography.titleLarge,color=KidHeading)
    val large=androidx.compose.ui.platform.LocalDensity.current.fontScale>=1.6f
    val card:@Composable (ChessDifficulty,Modifier)->Unit={level,modifier ->
        val chosen=difficulty==level
        val color=when(level){ChessDifficulty.EASY->KidSuccess;ChessDifficulty.NORMAL->Color(0xFF007AFF);else->KidHeading}
        Column(modifier.background(Color.White,RoundedCornerShape(22.dp)).border(if(chosen)3.dp else 1.dp,color.copy(alpha=if(chosen)1f else .18f),RoundedCornerShape(22.dp)).semantics{this.selected=chosen}.clickable{select(level)}.padding(14.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text(when(level){ChessDifficulty.EASY->"♟";ChessDifficulty.NORMAL->"♜";else->"♛"},Modifier.background(color,RoundedCornerShape(20.dp)).padding(14.dp),fontSize=29.sp,color=Color.White)
            Text("★".repeat(level.ordinal+1)+"☆".repeat(2-level.ordinal),fontSize=12.sp,color=color)
            Text(chessDifficultyTitle(level,tr),style=MaterialTheme.typography.titleMedium,color=KidHeading,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
    if(large)Column(verticalArrangement=Arrangement.spacedBy(12.dp)){ChessDifficulty.entries.forEach{card(it,Modifier.fillMaxWidth())}}
    else Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){ChessDifficulty.entries.forEach{card(it,Modifier.weight(1f))}}

    Text(when (difficulty) {
        ChessDifficulty.EASY -> InterfaceStrings.text(tr,"Yasal hamleler arasından rastgele oynar.","Chooses randomly among legal moves.")
        ChessDifficulty.NORMAL -> InterfaceStrings.text(tr,"Taş almayı, riskleri ve merkezi değerlendirir.","Evaluates captures, risks and the centre.")
        ChessDifficulty.CHALLENGE -> InterfaceStrings.text(tr,"Rakibin olası bir cevabını da hesaplar. Ölçülmüş Elo seviyesi yoktur.","Also considers the opponent's possible reply. No measured Elo rating.")
    })
}

@Composable internal fun ChessLanguagePill(tr: Boolean) {
    Text(InterfaceStrings.text(tr,"Arayüz dili","Interface language")+": "+LearningLanguages.title(if(tr)"tr"else InterfaceStrings.language),Modifier.background(Color.White,RoundedCornerShape(50)).padding(horizontal=14.dp,vertical=10.dp),fontWeight=FontWeight.Bold,color=KidHeading,style=MaterialTheme.typography.bodyMedium)
}
