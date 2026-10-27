package com.demarpsoftware.kidswordplus

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import com.demarpsoftware.kidswordplus.content.Word
import com.demarpsoftware.kidswordplus.games.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable internal fun GameSetup(state: AppState, kind: GameKind, count: Int, select: (Int)->Unit, back: ()->Unit, start: ()->Unit) {
    val tr=state.ui=="tr"
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    val available by produceState<Int?>(null,state.pack,state.completed) {
        value=withContext(Dispatchers.IO) { state.pack?.let {GameRules.uniqueWords(it,state.completed).size} ?: 0 }
    }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        TextButton(back){Text(t("Oyunlara dön","Back to games"))}
        Text(if(kind==GameKind.MEMORY)t("Zorluk seviyesini seç","Choose a difficulty") else t("Kaç kelime eşleştirelim?","How many words shall we match?"),style=MaterialTheme.typography.titleLarge,color=KidHeading)
        if(available==null) LinearProgressIndicator(Modifier.fillMaxWidth())
        (if(kind==GameKind.MEMORY)listOf(2,4,6,8)else listOf(2,3,4)).forEach { number ->
            val missing=(number-(available?:0)).coerceAtLeast(0)
            Card(onClick={select(number)},enabled=available!=null&&missing==0,
                modifier=Modifier.fillMaxWidth().semantics { selected=count==number },shape=RoundedCornerShape(20.dp),
                colors=CardDefaults.cardColors(containerColor=Color.White,disabledContainerColor=Color.White.copy(alpha=.75f)),
                border=BorderStroke(if(count==number)2.dp else 1.dp,if(count==number)KidHeading else KidHeading.copy(alpha=.12f))) {
                Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    Column(horizontalAlignment=Alignment.CenterHorizontally) {
                        DesignArt("ui/games/${if(kind==GameKind.MEMORY)"memory-cards" else "match"}.webp",Modifier.size(56.dp),decodeLimit=192)
                        if(kind==GameKind.MEMORY) Row {repeat(number/2){DesignArt("ui/games/daily-star.webp",Modifier.size(13.dp),decodeLimit=64)}}
                    }
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                        Text(if(kind==GameKind.MEMORY) "${memoryLevel(number,tr)} · ${if(number==2)"2×2" else "4×${number/2}"}" else "$number ${t("kelime","words")}",style=MaterialTheme.typography.titleMedium,color=KidHeading)
                        if(kind==GameKind.MEMORY)Text("${number*2} ${t("kart","cards")}")
                        if(available!=null&&missing>0)Text(InterfaceStrings.render(tr, "{0} farklı kelime daha çalışmalısın", "Study {0} more distinct words", (missing).toString()),style=MaterialTheme.typography.bodyMedium)
                    }
                    if(count==number) GameMark(true)
                }
            }
        }
        KidAction(t("Oyuna başla","Start game"),enabled=!state.busy&&available!=null&&available!!>=count,onClick=start)
    }
}
private fun memoryLevel(pairs:Int,tr:Boolean)=when(pairs){2->InterfaceStrings.text(tr,"Başlangıç","Beginner");4->InterfaceStrings.text(tr,"Kolay","Easy");6->InterfaceStrings.text(tr,"Orta","Medium");else->InterfaceStrings.text(tr,"Zor","Hard")}

