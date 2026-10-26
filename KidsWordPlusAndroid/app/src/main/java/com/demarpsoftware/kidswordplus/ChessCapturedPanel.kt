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
                if(tr) {if(byPlayer) "Senin aldığın taşlar" else "Bilgisayarın aldığı taşlar"}
                else {if(byPlayer) "Your captures" else "Computer captures"}
            } else if(tr) {if(byPlayer==game.playerWhite) "Beyazın aldığı taşlar" else "Siyahın aldığı taşlar"}
              else {if(byPlayer==game.playerWhite) "White captures" else "Black captures"}
            Text(title,style=MaterialTheme.typography.labelMedium,color=KidBody)
            if(taken.isEmpty()) Text("—",color=KidSecondary)
            else Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                taken.forEach {piece ->
                    Box(Modifier.size(42.dp).clearAndSetSemantics {
                        contentDescription=(if(tr) {if(piece.white) "Beyaz" else "Siyah"} else {if(piece.white) "White" else "Black"})+", "+pieceTitle(piece.kind,tr)
                    }) {ChessPieceImage(piece)}
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}
