package com.demarpsoftware.kidswordplus

import org.junit.Assert.*
import org.junit.Test
import org.json.JSONObject
import java.io.File
import kotlin.random.Random

class ChessSecretTests {
    @Test fun everySecretAppearsBeforeARepeatAndCyclesDoNotRepeatAtBoundary() {
        val ids=chessSecretCopies.map {it.id}
        assertEquals(40,ids.size)
        var deck=ChessSecretDeck()
        val random=Random(45)
        var previous:String?=null
        repeat(20) {
            val drawn=mutableSetOf<String>()
            repeat(ids.size) {
                val (next,id)=deck.draw(ids,random)
                assertNotEquals(previous,id);assertTrue(drawn.add(id))
                previous=id;deck=next
            }
            assertEquals(ids.toSet(),drawn)
        }
    }
    @Test fun persistenceKeepsRemainingOrderAndLastSelection() {
        var deck=ChessSecretDeck()
        val random=Random(12)
        repeat(17) {
            deck=deck.draw(chessSecretCopies.map {it.id},random).first
            assertEquals(deck,ChessSecretDeck.decode(deck.encode()))
        }
        val restored=ChessSecretDeck.decode(deck.encode())
        assertEquals(deck.draw(chessSecretCopies.map {it.id},Random(5)),restored.draw(chessSecretCopies.map {it.id},Random(5)))
    }
    @Test fun invalidSavedIdsAndDuplicatesAreRemoved() {
        val deck=ChessSecretDeck(listOf("unknown","a","a","b"),"c")
        val (next,id)=deck.draw(listOf("a","b","c"),Random(1))
        assertEquals("a",id);assertEquals(listOf("b"),next.remaining)
        assertEquals(ChessSecretDeck(),ChessSecretDeck.decode("broken json"))
    }
    @Test fun allFortySecretsHaveThirteenMatchingTranslations() {
        val root=File(requireNotNull(System.getProperty("kwp.packages"))).parentFile
        val master=JSONObject(File(root,"planning/portal-refresh/chess-secrets.json").readText())
        val dictionary=JSONObject(File(root,"KidsWordPlusAndroid/app/src/main/assets/localization/interface.json").readText())
        val tips=master.getJSONArray("tips")
        assertEquals(40,tips.length());assertEquals(13,master.getJSONArray("languages").length())
        repeat(tips.length()) {index->
            val item=tips.getJSONObject(index);val text=item.getJSONObject("text")
            val copy=chessSecretCopies.first {it.id==item.getString("id")}
            assertEquals(text.getString("tr"),copy.turkish);assertEquals(text.getString("en"),copy.english)
            for(language in InterfaceStrings.codes.filter {it !in setOf("tr","en")})
                assertEquals("${copy.id} $language",text.getString(language),dictionary.getJSONObject(language).getString(copy.english))
        }
    }
}
