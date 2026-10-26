package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.chess.*
import org.junit.Assert.*
import org.junit.Test

class ChessFreePracticeTests {
    @Test fun eachPieceHasExpectedReachAndCannotMoveOffBoard() {
        val counts=listOf(8,27,14,13,8,2)
        PieceKind.entries.forEachIndexed { index,kind ->
            val start=ChessFreePractice.start(kind)
            assertEquals(counts[index],start.destinations.size)
            assertNull(start.move(-1))
            assertNull(start.move(start.location))
            val next=start.move(start.destinations.first())!!
            assertEquals(1,next.board.count { it!=null })
            assertTrue(next.destinations.isNotEmpty())
        }
    }
    @Test fun pawnMovesRepeatedlyWithoutTurnsAndPromotesToQueen() {
        var position=ChessFreePractice.start(PieceKind.PAWN)
        position=position.move(square("d4"))!!
        assertNull(position.move(square("d6")))
        assertNull(position.move(square("e5")))
        for(target in listOf("d5","d6","d7","d8")) position=position.move(square(target))!!
        assertEquals(PieceKind.QUEEN,position.kind)
        assertTrue(square("a8") in position.destinations)
        assertEquals(PieceKind.PAWN,ChessFreePractice.start(PieceKind.PAWN).kind)
    }
}
