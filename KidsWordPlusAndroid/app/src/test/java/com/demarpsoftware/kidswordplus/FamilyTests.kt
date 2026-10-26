package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.content.*
import com.demarpsoftware.kidswordplus.family.*
import com.demarpsoftware.kidswordplus.games.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.time.LocalDate

class FamilyTests {
    @Test fun startersAndThresholdsRequireBothWordsAndExtraTask() {
        assertEquals(21, Companions.all.size)
        assertEquals(21, Companions.all.map { it.id }.distinct().size)
        val starter = Companions.unlocked(CompanionEvidence(), emptySet())
        assertEquals(6, starter.size)
        assertTrue("piko-default" in starter)
        assertFalse("momo" in Companions.unlocked(CompanionEvidence(words = 25, games = 9), emptySet()))
        assertFalse("momo" in Companions.unlocked(CompanionEvidence(words = 24, games = 10), emptySet()))
        assertTrue("momo" in Companions.unlocked(CompanionEvidence(words = 25, games = 10), emptySet()))
        assertFalse("toto" in Companions.unlocked(CompanionEvidence(words = 200), emptySet()))
        assertTrue("toto" in Companions.unlocked(CompanionEvidence(words = 200, perfect = true), emptySet()))
        assertFalse("ollie" in Companions.unlocked(CompanionEvidence(words = 300, bestStreak = 6), emptySet()))
        assertTrue("ollie" in Companions.unlocked(CompanionEvidence(words = 300, bestStreak = 7), emptySet()))
        assertFalse("leo" in Companions.unlocked(CompanionEvidence(words = 1500), emptySet()))
        assertTrue("leo" in Companions.unlocked(CompanionEvidence(words = 1500, level = true), emptySet()))
    }
    @Test fun earnedFriendsStayUnlockedAndNovaRequiresAllTwenty() {
        val all = Companions.unlocked(CompanionEvidence(1500, 10, 7, true, true), emptySet())
        assertEquals(21, all.size)
        assertEquals(all, Companions.unlocked(CompanionEvidence(), all + "unknown"))
        val incomplete = all - "leo"
        assertFalse("nova" in Companions.unlocked(CompanionEvidence(), incomplete))
        assertTrue("nuki" in Companions.unlocked(CompanionEvidence(words = 50), emptySet()))
        assertFalse("nuki" in Companions.unlocked(CompanionEvidence(words = 49), emptySet()))
    }
    @Test fun characterStreakCombinesLanguagesWithoutCountingSameDateTwice() {
        val first = LocalDate.of(2026, 10, 19)
        val days = (0L..6L).associate { offset -> "${if (offset % 2L == 0L) "tr" else "en"}|${first.plusDays(offset)}" to DayActivity(reviews = setOf("a")) } +
            ("en|$first" to DayActivity(newWords = setOf("b")))
        val evidence = Companions.evidence(setOf("tr|a", "en|a"), GameProgress(days), false, false)
        assertEquals(2, evidence.words)
        assertEquals(7, evidence.bestStreak)
        val gapped = GameProgress(days.filterKeys { !it.endsWith(first.plusDays(3).toString()) })
        assertEquals(3, Companions.evidence(emptySet(), gapped, false, false).bestStreak)
    }
    @Test fun parentHoldNeedsThreeContinuousSecondsAndDoesNotSurviveReset() {
        val empty = ParentGate()
        assertFalse(empty.tick(10000).unlocked)
        assertTrue(empty.allowsManagement(false))
        assertFalse(empty.allowsManagement(true))
        assertTrue(empty.begin(0).tick(3000).allowsManagement(true))
        val pressed = empty.begin(1000)
        assertFalse(pressed.tick(3999).unlocked)
        assertEquals(0.5f, pressed.fraction(2500), 0.001f)
        assertTrue(pressed.tick(4000).unlocked)
        assertFalse(pressed.release().tick(10000).unlocked)
        assertFalse(pressed.release().begin(5000).tick(7999).unlocked)
        assertFalse(ParentGate().unlocked)
        assertTrue(pressed.tick(4000).release().unlocked)
        assertEquals(0f, pressed.fraction(999), 0f)
    }
    private fun word(id: String, level: String? = "beginner") = Word(id, "animals", "pets", 1, 1, id, "a.mp3", "a.jpg", true, level)
    private fun pack(language: String): LearningPack {
        val words = listOf(word("a"), word("b"), word("c", "advanced"), word("d", "advanced"))
        return LearningPack(language, File("unused"), File("unused"), words,
            listOf(Lesson("animals/pets/1", "animals", "pets", 1, words.take(2)), Lesson("animals/pets/2", "animals", "pets", 2, words.drop(2))))
    }
    @Test fun learningLevelRequiresAllWordsInSameLanguage() {
        val pack = pack("tr")
        assertFalse(Companions.finishedLevel(pack, setOf("tr|a", "en|b")))
        assertTrue(Companions.finishedLevel(pack, setOf("tr|a", "tr|b")))
        assertFalse(Companions.finishedLevel(null, setOf("tr|a", "tr|b")))
        assertFalse(Companions.finishedLevel(pack.copy(words = pack.words.map { it.copy(difficulty = null) }), setOf("tr|a", "tr|b", "tr|c", "tr|d")))
    }
    @Test fun reportsUseMondayWeekAndCurrentLanguageActiveContent() {
        val monday = LocalDate.of(2026, 10, 19)
        val today = monday.plusDays(2)
        val progress = GameProgress(days = mapOf(
            "tr|${monday.minusDays(1)}" to DayActivity(newWords = setOf("old")),
            "tr|$monday" to DayActivity(newWords = setOf("a"), reviews = setOf("b")),
            "tr|${monday.plusDays(1)}" to DayActivity(newWords = setOf("a"), reviews = setOf("b")),
            "tr|$today" to DayActivity(sessions = setOf("one"), rewarded = true),
            "en|$monday" to DayActivity(newWords = setOf("other"), sessions = setOf("x", "y"), rewarded = true)))
        val report = ParentReport.build(pack("tr"), setOf("tr|a", "tr|b", "tr|deleted", "en|c"), progress, today)
        assertEquals(2, report.studied)
        assertEquals(4, report.total)
        assertEquals(1, report.lessons)
        assertEquals(1, report.weeklyNew)
        assertEquals(2, report.weeklyReviews)
        assertEquals(3, report.activeDays)
        assertEquals(1, report.games)
        assertEquals(20, report.stars)
        assertEquals(4, report.streak)
        assertEquals(monday, report.daily.first().first)
        assertEquals(monday.plusDays(6), report.daily.last().first)
        assertEquals(2, report.categories.single().studied)
        assertEquals(0, ParentReport.build(pack("en"), emptySet(), GameProgress(), today).streak)
    }
}
