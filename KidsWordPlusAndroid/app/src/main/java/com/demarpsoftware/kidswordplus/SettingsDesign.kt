package com.demarpsoftware.kidswordplus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable internal fun SettingsIcon(name:String) {
    val tint=when(name){"sound","notifications","download"->Color(0xFF246BCE);"target","learning-days"->Color(0xFF248A68);"privacy","parent-lock"->Color(0xFF52627C);else->KidHeading}
    Box(Modifier.size(44.dp).background(tint.copy(alpha=.09f),RoundedCornerShape(14.dp)).clearAndSetSemantics {},contentAlignment=Alignment.Center) {
        when(name){
            "help"->Text("?",color=tint,fontWeight=FontWeight.Bold,fontSize=25.sp)
            "speech"->DesignArt("ui/games/speak-microphone.webp",Modifier.size(28.dp),decodeLimit=128)
            "daily-star"->DesignArt("ui/games/daily-star.webp",Modifier.size(28.dp),decodeLimit=128)
            "download"->Canvas(Modifier.size(28.dp)) {
                val w=size.width
                val stroke=w*.075f
                drawCircle(tint,w*.43f,style=androidx.compose.ui.graphics.drawscope.Stroke(stroke))
                fun line(ax:Float,ay:Float,bx:Float,by:Float)=drawLine(tint,androidx.compose.ui.geometry.Offset(w*ax,w*ay),androidx.compose.ui.geometry.Offset(w*bx,w*by),stroke,androidx.compose.ui.graphics.StrokeCap.Round)
                line(.5f,.25f,.5f,.7f);line(.32f,.52f,.5f,.7f);line(.5f,.7f,.68f,.52f)
            }
            else->DesignArt("ui/parent/$name.webp",Modifier.size(28.dp),decodeLimit=128)
        }
    }
}

@Composable internal fun SettingsCard(modifier:Modifier=Modifier,content:@Composable ColumnScope.()->Unit) {
    val shape=RoundedCornerShape(22.dp)
    Column(Modifier.fillMaxWidth().shadow(2.dp,shape).clip(shape).then(modifier)
        .background(Brush.linearGradient(listOf(Color.White,Color(0xFFFAFBFF))))
        .border(1.dp,Color(0xFFE7EAF2),shape).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp),content=content)
}

@Composable internal fun SettingsAction(title:String,enabled:Boolean=true,click:()->Unit) {
    val sound=LocalInteractionSound.current
    BoxWithConstraints(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center) {
        Box(Modifier.widthIn(max=if(maxWidth>=600.dp)280.dp else 320.dp).fillMaxWidth().heightIn(min=54.dp).shadow(2.dp,RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp)).background(if(enabled)Brush.linearGradient(listOf(Color(0xFF7553BF),Color(0xFF51429A)))else Brush.linearGradient(listOf(Color(0xFFE3E6EC),Color(0xFFE3E6EC))))
            .clickable(enabled=enabled,role=Role.Button){click();sound("tap")}.padding(horizontal=20.dp,vertical=16.dp),contentAlignment=Alignment.Center) {
            Text(title,color=if(enabled)Color.White else KidSecondary,style=MaterialTheme.typography.titleMedium,textAlign=TextAlign.Center)
        }
    }
}
