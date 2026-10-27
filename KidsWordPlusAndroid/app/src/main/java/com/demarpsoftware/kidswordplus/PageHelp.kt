package com.demarpsoftware.kidswordplus

import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.demarpsoftware.kidswordplus.chess.*

internal class PageHelpState(initiallyShowing:Boolean=false) {
    var owner:Any?=null
    var topic by mutableStateOf<String?>(null)
    var showing by mutableStateOf(initiallyShowing)
}
internal val LocalPageHelp=staticCompositionLocalOf<PageHelpState?>{null}
@Composable internal fun PageHelp(topic:String) {
    val help=LocalPageHelp.current ?: return
    val owner=remember {Any()}
    DisposableEffect(topic) {
        help.owner=owner
        help.topic=topic
        onDispose {if(help.owner===owner){help.topic=null;help.showing=false;help.owner=null}}
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun PageHelpSheet(help:PageHelpState,tr:Boolean) {
    val topic=help.topic ?: return
    if(!help.showing)return
    val guide=PageHelpCatalog.guide(topic,tr)
    val sheet=rememberModalBottomSheetState(skipPartiallyExpanded=true)
    ModalBottomSheet(onDismissRequest={help.showing=false},sheetState=sheet,sheetMaxWidth=760.dp,
        containerColor=Color(0xFFF5F0FC),contentColor=KidBody) {
        Box(Modifier.fillMaxWidth().fillMaxHeight(.95f)) {
            LearningBackdrop()
            Column(Modifier.fillMaxSize().padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Spacer(Modifier.width(48.dp))
                    Text(InterfaceStrings.text(tr,"Nasıl kullanılır?","How to use"),Modifier.weight(1f),textAlign=TextAlign.Center,style=MaterialTheme.typography.titleMedium,color=KidHeading)
                    IconButton(onClick={help.showing=false},modifier=Modifier.semantics {contentDescription=InterfaceStrings.text(tr,"Yardımı kapat","Close help")}) {
                        Box(Modifier.size(28.dp).background(KidHeading.copy(alpha=.12f),CircleShape),contentAlignment=Alignment.Center) {Text("×",color=KidHeading,fontSize=24.sp,modifier=Modifier.clearAndSetSemantics {})}
                    }
                }
                key(topic) {
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom=20.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
                        Text(guide.title,style=MaterialTheme.typography.titleLarge,color=KidHeading,modifier=Modifier.semantics {heading()})
                        Text(InterfaceStrings.text(tr,"Birlikte adım adım bakalım.","Let’s go through it step by step."),color=KidSecondary)
                        when(guide.demo) {
                            HelpDemo.BOARD -> HelpDemoPanel {ChessHelpBoard(tr)}
                            HelpDemo.CARDS -> HelpDemoPanel {HelpCardsDemo(tr)}
                            HelpDemo.SOUND -> HelpDemoPanel {HelpSoundDemo(tr)}
                            HelpDemo.NONE -> Unit
                        }
                        guide.steps.forEachIndexed { index,step ->
                            Column(Modifier.fillMaxWidth().background(Color.White,RoundedCornerShape(22.dp)).padding(16.dp)) {
                                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                                    Text("${index+1}",Modifier.background(KidHeading,CircleShape).padding(horizontal=12.dp,vertical=6.dp).clearAndSetSemantics {},color=Color.White)
                                    Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
                                        Text(step.title,style=MaterialTheme.typography.titleMedium,color=KidHeading)
                                        Text(step.body,color=KidBody)
                                    }
                                }
                            }
                        }
                        KidAction(InterfaceStrings.text(tr,"Anladım","Got it"),tone=2){help.showing=false}
                    }
                }
            }
        }
    }
}

