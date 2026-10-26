package com.demarpsoftware.kidswordplus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.demarpsoftware.kidswordplus.chess.Piece
import com.demarpsoftware.kidswordplus.chess.PieceKind

internal enum class ChessAppearance(val stored: String) {
    CLASSIC("classic"), CHARACTERS("characters");
    companion object { fun decode(value: String?) = entries.find { it.stored == value } ?: CLASSIC }
}
@Stable internal class ChessAppearanceSettings(initial: ChessAppearance, private val save: (ChessAppearance) -> Unit) {
    var value by mutableStateOf(initial)
        private set
    fun select(value: ChessAppearance) {
        if (this.value == value) return
        this.value = value
        save(value)
    }
}
internal val LocalChessAppearance = staticCompositionLocalOf<ChessAppearanceSettings?> { null }

@Composable internal fun ChessAppearanceProvider(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val preferences = remember(context) { context.getSharedPreferences("chess-appearance", 0) }
    val settings = remember(preferences) {
        ChessAppearanceSettings(ChessAppearance.decode(preferences.getString("chess.piece.appearance.v1", null))) {
            preferences.edit().putString("chess.piece.appearance.v1", it.stored).apply()
        }
    }
    CompositionLocalProvider(LocalChessAppearance provides settings, content = content)
}

@Composable internal fun ChessAppearancePicker(tr: Boolean) {
    val settings = LocalChessAppearance.current ?: return
    val large = LocalDensity.current.fontScale >= 1.6f
    @Composable fun Choice(appearance: ChessAppearance, modifier: Modifier) {
        val chosen = settings.value == appearance
        val characters = appearance == ChessAppearance.CHARACTERS
        val color = if (characters) Color(0xFFAF52DE) else Color(0xFF007AFF)
        Card(onClick = { settings.select(appearance) }, modifier = modifier.semantics {
            contentDescription = if (characters) { InterfaceStrings.text(tr,"3D, Karakter taşları","3D, Character pieces") } else { InterfaceStrings.text(tr,"2D, Klasik taşlar","2D, Classic pieces") }
            this.selected = chosen
        }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = if(chosen)color.copy(alpha=.12f).compositeOver(Color.White) else Color.White),
            border = BorderStroke(if(chosen)2.5.dp else 1.dp,if(chosen)color else color.copy(alpha=.15f)), elevation = CardDefaults.cardElevation(3.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal=10.dp,vertical=9.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(74.dp,62.dp),contentAlignment=Alignment.Center) {
                    Box(Modifier.offset(x=(-14).dp,y=(-3).dp).size(46.dp,54.dp)){ChessPieceImage(Piece(PieceKind.KNIGHT,true),inset=0.dp,appearanceOverride=appearance)}
                    Box(Modifier.offset(x=16.dp,y=5.dp).size(46.dp,54.dp)){ChessPieceImage(Piece(PieceKind.ROOK,false),inset=0.dp,appearanceOverride=appearance)}
                }
                Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Text(if(characters)"3D" else "2D",style=MaterialTheme.typography.titleMedium,color=KidHeading)
                    Canvas(Modifier.size(18.dp)) {
                        if(chosen) {
                            drawCircle(color)
                            drawPath(Path().apply{moveTo(size.width*.25f,size.height*.5f);lineTo(size.width*.43f,size.height*.68f);lineTo(size.width*.77f,size.height*.31f)},Color.White,style=Stroke(2.dp.toPx(),cap=StrokeCap.Round,join=StrokeJoin.Round))
                        } else drawCircle(Color.Gray.copy(alpha=.35f),style=Stroke(1.5.dp.toPx()))
                    }
                }
            }
        }
    }
    Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Text(InterfaceStrings.text(tr,"Taş görünümü","Piece appearance"),style=MaterialTheme.typography.bodyMedium,color=KidHeading)
        if(large) {
            Choice(ChessAppearance.CLASSIC,Modifier.fillMaxWidth())
            Choice(ChessAppearance.CHARACTERS,Modifier.fillMaxWidth())
        } else Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Choice(ChessAppearance.CLASSIC,Modifier.weight(1f))
            Choice(ChessAppearance.CHARACTERS,Modifier.weight(1f))
        }
    }
}