@Composable internal fun ColumnScope.GamePlay(state:AppState,model:AppModel,tablet:Boolean,game:GameSession,audioError:Boolean,hear:(Word)->Unit,stop:()->Unit,changeLevel:()->Unit) {
    val tr=state.ui=="tr"
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    val large=LocalDensity.current.fontScale>=1.6f
    val adBreak = LocalGameAdBreak.current
    var activeMemoryCard by remember(game.id) { mutableIntStateOf(-1) }
    val playing=LocalWordAudio.current
    var showResult by remember(game.id) { mutableStateOf(false) }
    val motion=LocalKidMotion.current
    val feedback=LocalInteractionSound.current
    val correctCount=game.matched.size+game.matchedCards.size+(if(game.solved)1 else 0)
    var previousCorrect by remember(game.id){mutableIntStateOf(correctCount)}
    var previousWrong by remember(game.id,game.index){mutableIntStateOf(game.wrong.size)}
    var wasWaiting by remember(game.id){mutableStateOf(game.waiting)}
    LaunchedEffect(game.id,correctCount,game.wrong.size,game.waiting) {
        if(correctCount>previousCorrect){stop();feedback("success")}
        else if(game.wrong.size>previousWrong || (game.kind==GameKind.MEMORY && game.waiting && !wasWaiting)){stop();feedback("retry")}
        previousCorrect=correctCount;previousWrong=game.wrong.size;wasWaiting=game.waiting
    }
    LaunchedEffect(game.id,game.finished) {
        if(game.finished) {
            if(game.kind in listOf(GameKind.MEMORY,GameKind.MATCHING)&&motion)delay(750)
            showResult=true
        }
    }
    if(game.finished&&showResult) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(18.dp)) {
            LessonCelebration(state.companion,state.gameProgress.stars(state.language),10,tr,false){true}
            Text(when(game.kind){GameKind.MEMORY->t("Bütün çiftleri buldun!","You found every pair!");GameKind.MATCHING->t("Hepsini eşleştirdin!","You matched them all!");else->t("Harika çalışma!","Great practice!")},style=MaterialTheme.typography.headlineMedium,color=KidHeading,textAlign=TextAlign.Center)
            Text(if(game.kind==GameKind.MEMORY)InterfaceStrings.render(tr,"{1} denemede {0} çift","{0} pairs in {1} attempts",game.words.size.toString(),game.attempts.toString()) else "${game.firstAnswers.values.count{it}} / ${game.words.size} ${t("ilk denemede doğru","correct on the first try")}",textAlign=TextAlign.Center)
            KidAction(t("Tekrar oyna","Play again"),enabled=!state.busy){stop();adBreak { model.startGame(game.kind,game.words.size) }}
            GameShareInfo.completed(game,tr)?.let {share ->
                FamilyShareButton(share.title,share.detail,tr,symbol=share.symbol,onOpen=stop)
            }
            if(game.kind in listOf(GameKind.MEMORY,GameKind.MATCHING))TextButton({stop();adBreak(changeLevel)}){Text(t("Seviyeyi değiştir","Change level"))}
            TextButton({stop();adBreak { model.endGame() }}){Text(t("Oyunlara dön","Back to games"))}
        }
        return
    }
    if(audioError)Text(t("Ses oynatılamadı. Tekrar dinlemeyi deneyebilirsin.","Audio could not play. Try listening again."),color=MaterialTheme.colorScheme.error)
    when(game.kind) {
        GameKind.LISTENING,GameKind.REVIEW -> {
            Column(Modifier.weight(1f).fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text("${game.index+1} / ${game.questions.size}",fontWeight=FontWeight.Bold)
                GameProgressBar(game.index,game.questions.size)
                Text(t("Duyduğun kelimenin görselini seç","Choose the picture for the word you hear"),style=MaterialTheme.typography.titleMedium,textAlign=TextAlign.Center)
                // Share the remaining height equally: all four choices stay above the controls.
                Column(Modifier.weight(1f).widthIn(max=720.dp).fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                    game.questions[game.index].options.chunked(2).forEachIndexed {rowIndex,words ->
                        Row(Modifier.weight(1f).fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                            words.forEachIndexed {columnIndex,word ->
                                val correct=game.solved&&word.id==game.target?.id
                                val wrong=word.id in game.wrong
                                Box(Modifier.weight(1f).fillMaxHeight()) {
                                    GamePicture(state.pack!!.image(word),InterfaceStrings.render(tr, "Görsel {0}", "Picture {0}", (rowIndex*2+columnIndex+1).toString()),correct,wrong,
                                        enabled=game.heard&&!game.solved&&!wrong&&!state.busy,portrait=false) {stop();model.gameAnswer(word.id)}
                                }
                            }
                        }
                    }
                }
                Text(when {
                    game.solved -> t("Harika, doğru buldun!","Great, you found it!")
                    game.wrong.isNotEmpty() -> t("Bir kez daha dinle ve dene.","Listen again and try another picture.")
                    !game.heard -> t("Önce sesi dinle.","Listen to the sound first.")
                    else -> " "
                },color=if(game.solved)KidSuccess else KidBody,style=MaterialTheme.typography.bodyMedium,textAlign=TextAlign.Center)
            }
            // Keep playback and next-question controls above the shared tabs.
            if(large)Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                KidAction(t("Dinle","Listen"),enabled=!state.busy&&!game.solved,tone=1,audioActive=playing==state.pack!!.audio(game.target!!).path){hear(game.target!!)}
                KidAction(if(game.index==game.questions.lastIndex)t("Sonucu gör","See result") else t("Devam","Next"),enabled=game.solved&&!state.busy,tone=2){stop();model.gameNext()}
            } else Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)){KidAction(t("Dinle","Listen"),enabled=!state.busy&&!game.solved,tone=1,audioActive=playing==state.pack!!.audio(game.target!!).path){hear(game.target!!)}}
                Box(Modifier.weight(1f)){KidAction(if(game.index==game.questions.lastIndex)t("Sonucu gör","See result") else t("Devam","Next"),enabled=game.solved&&!state.busy,tone=2){stop();model.gameNext()}}
            }
        }
        GameKind.MEMORY -> {
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(16.dp)) {
                Text(t("İki kart aç ve aynı görselleri bul.","Turn two cards and find matching pictures."),style=MaterialTheme.typography.titleMedium,textAlign=TextAlign.Center)
                Text("${game.matchedCards.size/2} / ${game.words.size} · ${game.attempts} ${t("deneme","attempts")}")
                GameProgressBar(game.matchedCards.size,game.cards.size)
                val columns=if(game.words.size==2)2 else 4
                Box(Modifier.widthIn(max=if(tablet)640.dp else 600.dp).fillMaxWidth()) {
                    GameGrid(columns,game.cards,8) {index,word ->
                        val revealed=index in game.faceUp||index in game.matchedCards
                        MemoryPicture(state.pack!!.image(word),if(revealed)word.text else InterfaceStrings.render(tr, "Kapalı kart {0}", "Hidden card {0}", (index+1).toString()),revealed,index in game.matchedCards,playing==state.pack!!.audio(word).path&&activeMemoryCard==index,!revealed&&!game.waiting&&!state.busy,wrong=game.waiting && index in game.faceUp) {activeMemoryCard=index;model.memoryFlip(index);hear(word)}
                    }
                }
            }
            if(game.waiting)KidAction(t("Kartları kapat ve devam et","Close cards and continue")){stop();model.memoryHide()}
        }
        GameKind.MATCHING -> {
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val rowHeight=((maxHeight-140.dp)/game.words.size).coerceIn(88.dp,180.dp)
                val rowPx=with(LocalDensity.current){rowHeight.toPx()}
                val gapPx=with(LocalDensity.current){16.dp.toPx()}
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(16.dp)) {
                    if(!game.finished) Text(
                        if(game.selected==null)t("1. Önce bir Ses düğmesine dokun ve dinle.","1. Tap a Sound button and listen first.")
                        else t("2. Şimdi duyduğun sesin görseline dokun.","2. Now tap the picture for the sound you heard."),
                        Modifier.fillMaxWidth(),color=KidHeading,style=MaterialTheme.typography.titleMedium,textAlign=TextAlign.Center)
                    Text("${game.matched.size} / ${game.words.size}")
                    GameProgressBar(game.matched.size,game.words.size)
                    val anchors=remember(game.id){mutableStateMapOf<String,Offset>()}
                    Box(Modifier.fillMaxWidth()) {
                        Canvas(Modifier.matchParentSize()) {
                            game.matched.forEach {id ->
                                val a=anchors["s$id"];val b=anchors["p$id"]
                                if(a!=null&&b!=null) {
                                    val path=Path().apply{moveTo(a.x,a.y);cubicTo(a.x+24.dp.toPx(),a.y,b.x-24.dp.toPx(),b.y,b.x,b.y)}
                                    drawPath(path,KidSuccess,style=Stroke(4.dp.toPx(),cap=StrokeCap.Round))
                                    drawCircle(KidSuccess,4.dp.toPx(),a);drawCircle(KidSuccess,4.dp.toPx(),b)
                                }
                            }
                        }
                        Column(verticalArrangement=Arrangement.spacedBy(16.dp)) {
                            game.words.forEachIndexed {index,sound ->
                                val picture=game.cards[index]
                                Row(Modifier.fillMaxWidth().height(rowHeight),horizontalArrangement=Arrangement.spacedBy(if(large)32.dp else 64.dp),verticalAlignment=Alignment.CenterVertically) {
                                    Box(Modifier.weight(1f).onGloballyPositioned {val p=it.positionInParent();anchors["s${sound.id}"]=Offset(p.x+it.size.width,index*(rowPx+gapPx)+rowPx/2)}) {
                                        GameSound(InterfaceStrings.render(tr, "Ses {0}", "Sound {0}", (index+1).toString()),game.selected==sound.id,sound.id in game.matched,playing==state.pack!!.audio(sound).path,!state.busy&&sound.id !in game.matched){hear(sound)}
                                    }
                                    Box(Modifier.weight(1f).height(rowHeight).onGloballyPositioned {val p=it.positionInParent();anchors["p${picture.id}"]=Offset(p.x,index*(rowPx+gapPx)+rowPx/2)}) {
                                        GamePicture(state.pack!!.image(picture),InterfaceStrings.render(tr, "Görsel {0}", "Picture {0}", (index+1).toString()),picture.id in game.matched,false,game.selected!=null&&picture.id !in game.matched&&!state.busy,portrait=false){stop();model.gameAnswer(picture.id)}
                                    }
                                }
                            }
                        }
                    }
                    if(!game.finished && game.wrong.isNotEmpty())Text(t("Bir kez daha dene.","Try again."))
                }
            }
        }
    }
}

