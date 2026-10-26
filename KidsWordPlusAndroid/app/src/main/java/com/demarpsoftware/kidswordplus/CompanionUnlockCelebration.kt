package com.demarpsoftware.kidswordplus

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable internal fun CompanionUnlockCelebration(friend:String,tr:Boolean,close:()->Unit,friends:()->Unit) {
    val motion=LocalKidMotion.current
    val confetti=remember(friend){Animatable(0f)}
    LaunchedEffect(friend,motion){if(motion)confetti.animateTo(1f,tween(1800))else confetti.snapTo(1f)}
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    Dialog(onDismissRequest=close,properties=DialogProperties(usePlatformDefaultWidth=false)) {
        BoxWithConstraints(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) {
            val limit=maxHeight*.90f
            Surface(Modifier.widthIn(max=440.dp).fillMaxWidth(.92f).heightIn(max=limit),shape=RoundedCornerShape(32.dp),color=Color.Transparent,shadowElevation=12.dp) {
                Box(Modifier.background(Brush.verticalGradient(listOf(Color(0xFFF0DFFF),Color(0xFFFFF1CE),Color(0xFFFFE4F0)))).border(2.dp,Color.White.copy(alpha=.9f),RoundedCornerShape(32.dp))) {
                    if(motion)Canvas(Modifier.matchParentSize().clearAndSetSemantics {}) {
                        val p=confetti.value
                        val colors=listOf(Color(0xFF9B5DE5),Color(0xFFFFB703),Color(0xFFF15BB5),Color(0xFF00BBF9),Color(0xFF43AA8B))
                        repeat(24){index ->
                            val x=size.width*(index+1)/25f
                            val y=-20.dp.toPx()+size.height*p+(index%4)*12.dp.toPx()
                            drawContext.canvas.save()
                            drawContext.canvas.translate(x,y)
                            drawContext.canvas.rotate(p*(index*31f))
                            drawRoundRect(colors[index%colors.size].copy(alpha=1-p),topLeft=androidx.compose.ui.geometry.Offset(-3.dp.toPx(),-5.dp.toPx()),size=androidx.compose.ui.geometry.Size(6.dp.toPx(),10.dp.toPx()),cornerRadius=androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()))
                            drawContext.canvas.restore()
                        }
                    }
                    Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(16.dp)) {
                        Text("✦  ★  ✦",color=Color(0xFFC17A00),style=MaterialTheme.typography.headlineMedium,modifier=Modifier.clearAndSetSemantics {})
                        Text(t("Yeni arkadaşın açıldı!","A new friend unlocked!"),Modifier.fillMaxWidth(),textAlign=TextAlign.Center,style=MaterialTheme.typography.headlineMedium,color=KidHeading)
                        Box(Modifier.size(220.dp).background(Brush.radialGradient(listOf(Color.White,Color(0xFFFFD86F).copy(alpha=.45f))),CircleShape),contentAlignment=Alignment.Center) {
                            HomeGreeting(friend,Modifier.size(200.dp),onComplete={},onWave={})
                        }
                        Text(t("Birlikte yeni keşiflere hazırsınız!","You’re ready for new adventures together!"),Modifier.fillMaxWidth(),textAlign=TextAlign.Center,color=KidBody,style=MaterialTheme.typography.bodyLarge)
                        KidAction(t("Arkadaşlarıma git","Go to my friends"),onClick=friends)
                        TextButton(onClick=close,modifier=Modifier.align(Alignment.CenterHorizontally)) {Text(t("Kapat","Close"),color=KidHeading)}
                    }
                }
            }
        }
    }
}
