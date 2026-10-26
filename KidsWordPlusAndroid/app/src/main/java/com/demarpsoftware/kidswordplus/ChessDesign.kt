package com.demarpsoftware.kidswordplus

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.demarpsoftware.kidswordplus.chess.*

@Composable internal fun ChessInstruction(text:String) {
    Column(Modifier.fillMaxWidth().background(Color.White,RoundedCornerShape(22.dp)).padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        Text(text,style=MaterialTheme.typography.titleMedium,color=KidBody,textAlign=TextAlign.Center)
    }
}
@Composable internal fun ChessProgress(value:Int,total:Int,tr:Boolean) {
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
        Text(InterfaceStrings.text(tr,"Tamamlanan hedefler","Completed goals"),fontWeight=FontWeight.Bold,color=KidHeading)
        Text("$value / $total",fontWeight=FontWeight.Bold,color=KidHeading)
    }
    LinearProgressIndicator(progress={value.toFloat()/total.coerceAtLeast(1)},modifier=Modifier.fillMaxWidth(),color=KidHeading,drawStopIndicator={})
}
@Composable internal fun ChessSegmented(labels:List<String>,selected:Int,choose:(Int)->Unit) {
    val large=LocalDensity.current.fontScale>=1.6f
    val shape=RoundedCornerShape(14.dp)
    if(large)Column(Modifier.fillMaxWidth().background(Color(0xFFEDE9F1),shape).padding(4.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
        labels.forEachIndexed {index,title -> ChessSegment(title,index==selected,Modifier.fillMaxWidth()){choose(index)}}
    } else Row(Modifier.fillMaxWidth().background(Color(0xFFEDE9F1),shape).padding(4.dp),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
        labels.forEachIndexed {index,title ->ChessSegment(title,index==selected,Modifier.weight(1f)){choose(index)}}
    }
}
@Composable private fun ChessSegment(title:String,chosen:Boolean,modifier:Modifier,click:()->Unit) {
    Text(title,modifier.background(if(chosen)Color.White else Color.Transparent,RoundedCornerShape(11.dp))
        .semantics{selected=chosen}.clickable(role=Role.RadioButton,onClick=click).padding(horizontal=8.dp,vertical=12.dp),
        style=MaterialTheme.typography.bodyMedium,fontWeight=FontWeight.Bold,color=KidHeading,textAlign=TextAlign.Center)
}
@Composable internal fun ChessPieceStrip(selected:PieceKind?,white:Boolean,tr:Boolean,enabled:Boolean=true,placement:ChessPlacementDrag?=null,columns:Int?=null,choose:(PieceKind)->Unit) {
    val item:@Composable (PieceKind,Modifier)->Unit={kind,modifier ->
            Column(modifier.chessPlacementSource(placement,Piece(kind,white),enabled){choose(kind)}.background(Color.White,RoundedCornerShape(16.dp))
                .border(if(selected==kind)2.5.dp else 1.dp,KidHeading.copy(alpha=if(selected==kind)1f else .15f),RoundedCornerShape(16.dp))
                .semantics(mergeDescendants=true){this.selected=selected==kind;contentDescription=(if(white){InterfaceStrings.text(tr,"Beyaz ","White ")}else{InterfaceStrings.text(tr,"Siyah ","Black ")})+pieceTitle(kind,tr)}
                .clickable(enabled=enabled,role=Role.RadioButton){choose(kind)}.padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(5.dp)) {
                Box(Modifier.size(52.dp)){ChessPieceImage(Piece(kind,white),2.dp)}
                Text(pieceTitle(kind,tr),fontSize=12.sp,fontWeight=FontWeight.Bold,color=KidHeading,textAlign=TextAlign.Center)
            }
    }
    if(columns!=null)Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(4.dp)) {
        PieceKind.entries.chunked(columns).forEach{row ->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)){row.forEach{item(it,Modifier.weight(1f))}}}
    }else Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        PieceKind.entries.forEach{item(it,Modifier.width(70.dp))}
    }
}

