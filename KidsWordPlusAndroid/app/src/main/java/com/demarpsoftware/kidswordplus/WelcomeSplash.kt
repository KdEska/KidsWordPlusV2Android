package com.demarpsoftware.kidswordplus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.demarpsoftware.kidswordplus.family.Companions

/** Once per process launch; AppModel keeps dismissal across configuration changes. */
@Composable internal fun WelcomeSplash(state:AppState,finish:()->Unit) {
    val sound=LocalInteractionSound.current
    LaunchedEffect(Unit) {kotlinx.coroutines.delay(250);sound("welcome")}
    val tr=state.ui=="tr"
    val motion=LocalKidMotion.current
    val friend=Companions.all.firstOrNull {it.id==state.companion}?.name ?: "Piko"
    var wave by remember(state.companion) {mutableStateOf(false)}
    val sparkle by animateFloatAsState(if(wave&&motion)1f else 0f,if(motion)tween(250)else snap(),label="welcomeSparkles")
    BoxWithConstraints(Modifier.fillMaxSize()) {
        LearningBackdrop()
        val scene=(maxHeight.value*.34f).coerceIn(120f,280f).dp
        val compact=maxHeight<500.dp || LocalDensity.current.fontScale>1.4f
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal=24.dp).then(if(compact)Modifier.verticalScroll(rememberScrollState())else Modifier),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(if(compact)8.dp else 16.dp)) {
            if(compact)Spacer(Modifier.height(8.dp))else Spacer(Modifier.weight(1f))
            DesignArt("ui/brand/logo.webp",Modifier.widthIn(max=312.5.dp).fillMaxWidth().heightIn(max=115.dp).height(if(compact)60.dp else 115.dp),description="KidsWordPlus",decodeLimit=1024)
            if(!compact)Spacer(Modifier.weight(1f))
            Box(Modifier.size(scene+24.dp),contentAlignment=Alignment.Center) {
                Box(Modifier.width(scene*.56f).height(18.dp).offset(y=scene*.44f).background(Color.Black.copy(alpha=.1f),CircleShape))
                HomeGreeting(state.companion,Modifier.size(scene).semantics {contentDescription=friend},onComplete={},onWave={wave=true})
                if(motion)repeat(4) {index ->
                    DesignArt("ui/games/daily-star.webp",Modifier.size(if(index%2==0)32.dp else 23.dp)
                        .offset(x=scene*(if(index%2==0)-.46f else .46f),y=scene*(if(index<2)-.30f else .18f))
                        .graphicsLayer {alpha=sparkle;scaleX=.3f+.7f*sparkle;scaleY=scaleX;rotationZ=if(wave)index*18f else -20f},decodeLimit=128)
                }
            }
            Column(Modifier.widthIn(max=360.dp).fillMaxWidth().background(Color.White.copy(alpha=.85f),RoundedCornerShape(30.dp)).padding(horizontal=24.dp,vertical=20.dp).semantics(mergeDescendants=true){},horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text(if(state.name.isBlank()){InterfaceStrings.text(tr,"Merhaba!","Hello!")}else{InterfaceStrings.render(tr,"Merhaba, {0}!","Hello, {0}!",state.name)},style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold,color=KidHeading,textAlign=TextAlign.Center)
                Text(InterfaceStrings.render(tr,"{0} seninle keşfetmeye hazır!","{0} is ready to explore with you!",friend),style=MaterialTheme.typography.titleMedium,color=KidBody,textAlign=TextAlign.Center)
            }
            if(!compact)Spacer(Modifier.weight(1f))
            KidAction(InterfaceStrings.text(tr,"Başlayalım","Let's go"),tone=2,icon="play") {sound("start");finish()}
            Spacer(Modifier.height(8.dp))
        }
    }
}
