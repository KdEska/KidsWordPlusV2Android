package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.chess.*
import org.junit.Assert.*
import org.junit.Test

class ChessAcademyTests {
    @Test fun fullSetupMatchesPlayableStartingBoard() {
        assertEquals(32, ChessBoardSetup.placements.size)
        assertEquals(32, ChessBoardSetup.placements.map { it.first }.toSet().size)
        var count = 0
        for ((target, piece) in ChessBoardSetup.placements) {
            assertEquals(count, ChessBoardSetup.board(count).count { it != null })
            count = ChessBoardSetup.place(count, piece.kind, target)
        }
        assertEquals(32, count)
        assertEquals(ChessPosition.initial().board, ChessBoardSetup.board(count))
        assertEquals(Piece(PieceKind.QUEEN, true), ChessBoardSetup.board(32)[square("d1")])
        assertEquals(Piece(PieceKind.KING, false), ChessBoardSetup.board(32)[square("e8")])
        assertEquals(32, ChessBoardSetup.place(32, PieceKind.KING, square("e1")))
    }
    @Test fun setupRejectsWrongKindSquareAndInvalidProgress() {
        assertEquals(0, ChessBoardSetup.place(0, PieceKind.KING, square("a1")))
        assertEquals(0, ChessBoardSetup.place(0, PieceKind.ROOK, square("h1")))
        assertEquals(1, ChessBoardSetup.place(0, PieceKind.ROOK, square("a1")))
        for (bad in listOf(-1,33)) {
            assertThrows(IllegalArgumentException::class.java) { ChessBoardSetup.board(bad) }
            assertThrows(IllegalArgumentException::class.java) { ChessBoardSetup.place(bad, PieceKind.ROOK, 0) }
        }
    }
    @Test fun safetySeedsStartInCheckAndRequestedMovesResolveIt() {
        assertEquals(4, ChessSafety.baseTasks.size)
        assertTrue(ChessSafety.ids.size > 40)
        for (task in ChessSafety.tasks) {
            assertTrue(task.position.inCheck(true))
            assertFalse(task.position.inCheck(false))
            assertTrue(task.accepts(task.source, task.target))
            assertFalse(task.accepts(task.source, -1))
            if (task.source != null) {
                assertFalse(task.after().inCheck(true))
                assertFalse(task.accepts(null, task.target))
            } else {
                val attacker = task.position.board[task.target]!!
                assertFalse(attacker.white)
                assertEquals(PieceKind.ROOK, attacker.kind)
            }
        }
    }
    @Test fun safetyCreditIsCorrectAndIdempotent() {
        val task = ChessSafety.baseTasks.last()
        assertTrue(ChessSafety.finish(emptySet(), task.id, square("e1"), task.target).isEmpty())
        assertTrue(ChessSafety.finish(emptySet(), "unknown", task.source, task.target).isEmpty())
        val completed = ChessSafety.finish(emptySet(), task.id, task.source, task.target)
        assertEquals(setOf("block"), completed)
        assertEquals(completed, ChessSafety.finish(completed, task.id, task.source, task.target))
        val escape = ChessSafety.tasks.single { it.id == "escape" }
        assertNull(escape.position.play(ChessMove(square("e1"), square("e2"))))
    }
}