@Composable internal fun IosChessBoard(board:List<Piece?>,tablet:Boolean,tr:Boolean,selected:Int?=null,target:Int?=null,legal:Set<Int> = emptySet(),last:Set<Int> = emptySet(),flipped:Boolean=false,enabled:Boolean=true,drag:((Int,Int)->Unit)?=null,animation:ChessMatchAnimation?=null,placement:ChessPlacementDrag?=null,dashedTarget:Boolean=false,tap:(Int)->Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center) {
        Column(Modifier.width(minOf(maxWidth,if(tablet)560.dp else 420.dp)),verticalArrangement=Arrangement.spacedBy(4.dp)) {
            var boardPixels by remember { mutableStateOf(0) }
            var dragSource by remember { mutableStateOf<Int?>(null) }
            var dragPoint by remember { mutableStateOf(Offset.Zero) }
            val latestDrag by rememberUpdatedState(drag)
            val latestBoard by rememberUpdatedState(board)
            val latestEnabled by rememberUpdatedState(enabled)
            LaunchedEffect(board,flipped,enabled) { dragSource=null }
            Box(Modifier.fillMaxWidth().onSizeChanged { boardPixels=it.width }.onGloballyPositioned{placement?.bounds=it.boundsInRoot()}.clip(RoundedCornerShape(16.dp))
                .pointerInput(flipped,enabled,drag!=null) {
                    if(enabled && drag!=null) detectDragGesturesAfterLongPress(
                        onDragStart={point ->
                            val source=chessDropSquare(point.x,point.y,boardPixels.toFloat(),flipped)
                            if(source!=null && latestBoard[source]!=null) {dragSource=source;dragPoint=point}
                        },
                        onDrag={change,amount -> if(dragSource!=null) {change.consume();dragPoint+=amount}},
                        onDragCancel={dragSource=null},
                        onDragEnd={
                            val source=dragSource
                            val destination=chessDropSquare(dragPoint.x,dragPoint.y,boardPixels.toFloat(),flipped)
                            dragSource=null
                            if(latestEnabled && source!=null && destination!=null && source!=destination) latestDrag?.invoke(source,destination)
                        }
                    )
                }) {
            Column(Modifier.fillMaxWidth()) {
                repeat(8){row ->Row(Modifier.fillMaxWidth()) {repeat(8){col ->
                    val index=if(flipped)63-row*8-col else row*8+col
                    val piece=board[index]
                    val visualPiece=(animation?.visual?.before ?: board)[index]
                    val moving=animation?.visual?.movers?.any {it.from==index}==true
                    val light=(index/8+index%8)%2==0
                    val line=when(index){selected->Color(0xFFFFC52E);target->KidSuccess;in last->Color(0xFFFFAE00);else->Color.Transparent}
                    val description=squareName(index)+", "+(piece?.let{(if(it.white){InterfaceStrings.text(tr,"Beyaz ","White ")}else{InterfaceStrings.text(tr,"Siyah ","Black ")})+pieceTitle(it.kind,tr)}?:InterfaceStrings.text(tr,"boş","empty"))+(if(index in legal){InterfaceStrings.text(tr,", yasal hedef",", legal destination")}else "")+(if(index==target){InterfaceStrings.text(tr,", hedef",", target")}else "")
                    Box(Modifier.weight(1f).aspectRatio(1f).background(if(light)Color(0xFFF7E8C9)else Color(0xFF704F9E))
                        .semantics{contentDescription=description;this.selected=index==selected}
                        .clickable(enabled=enabled,role=Role.Button){tap(index)}) {
                        if(col==0)Text("${8-index/8}",fontSize=9.sp,color=if(light)KidBody else Color.White,modifier=Modifier.padding(2.dp))
                        if(visualPiece!=null && index!=dragSource && !moving)ChessPieceImage(visualPiece)
                        else if(index in legal)Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Box(Modifier.fillMaxSize(.25f).aspectRatio(1f).background(KidSuccess.copy(alpha=.8f),RoundedCornerShape(50)))}
                        if(index==target&&dashedTarget)Canvas(Modifier.matchParentSize().padding(3.dp)) {
                            drawRect(Color(0xFFFF9500),style=androidx.compose.ui.graphics.drawscope.Stroke(4.dp.toPx(),pathEffect=androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(),3.dp.toPx()))))
                        }else if(line!=Color.Transparent)Box(Modifier.matchParentSize().border(3.dp,line))
                        if(index==placement?.hovered)Box(Modifier.matchParentSize().border(4.dp,Color.Cyan))
                    }
                }}}
            }
            animation?.let { frame ->
                val cell=with(LocalDensity.current){(boardPixels/8f).toDp()}
                fun point(index: Int): Offset {
                    val display=if(flipped)63-index else index
                    return Offset((display%8+.5f)*boardPixels/8f,(display/8+.5f)*boardPixels/8f)
                }
                Canvas(Modifier.matchParentSize()) {
                    frame.visual.movers.forEach { mover ->
                        drawLine(Color.Cyan.copy(alpha=.75f),point(mover.from),point(mover.to),4.dp.toPx(),StrokeCap.Round,pathEffect=PathEffect.dashPathEffect(floatArrayOf(7.dp.toPx(),5.dp.toPx())))
                    }
                }
                frame.visual.movers.forEach { mover ->
                    val from=point(mover.from);val to=point(mover.to)
                    Box(Modifier.size(cell).graphicsLayer {
                        translationX=from.x+(to.x-from.x)*frame.progress.value-boardPixels/16f
                        translationY=from.y+(to.y-from.y)*frame.progress.value-boardPixels/16f
                        shadowElevation=5.dp.toPx()
                    }.clearAndSetSemantics {}) {ChessPieceImage(mover.piece)}
                }
            }
            dragSource?.let { source ->
                val cell=with(LocalDensity.current){(boardPixels/8f).toDp()}
                Box(Modifier.size(cell).graphicsLayer {
                    translationX=dragPoint.x-boardPixels/16f
                    translationY=dragPoint.y-boardPixels/16f
                    shadowElevation=8.dp.toPx()
                }) { latestBoard[source]?.let { ChessPieceImage(it) } }
            }
            }
            Row(Modifier.fillMaxWidth()){repeat(8){col ->Text(('a'+if(flipped)7-col else col).toString(),Modifier.weight(1f),textAlign=TextAlign.Center,fontSize=11.sp,color=KidBody,fontWeight=FontWeight.Bold)}}
            if(drag!=null && enabled) Text(chessDragInstruction(tr),fontSize=12.sp,color=KidBody,textAlign=TextAlign.Center,modifier=Modifier.fillMaxWidth())
        }
    }
}
@Composable internal fun ChessSidePicker(white:Boolean,tr:Boolean,choose:(Boolean)->Unit) {
    Text(InterfaceStrings.text(tr,"Hangi renkle oynayalım?","Choose your side"),style=MaterialTheme.typography.titleLarge,color=KidHeading)
    val large=LocalDensity.current.fontScale>=1.6f
    val content:@Composable (Boolean,Modifier)->Unit={side,modifier ->
        val chosen=white==side
        Column(modifier.background(if(chosen)Color(0xFFF0E7FF)else Color.White,RoundedCornerShape(24.dp)).border(if(chosen)3.dp else 1.dp,KidHeading.copy(alpha=if(chosen)1f else .12f),RoundedCornerShape(24.dp)).semantics{selected=chosen}.clickable(role=Role.RadioButton){choose(side)}.padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(88.dp)){ChessPieceImage(Piece(PieceKind.KING,side),appearanceOverride=ChessAppearance.CHARACTERS)}
            Text(if(side){InterfaceStrings.text(tr,"Beyaz","White")}else{InterfaceStrings.text(tr,"Siyah","Black")},style=MaterialTheme.typography.titleMedium,color=KidHeading)
            Text(if(chosen)"✓"else "○",color=KidHeading,fontSize=22.sp)
        }
    }
    if(large)Column(verticalArrangement=Arrangement.spacedBy(14.dp)){content(true,Modifier.fillMaxWidth());content(false,Modifier.fillMaxWidth())}
    else Row(horizontalArrangement=Arrangement.spacedBy(14.dp)){content(true,Modifier.weight(1f));content(false,Modifier.weight(1f))}
}

