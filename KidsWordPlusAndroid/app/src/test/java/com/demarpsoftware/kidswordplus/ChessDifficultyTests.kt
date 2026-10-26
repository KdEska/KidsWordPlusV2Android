package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.chess.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class ChessDifficultyTests {
    @Test fun easyUsesRandomLegalMovesAndAllLevelsRespectFinishedGames() {
        val start = ChessPosition.initial()
        val moves = (0..12).map { start.computerMove(Random(it), ChessDifficulty.EASY)!! }
        assertTrue(moves.toSet().size > 1)
        assertTrue(moves.all { it in start.legalMoves() })
        val ended = ChessPuzzles.endings.first { it.answer == ChessEnding.MATE }.position
        for (level in ChessDifficulty.entries) assertNull(ended.computerMove(difficulty = level))
    }
    @Test fun hardAvoidsPoisonedCaptureThatMediumPrefers() {
        val position = ChessPosition.fromFen("6k1/8/1p6/b7/8/8/8/R5K1 w - - 0 1")
        val poisoned = ChessMove(square("a1"), square("a5"))
        assertEquals(poisoned, position.computerMove(Random(2), ChessDifficulty.NORMAL))
        val hard = position.computerMove(Random(2), ChessDifficulty.CHALLENGE, 5000)!!
        assertTrue(hard in position.legalMoves())
        assertNotEquals(poisoned, hard)
        assertEquals(position.computerMove(Random(2)), position.computerMove(Random(2), ChessDifficulty.CHALLENGE, 0))
    }
    @Test fun hardAndMediumFindMateAndCancellationIsObserved() {
        val position = ChessPosition.fromFen("7k/5Q2/6K1/8/8/8/8/8 w - - 0 1")
        for (level in listOf(ChessDifficulty.NORMAL, ChessDifficulty.CHALLENGE)) {
            val move = position.computerMove(Random(1), level)!!
            assertEquals(ChessOutcome.WHITE_WON, position.play(move)!!.outcome())
        }
        assertThrows(java.util.concurrent.CancellationException::class.java) {
            position.computerMove(difficulty = ChessDifficulty.CHALLENGE, checkCancellation = { throw java.util.concurrent.CancellationException() })
        }
    }
    @Test fun chosenLevelSurvivesMovesUndoClockAndCustomOrigins() {
        val origin = ChessCustomPosition.standard().copy(whiteTurn = false)
        for (level in ChessDifficulty.entries) {
            val game = ChessGame(computer = false, position = origin.position, startFen = origin.fen(), clock = ChessClock.minutes(5), difficulty = level)
            val next = game.play(ChessMove(square("e7"), square("e5")))!!.elapse(1000)
            assertEquals(next, ChessGame.decode(next.encode()))
            assertEquals(level, next.undo()!!.difficulty)
            val stats = ChessStatistics().record(game.elapse(300000))
            assertEquals(level, stats.games.single().difficulty)
            assertEquals(stats, ChessStatistics.decode(stats.encode()))
        }
        val legacy = org.json.JSONObject(ChessGame().encode()).apply { remove("difficulty") }.toString()
        assertEquals(ChessDifficulty.NORMAL, ChessGame.decode(legacy).difficulty)
        val invalid = org.json.JSONObject(legacy).put("difficulty", "unknown").toString()
        assertThrows(IllegalArgumentException::class.java) { ChessGame.decode(invalid) }
    }
}
