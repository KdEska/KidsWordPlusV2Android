package com.demarpsoftware.kidswordplus

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.demarpsoftware.kidswordplus.chess.*

@Composable internal fun ChessCapturedPanel(game:ChessGame,tr:Boolean) {
    val captures=remember(game.history,game.startFen) {chessCapturedPieces(game.history,game.startFen)}
    KidSurface {
        listOf(true,false).forEach {byPlayer ->
            val taken=captures.filter {it.white != if(byPlayer)game.playerWhite else !game.playerWhite}
            val title=if(game.computer) {
                if(byPlayer) InterfaceStrings.text(tr,"Senin aldığın taşlar","Your captures") else InterfaceStrings.text(tr,"Bilgisayarın aldığı taşlar","Computer captures")
            } else if(byPlayer==game.playerWhite) InterfaceStrings.text(tr,"Beyazın aldığı taşlar","White captures") else InterfaceStrings.text(tr,"Siyahın aldığı taşlar","Black captures")
            Text(title,style=MaterialTheme.typography.labelMedium,color=KidBody)
            if(taken.isEmpty()) Text("—",color=KidSecondary)
            else Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                taken.forEach {piece ->
                    Box(Modifier.size(42.dp).clearAndSetSemantics {
                        contentDescription=(if(piece.white) InterfaceStrings.text(tr,"Beyaz","White") else InterfaceStrings.text(tr,"Siyah","Black"))+", "+pieceTitle(piece.kind,tr)
                    }) {ChessPieceImage(piece)}
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}
