package com.demarpsoftware.kidswordplus

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.demarpsoftware.kidswordplus.chess.*

@Composable internal fun ChessOpeningsScreen(state: AppState, model: AppModel, tablet: Boolean, back: () -> Unit) {
    val tr = state.ui == "tr"
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    var libraryPage by rememberSaveable { mutableStateOf(true) }
    var openingId by rememberSaveable { mutableStateOf(ChessOpenings.all.first().id) }
    PageHelp(if(libraryPage)"openings"else "opening")
    BackHandler(!libraryPage) { libraryPage = true }
    ChessStopSpeechOnChange(libraryPage)
    if (libraryPage) {
        ChessOpeningLibrary(state, tablet, back) { openingId = it; libraryPage = false }
        return
    }
    ChessOpeningLesson(state, model, ChessOpenings.all.first { it.id == openingId }, tablet) { libraryPage = true }
}
