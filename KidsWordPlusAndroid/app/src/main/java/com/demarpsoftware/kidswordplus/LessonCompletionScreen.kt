package com.demarpsoftware.kidswordplus

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.demarpsoftware.kidswordplus.content.*
import kotlin.math.sin
import kotlin.math.cos

@Composable internal fun LessonCompletionScreen(state:AppState,lesson:Lesson,pack:LearningPack,earnedStars:Int,celebrated:Boolean,
    next:Lesson?,open:(Lesson,Boolean)->Unit,back:()->Unit,celebrate:()->Boolean,stopAudio:()->Unit) {
    val tr=state.ui=="tr"
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    val images=remember(lesson.id,pack) {lesson.words.map(pack::image)}
    val studied=lesson.words.count {LearningRules.progressKey(state.language,it.id) in state.completed}
    Column(Modifier.widthIn(max=960.dp).fillMaxWidth().verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)) {
        key(lesson.id) {LessonCelebration(state.companion,state.gameProgress.stars(state.language),earnedStars,tr,celebrated,celebrate)}
        Text(t("Harika çalışma!","Great practice!"),style=MaterialTheme.typography.headlineLarge,color=KidHeading,textAlign=TextAlign.Center)
        Text("$studied / ${lesson.words.size} ${t("kelime çalışıldı","words studied")}",textAlign=TextAlign.Center,color=KidBody)
        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            lesson.words.forEachIndexed {index,word ->
                Box(Modifier.size(80.dp).background(Color.White,RoundedCornerShape(14.dp)).padding(8.dp)) {WordImage(images[index],word.text,false,tr,64)}
            }
        }
        FamilyShareButton(t("Bir ders daha tamamlandı!","Another lesson completed!"),
            LearningLanguages.title(state.language)+" · ${lesson.words.size}",tr,
            symbol=FamilyShareSymbol.LESSON,images=images.take(3),onOpen=stopAudio)
        if(next!=null)KidAction(if(next.category!=lesson.category)t("Sonraki kategoriye geç","Next category")else if(next.subcategory!=lesson.subcategory)t("Sonraki konuya geç","Next topic")else t("Sonraki derse geç","Next lesson"),enabled=!state.busy){open(next,false)}
        KidAction(t("Dersi tekrar et","Repeat lesson"),enabled=!state.busy,tone=3,icon="replay"){open(lesson,false)}
        KidAction(t("Derslere dön","Back to lessons"),enabled=!state.busy,tone=1,icon="back",onClick=back)
    }
}

@Composable internal fun LessonCelebration(character:String,total:Int,earned:Int,tr:Boolean,celebrated:Boolean,celebrate:()->Boolean) {
    val motion=LocalKidMotion.current
    val feedback=LocalInteractionSound.current
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    var active by remember(lifecycle) {mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))}
    // The lesson owner keeps consumption across tab changes and screen recreation.
    val progress=remember {Animatable(if(celebrated)1f else 0f)}
    val startCelebration by rememberUpdatedState(celebrate)
    DisposableEffect(lifecycle) {
        val observer=LifecycleEventObserver {_,_ ->active=lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)}
        lifecycle.addObserver(observer);onDispose {lifecycle.removeObserver(observer)}
    }
    LaunchedEffect(active,motion) {
        if(!active){progress.snapTo(1f);return@LaunchedEffect}
        if(startCelebration()){feedback("celebration");if(motion){progress.snapTo(0f);progress.animateTo(1f,tween(2000+earned*350))}else progress.snapTo(1f)}
        else progress.snapTo(1f)
    }
    val review by rememberUpdatedState(LocalCompletionReview.current)
    LaunchedEffect(active) {
        if(active){review(false);kotlinx.coroutines.delay(6500);review(true)}
    }
    val elapsed=progress.value*(2000+earned*350)
    val arrived=if(progress.value>=1f)earned else if(elapsed<1850)0 else (1+((elapsed-1850)/350).toInt()).coerceIn(0,earned)
    val visibleTotal=total-earned+arrived
    Box(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End) {
                Row(Modifier.background(Color.White,RoundedCornerShape(50)).padding(8.dp).clearAndSetSemantics {contentDescription=(InterfaceStrings.text(tr,"Toplam yıldız: ","Total stars: "))+visibleTotal},verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                    DesignArt("ui/games/daily-star.webp",Modifier.size(22.dp),decodeLimit=128)
                    Text("$visibleTotal",color=Color(0xFF945000),style=MaterialTheme.typography.titleMedium)
                }
            }
            DesignArt("companions/$character.png",Modifier.height(150.dp).fillMaxWidth().graphicsLayer {translationY=if(motion)(-7).dp.toPx()*sin(progress.value*Math.PI).toFloat()else 0f},decodeLimit=512)
            if(earned>0)Text(InterfaceStrings.text(tr,"+$earned yıldız!","+$earned stars!"),style=MaterialTheme.typography.titleMedium,color=KidHeading)
        }
        if(motion)Canvas(Modifier.matchParentSize().clearAndSetSemantics {}) {
            val p=progress.value
            val colors=listOf(KidHeading,Color(0xFFFF9800),KidSuccess,Color.Cyan,Color(0xFFEC4899))
            repeat(18) {i ->
                val x=size.width*(i+1)/20f
                val y=(20+i%3*15).dp.toPx()+(size.height-35.dp.toPx())*p
                val local=((p-(i%4)*.04f)/(1-(i%4)*.04f)).coerceIn(0f,1f)
                val center=androidx.compose.ui.geometry.Offset(x,y)
                drawContext.canvas.save()
                drawContext.canvas.translate(center.x,center.y)
                drawContext.canvas.rotate(i*47f*local)
                drawRoundRect(colors[i%5].copy(alpha=1-local),topLeft=androidx.compose.ui.geometry.Offset(-3.dp.toPx(),-5.dp.toPx()),size=androidx.compose.ui.geometry.Size(6.dp.toPx(),10.dp.toPx()),cornerRadius=androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()))
                drawContext.canvas.restore()
            }
            if(earned>0) {
                repeat(earned) { index ->
                val arrival=((elapsed-250-index*350)/1600).coerceIn(0f,1f)
                if(arrival>0f && arrival<1f) {
                val x=size.width/2+(size.width/2-32.dp.toPx())*arrival + sin(arrival*Math.PI).toFloat()*(index%3-1)*24.dp.toPx()
                val y=size.height-22.dp.toPx()+(22.dp.toPx()-size.height+22.dp.toPx())*arrival
                val radius=15.dp.toPx()*(1.2f-.7f*arrival)
                val star=androidx.compose.ui.graphics.Path()
                repeat(10) {point ->
                    val angle=point*Math.PI/5-Math.PI/2
                    val r=if(point%2==0)radius else radius*.46f
                    val px=x+cos(angle).toFloat()*r;val py=y+sin(angle).toFloat()*r
                    if(point==0)star.moveTo(px,py)else star.lineTo(px,py)
                }
                star.close();drawPath(star,Color(0xFFFFC107))
                }
                }
            }
        }
    }
}