@Composable internal fun ChessTaskMenu(labels:List<String>,selected:Int,choose:(Int)->Unit) {
    var expanded by remember{mutableStateOf(false)}
    Box(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().background(Color.White,RoundedCornerShape(16.dp)).border(1.dp,KidHeading.copy(alpha=.16f),RoundedCornerShape(16.dp)).clickable(role=Role.DropdownList){expanded=true}.padding(14.dp),verticalAlignment=Alignment.CenterVertically) {
            Text(labels[selected],Modifier.weight(1f),style=MaterialTheme.typography.titleMedium,color=KidHeading)
            Text("⌄",fontSize=22.sp,color=KidHeading)
        }
        DropdownMenu(expanded,{expanded=false},modifier=Modifier.heightIn(max=360.dp)) {
            labels.forEachIndexed{index,label -> DropdownMenuItem(text={Text(label)},onClick={choose(index);expanded=false},leadingIcon={if(index==selected)Text("✓",color=KidSuccess)})}
        }
    }
}

internal data class ChessNavigation(val owner:Any,val back:()->Unit,val hideTabs:Boolean,val reset:(()->Unit)?=null,val settings:(()->Unit)?=null)
internal val LocalChessNavigation=staticCompositionLocalOf<MutableState<ChessNavigation?>?>{null}
@Composable internal fun ChessPageNavigation(back:()->Unit,hideTabs:Boolean=true,reset:(()->Unit)?=null,settings:(()->Unit)?=null) {
    val holder=LocalChessNavigation.current
    val latestBack by rememberUpdatedState(back)
    androidx.activity.compose.BackHandler { latestBack() }
    val latestReset by rememberUpdatedState(reset)
    val latestSettings by rememberUpdatedState(settings)
    val owner=remember{Any()}
    DisposableEffect(holder,hideTabs,reset!=null,settings!=null) {
        holder?.value=ChessNavigation(owner,{latestBack()},hideTabs,if(reset!=null){{latestReset?.invoke()}}else null,if(settings!=null){{latestSettings?.invoke()}}else null)
        onDispose{if(holder?.value?.owner===owner)holder.value=null}
    }
}

