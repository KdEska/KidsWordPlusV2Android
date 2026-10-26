package com.demarpsoftware.kidswordplus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable private fun HomeChessPortalContent(tr:Boolean,open:()->Unit,pieces:()->Unit,openings:()->Unit) {
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    val sound=LocalInteractionSound.current
    val shape=RoundedCornerShape(30.dp)
    Column(Modifier.fillMaxWidth().shadow(8.dp,shape).clip(shape)
        .background(Brush.linearGradient(listOf(Color(0xFF6633B8),Color(0xFF2B246E))))
        .border(1.5.dp,Color.White.copy(alpha=.45f),shape)
        .padding(22.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Canvas(Modifier.size(20.dp).clearAndSetSemantics {}) {
                val w=size.width;val h=size.height
                drawPath(Path().apply {moveTo(w*.1f,h*.25f);lineTo(w*.32f,h*.44f);lineTo(w*.5f,h*.1f);lineTo(w*.68f,h*.44f);lineTo(w*.9f,h*.25f);lineTo(w*.8f,h*.8f);lineTo(w*.2f,h*.8f);close()},Color(0xFFFFD84D))
                drawLine(Color(0xFFFFD84D),androidx.compose.ui.geometry.Offset(w*.2f,h*.95f),androidx.compose.ui.geometry.Offset(w*.8f,h*.95f),h*.08f)
            }
            Text(t("SATRANÇ MACERASI","CHESS ADVENTURE"),Modifier.weight(1f),color=Color.White,fontSize=12.sp,fontWeight=FontWeight.Bold,letterSpacing=1.2.sp)
            Text("✦",color=Color(0xFFFFD84D),fontSize=24.sp,modifier=Modifier.clearAndSetSemantics {})
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val wide=maxWidth>=600.dp && androidx.compose.ui.platform.LocalDensity.current.fontScale<1.6f
            if(wide)Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(24.dp)) {
                ChessPortalArtwork(Modifier.width(344.dp))
                ChessPortalCopy(tr,Modifier.weight(1f))
            }else Column(verticalArrangement=Arrangement.spacedBy(16.dp)) {
                ChessPortalArtwork(Modifier.fillMaxWidth())
                ChessPortalCopy(tr,Modifier.fillMaxWidth())
            }
        }
        HomeChessDiscoveries(tr,pieces,openings)
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFFFB02B),Color(0xFFEB6614)))).clickable(role=Role.Button,onClick={sound("category");open()}).padding(14.dp),
            verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Text(t("Satranç dünyasını keşfet","Explore the chess world"),Modifier.weight(1f),color=Color.White,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
            Box(Modifier.size(36.dp).background(Color.White.copy(alpha=.2f),RoundedCornerShape(50)),contentAlignment=Alignment.Center) {
                Text("→",color=Color.White,fontSize=24.sp,fontWeight=FontWeight.Bold,modifier=Modifier.clearAndSetSemantics {})
            }
        }
    }
}

@Composable private fun ChessPortalArtwork(modifier:Modifier) {
    DesignArt("ui/chess/world.webp",modifier.clip(RoundedCornerShape(24.dp))
        .background(Color.White.copy(alpha=.12f)).border(1.dp,Color.White.copy(alpha=.2f),RoundedCornerShape(24.dp)).padding(12.dp).height(190.dp),decodeLimit=1024)
}

