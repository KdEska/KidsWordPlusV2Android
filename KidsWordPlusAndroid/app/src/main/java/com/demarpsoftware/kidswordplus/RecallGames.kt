package com.demarpsoftware.kidswordplus

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.demarpsoftware.kidswordplus.content.Word
import com.demarpsoftware.kidswordplus.games.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal fun recallTitle(kind:RecallKind,ui:String)=InterfaceStrings.text(ui,"Kelime Treni","Word Train")

@Composable internal fun RecallGame(state:AppState,model:AppModel,kind:RecallKind,
    play:(Word,()->Unit,()->Unit)->Unit,stop:()->Unit,success:()->Unit,back:()->Unit,study:()->Unit) {
    val adBreak=LocalGameAdBreak.current
    val pack=state.pack ?: return
    fun t(a:String,b:String)=InterfaceStrings.text(state.ui,a,b)
    val settings=androidx.compose.ui.platform.LocalContext.current.getSharedPreferences("word-train",0)
    var wagonCount by rememberSaveable {mutableStateOf(settings.getInt("wagons",6).coerceIn(3,12))}
    fun setWagons(value:Int){wagonCount=value.coerceIn(3,12);settings.edit().putInt("wagons",wagonCount).apply()}
    var pool by remember(pack,kind){mutableStateOf<List<Word>>(emptyList())}
    var round by rememberSaveable(pack.language,kind.name){mutableStateOf<RecallRound?>(null)}
    var loading by remember(pack,kind){mutableStateOf(true)}
    var started by rememberSaveable(pack.language,kind.name){mutableStateOf(false)}
    var audioError by remember {mutableStateOf(false)}
    var credited by rememberSaveable(round?.id){mutableStateOf(false)}
    var bagBounds by remember {mutableStateOf<Rect?>(null)}
    var rootOrigin by remember {mutableStateOf(Offset.Zero)}
    var dragged by remember {mutableStateOf<Word?>(null)}
    var dragPoint by remember {mutableStateOf(Offset.Zero)}
    LaunchedEffect(pack,kind) {
        pool=withContext(Dispatchers.IO) {GameRules.uniqueWords(pack,state.completed,state.gameProgress.reviewIds(state.language,state.completed,pack.words.map{it.id}.toSet())).filter{it.category!="alphabet" && it.subcategory!="alphabet"}}
        if(round==null)round=RecallRound.create(kind,pool.map{it.id},wagonCount=wagonCount)
        loading=false
    }
    DisposableEffect(Unit){onDispose{stop()}}
    val r=round
    val q=r?.question
    val travel=remember(r?.id){Animatable(0f)}
    val interactionSound=LocalInteractionSound.current
    var departureSeen by rememberSaveable(r?.id){mutableStateOf(false)}
    var departed by remember(r?.id){mutableStateOf(false)}
    val motion=LocalKidMotion.current
    LaunchedEffect(r?.id,r?.finished) {
        if(r?.finished==true) {
            if(departureSeen){travel.snapTo(r.questions.size+2f);departed=true;return@LaunchedEffect}
            departureSeen=true
            delay(180) // Let the full train appear before departure begins.
            stop();interactionSound("train")
            if(!motion){travel.snapTo(r.questions.size+2f);departed=true}
            else {travel.animateTo(r.questions.size+2f,tween(4800,easing=LinearEasing));departed=true}
        }
    }
    LaunchedEffect(r?.id,r?.index,r?.phase) {
        if(r?.phase==RecallPhase.CORRECT && r.index==r.questions.lastIndex){delay(650);round=round?.next()}
    }
    val pageScroll=rememberScrollState()
    LaunchedEffect(r?.id,r?.index,r?.finished){pageScroll.scrollTo(0)}
    val words=remember(pack){pack.words.associateBy{it.id}}
    LaunchedEffect(r?.id,r?.index,state.soundEnabled) {audioError=false;if(!state.soundEnabled)round=round?.hear()}
    LaunchedEffect(r?.id,r?.finished,state.busy,state.message) {
        if(r?.finished==true && !credited && !state.busy && state.message==null)model.completeRecallRound(pack.language,r){credited=true}
    }
    fun answer(id:String) {
        val current=round ?: return
        val next=current.answer(id)
        if(next==current)return
        stop();round=next
        if(next.phase==RecallPhase.CORRECT)success() else interactionSound("retry")
    }
    fun listen(word:Word,gate:Boolean=false) {
        audioError=false
        val ticket=round ?: return
        play(word,{val current=round;if(gate && current?.id==ticket.id && current.index==ticket.index)round=current.hear()},{audioError=true})
    }
    BoxWithConstraints(Modifier.fillMaxSize().onGloballyPositioned{rootOrigin=it.boundsInRoot().topLeft}) {
        val boardLimit=(maxHeight-180.dp).coerceAtLeast(300.dp)
        val columns=2
        val cardHeight=(maxWidth*.43f).coerceIn(130.dp,190.dp)
        Column(Modifier.fillMaxSize().verticalScroll(pageScroll),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)) {
            when {
                loading -> CircularProgressIndicator()
                r==null -> KidSurface {
                    Text(t("Önce birkaç kelime keşfedelim","Let's discover some words first"),style=MaterialTheme.typography.titleLarge,color=KidHeading)
                    Text(t("Oyun için en az dört farklı görsel ve sese sahip kelime çalışmalısın.","Study at least four words with different pictures and sounds to play."))
                    KidAction(t("Kategorilere git","Explore categories"),onClick=study)
                }
                !started -> KidSurface {
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically) {
                        DesignArt("companions/piko-default.png",Modifier.size(100.dp),decodeLimit=384)
                        Text("🚂",fontSize=64.sp)
                    }
                    Text(recallTitle(kind,state.ui),Modifier.fillMaxWidth(),textAlign=TextAlign.Center,style=MaterialTheme.typography.headlineMedium,color=KidHeading)
                    Text(t("Kelimeyi dinle. Doğru resmi seçerek vagonları doldur.","Listen to the word. Choose the right picture to fill the wagons."),textAlign=TextAlign.Center)
                    Text(t("3–12 vagon, süre sınırı yok.","3–12 wagons, no time limit."),color=KidSecondary)
                    TrainWagonSetting(wagonCount,t("Vagon sayısı","Wagon count"),::setWagons)
                    KidAction(t("Başlayalım","Let’s begin"),tone=2){round=RecallRound.create(kind,pool.map{it.id},wagonCount=wagonCount);started=true}
                }
                r.finished && !departed -> {
                    Text(t("Tren yola çıkıyor!","The train is leaving!"),style=MaterialTheme.typography.headlineSmall,color=KidHeading)
                    WordTrainBoard(r,words,pack::image,travel.value,boardLimit)
                }
                r.finished -> KidSurface {
                    LessonCelebration(state.companion,state.gameProgress.stars(state.language),10,state.ui=="tr",false){true}
                    Text(t("Harika çalışma!","Great practice!"),Modifier.fillMaxWidth(),textAlign=TextAlign.Center,style=MaterialTheme.typography.headlineMedium,color=KidHeading)
                    Text("${r.questions.size} / ${r.questions.size}",Modifier.fillMaxWidth(),textAlign=TextAlign.Center,color=KidSuccess,style=MaterialTheme.typography.titleLarge)
                    TrainWagonSetting(wagonCount,t("Vagon sayısı","Wagon count"),::setWagons)
                    KidAction(t("Yeni tur","New round"),enabled=!state.busy){stop();adBreak { round=RecallRound.create(kind,pool.map{it.id},wagonCount=wagonCount);audioError=false }}
                    KidAction(t("Oyunlara dön","Back to games"),onClick={stop();adBreak(back)})
                }
                q!=null -> {
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("${r.index+1} / ${r.questions.size}",Modifier.weight(1f),color=KidHeading);Text(LearningLanguages.title(pack.language),color=KidSecondary)}
                    KidProgress(r.index/r.questions.size.toFloat())
                        Row(Modifier.fillMaxWidth().height(72.dp).gameAnswerMotion(r.phase==RecallPhase.CORRECT,false).onGloballyPositioned{bagBounds=it.boundsInRoot()}
                            .testTag("recall.bag").semantics{contentDescription=t("Kelime Treni","Word Train")}
                            .background(if(dragged!=null && bagBounds?.contains(dragPoint)==true)Color(0xFFDDF7DB) else Color.White,RoundedCornerShape(22.dp))
                            .border(3.dp,if(r.phase==RecallPhase.CORRECT)KidSuccess else KidHeading.copy(alpha=.35f),RoundedCornerShape(22.dp)).padding(10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                            DesignArt("companions/piko-default.png",Modifier.size(48.dp),decodeLimit=256)
                            Text("🚂",fontSize=32.sp)
                            Column(Modifier.weight(1f)) {
                                Text(t("Sıradaki vagon:","Next wagon:"),color=KidSecondary)
                                Text(words[q.target]?.text.orEmpty(),color=KidHeading,style=MaterialTheme.typography.titleLarge)
                            }
                            if(r.phase==RecallPhase.CORRECT)Text("✓",color=KidSuccess,fontSize=30.sp)
                        }
                        WordTrainBoard(r,words,pack::image,0f,boardLimit)
                        if(r.phase==RecallPhase.PICK) Text(
                            if(!state.soundEnabled)t("Doğru görseli seç ve vagonu doldur.","Choose the correct picture to fill the wagon.")
                            else if(!r.heard)t("1. Önce Dinle düğmesine dokun ve sesi dinle.","1. Tap Listen and listen to the sound first.")
                            else t("2. Şimdi doğru görseli seç ve vagonu doldur.","2. Now choose the correct picture to fill the wagon."),
                            Modifier.fillMaxWidth(),color=KidHeading,style=MaterialTheme.typography.titleMedium,textAlign=TextAlign.Center)
                        KidAction(t("Dinle","Listen"),enabled=state.soundEnabled && r.phase==RecallPhase.PICK,tone=1,audioActive=words[q.target]?.let {LocalWordAudio.current==pack.audio(it).path}==true){words[q.target]?.let{listen(it,true)}}
                    val ids=q.answers
                    ids.chunked(columns).forEach {row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                        row.forEach {id-> val word=words[id] ?: return@forEach
                            RecallPicture(word,pack.image(word),Modifier.weight(1f).height(cardHeight),
                                correct=r.phase==RecallPhase.CORRECT && id==q.target,wrong=id in r.wrong,
                                enabled=r.canAnswer && id !in r.wrong,
                                drag=if(r.canAnswer){{w,point->dragged=w;dragPoint=point}}else null,
                                endDrag={val idToDrop=dragged?.id;if(idToDrop!=null && bagBounds?.contains(dragPoint)==true)answer(idToDrop);dragged=null}) {
                                answer(id)
                            }
                        }
                        repeat(columns-row.size){Spacer(Modifier.weight(1f))}
                    }}
                    Box(Modifier.fillMaxWidth().heightIn(min=32.dp),contentAlignment=Alignment.Center) {
                        Text(if(r.phase==RecallPhase.CORRECT)t("Harika, doğru buldun!","Great, you found it!") else if(r.wrong.isNotEmpty())t("Bir daha deneyebilirsin!","You can try again!") else "",color=if(r.phase==RecallPhase.CORRECT)KidSuccess else KidSecondary,textAlign=TextAlign.Center)
                    }
                    if(audioError)Text(t("Ses açılamadı. Yeniden deneyebilirsin.","Could not play audio. Try again."),color=MaterialTheme.colorScheme.error)
                    if(r.phase==RecallPhase.CORRECT && r.index<r.questions.lastIndex)KidAction(t("Devam","Next"),enabled=!state.busy,tone=2){stop();round=r.next()}
                }
            }
        }
        dragged?.let {word->
            val point=dragPoint-rootOrigin
            Box(Modifier.offset{IntOffset(point.x.toInt()-40.dp.roundToPx(),point.y.toInt()-40.dp.roundToPx())}.size(80.dp).background(Color.White,RoundedCornerShape(16.dp)).border(2.dp,KidHeading,RoundedCornerShape(16.dp))) {
                GameImage(pack.image(word),Modifier.fillMaxSize().padding(5.dp),rounded=true)
            }
        }
    }
}

