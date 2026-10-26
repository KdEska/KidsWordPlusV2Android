package com.demarpsoftware.kidswordplus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.activity.compose.BackHandler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.demarpsoftware.kidswordplus.content.Word
import com.demarpsoftware.kidswordplus.games.*
import kotlinx.coroutines.delay
import java.time.LocalDate

internal val LocalGamesBack=staticCompositionLocalOf<MutableState<(() -> Unit)?>?>{null}

@Composable internal fun ColumnScope.GamesScreen(state: AppState, model: AppModel, tablet: Boolean,
    play: (Word, () -> Unit, () -> Unit) -> Unit, stop: () -> Unit, study: () -> Unit, parent: () -> Unit, success: () -> Unit) {
    val tr = state.ui == "tr"
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    val pack = state.pack ?: return
    val game = state.game
    val latestState by rememberUpdatedState(state)
    var pairs by rememberSaveable { mutableIntStateOf(2) }
    var matchCount by rememberSaveable { mutableIntStateOf(2) }
    var audioError by remember(game?.id, game?.index) { mutableStateOf(false) }
    var dailyPage by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.language) { model.refreshDay() }
    // Refresh at local midnight, including when the screen remains open.
    LaunchedEffect(Unit) { while (true) { delay(30_000); if (latestState.today != LocalDate.now()) model.refreshDay() } }
    var pairChoice by rememberSaveable { mutableStateOf<GameKind?>(null) }
    var scoreboard by rememberSaveable { mutableStateOf(false) }
    var speechPage by rememberSaveable(state.language) { mutableStateOf(false) }
    var recallPage by rememberSaveable(state.language) { mutableStateOf<String?>(null) }
    val recallKind=recallPage?.let {runCatching {RecallKind.valueOf(it)}.getOrNull()}
    var requested by rememberSaveable { mutableStateOf<GameKind?>(null) }
    LaunchedEffect(game?.id,requested,state.busy) {
        if(game!=null) { requested=null; pairChoice=null }
        else if(requested!=null&&!state.busy)model.clearMessage()
    }
    val gamesBack=LocalGamesBack.current
    DisposableEffect(speechPage,dailyPage,scoreboard,pairChoice,requested,recallPage) {
        gamesBack?.value=if(speechPage||dailyPage||scoreboard||pairChoice!=null||requested!=null||recallPage!=null) {
            {stop();speechPage=false;recallPage=null;dailyPage=false;scoreboard=false;pairChoice=null;requested=null;model.clearMessage()}
        } else null
        onDispose {gamesBack?.value=null}
    }
    val helpKind=game?.kind ?: pairChoice ?: requested
    PageHelp(when {
        recallKind==RecallKind.BAG -> "bag"
        helpKind!=null -> when(helpKind) {GameKind.LISTENING -> "listening";GameKind.REVIEW -> "review";GameKind.MATCHING -> "matching";GameKind.MEMORY -> "memory"}
        speechPage -> "speech"
        dailyPage -> "daily"
        scoreboard -> "scoreboard"
        else -> "games"
    })
    val helpOpen=LocalPageHelp.current?.showing==true
    ScreenTitle(when { recallKind!=null -> recallTitle(recallKind,state.ui); speechPage -> t("Konuşma pratiği","Speaking practice"); game!=null -> gameTitle(game.kind,tr); requested!=null -> gameTitle(requested!!,tr); pairChoice!=null -> gameTitle(pairChoice!!,tr); dailyPage -> t("Bugünün görevleri","Today's tasks"); scoreboard -> t("Skor tablosu","Scoreboard"); else -> t("Oyunlar","Games") })
    BackHandler(game==null&&(speechPage||dailyPage||scoreboard||pairChoice!=null||requested!=null||recallPage!=null)){stop();recallPage=null;speechPage=false;dailyPage=false;scoreboard=false;pairChoice=null;requested=null;model.clearMessage()}
    if(game==null && recallKind!=null) {
        RecallGame(state,model,recallKind,play,stop,success,{recallPage=null},study)
        return
    }
    if(game==null && speechPage) {
        CompositionLocalProvider(LocalInteractionSound provides {}) {
            SpeakingPractice(state,model,play,stop,success,parent,{speechPage=false},helpOpen)
        }
        return
    }
    if(game==null && requested!=null) {
        val emptyReview=requested==GameKind.REVIEW&&state.gameProgress.reviewIds(state.language,state.completed,pack.words.map{it.id}.toSet()).isEmpty()
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            TextButton({requested=null;model.clearMessage()}) {Text(t("Oyunlara dön","Back to games"))}
            if(state.busy)CircularProgressIndicator()
            else {
                Text(if(emptyReview)t("Bekleyen tekrarın yok!","No words waiting for review!") else t("Önce birkaç kelime keşfedelim","Let's discover some words first"),style=MaterialTheme.typography.titleLarge,color=KidHeading)
                Text(if(emptyReview)t("Yeni kelimeler keşfedebilir veya Dinle ve Seç oynayabilirsin.","Discover new words or play Listen and Choose.")else t("Oyun için en az dört farklı görsel ve sese sahip kelime çalışmalısın.","Study at least four words with different pictures and sounds to play."))
                KidAction(t("Kategorilere git","Explore categories"),onClick=study)
            }
        }
        return
    }
    if(game==null && pairChoice!=null) {
        GameSetup(state,pairChoice!!,if(pairChoice==GameKind.MEMORY)pairs else matchCount,
            {if(pairChoice==GameKind.MEMORY)pairs=it else matchCount=it},
            {pairChoice=null}, { val kind=pairChoice!!;stop();model.startGame(kind,if(kind==GameKind.MEMORY)pairs else matchCount) })
        return
    }
    if(game==null&&dailyPage){Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(20.dp)){DailyTasks(state);KidAction(t("Kelimeleri çalış","Study words"),onClick=study)};return}
    if(game==null&&scoreboard){Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(18.dp)){DesignArt("ui/games/scoreboard.webp",Modifier.height(180.dp).fillMaxWidth(),decodeLimit=512);Text(t("Skor tablosu","Scoreboard"),style=MaterialTheme.typography.headlineMedium,color=KidHeading);Text(t("Bu cihazdaki ilerlemen","Your progress on this device"));Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)){DesignArt("ui/games/daily-star.webp",Modifier.size(28.dp),decodeLimit=128);Text("${state.gameProgress.stars(state.language)} ${t("yıldız","stars")}",style=MaterialTheme.typography.titleLarge,color=Color(0xFF945000))}};return}
    if (game == null) {
        val review = state.gameProgress.reviewIds(state.language,state.completed,pack.words.map {it.id}.toSet()).size
        Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(20.dp)) {
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                DesignArt("ui/brand/logo.webp",Modifier.size(150.dp,55.dp),"KidsWordPlus",decodeLimit=512)
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)){DesignArt("ui/games/daily-star.webp",Modifier.size(26.dp),decodeLimit=128);Text("${state.gameProgress.stars(state.language)}",color=Color(0xFF945000),fontWeight=FontWeight.Bold)}
                DesignArt("companions/${state.companion}.png",Modifier.size(52.dp),decodeLimit=192)
            }
            val day=state.gameProgress.activity(state.language,state.today)
            Row(Modifier.fillMaxWidth().background(Color.White,RoundedCornerShape(50)).padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
                Text(t("Günlük skor","Daily score"),Modifier.weight(1f),fontWeight=FontWeight.Bold)
                Text("${minOf(100,day.newWords.size*5+day.reviews.size*2+day.sessions.size*10)} / 100",color=KidHeading,style=MaterialTheme.typography.titleLarge)
            }
            MenuBackground(1,Modifier.fillMaxWidth().clickable {scoreboard=!scoreboard}) {
                Row(Modifier.padding(horizontal=18.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    DesignArt("ui/games/scoreboard.webp",Modifier.size(52.dp),decodeLimit=192)
                    Text(t("Skor tablosu","Scoreboard"),Modifier.weight(1f),color=Color.White,style=MaterialTheme.typography.titleMedium)
                    DesignArt("ui/navigation/next.webp",Modifier.size(26.dp),decodeLimit=128)
                }
            }
            if(scoreboard) KidSurface {Text(t("Bu cihazdaki ilerlemen","Your progress on this device"),style=MaterialTheme.typography.titleLarge,color=KidHeading);Text("★ ${state.gameProgress.stars(state.language)} ${t("yıldız","stars")}")}

            val menu=listOf(
                Triple(t("Dinle ve Seç","Listen and Choose"),t("Sesi dinle, doğru görseli bul.","Listen and find the right picture."),"listen-choose"),
                Triple(t("Şimdi sen söyle","Your turn to speak"),t("Önce kelimeyi dinle, sonra sen söyle.","Listen to the word, then say it yourself."),"speak-microphone"),
                Triple(t("Eşleştir","Match Pairs"),t("Sesleri dinle, görselleriyle eşleştir.","Match each sound to its picture."),"match"),
                Triple(t("Hafıza Kartları","Memory Cards"),t("Kartları aç, aynı görselleri bul.","Turn the cards and find matching pictures."),"memory-cards"),
                Triple(t("Kelime Treni","Word Train"),t("Dinle, vagonları doldur!","Listen and fill the wagons!"),"panda-bag"),
                Triple(t("Bugünün görevleri","Today's tasks"),t("Görevleri tamamla, yıldızları kazan!","Complete tasks and earn stars!"),"calendar"),
                Triple(t("Tekrar zamanı","Time to review"),"$review ${t("kelime seni bekliyor","words waiting for you")}","repeat-words"))
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val cols=if(androidx.compose.ui.platform.LocalDensity.current.fontScale>=1.6f || maxWidth<300.dp)1 else if(tablet)maxOf(2,((maxWidth.value+14)/254).toInt()) else 2
                Column(verticalArrangement=Arrangement.spacedBy(14.dp)) {
                    menu.withIndex().toList().chunked(cols).forEach { row ->Row(horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                        row.forEach {(i,item) ->
                            MenuBackground(when(item.third){"listen-choose"->3;"speak-microphone","memory-cards"->2;"match"->0;"calendar"->4;else->1},Modifier.weight(1f).clickable(enabled=!state.busy) {
                                stop();when(item.third){"panda-bag"->recallPage=RecallKind.BAG.name;"listen-choose"->{requested=GameKind.LISTENING;model.startGame(GameKind.LISTENING)};"match"->pairChoice=GameKind.MATCHING;"memory-cards"->pairChoice=GameKind.MEMORY;"calendar"->dailyPage=!dailyPage;"repeat-words"->{requested=GameKind.REVIEW;model.startGame(GameKind.REVIEW)};else->speechPage=true}
                            }) {
                                Column(Modifier.fillMaxWidth().padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)) {
                                    if(item.third=="panda-bag")Text("🚂",Modifier.fillMaxWidth().height(90.dp),textAlign=TextAlign.Center,fontSize=64.sp)
                                    else DesignArt("ui/games/${item.third}.webp",Modifier.fillMaxWidth().height(90.dp),decodeLimit=384)
                                    Text(item.first,Modifier.fillMaxWidth().heightIn(min=68.dp),textAlign=TextAlign.Center,style=MaterialTheme.typography.titleLarge,color=Color.White)
                                    Text(item.second,Modifier.fillMaxWidth().heightIn(min=100.dp),textAlign=TextAlign.Center,color=Color.White,fontSize=17.sp)
                                }
                            }
                        };repeat(cols-row.size){Spacer(Modifier.weight(1f))}
                    }}
                }
            }
        }
        return
    }
    LaunchedEffect(game.id, game.faceUp,helpOpen) {
        if (game.waiting && !helpOpen) { delay(1_000); model.memoryHide() }
    }
    fun hear(word: Word) {
        audioError = false
        play(word, { model.gameHeard(game.id, word.id) }, { audioError = true })
    }
    GamePlay(state,model,tablet,game,audioError,::hear,stop,
        {stop();model.endGame();pairChoice=game.kind})
}

@Composable private fun DailyTasks(state: AppState) { GameDailyTasks(state) }
internal fun gameTitle(kind: GameKind, tr: Boolean) = when (kind) {
    GameKind.LISTENING -> InterfaceStrings.text(tr,"Dinle ve Seç","Listen and Choose")
    GameKind.REVIEW -> InterfaceStrings.text(tr,"Tekrar Zamanı","Time to Review")
    GameKind.MATCHING -> InterfaceStrings.text(tr,"Eşleştir","Match Pairs")
    GameKind.MEMORY -> InterfaceStrings.text(tr,"Hafıza Kartları","Memory Cards")
}