@Composable internal fun PageHelpButton(enabled:Boolean,tr:Boolean,open:()->Unit) {
    IconButton(enabled=enabled,onClick=open,modifier=Modifier.width(52.dp).height(44.dp).semantics {contentDescription=InterfaceStrings.text(tr,"Bu sayfa için yardım","Help for this page")}) {
        Box(Modifier.size(27.dp).background(if(enabled)KidHeading else KidSecondary,CircleShape).clearAndSetSemantics {},contentAlignment=Alignment.Center) {
            Text("?",color=Color.White,style=MaterialTheme.typography.titleMedium)
        }
    }
}
@Composable private fun HelpDemoPanel(content:@Composable ColumnScope.()->Unit) {
    Column(Modifier.fillMaxWidth().background(KidHeading.copy(alpha=.07f),RoundedCornerShape(24.dp)).padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp),content=content)
}
@Composable private fun HelpCardsDemo(tr:Boolean) {
    var played by rememberSaveable {mutableStateOf(false)}
    Row(horizontalArrangement=Arrangement.spacedBy(20.dp),modifier=Modifier.clearAndSetSemantics {}) {
        repeat(2) {Box(Modifier.size(84.dp,100.dp).background(Color.White,RoundedCornerShape(18.dp)).border(2.dp,KidHeading.copy(alpha=.3f),RoundedCornerShape(18.dp)),contentAlignment=Alignment.Center) {
            if(played)DesignArt("ui/games/daily-star.webp",Modifier.size(40.dp),decodeLimit=128)else Text("?",color=KidHeading,fontSize=32.sp)
        }}
    }
    Text(InterfaceStrings.text(tr,"Örnek: birbirine uyan iki kartı bul.","Example: find two matching cards."),color=KidHeading,textAlign=TextAlign.Center,style=MaterialTheme.typography.titleSmall)
    TextButton(onClick={played=!played}) {Text(if(played) InterfaceStrings.text(tr,"Örneği başa al","Reset example") else InterfaceStrings.text(tr,"Örneği göster","Show example"))}
}
@Composable private fun HelpSoundDemo(tr:Boolean) {
    val motion=LocalKidMotion.current
    Row(Modifier.background(Color(0xFF1966B4),RoundedCornerShape(20.dp)).padding(18.dp).clearAndSetSemantics {},verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        Row(Modifier.height(30.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(3.dp)) {
            repeat(5) {index ->
                val value=if(motion) {
                    val transition=rememberInfiniteTransition(label="helpSound")
                    val scale by transition.animateFloat(.25f,1f,infiniteRepeatable(tween(450,delayMillis=index*70),RepeatMode.Reverse),label="helpSoundBar")
                    scale
                } else .5f
                Box(Modifier.width(4.dp).fillMaxHeight(value).background(Color.White,CircleShape))
            }
        }
        Text(InterfaceStrings.text(tr,"Dinle","Listen"),color=Color.White,style=MaterialTheme.typography.titleMedium)
    }
    Text(InterfaceStrings.text(tr,"Ses çalarken düğme böyle hareket eder.","This is how a button moves while sound plays."),color=KidHeading,textAlign=TextAlign.Center,style=MaterialTheme.typography.titleSmall)
}
@Composable private fun ChessHelpBoard(tr:Boolean) {
    var played by rememberSaveable {mutableStateOf(false)}
    val motion=LocalKidMotion.current
    val progress=remember {Animatable(if(played)1f else 0f)}
    val density=LocalDensity.current
    LaunchedEffect(played,motion) {
        if(motion)progress.animateTo(if(played)1f else 0f,tween(800))
        else progress.snapTo(if(played)1f else 0f)
    }
    Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(200.dp).clearAndSetSemantics {}) {
            Column {
                repeat(4) {row -> Row { repeat(4) {col ->
                    Box(Modifier.size(50.dp).background(if((row+col)%2==0)Color(0xFFFFF0D5)else KidHeading.copy(alpha=.6f)))
                } } }
            }
            Box(Modifier.offset(50.dp,50.dp).size(50.dp).border(3.dp,Color.Cyan,RoundedCornerShape(8.dp)))
            Box(Modifier.offset(50.dp,100.dp).size(50.dp).graphicsLayer {translationY=with(density){(-50).dp.toPx()}*progress.value}) {
                ChessPieceImage(Piece(PieceKind.PAWN,true))
            }
        }
        Text(InterfaceStrings.text(tr,"Örnek: taşı seç, hedef kareye dokun.","Example: select the piece, then tap the target square."),color=KidHeading)
        TextButton(onClick={played=!played}){Text(if(played) InterfaceStrings.text(tr,"Örneği başa al","Reset example") else InterfaceStrings.text(tr,"Örneği göster","Show example"))}
    }
}