@Composable private fun RecallPicture(word:Word,file:java.io.File,modifier:Modifier,hidden:Boolean=false,caption:Boolean=false,correct:Boolean=false,wrong:Boolean=false,enabled:Boolean=true,
    drag:((Word,Offset)->Unit)?=null,endDrag:()->Unit={},click:()->Unit) {
    var origin by remember{mutableStateOf(Offset.Zero)}
    val latestDrag by rememberUpdatedState(drag)
    val latestEnd by rememberUpdatedState(endDrag)
    val shape=RoundedCornerShape(18.dp)
    Box(modifier.gameAnswerMotion(correct,wrong).onGloballyPositioned{origin=it.boundsInRoot().topLeft}
        .then(if(drag!=null)Modifier.pointerInput(word.id,enabled){
            if(enabled)detectDragGestures(onDragStart={latestDrag?.invoke(word,origin+it)},onDrag={change,_->change.consume();latestDrag?.invoke(word,origin+change.position)},onDragEnd={latestEnd()},onDragCancel={latestDrag?.invoke(word,Offset(-10000f,-10000f));latestEnd()})
        }else Modifier)
        .clip(shape).background(Color.White).border(if(correct||wrong)3.dp else 1.dp,if(correct)KidSuccess else if(wrong)Color(0xFFE14646) else KidHeading.copy(alpha=.16f),shape)
        .clickable(enabled=enabled,role=Role.Button,onClick=click).semantics{contentDescription=if(hidden)"?" else word.text}.padding(6.dp),contentAlignment=Alignment.Center) {
        if(hidden)Text("?",color=KidHeading,fontSize=32.sp) else Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(3.dp)) {
            Box(Modifier.weight(1f).fillMaxWidth()){GameImage(file,Modifier.fillMaxSize(),rounded=true)}
            if(caption)Text(word.text,color=KidHeading,style=MaterialTheme.typography.labelMedium,maxLines=2,textAlign=TextAlign.Center)
        }
        if(correct)Text("✓",Modifier.align(Alignment.TopEnd),color=KidSuccess,fontSize=24.sp)
    }
}