@Composable internal fun ChessPuzzleRoundHeader(round:ChessPuzzleRound,tr:Boolean,busy:Boolean,next:()->Unit) {
    val adBreak=LocalGameAdBreak.current
    Text(InterfaceStrings.render(tr,"Bölüm {0} / 10 · Görev {1} / 10","Stage {0} / 10 · Task {1} / 10",round.block.toString(),round.blockTask.toString()),style=MaterialTheme.typography.titleMedium,color=KidHeading)
    ChessProgress(round.blockCompleted,10,tr)
    Text("${round.completed} / 100",color=KidSecondary)
    if(round.solved)KidAction(if(round.finished){InterfaceStrings.text(tr,"Yeni tur","New round")}else if(round.blockFinished){InterfaceStrings.text(tr,"Sonraki 10 göreve geç","Next 10 tasks")}else{InterfaceStrings.text(tr,"Sonraki hedef","Next goal")},enabled=!busy,tone=2,onClick={adBreak(next)})
}

@Composable internal fun chessDragInstruction(tr:Boolean):String {
    val timeout=androidx.compose.ui.platform.LocalViewConfiguration.current.longPressTimeoutMillis
    val seconds=java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag(InterfaceStrings.language)).apply {maximumFractionDigits=2}.format(timeout/1000.0)
    return InterfaceStrings.render(tr,"Taşı {0} saniye basılı tut. Taş yükselince hedef kareye sürükleyip bırak. İstersen taşa ve kareye dokun.","Hold the piece for {0} seconds. When it lifts, drag it to the target square and release. You can also tap the piece and square.",seconds)
}
