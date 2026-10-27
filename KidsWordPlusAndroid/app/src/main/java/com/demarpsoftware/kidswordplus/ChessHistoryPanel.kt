package com.demarpsoftware.kidswordplus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.demarpsoftware.kidswordplus.chess.*

@Composable internal fun ChessHistoryPanel(game:ChessGame,tr:Boolean) {
    var expanded by rememberSaveable(game.id) {mutableStateOf(false)}
    val rows=remember(game.id,game.startFen,game.history) {chessHistoryRows(game.history,game.startFen)}
    val scroll=rememberLazyListState()
    LaunchedEffect(expanded,rows.size,game.history.size) {
        if(expanded && rows.isNotEmpty())scroll.scrollToItem(rows.lastIndex)
    }
    KidSurface {
        TextButton(onClick={expanded=!expanded},modifier=Modifier.fillMaxWidth().semantics {stateDescription=if(expanded) InterfaceStrings.text(tr,"Açık","Expanded") else InterfaceStrings.text(tr,"Kapalı","Collapsed")}) {
            Text(InterfaceStrings.text(tr,"Hamleler","Moves"),Modifier.weight(1f),style=MaterialTheme.typography.titleMedium,color=KidHeading)
            Text(if(expanded)"⌃"else "⌄",color=KidHeading)
        }
        if(expanded) {
            if(rows.isEmpty())Text(InterfaceStrings.text(tr,"Henüz hamle yapılmadı.","No moves yet."))
            else LazyColumn(Modifier.fillMaxWidth().heightIn(max=220.dp),state=scroll,verticalArrangement=Arrangement.spacedBy(10.dp)) {
                items(rows,key={it.number}) {row ->
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.Top,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        Text("${row.number}.",Modifier.widthIn(min=28.dp),color=KidSecondary)
                        Box(Modifier.weight(1f)){HistoryCell(row.white,tr)}
                        Box(Modifier.weight(1f)){HistoryCell(row.black,tr)}
                    }
                }
            }
        }
    }
}
@Composable private fun HistoryCell(entry:ChessHistoryEntry?,tr:Boolean) {
    if(entry==null){Text("—",color=KidSecondary);return}
    val side=if(entry.actor.white) InterfaceStrings.text(tr,"Beyaz","White") else InterfaceStrings.text(tr,"Siyah","Black")
    val name=pieceTitle(entry.actor.kind,tr)+(entry.move.promotion?.let {" → "+pieceTitle(it,tr)} ?: "")
    val notation="${squareName(entry.move.from)} → ${squareName(entry.move.to)}"
    Column(Modifier.clearAndSetSemantics {contentDescription="$side, $name, $notation"},verticalArrangement=Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(32.dp)){ChessPieceImage(entry.actor)}
            Column {Text(side,style=MaterialTheme.typography.labelSmall,color=KidSecondary);Text(name,style=MaterialTheme.typography.labelMedium,color=KidBody)}
        }
        Text(notation,fontFamily=FontFamily.Monospace,style=MaterialTheme.typography.bodySmall,color=KidBody)
    }
}
