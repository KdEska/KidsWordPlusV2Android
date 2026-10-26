package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.games.GameKind
import com.demarpsoftware.kidswordplus.games.GameSession

/** Public completion copy only; no child/profile data enters the share card. */
internal data class GameShareInfo(val title:String,val detail:String,val symbol:FamilyShareSymbol) {
    companion object {
        fun completed(game:GameSession,tr:Boolean):GameShareInfo? {
            if(!game.finished)return null
            val count=if(game.kind==GameKind.LISTENING||game.kind==GameKind.REVIEW)game.questions.size else game.words.size
            val title=when(game.kind) {
                GameKind.LISTENING -> InterfaceStrings.text(tr,"Dinle ve Seç","Listen and Choose")
                GameKind.REVIEW -> InterfaceStrings.text(tr,"Tekrar zamanı","Time to review")
                GameKind.MATCHING -> InterfaceStrings.text(tr,"Eşleştir","Match Pairs")
                GameKind.MEMORY -> InterfaceStrings.text(tr,"Hafıza Kartları","Memory Cards")
            }
            val symbol=when(game.kind) {
                GameKind.LISTENING,GameKind.REVIEW -> FamilyShareSymbol.LISTENING
                GameKind.MATCHING -> FamilyShareSymbol.MATCHING
                GameKind.MEMORY -> FamilyShareSymbol.MEMORY
            }
            return GameShareInfo(title,(InterfaceStrings.text(tr,"Tamamlandı","Completed"))+" · $count",symbol)
        }
    }
}