@Composable private fun ChessPortalCopy(tr:Boolean,modifier:Modifier) {
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    Column(modifier,verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(t("Küçük hamleler, büyük keşifler!","Little moves, big discoveries!"),color=Color.White,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
        Text(t("Atla zıpla, şahını koru ve ilk matını bul. Her görev yeni bir macera!","Jump with the knight, protect your king and find your first checkmate. Every task is a new adventure!"),color=Color.White.copy(alpha=.9f),style=MaterialTheme.typography.bodyLarge)
        Row(Modifier.background(Color.White.copy(alpha=.13f),RoundedCornerShape(50)).padding(horizontal=12.dp,vertical=8.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically) {
            Text("✧",color=Color.White,modifier=Modifier.clearAndSetSemantics {})
            Text(t("Öğren","Learn"),color=Color.White,fontWeight=FontWeight.Bold)
            Text("✦",color=Color.White,modifier=Modifier.clearAndSetSemantics {})
            Text(t("Dene","Try"),color=Color.White,fontWeight=FontWeight.Bold)
        }
    }
}

private object HomeChessDiscovery {
    val piece=com.demarpsoftware.kidswordplus.chess.PieceKind.entries.random()
    val opening=com.demarpsoftware.kidswordplus.chess.ChessOpenings.all.random()
    val step=kotlin.random.Random.nextInt(opening.steps.size)
}

@Composable private fun HomeChessDiscoveries(tr:Boolean,pieces:()->Unit,openings:()->Unit) {
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    val opening=HomeChessDiscovery.opening
    val move=opening.steps[HomeChessDiscovery.step].move
    val secret=ChessSecrets.current(androidx.compose.ui.platform.LocalContext.current)
    val tip=InterfaceStrings.text(tr,secret.turkish,secret.english)
    val panels:List<@Composable ColumnScope.()->Unit> = listOf({
        Text(t("Bugünün taşı","Today's piece"),color=Color.White,fontWeight=FontWeight.Bold)
        Box(Modifier.size(108.dp,124.dp)){ChessPieceImage(com.demarpsoftware.kidswordplus.chess.Piece(HomeChessDiscovery.piece,true))}
        Text(pieceTitle(HomeChessDiscovery.piece,tr),color=Color.White,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
        Text(t("Taşları tanı, hamlelerini keşfet!","Meet the pieces and discover their moves!"),color=Color.White,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
    },{
        Text(t("Ünlü bir açılıştan hamle","A move from a famous opening"),color=Color.White,fontWeight=FontWeight.Bold,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
        HomeOpeningPreview()
        Text(t(opening.tr,opening.en),color=Color.White,fontWeight=FontWeight.Bold,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
        Text(com.demarpsoftware.kidswordplus.chess.squareName(move.from)+" → "+com.demarpsoftware.kidswordplus.chess.squareName(move.to),color=Color.White,fontWeight=FontWeight.Bold)
    },{
        Text(t("Satranç sırrı","Chess secret"),color=Color.White,fontWeight=FontWeight.Bold)
        Canvas(Modifier.size(108.dp,124.dp).clearAndSetSemantics {}) {
            val center=androidx.compose.ui.geometry.Offset(size.width/2,size.height*.4f)
            drawCircle(Color(0xFFFFD84D),size.width*.25f,center)
            drawRoundRect(Color(0xFFFFD84D),topLeft=androidx.compose.ui.geometry.Offset(size.width*.4f,size.height*.5f),size=androidx.compose.ui.geometry.Size(size.width*.2f,size.height*.2f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
            drawRoundRect(Color(0xFFFFD84D),topLeft=androidx.compose.ui.geometry.Offset(size.width*.42f,size.height*.74f),size=androidx.compose.ui.geometry.Size(size.width*.16f,size.height*.04f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()))
        }
        Text(tip,color=Color.White,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
    })
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val wide=maxWidth>=600.dp && androidx.compose.ui.platform.LocalDensity.current.fontScale<1.6f
        if(wide)Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            panels.forEachIndexed {index,panel->ChessDiscoveryPanel(Modifier.weight(1f),true,when(index){0->pieces;1->openings;else->null},panel)}
        }else Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
            panels.forEachIndexed {index,panel->ChessDiscoveryPanel(Modifier.fillMaxWidth(),false,when(index){0->pieces;1->openings;else->null},panel)}
        }
    }
}

@Composable private fun ChessDiscoveryPanel(modifier:Modifier,wide:Boolean,open:(()->Unit)?,content:@Composable ColumnScope.()->Unit) {
    Column(modifier.clip(RoundedCornerShape(22.dp)).background(Color.White.copy(alpha=.12f)).then(if(open!=null)Modifier.clickable(role=Role.Button,onClick=open)else Modifier).padding(14.dp).heightIn(min=if(wide)350.dp else 0.dp),
        verticalArrangement=Arrangement.spacedBy(12.dp),horizontalAlignment=Alignment.CenterHorizontally,content=content)
}

@Composable private fun HomeOpeningPreview() {
    val opening=HomeChessDiscovery.opening
    val move=opening.steps[HomeChessDiscovery.step].move
    val board=opening.position(HomeChessDiscovery.step+1).board
    androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
        Column(Modifier.widthIn(max=260.dp).fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)).clearAndSetSemantics {}) {
            repeat(8) {row->Row(Modifier.weight(1f)) {
                repeat(8) {col->
                    val index=row*8+col
                    val color=when {index==move.to->Color(0xFFFF9800);index==move.from->Color(0xFFFFDD66);(row+col)%2==0->Color(0xFFF5EBCF);else->Color(0xFF7D59A1)}
                    Box(Modifier.weight(1f).fillMaxHeight().background(color)) {board[index]?.let {ChessPieceImage(it,inset=1.dp)}}
                }
            }}
        }
    }
}

@Composable internal fun HomeChessPortalCard(tr:Boolean,open:()->Unit,pieces:()->Unit,openings:()->Unit) {
    androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides if(InterfaceStrings.language=="ar")androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr) {
        HomeChessPortalContent(tr,open,pieces,openings)
    }
}
