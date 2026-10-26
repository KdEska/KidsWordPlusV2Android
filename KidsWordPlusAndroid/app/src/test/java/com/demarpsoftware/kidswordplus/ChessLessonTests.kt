package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.chess.*
import org.junit.Assert.*
import org.junit.Test

class ChessLessonTests {
    @Test fun allReferenceGoalsAreReachableAndUnique() {
        assertEquals(9, ChessLessons.baseTasks.size)
        assertTrue(ChessLessons.tasks.size > 100)
        assertEquals(ChessLessons.tasks.size, ChessLessons.ids.size)
        assertEquals(PieceKind.entries.toSet(), ChessLessons.tasks.map { it.kind }.toSet())
        for (task in ChessLessons.tasks) {
            assertTrue(task.id, task.accepts(task.origin, task.target))
            assertFalse(task.accepts(task.origin, task.origin))
            assertFalse(task.accepts(task.target, task.origin))
            assertEquals(task.capture, task.position.board[task.target]?.white == false)
        }
    }
    @Test fun capturesAndObstaclesTeachDifferentRules() {
        val pawn = ChessLessons.tasks.single { it.id == "capture.pawn" }
        assertTrue(pawn.accepts(square("d4"), square("e5")))
        assertFalse(pawn.accepts(square("d4"), square("d5"))) // legal, but not the requested goal
        val knight = ChessLessons.tasks.single { it.id == "jump.knight" }
        assertTrue(knight.position.movesFrom(knight.origin).any { it.to == knight.target })
        val rook = ChessLessons.tasks.single { it.id == "move.rook" }
        val blocked = rook.position.board.toMutableList().apply { this[square("e4")] = Piece(PieceKind.PAWN, true) }
        assertFalse(rook.position.copy(board = blocked).movesFrom(rook.origin).any { it.to == rook.target })
    }
    @Test fun completionRequiresCorrectMoveAndIsIdempotent() {
        val task = ChessLessons.tasks.first()
        assertTrue(ChessLessons.finish(emptySet(), "unknown", task.origin, task.target).isEmpty())
        assertTrue(ChessLessons.finish(emptySet(), task.id, task.origin, task.origin).isEmpty())
        val done = ChessLessons.finish(emptySet(), task.id, task.origin, task.target)
        assertEquals(setOf(task.id), done)
        assertEquals(done, ChessLessons.finish(done, task.id, task.origin, task.target))
    }
}
