package com.demarpsoftware.kidswordplus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.demarpsoftware.kidswordplus.chess.*

internal enum class ChessGuideDestination { PIECES, PRACTICE, MENU }

@Composable internal fun ChessFirstStepsGuide(state: AppState, dontShowAgain: Boolean, setDontShowAgain: (Boolean) -> Unit, dismiss: () -> Unit, finish: (ChessGuideDestination) -> Unit) {
    fun t(a: String, b: String) = InterfaceStrings.text(state.ui, a, b)
    val audio = LocalChessAudio.current
    var step by rememberSaveable { mutableIntStateOf(0) }
    var landed by rememberSaveable(step) { mutableStateOf(false) }
    val preferences=androidx.compose.ui.platform.LocalContext.current.getSharedPreferences("chess-guide-round",0)
    val seed=rememberSaveable { kotlin.random.Random.nextLong() }
    val excluded=rememberSaveable { preferences.getString("previous", "").orEmpty() }
    val moves=remember(seed,excluded) { ChessGuideGenerator.generate(kotlin.random.Random(seed),excluded.split("|").toSet()) }
    LaunchedEffect(moves) { preferences.edit().putString("previous",moves.joinToString("|"){it.signature}).apply() }
    val exercise = ChessFirstSteps(moves,step)
    LaunchedEffect(landed, step) {
        if (landed) {
            kotlinx.coroutines.delay(1400)
            step += 1
        }
    }
    fun update(next: ChessFirstSteps) { step = next.step }
    DisposableEffect(Unit) { audio?.stop(); onDispose { audio?.stop() } }

    Dialog(onDismissRequest = dismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.widthIn(max = 1100.dp).fillMaxWidth().safeDrawingPadding().padding(12.dp), shape = RoundedCornerShape(28.dp)) {
            BoxWithConstraints {
                val boardSize = minOf(maxWidth - 32.dp, (maxHeight - 300.dp).coerceAtLeast(200.dp))
                LearningBackdrop()
                Column(Modifier.fillMaxWidth().verticalScroll(remember(step) { ScrollState(0) }).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(t("İlk satranç adımlarım", "My first chess steps"), Modifier.fillMaxWidth().semantics { heading() }, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium, color = KidHeading)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        if (step > 0) TextButton(onClick = { audio?.stop(); update(exercise.back()) }) { Text(t("Geri", "Back")) }
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = dismiss) { Text(t("Daha sonra", "Later")) }
                    }
                    Text("${if(step==0)0 else if(exercise.complete)moves.size else (step-1)/2+1} / ${moves.size}", color = KidSecondary)
                    KidProgress((step + 1) / (exercise.finalStep + 1f))
                    if (step == 0 || exercise.complete) {
                        Row(Modifier.fillMaxWidth().toggleable(value = dontShowAgain, role = Role.Checkbox, onValueChange = setDontShowAgain), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = dontShowAgain, onCheckedChange = null)
                            Text(t("Bir daha gösterme", "Don’t show again"), Modifier.padding(start = 8.dp))
                        }
                        DesignArt("companions/${state.companion}.png", Modifier.size(100.dp), decodeLimit = 512)
                    }
                    val selections = listOf(
                        t("Bu bir piyon. Ona dokun!", "This is a pawn. Tap it!"),
                        t("Bu bir fil. Ona dokun!", "This is a bishop. Tap it!"),
                        t("Bu bir at. Ona dokun!", "This is a knight. Tap it!"),
                        t("Bu bir kale. Ona dokun!", "This is a rook. Tap it!"),
                        t("Bu bir vezir. Ona dokun!", "This is a queen. Tap it!"),
                        t("Bu bir şah. Ona dokun!", "This is a king. Tap it!"))
                    val instructions = listOf(
                        t("Taşı yeşil hedefe götür.", "Move the piece to the green target."),
                        t("Rakip taşlara dikkat et. Güvenli yeşil hedefe git.", "Watch the opposing pieces. Move to the safe green target."),
                        t("Yeşil işaretli rakip taşı al.", "Capture the opposing piece marked in green."))
                    val stages=listOf(t("Taşları hareket ettir", "Move the pieces"),t("Rakip taşlarla hareket et", "Move around opposing pieces"),t("Rakip taşları al", "Capture opposing pieces"))
                    if(step>0 && !exercise.complete)Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center) {
                        stages.forEachIndexed {index,title->Text("${index+1} / 3 · $title",modifier=Modifier.fillMaxWidth().alpha(0f).clearAndSetSemantics {},textAlign=TextAlign.Center) }
                        Text("${exercise.move.stage+1} / 3 · ${stages[exercise.move.stage]}",color=KidSecondary,textAlign=TextAlign.Center)
                    }
                    val success = "✓ " + t("Harika, doğru hamle!", "Great, correct move!")
                    val instruction = when {
                        step == 0 -> t("Satranç oynamaya hazır mısın?", "Ready to play chess?")
                        exercise.complete -> t("Tebrikler! Taşları hareket ettirdin ve rakip taşları aldın!", "Well done! You moved the pieces and captured opposing pieces!")
                        landed -> success
                        exercise.selecting -> selections[when(exercise.move.piece) {
                            PieceKind.PAWN -> 0
                            PieceKind.BISHOP -> 1
                            PieceKind.KNIGHT -> 2
                            PieceKind.ROOK -> 3
                            PieceKind.QUEEN -> 4
                            else -> 5
                        }]
                        else -> instructions[exercise.move.stage]
                    }
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        // Reserve the tallest translated prompt, including success, for every move.
                        if (step > 0 && !exercise.complete) (selections + instructions + success).forEach { prompt ->
                            Text(prompt, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().alpha(0f).clearAndSetSemantics {})
                        }
                        Text(instruction, style = MaterialTheme.typography.headlineMedium, color = if (landed) KidSuccess else KidHeading,
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().semantics { heading() })
                    }
                    if (step == 0) {
                        Text(t("Taşları hareket ettirelim, rakip taşları tanıyalım ve taş almayı öğrenelim!", "Let’s move the pieces, meet opposing pieces and learn to capture!"), textAlign = TextAlign.Center)
                        KidAction(t("Başlayalım", "Let’s begin"), tone = 2) { update(exercise.begin()) }
                    } else if (!exercise.complete) {
                        FirstStepsBoard(exercise, state.ui, boardSize, landed) { square ->
                            val next = exercise.tap(square)
                            if (!exercise.selecting && next != exercise) {
                                landed = true
                                audio?.celebrate(state.ui)
                            } else update(next)
                        }
                    } else {
                        run {
                            Text(t("Şimdi ne yapmak istersin?", "What would you like to do next?"), style = MaterialTheme.typography.titleMedium)
                            GuideChoice(t("Taşları tanı", "Meet the pieces"), t("Yeni başlıyorsan buradan başla. Her taşın hareketini keşfet.", "Start here if you are new. Discover how each piece moves.")) { finish(ChessGuideDestination.PIECES) }
                            GuideChoice(t("Birlikte alıştırma yapalım", "Practise together"), t("Hedef karelerle adım adım pratik yap.", "Practise step by step with target squares.")) { finish(ChessGuideDestination.PRACTICE) }
                            GuideChoice(t("Satranç biliyorum", "I know chess"), t("Satranç sayfasına geç ve oynamak istediğin bölümü seç.", "Open the chess page and choose what to play.")) { finish(ChessGuideDestination.MENU) }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun GuideChoice(title: String, detail: String, click: () -> Unit) {
    IosButton(onClick = click) {
        Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Text(detail, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}

@Composable private fun FirstStepsBoard(exercise: ChessFirstSteps, language: String, boardSize: Dp, landed: Boolean, tap: (Int) -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
    Column(Modifier.width(boardSize).clip(RoundedCornerShape(16.dp))) {
        repeat(8) { row ->
            Row {
                repeat(8) { column ->
                    val square = row * 8 + column
                    val pawn = square == if (landed) exercise.move.to else exercise.move.from
                    val target = !landed && square in exercise.targets
                    val enabled = !landed && ((exercise.selecting && pawn) || target)
                    val description = renderNamedInterfaceTemplate(
                        if (pawn) InterfaceStrings.text(language, "{square} karesindeki taşı seç", "Select the piece on {square}")
                        else InterfaceStrings.text(language, "Hedef kare {square}", "Target square {square}"),
                        mapOf("square" to squareName(square)))
                    Box(Modifier.weight(1f).aspectRatio(1f)
                        .background(if ((row + column) % 2 == 0) Color(0xFFF7E8C9) else Color(0xFF70509E))
                        .then(if (enabled) Modifier.clickable(role = Role.Button, onClick = { tap(square) }).semantics { contentDescription = description }
                              else if (pawn) Modifier.semantics { contentDescription = description } else Modifier)
                        .then(if (pawn || target) Modifier.border(3.dp, if (landed) KidSuccess else if (pawn && !exercise.selecting) Color.Blue else Color(0xFFF4A62A)) else Modifier),
                        contentAlignment = Alignment.Center) {
                        val occupant=if(pawn)Piece(exercise.move.piece,true) else if(landed && square==exercise.move.from)null else if(landed && square==exercise.move.to)null else exercise.move.board[square]
                        if(occupant!=null)ChessPieceImage(occupant)
                        if (target && occupant==null) Box(Modifier.fillMaxSize(.32f).background(KidSuccess, androidx.compose.foundation.shape.CircleShape))
                        if(target && occupant!=null)Box(Modifier.fillMaxSize().border(4.dp,KidSuccess))
                    }
                }
            }
        }
    }
}
}
