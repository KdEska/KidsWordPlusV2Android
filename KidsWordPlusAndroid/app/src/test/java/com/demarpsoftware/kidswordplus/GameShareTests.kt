package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.content.Word
import com.demarpsoftware.kidswordplus.games.*
import org.junit.Assert.*
import org.junit.Test

class GameShareTests {
    private val words=(1..4).map {Word("w$it","animals","pets",1,it,"word$it","$it.mp3","$it.png",false)}
    @Test fun unfinishedRoundsNeverProduceCompletionCards() {
        GameKind.entries.forEach {kind ->
            assertNull(GameShareInfo.completed(GameSession(language="tr",kind=kind,words=words),true))
        }
    }
    @Test fun completedCardsUseQuestionsOrPairsRatherThanCardsOrCorrectAnswers() {
        GameKind.entries.forEach {kind ->
            val game=GameSession(language="tr",kind=kind,words=words,questions=words.take(3).map {Question(it,words)},cards=words+words,finished=true)
            val count=if(kind==GameKind.LISTENING||kind==GameKind.REVIEW)3 else 4
            assertEquals("Tamamlandı · $count",GameShareInfo.completed(game,true)!!.detail)
            assertEquals("Completed · $count",GameShareInfo.completed(game,false)!!.detail)
        }
        val memory=GameShareInfo.completed(GameSession(language="tr",kind=GameKind.MEMORY,words=words,finished=true),false)!!
        assertEquals(FamilyShareSymbol.MEMORY,memory.symbol)
        assertEquals("Memory Cards",memory.title)
    }
}
