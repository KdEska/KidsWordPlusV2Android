package com.demarpsoftware.kidswordplus

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import com.demarpsoftware.kidswordplus.chess.*

@Composable internal fun ChessStatisticsPanel(statistics:ChessStatistics,tr:Boolean) {
    val games=statistics.games.filter {it.computer}
    val draws=games.count {it.outcome!=ChessOutcome.WHITE_WON && it.outcome!=ChessOutcome.BLACK_WON}
    val known=games.filter {it.playerWhite!=null}
    val wins=known.count {(it.outcome==ChessOutcome.WHITE_WON && it.playerWhite==true)||(it.outcome==ChessOutcome.BLACK_WON && it.playerWhite==false)}
    val losses=known.count {(it.outcome==ChessOutcome.WHITE_WON && it.playerWhite==false)||(it.outcome==ChessOutcome.BLACK_WON && it.playerWhite==true)}
    KidSurface {
        Text(InterfaceStrings.text(tr,"İstatistikler","Statistics"),style=MaterialTheme.typography.titleMedium,color=KidHeading)
        Text((InterfaceStrings.text(tr,"Bilgisayarla oynanan oyun: ","Computer games played: "))+games.size)
        Row(Modifier.fillMaxWidth()) {
            listOf(wins to InterfaceStrings.text(tr,"Kazandın","Won"),losses to InterfaceStrings.text(tr,"Kaybettin","Lost"),draws to InterfaceStrings.text(tr,"Beraberlik","Draw")).forEach {(count,label) ->
                Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally) {
                    Text(count.toString(),style=MaterialTheme.typography.headlineSmall,color=KidHeading)
                    Text(label,style=MaterialTheme.typography.labelMedium)
                }
            }
        }
        val legacy=games.count {it.playerWhite==null && it.outcome in listOf(ChessOutcome.WHITE_WON,ChessOutcome.BLACK_WON)}
        if(legacy>0) Text(InterfaceStrings.render(tr, "{0} eski oyunda oyuncu rengi kayıtlı değil.", "Player color is unavailable for {0} older games.", (legacy).toString()),style=MaterialTheme.typography.bodySmall,color=KidSecondary)
    }
}
