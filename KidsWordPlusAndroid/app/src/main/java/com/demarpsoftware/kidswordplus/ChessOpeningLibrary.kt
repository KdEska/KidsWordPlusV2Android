package com.demarpsoftware.kidswordplus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.demarpsoftware.kidswordplus.chess.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Composable internal fun openingCopy(language: String): State<Map<String, String>> {
    val context = LocalContext.current
    return produceState(emptyMap(), language) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val root = context.assets.open("chess/voices/$language/content.json").bufferedReader().use { JSONObject(it.readText()) }
                root.keys().asSequence().filter { it.startsWith("opening.") }.associateWith { root.getString(it) }
            }.getOrDefault(emptyMap())
        }
    }
}

private val openingColors = listOf(Color(0xFFAF52DE), Color(0xFFFF9500), Color(0xFF30B0C7), Color(0xFF007AFF), Color(0xFFFF2D55), Color(0xFF5856D6), Color(0xFF34C759), Color(0xFF32ADE6))

@Composable internal fun ChessOpeningLibrary(state: AppState, tablet: Boolean, back: () -> Unit, select: (String) -> Unit) {
    val tr = state.ui == "tr"
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    val audio = LocalChessAudio.current
    ScreenTitle(t("Açılışları öğren", "Learn openings"))
    val copy by openingCopy(state.ui)
    var principle by rememberSaveable { mutableStateOf<String?>(null) }
    val principles = listOf("center" to t("Merkezi kontrol et", "Control the centre"), "develop" to t("Taşlarını geliştir", "Develop your pieces"), "safety" to t("Şahını güvende tut", "Keep your king safe"), "threats" to t("Tehditleri kontrol et", "Check for threats"))
    val large = LocalDensity.current.fontScale >= 1.6f
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        ChessPageNavigation(back,hideTabs=false)
        KidSurface {
            if (tablet && !large) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                DesignArt("ui/chess/openings-lesson-hero.webp", Modifier.widthIn(max = 430.dp).weight(1f).aspectRatio(1.5f).clip(RoundedCornerShape(24.dp)), decodeLimit = 768)
                Text(t("Açılışları öğren", "Learn openings"), Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium, color = KidHeading)
            } else {
                DesignArt("ui/chess/openings-lesson-hero.webp", Modifier.fillMaxWidth().aspectRatio(1.5f).clip(RoundedCornerShape(24.dp)), decodeLimit = 768)
                Text(t("Açılışları öğren", "Learn openings"), style = MaterialTheme.typography.headlineMedium, color = KidHeading)
            }
        }
        principles.withIndex().toList().chunked(if (large) 1 else if (tablet) 4 else 2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (index, entry) ->
                    val color = openingColors[index]
                    Card(onClick = { audio?.stop(); principle = entry.first.takeUnless { principle == it } }, modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(2.dp, color.copy(alpha = if(principle == entry.first) .7f else .2f))) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            PrincipleIcon(index, color)
                            Text(entry.second, Modifier.fillMaxWidth().heightIn(min = 42.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = KidHeading)
                            Text(if (principle == entry.first) "−" else "+", color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        principle?.let { key ->
            val color = openingColors[principles.indexOfFirst { it.first == key }.coerceAtLeast(0)]
            Text(copy["opening.$key"].orEmpty(), Modifier.fillMaxWidth().background(color.copy(alpha = .12f), RoundedCornerShape(22.dp)).border(1.dp, color.copy(alpha = .35f), RoundedCornerShape(22.dp)).padding(18.dp))
            ChessListenButton(state.ui, "opening.$key")
        }
        Text(t("Ünlü açılışlar", "Famous openings"), style = MaterialTheme.typography.titleLarge, color = KidHeading)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = if (large) 1 else maxOf(1, ((maxWidth.value + 14) / (if(tablet) 234 else 159)).toInt())
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ChessOpenings.all.withIndex().toList().chunked(columns).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        row.forEach { (index, opening) ->
                            val color = openingColors[index]
                            Card(onClick = { select(opening.id) }, modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(24.dp), border = BorderStroke(1.5.dp, color.copy(alpha = .25f)), elevation = CardDefaults.cardElevation(4.dp)) {
                                Box(Modifier.fillMaxWidth().background(color.copy(alpha = .14f)).padding(12.dp), contentAlignment = Alignment.Center) { OpeningPreview(opening) }
                                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(copy["opening.${opening.id}.name"] ?: if (tr) opening.tr else opening.en, Modifier.fillMaxWidth().heightIn(min = 50.dp), textAlign = TextAlign.Center, color = KidHeading, style = MaterialTheme.typography.titleMedium)
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text("${index+1}", Modifier.background(color.copy(alpha = .13f), RoundedCornerShape(50)).padding(8.dp), color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.weight(1f))
                                        DesignArt(if (opening.id in state.chessOpeningProgress.completed) "ui/navigation/green-tick.webp" else "ui/navigation/next.webp", Modifier.size(28.dp), decodeLimit = 128)
                                    }
                                }
                            }
                        }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
        ChessListenButton(state.ui, "opening.intro")
        Text(copy["opening.intro"].orEmpty(), color = KidBody, style = MaterialTheme.typography.bodyMedium)
        ChessAudioStatus(state.ui)
    }
}

@Composable private fun OpeningPreview(opening: ChessOpening) {
    val position = remember(opening.id) { opening.position(opening.steps.size) }
    val last = opening.steps.last().move.to
    Column(Modifier.widthIn(max = 220.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp))) {
        repeat(8) { row -> Row(Modifier.fillMaxWidth()) {
            repeat(8) { column ->
                val index = row * 8 + column
                Box(Modifier.weight(1f).aspectRatio(1f).background(if ((row+column)%2==0) Color(0xFFFAEDD1) else Color(0xFF8761AB)).then(if (index==last) Modifier.border(2.dp,Color.Yellow) else Modifier)) {
                    position.board[index]?.let { ChessPieceImage(it, inset = 1.dp, appearanceOverride = ChessAppearance.CHARACTERS) }
                }
            }
        } }
    }
}

@Composable private fun PrincipleIcon(index: Int, color: Color) {
    Canvas(Modifier.size(62.dp).background(color,RoundedCornerShape(20.dp)).padding(16.dp)) {
        val white=Color.White; val stroke=Stroke(2.5.dp.toPx()); val w=size.width;val h=size.height
        when(index) {
            0 -> {drawCircle(white,w*.42f,center,style=stroke);drawCircle(white,w*.19f,center,style=stroke);drawLine(white,Offset(w*.65f,h*.35f),Offset(w,h*0f),3.dp.toPx())}
            1 -> {drawLine(white,Offset(w*.1f,h*.9f),Offset(w*.9f,h*.1f),3.dp.toPx());drawLine(white,Offset(w*.35f,h*.1f),Offset(w*.9f,h*.1f),3.dp.toPx());drawLine(white,Offset(w*.9f,h*.1f),Offset(w*.9f,h*.65f),3.dp.toPx())}
            2 -> {val p=Path().apply{moveTo(w*.5f,0f);lineTo(w,h*.2f);lineTo(w*.85f,h*.7f);lineTo(w*.5f,h);lineTo(w*.15f,h*.7f);lineTo(0f,h*.2f);close()};drawPath(p,white,style=stroke)}
            else -> {drawOval(white,Offset(0f,h*.2f),androidx.compose.ui.geometry.Size(w,h*.6f),style=stroke);drawCircle(white,w*.12f)}
        }
    }
}
