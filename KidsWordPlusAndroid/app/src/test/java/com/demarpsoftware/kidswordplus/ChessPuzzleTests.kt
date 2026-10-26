package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.chess.*
import org.junit.Assert.*
import org.junit.Test

class ChessPuzzleTests {
    @Test fun escapePreviewsAreLegalAndLeaveTheOriginalPositionIntact() {
        for(task in ChessPuzzles.endings) {
            val before=task.position
            if(task.answer==ChessEnding.CHECK) {
                assertEquals(before.legalMoves().toSet(),task.escapeMoves.toSet())
                assertTrue(task.escapeMoves.isNotEmpty())
                for(move in task.escapeMoves) {
                    val after=before.play(move)!!
                    assertFalse(after.inCheck(before.whiteTurn))
                    assertEquals(before,task.position)
                }
            } else assertTrue(task.escapeMoves.isEmpty())
        }
    }

    @Test fun referenceEndingsCoverCheckMateAndStalemate() {
        assertEquals(listOf(ChessEnding.CHECK, ChessEnding.MATE, ChessEnding.STALEMATE), ChessPuzzles.baseEndings.map { it.answer })
        val (check, mate, stale) = ChessPuzzles.baseEndings
        assertTrue(check.position.inCheck(true))
        assertTrue(check.position.legalMoves().isNotEmpty())
        assertEquals(ChessOutcome.BLACK_WON, mate.position.outcome())
        assertEquals(ChessOutcome.STALEMATE, stale.position.outcome())
        for (task in ChessPuzzles.endings) {
            assertFalse(task.position.inCheck(false))
            for (answer in ChessEnding.entries) assertEquals(answer == task.answer, task.accepts(answer))
        }
    }
    @Test fun everyMateSeedHasRealMateAndAcceptsAllWinningMoves() {
        val reference = listOf("c2b2", "f2g2", "a7a8")
        for ((index, task) in ChessPuzzles.baseMates.withIndex()) {
            assertNull(task.position.outcome())
            assertFalse(task.position.inCheck(true))
            assertFalse(task.position.inCheck(false))
            assertTrue(task.solutions.any { it.notation == reference[index] })
            for (move in task.position.legalMoves()) {
                val after = task.position.play(move)!!
                val isMate = after.inCheck(false) && after.legalMoves().isEmpty()
                assertEquals(isMate, task.accepts(move))
            }
            assertFalse(task.accepts(ChessMove(square("a8"), square("a8"))))
        }
    }
    @Test fun nonMateChecksAndStalematesAreRejected() {
        val task = ChessPuzzles.mates.first()
        val nonMate = task.position.legalMoves().filter { !task.accepts(it) }
        assertTrue(nonMate.isNotEmpty())
        assertTrue(nonMate.any { task.position.play(it)!!.inCheck(false) })
        assertTrue(nonMate.any { task.position.play(it)!!.outcome() == ChessOutcome.STALEMATE })
    }
    @Test fun onlyCorrectKnownGoalsEarnProgressOnce() {
        assertEquals(480, ChessPuzzles.ids.size)
        var completed = emptySet<String>()
        for (task in ChessPuzzles.endings) {
            assertEquals(completed, ChessPuzzles.finishEnding(completed, task.id, ChessEnding.entries.first { it != task.answer }))
            completed = ChessPuzzles.finishEnding(completed, task.id, task.answer)
            assertEquals(completed, ChessPuzzles.finishEnding(completed, task.id, task.answer))
        }
        for (task in ChessPuzzles.mates) {
            val move = task.referenceMove!!
            completed = ChessPuzzles.finishMate(completed, task.id, move)
            assertEquals(completed, ChessPuzzles.finishMate(completed, task.id, move))
        }
        assertEquals(ChessPuzzles.ids, completed)
        assertEquals(completed, ChessPuzzles.finishEnding(completed, "unknown", ChessEnding.MATE))
        assertEquals(completed, ChessPuzzles.finishMate(completed, "unknown", ChessPuzzles.mates.first().solutions.first()))
    }
}
