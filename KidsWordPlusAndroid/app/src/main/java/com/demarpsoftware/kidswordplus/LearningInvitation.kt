package com.demarpsoftware.kidswordplus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.demarpsoftware.kidswordplus.content.Lesson

@Composable internal fun LearningInvitation(state: AppState, dismiss: () -> Unit, start: (Lesson?) -> Unit) {
    fun t(tr: String, en: String) = InterfaceStrings.text(state.ui, tr, en)
    val suggested = state.pack?.lessons?.let { lessons ->
        lessons.firstOrNull { it.id == state.lastLesson && it.words.any { word -> LearningRules.progressKey(state.language, word.id) !in state.completed } }
            ?: lessons.firstOrNull { it.words.any { word -> LearningRules.progressKey(state.language, word.id) !in state.completed } } ?: lessons.firstOrNull()
    }
    Dialog(onDismissRequest = dismiss) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
                DesignArt("companions/${state.companion}.png", Modifier.size(140.dp), decodeLimit = 512)
                Text(t("Kelime öğrenmeye hazır mısın?", "Ready to learn some words?"), style = MaterialTheme.typography.headlineMedium, color = KidHeading, textAlign = TextAlign.Center)
                Text(t("Birlikte yeni kelimeler keşfedelim!", "Let's discover new words together!"), textAlign = TextAlign.Center)
                if (suggested != null) KidAction(t("Derse başla", "Start lesson"), enabled = !state.busy) { start(suggested) }
                else KidAction(t("Kategorilere git", "Explore categories")) { start(null) }
                TextButton(dismiss) { Text(t("Daha sonra", "Maybe later")) }
            }
        }
    }
}
