package com.demarpsoftware.kidswordplus

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

internal val LocalWordAudio = staticCompositionLocalOf<String?> { null }

@Composable private fun audioPulse(active:Boolean):Float {
    if(!active)return 0f
    if(!LocalKidMotion.current)return .7f
    val transition=rememberInfiniteTransition(label="wordAudio")
    val pulse by transition.animateFloat(0f,1f,infiniteRepeatable(tween(785,easing=LinearEasing),RepeatMode.Reverse),label="wordAudioPulse")
    return pulse
}
internal fun Modifier.wordAudioEffect(active:Boolean)=composed {
    val pulse=audioPulse(active)
    drawWithContent {
        drawContent()
        if(active) {
            val radius=CornerRadius(22.dp.toPx())
            drawRoundRect(Color.Cyan.copy(alpha=.06f+.08f*pulse),cornerRadius=radius)
            drawRoundRect(Color.Cyan.copy(alpha=.20f+.15f*pulse),cornerRadius=radius,style=Stroke((5+3*pulse).dp.toPx()))
            val inset=1.5.dp.toPx()
            drawRoundRect(Color.White.copy(alpha=.65f+.35f*pulse),topLeft=Offset(inset,inset),size=Size((size.width-2*inset).coerceAtLeast(0f),(size.height-2*inset).coerceAtLeast(0f)),cornerRadius=radius,style=Stroke(3.dp.toPx()))
        }
    }
}
@Composable internal fun WordAudioIndicator(active:Boolean,color:Color=Color.White) {
    val pulse=audioPulse(active)
    Canvas(Modifier.size(28.dp)) {
        val heights=if(active)listOf(.25f+.35f*pulse,.9f-.5f*pulse,.5f+.5f*pulse,.9f-.5f*pulse,.25f+.35f*pulse)else listOf(.25f,.55f,.85f,.55f,.25f)
        heights.forEachIndexed { i,h ->
            val x=size.width*(i+1)/6
            drawLine(color,Offset(x,size.height*(1-h)/2),Offset(x,size.height*(1+h)/2),3.dp.toPx(),StrokeCap.Round)
        }
    }
}