// Same normalized contours and detail lines as iOS ChessFlatPiece, not a font glyph.
private fun flatPieceShapes(kind: PieceKind): List<Path> {
    fun polygon(vararg points: Float): Path = Path().apply {
        moveTo(points[0],points[1]); for(i in 2 until points.size step 2)lineTo(points[i],points[i+1]);close()
    }
    fun oval(x:Float,y:Float,w:Float,h:Float)=Path().apply{addOval(Rect(x,y,x+w,y+h))}
    fun rounded(x:Float,y:Float,w:Float,h:Float,r:Float)=Path().apply{addRoundRect(RoundRect(Rect(x,y,x+w,y+h),CornerRadius(r,r)))}
    if(kind==PieceKind.PAWN) return listOf(Path().apply {
        moveTo(.23f,.90f);lineTo(.77f,.90f);lineTo(.77f,.85f);lineTo(.70f,.79f);lineTo(.70f,.745f)
        cubicTo(.61f,.68f,.57f,.59f,.585f,.49f);lineTo(.585f,.46f);lineTo(.65f,.46f)
        quadraticTo(.70f,.44f,.66f,.415f);quadraticTo(.66f,.395f,.57f,.39f)
        cubicTo(.62f,.365f,.65f,.31f,.65f,.25f);cubicTo(.65f,.167f,.583f,.10f,.50f,.10f)
        cubicTo(.417f,.10f,.35f,.167f,.35f,.25f);cubicTo(.35f,.31f,.38f,.365f,.43f,.39f)
        quadraticTo(.34f,.395f,.34f,.415f);quadraticTo(.30f,.44f,.35f,.46f)
        lineTo(.415f,.46f);lineTo(.415f,.49f);cubicTo(.43f,.59f,.39f,.68f,.30f,.745f)
        lineTo(.30f,.79f);lineTo(.23f,.85f);close()
    })
    val paths=mutableListOf<Path>()
    if(kind!=PieceKind.KNIGHT) paths+=Path().apply {
        moveTo(.39f,.46f);lineTo(.61f,.46f);cubicTo(.57f,.59f,.58f,.67f,.70f,.76f)
        lineTo(.30f,.76f);cubicTo(.42f,.67f,.43f,.59f,.39f,.46f);close()
    }
    when(kind) {
        PieceKind.ROOK -> {
            paths+=polygon(.29f,.10f,.40f,.10f,.40f,.21f,.45f,.21f,.45f,.10f,.55f,.10f,.55f,.21f,.60f,.21f,.60f,.10f,.71f,.10f,.71f,.32f,.63f,.39f,.63f,.46f,.37f,.46f,.37f,.39f,.29f,.32f)
            paths+=rounded(.34f,.44f,.32f,.065f,.015f)
        }
        PieceKind.KING -> {
            paths+=polygon(.465f,.10f,.535f,.10f,.535f,.17f,.605f,.17f,.605f,.23f,.535f,.23f,.535f,.30f,.465f,.30f,.465f,.23f,.395f,.23f,.395f,.17f,.465f,.17f)
            paths+=Path().apply{moveTo(.37f,.46f);cubicTo(.31f,.43f,.25f,.38f,.28f,.33f);cubicTo(.31f,.25f,.44f,.27f,.50f,.31f);cubicTo(.56f,.27f,.69f,.25f,.72f,.33f);cubicTo(.75f,.38f,.69f,.43f,.63f,.46f);close()}
            paths+=oval(.34f,.43f,.32f,.075f)
        }
        PieceKind.QUEEN -> {
            paths+=polygon(.27f,.22f,.39f,.33f,.38f,.17f,.47f,.30f,.50f,.14f,.53f,.30f,.62f,.17f,.61f,.33f,.73f,.22f,.64f,.45f,.36f,.45f)
            listOf(.235f to .18f,.345f to .135f,.465f to .10f,.585f to .135f,.695f to .18f).forEach{(x,y)->paths+=oval(x,y,.07f,.07f)}
            paths+=oval(.34f,.43f,.32f,.075f)
        }
        PieceKind.BISHOP -> {
            paths+=Path().apply{moveTo(.50f,.17f);cubicTo(.55f,.22f,.66f,.28f,.66f,.36f);cubicTo(.66f,.42f,.59f,.46f,.50f,.46f);cubicTo(.41f,.46f,.34f,.42f,.34f,.36f);cubicTo(.34f,.28f,.45f,.22f,.50f,.17f);close()}
            paths+=oval(.465f,.10f,.07f,.07f);paths+=oval(.33f,.44f,.34f,.075f)
        }
        PieceKind.KNIGHT -> paths+=Path().apply {
            moveTo(.30f,.76f);cubicTo(.30f,.65f,.46f,.55f,.43f,.47f);lineTo(.43f,.38f);cubicTo(.38f,.41f,.32f,.45f,.27f,.44f)
            lineTo(.23f,.37f);lineTo(.34f,.24f);lineTo(.38f,.18f);lineTo(.39f,.10f);lineTo(.46f,.16f);lineTo(.52f,.12f)
            cubicTo(.67f,.18f,.73f,.32f,.72f,.46f);cubicTo(.74f,.58f,.66f,.65f,.69f,.76f);close()
        }
        PieceKind.PAWN -> Unit
    }
    paths+=rounded(.29f,.73f,.42f,.065f,.025f)
    paths+=polygon(.30f,.79f,.70f,.79f,.77f,.85f,.77f,.90f,.23f,.90f,.23f,.85f)
    return paths
}
private fun flatPieceDetails(kind:PieceKind)=Path().apply {
    fun line(x1:Float,y1:Float,x2:Float,y2:Float){moveTo(x1,y1);lineTo(x2,y2)}
    line(.27f,.85f,.73f,.85f)
    when(kind) {
        PieceKind.BISHOP -> line(.55f,.245f,.46f,.35f)
        PieceKind.ROOK -> {line(.30f,.32f,.70f,.32f);line(.38f,.39f,.62f,.39f)}
        PieceKind.QUEEN -> line(.365f,.395f,.635f,.395f)
        PieceKind.KING -> line(.50f,.325f,.50f,.42f)
        PieceKind.KNIGHT -> {moveTo(.54f,.235f);cubicTo(.67f,.35f,.66f,.52f,.62f,.67f);line(.265f,.37f,.315f,.365f)}
        PieceKind.PAWN -> {line(.38f,.46f,.62f,.46f);line(.30f,.79f,.70f,.79f)}
    }
}
@Composable internal fun FlatChessPiece(piece:Piece,modifier:Modifier=Modifier) {
    val shapes=remember(piece.kind){flatPieceShapes(piece.kind)}
    val details=remember(piece.kind){flatPieceDetails(piece.kind)}
    Canvas(modifier) {
        val side=minOf(size.width,size.height)
        val ink=Color(0xFF1F2126)
        val fill=if(piece.white)Color(0xFFFCFAF0) else Color(0xFF30333B)
        val detail=if(piece.white)ink else Color(0xFFE0E3E6)
        translate((size.width-side)/2,(size.height-side)/2) {
            scale(side,side,pivot=Offset.Zero) {
                val stroke=Stroke(.022f,cap=StrokeCap.Round,join=StrokeJoin.Round)
                shapes.forEach{drawPath(it,fill);drawPath(it,ink,style=stroke)}
                drawPath(details,detail,style=stroke)
                if(piece.kind==PieceKind.KNIGHT)drawOval(detail,Offset(.405f,.245f),androidx.compose.ui.geometry.Size(.037f,.037f))
            }
        }
    }
}