@Composable private fun <T> GameGrid(columns:Int,items:List<T>,gap:Int=12,content:@Composable (Int,T)->Unit) {
    Column(verticalArrangement=Arrangement.spacedBy(gap.dp)) {
        items.withIndex().toList().chunked(columns).forEach {row ->
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(gap.dp)) {
                row.forEach {item -> Box(Modifier.weight(1f)){content(item.index,item.value)}}
                repeat(columns-row.size){Spacer(Modifier.weight(1f))}
            }
        }
    }
}
@Composable internal fun GameImage(file:File,modifier:Modifier=Modifier,rounded:Boolean=false) {
    val bitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null,file.path) {
        value=null
        value=withContext(Dispatchers.IO) { runCatching {
            val options=android.graphics.BitmapFactory.Options().apply {inJustDecodeBounds=true}
            android.graphics.BitmapFactory.decodeFile(file.path,options)
            options.inSampleSize=1
            while(options.outWidth/options.inSampleSize>768||options.outHeight/options.inSampleSize>768)options.inSampleSize*=2
            options.inJustDecodeBounds=false
            android.graphics.BitmapFactory.decodeFile(file.path,options)?.let { it.asImageBitmap() }
        }.getOrNull() }
    }
    val image=bitmap
    if(image!=null) {
        if(rounded)BoxWithConstraints(modifier,contentAlignment=Alignment.Center) {
            val ratio=image.width.toFloat()/image.height
            val width=minOf(maxWidth,maxHeight*ratio)
            Image(image,null,Modifier.size(width,width/ratio).clip(RoundedCornerShape(16.dp)))
        } else Image(image,null,modifier,contentScale=androidx.compose.ui.layout.ContentScale.Fit)
    }
}
@Composable private fun GamePicture(file:File,description:String,correct:Boolean,wrong:Boolean,enabled:Boolean,portrait:Boolean=true,click:()->Unit) {
    val shape=RoundedCornerShape(if(portrait)20.dp else 16.dp)
    Box(Modifier.gameAnswerMotion(correct,wrong).fillMaxWidth().then(if(portrait)Modifier.aspectRatio(.75f) else Modifier.fillMaxHeight())
        .clip(shape).background(Color.White).border(if(correct||wrong)3.dp else 0.dp,if(correct)KidSuccess else if(wrong)Color(0xFFE14646) else Color.Transparent,shape)
        .semantics(mergeDescendants=true){contentDescription=description; if(correct)stateDescription="✓" else if(wrong)stateDescription="×"}
        .clickable(enabled=enabled,role=Role.Button,onClick=click).padding(if(portrait)12.dp else 8.dp)) {
        GameImage(file,Modifier.fillMaxSize(),rounded=true)
        if(correct||wrong)Box(Modifier.align(Alignment.TopEnd)){GameMark(correct)}
    }
}
@Composable private fun MemoryPicture(file:File,description:String,revealed:Boolean,matched:Boolean,audioActive:Boolean,enabled:Boolean,wrong:Boolean=false,click:()->Unit) {
    val motion=LocalKidMotion.current
    val angle by animateFloatAsState(if(revealed)180f else 0f,if(motion)tween(360) else snap(),label="memoryFlip")
    val front=angle>=90f
    val shape=RoundedCornerShape(18.dp)
    Box(Modifier.wordAudioEffect(audioActive).gameAnswerMotion(matched,wrong).fillMaxWidth().aspectRatio(.75f).semantics{contentDescription=description}
        .clickable(enabled=enabled,role=Role.Button,onClick=click)
        .graphicsLayer{rotationY=angle;cameraDistance=12*density}) {
        Box(Modifier.fillMaxSize().graphicsLayer{rotationY=if(front)180f else 0f}.clip(shape)
            .background(if(front)Color.White else Color(0xFFAF52DE))
            .border(if(matched)3.dp else 0.dp,if(matched)KidSuccess else Color.Transparent,shape).padding(8.dp),contentAlignment=Alignment.Center) {
            if(front)GameImage(file,Modifier.fillMaxSize(),rounded=true)
            else Text("★",fontSize=38.sp,color=Color.White)
            if(front&&matched)Box(Modifier.align(Alignment.TopEnd)){GameMark(true)}
        }
    }
}
@Composable private fun GameSound(title:String,selected:Boolean,matched:Boolean,audioActive:Boolean,enabled:Boolean,wrong:Boolean=false,click:()->Unit) {
    val shape=RoundedCornerShape(16.dp)
    Row(Modifier.wordAudioEffect(audioActive).fillMaxWidth().heightIn(min=64.dp).clip(shape)
        .background(androidx.compose.ui.graphics.Brush.verticalGradient(if(selected)listOf(Color(0xFF15478C),Color(0xFF1966B4))else listOf(Color(0xFF348DDE),Color(0xFF15478C))))
        .border(if(selected||matched)3.dp else 1.dp,if(matched)KidSuccess else Color.White.copy(alpha=if(selected).85f else .35f),shape)
        .semantics{this.selected=selected}.clickable(enabled=enabled,role=Role.Button,onClick=click).padding(8.dp),
        verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)) {
        WordAudioIndicator(audioActive)
        Text(title,Modifier.weight(1f),style=MaterialTheme.typography.titleMedium,color=Color.White,textAlign=TextAlign.Center)
        if(matched)GameMark(true)
    }
}
@Composable private fun GameMark(correct:Boolean) {
    if(correct)DesignArt("ui/navigation/green-tick.webp",Modifier.size(28.dp),decodeLimit=96)
    else DesignArt("ui/navigation/wrong-cross.webp",Modifier.size(30.dp),decodeLimit=96)
}
@Composable private fun GameProgressBar(value:Int,total:Int) {
    LinearProgressIndicator(progress={value.toFloat()/total.coerceAtLeast(1)},modifier=Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),color=KidSuccess,trackColor=Color.Black.copy(alpha=.09f))
}
@Composable internal fun GameDailyTasks(state:AppState) {
    val tr=state.ui=="tr"
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    val day=state.gameProgress.activity(state.language,state.today)
    Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(14.dp)) {
        Box(Modifier.widthIn(max=340.dp).fillMaxWidth().height(170.dp),contentAlignment=Alignment.BottomCenter) {
            DesignArt("ui/games/congrats-ribbon.webp",Modifier.fillMaxSize(),decodeLimit=768)
            Column(Modifier.padding(bottom=16.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                Text(if(day.rewarded)t("Tebrikler!","Well done!")else t("Günlük görevler","Daily tasks"),fontWeight=FontWeight.Bold,color=Color.White)
                Text(if(day.rewarded)t("Tamamlandı!","Completed!")else t("Günlük hedef","Daily goal"),style=MaterialTheme.typography.headlineMedium,color=Color.White)
            }
        }
        if(!day.rewarded)Text(t("Dersleri bitirince 5, oyunları tamamlayınca 10 yıldız kazan.","Earn 5 stars for finishing a lesson and 10 for completing a game."),textAlign=TextAlign.Center,fontWeight=FontWeight.Bold)
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            DesignArt("ui/games/calendar.webp",Modifier.size(36.dp),decodeLimit=128)
            Text(t("Günde öğren, yıldızları kazan!","Learn every day and earn stars!"),fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f))
        }
        DailyTaskRow(if(day.target==0)t("Tüm kelimeleri çalıştın","You have studied every word")else InterfaceStrings.render(tr, "{0} yeni kelime çalış", "Study {0} new words", (day.target).toString()),day.newWords.size,day.target,"new-words")
        DailyTaskRow(t("10 farklı kelime tekrar et","Review 10 different words"),day.reviews.size,10,"repeat-words")
        DailyTaskRow(t("2 oyun tamamla","Complete 2 games"),day.sessions.size,2,"daily-controller")
        var cursor=state.today
        fun active(date:java.time.LocalDate)=state.gameProgress.activity(state.language,date).let{it.newWords.isNotEmpty()||it.reviews.isNotEmpty()||it.sessions.isNotEmpty()}
        if(!active(cursor))cursor=cursor.minusDays(1)
        var streak=0
        while(active(cursor)){streak++;cursor=cursor.minusDays(1)}
        KidSurface {
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                DesignArt("ui/games/fire.webp",Modifier.size(50.dp),decodeLimit=192)
                Column(Modifier.weight(1f)) {
                    Text("$streak ${t("gün","days")}",style=MaterialTheme.typography.titleLarge,color=KidHeading)
                    Text(if(streak==0)t("Bugün serini başlat!","Start your streak today!")else t("Serin devam ediyor!","Keep your streak going!"))
                }
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                repeat(5){offset -> val number=maxOf(1,streak-3)+offset
                    Box(Modifier.size(34.dp).background(Color(0xFFF4EEF9),RoundedCornerShape(50)),contentAlignment=Alignment.Center) {
                        if(number<=streak)GameMark(true)else Text("$number",color=KidSecondary)
                    }
                }
            }
        }
        if(day.rewarded)FamilyShareButton(t("Bugünün görevleri tamamlandı!","Today’s tasks completed!"),"",tr,symbol=FamilyShareSymbol.DAILY)
        Text("★ ${state.gameProgress.stars(state.language)} ${t("yıldız","stars")}",color=Color(0xFF945000),fontWeight=FontWeight.Bold)
    }
}
@Composable private fun DailyTaskRow(title:String,value:Int,target:Int,art:String) {
    val large=LocalDensity.current.fontScale>=1.6f
    KidSurface {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            DesignArt("ui/games/$art.webp",Modifier.size(if(large)34.dp else 48.dp),decodeLimit=192)
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                Text(title,style=MaterialTheme.typography.titleMedium)
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)){GameProgressBar(if(target==0)1 else minOf(value,target),maxOf(1,target))}
                    Text("${minOf(value,target)} / $target",fontWeight=FontWeight.Bold,color=KidHeading)
                }
            }
            DesignArt("ui/games/daily-star.webp",Modifier.size(if(large)28.dp else 42.dp).graphicsLayer{alpha=if(value>=target)1f else .25f},decodeLimit=128)
        }
    }
}

