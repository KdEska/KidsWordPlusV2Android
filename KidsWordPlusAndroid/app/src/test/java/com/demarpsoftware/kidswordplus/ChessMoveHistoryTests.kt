package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.chess.*
import org.junit.Assert.*
import org.junit.Test

class ChessMoveHistoryTests {
    private fun move(value:String)=ChessMove(square(value.take(2)),square(value.substring(2,4)),value.getOrNull(4)?.let {code -> PieceKind.entries.first {it.code==code}})
    @Test fun pairedMovesRetainActorsAndIncompleteWhiteTurn() {
        val rows=chessHistoryRows(listOf("e2e4","d7d5","e4d5").map(::move))
        assertEquals(2,rows.size)
        assertEquals(Piece(PieceKind.PAWN,true),rows[0].white!!.actor)
        assertEquals("d7d5",rows[0].black!!.move.notation)
        assertEquals("e4d5",rows[1].white!!.move.notation)
        assertNull(rows[1].black)
        assertTrue(chessHistoryRows(emptyList()).isEmpty())
    }
    @Test fun customBlackStartKeepsEmptyWhiteCellAndMoveNumber() {
        val rows=chessHistoryRows(listOf("h8g8","a1b1").map(::move),"7k/7r/8/8/8/8/8/K7 b - - 0 12")
        assertEquals(listOf(12,13),rows.map {it.number})
        assertNull(rows[0].white)
        assertFalse(rows[0].black!!.actor.white)
        assertNull(rows[1].black)
    }
    @Test fun promotionRemembersPawnAndCastleRemembersKing() {
        val promoted=chessHistoryRows(listOf(move("a7a8q")),"7k/P7/8/8/8/8/8/K7 w - - 0 1").single().white!!
        assertEquals(PieceKind.PAWN,promoted.actor.kind)
        assertEquals(PieceKind.QUEEN,promoted.move.promotion)
        val castle=chessHistoryRows(listOf(move("e1g1")),"4k3/8/8/8/8/8/8/4K2R w K - 0 1").single().white!!
        assertEquals(PieceKind.KING,castle.actor.kind)
    }
    @Test fun capturesUseMovesInsteadOfMissingStartingMaterial() {
        assertTrue(chessCapturedPieces(emptyList(),"7k/8/8/8/8/8/8/K7 w - - 0 1").isEmpty())
        assertEquals(listOf(Piece(PieceKind.PAWN,false)),chessCapturedPieces(listOf("e2e4","d7d5","e4d5").map(::move)))
    }
    @Test fun enPassantAndPromotedCapturesKeepActualPiece() {
        assertEquals(listOf(Piece(PieceKind.PAWN,false)),chessCapturedPieces(listOf("e2e4","a7a6","e4e5","d7d5","e5d6").map(::move)))
        assertEquals(listOf(Piece(PieceKind.QUEEN,true)),chessCapturedPieces(listOf("a7a8q","b8a8").map(::move),"1r5k/P7/8/8/8/8/8/7K w - - 0 1"))
    }
}
