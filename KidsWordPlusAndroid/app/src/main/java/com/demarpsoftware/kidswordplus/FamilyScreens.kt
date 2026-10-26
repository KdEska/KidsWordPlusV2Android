package com.demarpsoftware.kidswordplus

import android.graphics.BitmapFactory
import android.os.SystemClock
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.demarpsoftware.kidswordplus.family.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.random.Random

@Composable internal fun CompanionImage(id: String, height: Int = 140) {
    val context = LocalContext.current
    val bitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, id) {
        value = withContext(Dispatchers.IO) { runCatching {
            context.assets.open("companions/$id.png").use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
        }.getOrNull() }
    }
    bitmap?.let { Image(it, Companions.all.find { friend -> friend.id == id }?.name ?: "", Modifier.fillMaxWidth().height(height.dp)) }
}
@Composable internal fun ColumnScope.CompanionScreen(state: AppState, model: AppModel, tablet: Boolean) {
    val tr = state.ui == "tr"
    Text(InterfaceStrings.text(tr,"Arkadaşların","Your companions"), style = MaterialTheme.typography.headlineMedium)
    LazyVerticalGrid(GridCells.Adaptive(if (tablet) 220.dp else 144.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(Companions.all, key = { it.id }) { friend ->
            val unlocked = friend.id in state.earned
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (friend.id != "nova" || unlocked) CompanionImage(friend.id, 120)
                Text(if (friend.id == "nova" && !unlocked) "???" else friend.name, style = MaterialTheme.typography.titleLarge)
                if (unlocked) {
                    Text(if (tr) friend.tr else friend.en)
                    IosButton(enabled = !state.busy && state.companion != friend.id, onClick = { model.selectCompanion(friend.id) }) { Text(if (state.companion == friend.id) { InterfaceStrings.text(tr,"Seçili","Selected") } else InterfaceStrings.text(tr,"Beni seç","Choose me")) }
                } else Text(Companions.requirement(friend, tr))
            } }
        }
    }
}
@Composable internal fun ParentGateScreen(state: AppState, model: AppModel) {
    val tr = state.ui == "tr"
    var challenge by remember { mutableStateOf(false) }
    val a = remember { Random.nextInt(11, 20) }; val b = remember { Random.nextInt(11, 20) }
    var answer by remember { mutableStateOf("") }; var wrong by remember { mutableStateOf(false) }
    var fraction by remember { mutableFloatStateOf(0f) }
    val started = state.parentGate.started
    LaunchedEffect(started) {
        if (started == null) { fraction = 0f; return@LaunchedEffect }
        while (true) {
            fraction = ((SystemClock.elapsedRealtime() - started) / 3000f).coerceIn(0f, 1f)
            model.tickParentHold(); delay(20)
        }
    }
    DisposableEffect(Unit) { onDispose { model.releaseParentHold() } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(22.dp,Alignment.CenterVertically),horizontalAlignment=Alignment.CenterHorizontally) {
        DesignArt("ui/parent/parent-lock.webp",Modifier.size(160.dp),decodeLimit=512)
        Text(InterfaceStrings.text(tr,"Ebeveyn alanı","Parent area"),style=MaterialTheme.typography.headlineMedium,color=KidHeading)
        KidSurface(Modifier.widthIn(max=340.dp)) {
            Text(InterfaceStrings.text(tr,"Devam etmek için","To continue"),Modifier.fillMaxWidth(),textAlign=TextAlign.Center,color=KidSecondary)
            Text(InterfaceStrings.text(tr,"Düğmeye 3 saniye basılı tutun.","Press and hold the button for 3 seconds."),Modifier.fillMaxWidth(),textAlign=TextAlign.Center,style=MaterialTheme.typography.titleMedium,color=KidHeading)
        }
        Box(Modifier.widthIn(max=260.dp).fillMaxWidth()){KidProgress(fraction)}
        val holdLabel = InterfaceStrings.text(tr,"Basılı tut","Press and hold")
        Text(holdLabel, Modifier.widthIn(max=260.dp).fillMaxWidth().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFFAF52DE),KidHeading)),RoundedCornerShape(50)).padding(20.dp)
            .semantics { role = Role.Button; contentDescription = holdLabel }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume(); model.beginParentHold()
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val pointer = event.changes.find { it.id == down.id } ?: break
                            if (!pointer.pressed || (pointer.position - down.position).getDistance() > 20.dp.toPx()) break
                            pointer.consume()
                        }
                    } finally { model.releaseParentHold() }
                }
            }, color = Color.White, textAlign = TextAlign.Center)
        OutlinedButton(onClick = { challenge = !challenge; model.releaseParentHold() }) { Text(InterfaceStrings.text(tr,"Alternatif doğrulama","Alternative verification")) }
        if (challenge) {
            Text(InterfaceStrings.text(tr,"Lütfen işlemin sonucunu yazın: $a + $b","Enter the answer: $a + $b"))
            OutlinedTextField(answer, { answer = it.take(4); wrong = false }, label = { Text(InterfaceStrings.text(tr,"Yanıt","Answer")) })
            IosButton(onClick = { wrong = !model.answerParentChallenge(a, b, answer) }) { Text(InterfaceStrings.text(tr,"Doğrula","Verify")) }
            if (wrong) Text(InterfaceStrings.text(tr,"Yeniden deneyin.","Try again."))
        }
    }
}