@Composable private fun TrainWagonSetting(count:Int,title:String,change:(Int)->Unit) {
    Row(Modifier.fillMaxWidth().testTag("train.settings"),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(title,Modifier.weight(1f),color=KidHeading,style=MaterialTheme.typography.titleMedium)
        OutlinedButton({change(count-1)},enabled=count>3,modifier=Modifier.testTag("train.fewer")){Text("−")}
        Text("$count",color=KidHeading,style=MaterialTheme.typography.titleLarge)
        OutlinedButton({change(count+1)},enabled=count<12,modifier=Modifier.testTag("train.more")){Text("+")}
    }
}

/** Each wagon follows the same serpentine track behind the locomotive. */
@Composable private fun WordTrainBoard(round:RecallRound,words:Map<String,Word>,image:(Word)->java.io.File,travel:Float,boardLimit:Dp) {
    val filled=if(round.finished)round.questions.size else round.index+if(round.phase==RecallPhase.CORRECT)1 else 0
    val colors=listOf(Color(0xFFCBEFFD),Color(0xFFFFDFB4),Color(0xFFE4D7FF),Color(0xFFD5F2DF),Color(0xFFFFD9E4),Color(0xFFFFEFA7))
    BoxWithConstraints(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color(0xFFEDF9FF)).testTag("recall.contents")) {
        val cell=maxWidth.value/3f
        val rows=(round.questions.size+2)/3
        val rowHeight=((boardLimit.value-56f)/rows).coerceIn(76f,112f)
        val boardHeight=56f+rows*rowHeight
        fun anchor(index:Int):Offset {
            if(index< -1)return Offset(maxWidth.value+cell,-0f)
            if(index== -1)return Offset(6f,0f)
            val row=index/3;val col=if(row%2==0)index%3 else 2-index%3
            return Offset(col*cell+6f,56f+row*rowHeight)
        }
        fun point(index:Float):Offset {
            val lo=kotlin.math.floor(index).toInt();val fraction=index-lo
            val a=anchor(lo);val b=anchor(lo+1)
            return a+(b-a)*fraction
        }
        Box(Modifier.fillMaxWidth().height(boardHeight.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                val density=1.dp.toPx()
                for(i in -1 until round.questions.lastIndex) {
                    val a=anchor(i)+Offset((cell-12f)/2f,46f)
                    val b=anchor(i+1)+Offset((cell-12f)/2f,46f)
                    drawLine(Color(0xFFB5AACB),a*density,b*density,5.dp.toPx())
                }
            }
            for(index in -1 until round.questions.size) {
                val position=point(index-travel)
                if(index== -1)Box(Modifier.offset(position.x.dp,position.y.dp).width((cell-12f).dp).height(52.dp).background(Color(0xFFDDEAFF),RoundedCornerShape(18.dp)),contentAlignment=Alignment.Center){Text("🚂",fontSize=40.sp)}
                else {
                    val word=if(index<filled)words[round.questions[index].target] else null
                    Column(Modifier.offset(position.x.dp,position.y.dp).width((cell-12f).dp).testTag("recall.collected.$index"),horizontalAlignment=Alignment.CenterHorizontally) {
                        Box(Modifier.fillMaxWidth().height(6.dp).background(KidHeading.copy(alpha=.55f),RoundedCornerShape(4.dp)))
                        Column(Modifier.fillMaxWidth().height((rowHeight-22f).dp).background(colors[index%colors.size],RoundedCornerShape(12.dp)).border(if(index==filled)2.dp else 1.dp,if(index==filled)KidHeading else Color.White,RoundedCornerShape(12.dp)).padding(4.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                            if(word!=null) {
                                GameImage(image(word),Modifier.fillMaxWidth().height((rowHeight-54f).dp),rounded=true)
                                Text(word.text,color=KidHeading,style=MaterialTheme.typography.labelSmall,maxLines=2,textAlign=TextAlign.Center)
                            } else Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text("${index+1}",color=KidHeading.copy(alpha=.35f),fontSize=28.sp)}
                        }
                        Row(Modifier.fillMaxWidth().padding(horizontal=10.dp),horizontalArrangement=Arrangement.SpaceBetween){Text("●",fontSize=14.sp,color=KidHeading);Text("●",fontSize=14.sp,color=KidHeading)}
                    }
                }
            }
        }
    }
}
