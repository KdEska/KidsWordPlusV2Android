package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.content.*
import com.demarpsoftware.kidswordplus.games.GameProgress
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class VocabularyLevelTests {
    private val root=File(requireNotNull(System.getProperty("kwp.packages")))
    private val mapping=JSONObject(File(root.parentFile,"planning/vocabulary-levels/levels.json").readText()).getJSONObject("levels").let {j->j.keys().asSequence().associateWith {j.getString(it)}}
    @Test fun everyLanguageIncludesAllEasierLevels() {
        for(language in LearningLanguages.codes) {
            val pack=LearningPack.load(File(root,"packages/$language"),File(root,"packages/base"),language)
            val sets=listOf("beginner","intermediate","advanced").map {level ->
                val selected=pack.atLevel(level,mapping)
                assertTrue(selected.words.isNotEmpty())
                val ranks=listOf("beginner","intermediate","advanced")
                assertEquals(pack.words.filter {ranks.indexOf(mapping[it.id])<=ranks.indexOf(level)}.map {it.id}.toSet(),selected.words.map {it.id}.toSet())
                assertEquals(selected.words.map {it.id}.toSet(),selected.lessons.flatMap {it.words}.map {it.id}.toSet())
                assertTrue(selected.words.all {it.difficulty==mapping[it.id]})
                assertTrue(selected.lessons.all {it.id.endsWith("@$level")&&it.words.isNotEmpty()})
                selected.words.map {it.id}.toSet()
            }
            assertTrue(sets[1].containsAll(sets[0]));assertTrue(sets[2].containsAll(sets[1]))
            assertTrue(sets[0].size < sets[1].size && sets[1].size < sets[2].size)
            assertEquals(pack.words.map {it.id}.toSet(),sets[2])
        }
    }
    @Test fun changingLevelsPreservesWordAndStarHistory() {
        val history=GameProgress().finishLesson("tr","animals/pets/1@beginner")
        assertEquals(5,history.stars("tr"))
        val restored=GameProgress.decode(history.encode()).finishLesson("tr","animals/pets/1@beginner")
        assertEquals(5,restored.stars("tr"))
        assertEquals(10,restored.finishLesson("tr","animals/pets/1@intermediate").stars("tr"))
    }
    @Test fun familiarAndTechnicalConceptsAreSeparated() {
        val db=JSONObject(File(root,"packages/tr/database.json").readText()).getJSONArray("words")
        val words=(0 until db.length()).map {db.getJSONObject(it)}.associateBy {it.getString("displayText")}
        for(text in listOf("Kedi","Su","Elma","El"))assertEquals("beginner",words.getValue(text).getString("difficulty"))
        for(text in listOf("Pulsar","Mikrometre","Sinir sistemi"))assertEquals("advanced",words.getValue(text).getString("difficulty"))
    }
}