@Composable internal fun Modifier.gameAnswerMotion(correct:Boolean,wrong:Boolean):Modifier {
    val enabled=LocalKidMotion.current
    val x=remember{Animatable(0f)}
    val y=remember{Animatable(0f)}
    val density=LocalDensity.current.density
    LaunchedEffect(correct,wrong,enabled) {
        x.snapTo(0f);y.snapTo(0f)
        if(enabled) {
            if(correct){y.animateTo(-7f,tween(160));y.animateTo(0f,tween(300))}
            else if(wrong){x.animateTo(-5f,tween(70));x.animateTo(5f,tween(90));x.animateTo(-3f,tween(90));x.animateTo(0f,tween(90))}
        }
    }
    return this.offset{IntOffset((x.value*density).roundToInt(),(y.value*density).roundToInt())}.drawWithContent {
        drawContent()
        if(correct && enabled && y.value<0f) {
            val glow=(-y.value/7f).coerceIn(0f,1f)
            repeat(3) {index ->
                val cx=size.width*(index+1)/4f;val cy=size.height*.3f-glow*20*density
                val path=androidx.compose.ui.graphics.Path()
                repeat(10) {point ->
                    val angle=point*Math.PI/5-Math.PI/2
                    val radius=(if(point%2==0)12f else 5.5f)*density*glow
                    val px=cx+kotlin.math.cos(angle).toFloat()*radius
                    val py=cy+kotlin.math.sin(angle).toFloat()*radius
                    if(point==0)path.moveTo(px,py)else path.lineTo(px,py)
                }
                path.close();drawPath(path,Color(0xFFFFC107).copy(alpha=glow))
            }
        }
    }
}
