package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.chess.*
import org.junit.Assert.*
import org.junit.Test

class ChessOpeningTests {
    @Test fun allEightReferenceLinesHave51LegalMovesAndCorrectTurns() {
        assertEquals(8, ChessOpenings.ids.size)
        assertEquals(51, ChessOpenings.all.sumOf { it.steps.size })
        for (opening in ChessOpenings.all) {
            var position = ChessPosition.initial()
            for ((index, step) in opening.steps.withIndex()) {
                assertEquals(position.whiteTurn, position.board[step.move.from]!!.white)
                assertTrue(step.move in position.legalMoves())
                position = position.play(step.move)!!
                assertFalse(position.inCheck(!position.whiteTurn))
                assertEquals(position, opening.position(index + 1))
            }
            assertEquals(ChessPosition.initial(), opening.position(-1))
            assertEquals(position, opening.position(999))
        }
    }
    @Test fun correctSequenceRequiredAndDuplicateStaleMoveCannotAdvance() {
        val opening = ChessOpenings.all.first()
        val empty = ChessOpeningProgress()
        val wrongLegal = ChessMove(square("d2"), square("d4"))
        assertEquals(empty, empty.advance(opening.id, 0, wrongLegal))
        assertEquals(empty, empty.advance("unknown", 0, opening.steps[0].move))
        val first = empty.advance(opening.id, 0, opening.steps[0].move)
        assertEquals(1, first.count(opening.id))
        assertEquals(first, first.advance(opening.id, 0, opening.steps[0].move))
        assertEquals(0, first.count("spanish"))
    }
    @Test fun completionRewindAndPersistenceKeepEarnedLessons() {
        var progress = ChessOpeningProgress()
        for (opening in ChessOpenings.all) for ((index, step) in opening.steps.withIndex()) progress = progress.advance(opening.id, index, step.move)
        assertEquals(ChessOpenings.ids, progress.completed)
        assertEquals(progress, ChessOpeningProgress.decode(progress.encode()))
        val opening = ChessOpenings.all.first()
        val backward = progress.rewind(opening.id)
        assertEquals(opening.steps.size - 1, backward.count(opening.id))
        val reset = backward.rewind(opening.id, restart = true)
        assertEquals(0, reset.count(opening.id))
        assertEquals(progress.completed, reset.completed)
        assertEquals(reset, ChessOpeningProgress.decode(reset.encode()))
    }
    @Test fun invalidVersionAndStepCountAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { ChessOpeningProgress.decode("""{"version":2,"counts":{},"completed":[]}""") }
        for (count in listOf(-1,7)) assertThrows(IllegalArgumentException::class.java) {
            ChessOpeningProgress.decode("""{"version":1,"counts":{"italian":$count},"completed":[]}""")
        }
        val restored = ChessOpeningProgress.decode("""{"version":1,"counts":{},"completed":["unknown"]}""")
        assertTrue(restored.completed.isEmpty())
        assertEquals(0, restored.count("italian"))
    }
}
