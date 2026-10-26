package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.content.*
import com.demarpsoftware.kidswordplus.games.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.LocalDate
import kotlin.random.Random

class GameTests {
    @get:Rule val temporary = TemporaryFolder()
    private val date = LocalDate.of(2026, 10, 19)
    private fun word(id: String) = Word(id, "animals", "pets", 1, 1, id, "audio/$id.mp3", "images/$id.jpg", true)
    private val words get() = (1..10).map { word("w$it") }
    private fun studied(language: String, words: List<Word>) = words.map { LearningRules.progressKey(language, it.id) }.toSet()

    @Test fun listeningRequiresAudioAndKeepsFirstWrongAnswerUntilNextRound() {
        val pool = words.take(4)
        var round = GameRules.start("tr", GameKind.LISTENING, pool, 2, random = Random(7))!!
        val target = round.target!!
        assertEquals(round, round.answer(target.id))
        assertEquals(round, round.next())
        round = round.hear(target.id)
        val wrong = round.questions[0].options.first { it.id != target.id }.id
        val first = round.answer(wrong)
        var progress = GameProgress().transition(round, first, date)
        assertEquals(setOf(target.id), progress.reviewIds("tr", studied("tr", pool), pool.map { it.id }.toSet()))
        val solved = first.answer(target.id)
        progress = progress.transition(first, solved, date)
        assertFalse(solved.firstAnswers.getValue(target.id))
        assertEquals(1, progress.answers.getValue("listening|tr|${target.id}").attempts)
        assertEquals(setOf(target.id), progress.reviewIds("tr", studied("tr", pool), pool.map { it.id }.toSet()))
        assertTrue(target.id in progress.activity("tr", date).reviews)
        val next = solved.next()
        assertFalse(next.heard)
        assertEquals(next, next.answer(next.target!!.id))
        val nextWrong = next.hear(next.target!!.id).answer(target.id)
        assertEquals(false, nextWrong.firstAnswers[next.target!!.id])
        val review = GameRules.start("tr", GameKind.REVIEW, pool, 2, setOf(target.id), Random(1))!!.hear(target.id)
        progress = progress.transition(review, review.answer(target.id), date)
        assertTrue(progress.reviewIds("tr", studied("tr", pool), pool.map { it.id }.toSet()).isEmpty())
        assertTrue(progress.reviewIds("en", studied("en", pool), pool.map { it.id }.toSet()).isEmpty())
        assertEquals(0, progress.activity("tr", date).sessions.size)
    }
    @Test fun onlyFullyCompletedListeningRoundCountsAsGame() {
        var game = GameRules.start("en", GameKind.LISTENING, words.take(4), 2, random = Random(1))!!
        var progress = GameProgress()
        repeat(game.questions.size) {
            val heard = game.hear(game.target!!.id)
            val answer = heard.answer(heard.target!!.id)
            progress = progress.transition(heard, answer, date)
            assertTrue(progress.activity("en", date).sessions.isEmpty())
            val next = answer.next()
            progress = progress.transition(answer, next, date)
            if (next.finished) {
                progress = progress.transition(answer, next, date)
                assertEquals(1, progress.activity("en", date).sessions.size)
            }
            game = next
        }
        assertTrue(game.finished)
        assertEquals(game, game.next())
    }
    @Test fun matchingNeedsSelectedSoundAndRecordsOnlyFirstAttempt() {
        val pool = words.take(2)
        var game = GameRules.start("en", GameKind.MATCHING, pool, 2, random = Random(3))!!
        assertEquals(game, game.answer(pool[0].id))
        game = game.hear(pool[0].id).answer(pool[1].id).answer(pool[0].id)
        assertFalse(game.firstAnswers.getValue(pool[0].id))
        assertNull(game.selected)
        assertFalse(game.finished)
        assertEquals(game, game.hear(pool[0].id))
        game = game.hear(pool[1].id).answer(pool[1].id)
        assertTrue(game.finished)
        assertEquals(game, game.answer(pool[1].id))
    }
    @Test fun memoryBlocksDuplicateAndThirdFlipAndCreditsOnce() {
        val a = word("a"); val b = word("b")
        var game = GameSession(language = "tr", kind = GameKind.MEMORY, words = listOf(a, b), cards = listOf(a, b, a, b))
        val one = game.flip(0)
        assertEquals(one, one.flip(0))
        val wrong = one.flip(1)
        assertTrue(wrong.waiting)
        assertEquals(1, wrong.attempts)
        assertEquals(wrong, wrong.flip(2))
        game = wrong.hide().flip(0).flip(2)
        assertEquals(setOf(0, 2), game.matchedCards)
        assertEquals(game, game.flip(0))
        val before = game.flip(1)
        val after = before.flip(3)
        assertTrue(after.finished)
        assertEquals(3, after.attempts)
        var progress = GameProgress().transition(before, after, date)
        progress = progress.transition(before, after, date)
        assertEquals(1, progress.memoryCompleted["tr"])
        assertEquals(3, progress.memoryAttempts["tr"])
        assertEquals(1, progress.activity("tr", date).sessions.size)
        assertTrue(progress.activity("tr", date).reviews.isEmpty()) // Same as iOS: memory adds a game, not review IDs.
    }
    @Test fun lessonRewardOnlyOnceIncludingLegacyAndSavedProgress() {
        val initial=GameProgress().prepareLessonRewards("tr",setOf("old"))
        assertEquals(0,initial.finishLesson("tr","old").stars("tr"))
        val first=initial.finishLesson("tr","new")
        assertEquals(5,first.stars("tr"))
        val saved=GameProgress.decode(first.encode())
        assertEquals(saved,saved.finishLesson("tr","new"))
        assertEquals(10,saved.finishLesson("tr","another").stars("tr"))
        val english=saved.prepareLessonRewards("en",emptySet()).finishLesson("en","new")
        assertEquals(5,english.stars("en"))
        assertEquals(5,english.stars("tr"))
    }
    @Test fun fixedStarRewardsPreserveLegacyBalanceAndPersist() {
        val legacy=GameProgress(days=mapOf("tr|$date" to DayActivity(rewarded=true)))
        assertEquals(20,legacy.stars("tr"))
        val lesson=legacy.prepareLessonRewards("tr",emptySet()).finishLesson("tr","lesson1")
        assertEquals(25,lesson.stars("tr"))
        val replay=lesson.finishLesson("tr","lesson1").finishLesson("tr","lesson2")
        assertEquals(30,replay.stars("tr"))
        val round=GameSession(language="tr",kind=GameKind.MEMORY,words=words.take(2))
        val won=round.copy(finished=true)
        val game=replay.transition(round,won,date)
        assertEquals(40,game.stars("tr"))
        assertEquals(game,game.transition(round,won,date.plusDays(1)))
        assertEquals(0,game.stars("en"))
        assertEquals(game,GameProgress.decode(game.encode()))
    }
    @Test fun dailyRewardIsUniqueAndDatesAndLanguagesAreIndependent() {
        val pool = words
        val active = pool.map { it.id }.toSet()
        var completed = emptySet<String>()
        var progress = GameProgress().prepare("tr", active, completed, date)
        pool.take(5).forEach {
            progress = progress.study("tr", it.id, true, date)
            completed = LearningRules.complete(completed, "tr", it.id)
            progress = progress.prepare("tr", active, completed, date)
        }
        pool.forEach { progress = progress.study("tr", it.id, false, date) }
        val memory = GameSession(language = "tr", kind = GameKind.MEMORY, words = pool.take(2))
        val complete = memory.copy(finished = true, attempts = 2)
        progress = progress.transition(memory, complete, date)
        assertEquals(10, progress.stars("tr"))
        val second = memory.copy(id = "second")
        progress = progress.transition(second, second.copy(finished = true, attempts = 2), date)
        repeat(5) { progress = progress.transition(second, second.copy(finished = true, attempts = 2), date).prepare("tr", active, completed, date) }
        assertEquals(20, progress.stars("tr"))
        assertEquals(0, progress.stars("en"))
        assertFalse(progress.activity("tr", date.plusDays(1)).rewarded)
        assertEquals(progress, GameProgress.decode(progress.encode()))
    }
    @Test fun lowRemainingContentAdaptsTargetAndDuplicateReviewsDoNotAdvance() {
        val active = setOf("a", "b")
        var progress = GameProgress().prepare("en", active, emptySet(), date)
        assertEquals(2, progress.activity("en", date).target)
        repeat(15) { progress = progress.study("en", "a", false, date) }
        assertEquals(1, progress.activity("en", date).reviews.size)
        val nextDay = date.plusDays(1)
        progress = progress.prepare("en", active, setOf("en|a", "en|b"), nextDay)
        assertEquals(0, progress.activity("en", nextDay).target)
        assertEquals(5, progress.activity("tr", nextDay).target)
    }
    @Test fun poolDeduplicatesActualMediaBytesAndExcludesUnstudiedWords() {
        val root = temporary.newFolder("pack"); val base = temporary.newFolder("base")
        val pool = words
        pool.forEachIndexed { index, w ->
            File(root, w.audio).apply { parentFile!!.mkdirs(); writeText("audio-$index") }
            File(base, w.image).apply { parentFile!!.mkdirs(); writeText("image-$index") }
        }
        File(base, pool[1].image).writeText("image-0")
        File(root, pool[3].audio).writeText("audio-2")
        val modified = pool.mapIndexed { index, w -> if (index == 5) w.copy(text = pool[4].text.uppercase()) else w }
        val pack = LearningPack("en", root, base, modified, emptyList())
        val selected = GameRules.uniqueWords(pack, studied("en", pool.dropLast(1)) + "tr|w10", random = Random(1))
        assertFalse(selected.any { it.id == "w10" })
        assertEquals(6, selected.size)
        assertEquals(selected.size, selected.map { PackageSafety.sha(pack.audio(it)) }.distinct().size)
        assertEquals(selected.size, selected.map { PackageSafety.sha(pack.image(it)) }.distinct().size)
        assertEquals(selected.size, selected.map { it.text.lowercase() }.distinct().size)
    }
    @Test fun difficultyAndReviewTargetsAreBounded() {
        assertNull(GameRules.start("tr", GameKind.LISTENING, words.take(3), 2))
        assertNull(GameRules.start("tr", GameKind.MATCHING, words, 8))
        assertNull(GameRules.start("tr", GameKind.MEMORY, words, 3))
        assertNull(GameRules.start("tr", GameKind.MEMORY, words.take(2), 8))
        assertNull(GameRules.start("tr", GameKind.REVIEW, words, 2, emptySet()))
        val review = GameRules.start("tr", GameKind.REVIEW, words, 2, setOf("w1", "w5"), Random(1))!!
        assertEquals(setOf("w1", "w5"), review.questions.map { it.target.id }.toSet())
        assertTrue(review.questions.all { it.options.size == 4 && it.options.map { w -> w.id }.distinct().size == 4 && it.target in it.options })
    }
}
