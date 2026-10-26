package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.chess.*
import org.junit.Assert.*
import org.junit.Test

class ChessCustomTests {
    private fun custom(fen: String) = ChessCustomPosition(ChessPosition.fromFen(fen).board)
    @Test fun validationRejectsMissingAdjacentFinishedAndWrongCheck() {
        assertEquals("kings", ChessCustomPosition().issue)
        assertEquals("adjacent", custom("8/8/8/8/8/8/4k3/4K3 w - - 0 1").issue)
        assertEquals("finished", custom("k7/8/8/8/8/8/8/4K3 w - - 0 1").issue)
        assertEquals("wrongCheck", custom("4k3/8/8/8/8/8/8/K3R3 w - - 0 1").issue)
        val pawn = ChessCustomPosition.standard().place(square("a7"), null).place(square("a8"), Piece(PieceKind.PAWN, false))
        assertEquals("pawns", pawn.issue)
        assertEquals("material", ChessCustomPosition.standard().place(square("a3"), Piece(PieceKind.PAWN, true)).issue)
    }
    @Test fun placementMovesKingAndDraftRoundTripsIncompleteBoard() {
        val draft = ChessCustomPosition().place(square("e1"), Piece(PieceKind.KING, true)).place(square("d1"), Piece(PieceKind.KING, true))
        assertNull(draft.board[square("e1")])
        assertEquals(1, draft.board.count { it != null })
        assertEquals(draft, ChessCustomPosition.decode(draft.encode()))
        assertTrue(draft.place(square("d1"), null).board.all { it == null })
        assertNull(ChessCustomPosition.standard().issue)
        assertEquals(15, ChessCustomPosition.standard().position.castling)
        assertEquals(0, ChessCustomPosition.standard().copy(allowCastling = false).position.castling)
    }
    @Test fun customMatchSaveAndUndoRetainOriginAndBlackTurn() {
        val origin = ChessCustomPosition.standard().copy(whiteTurn = false, allowCastling = false)
        val game = ChessGame(computer = false, position = origin.position, startFen = origin.fen())
        val played = game.play(ChessMove(square("e7"), square("e5")))!!
        assertEquals("1... e7e5", played.historyText())
        assertEquals(played, ChessGame.decode(played.encode()))
        assertEquals(game, played.undo())
        val computer = game.copy(computer = true, playerWhite = true)
        assertTrue(computer.computerTurn)
        assertNull(computer.play(ChessMove(square("e7"), square("e5")))!!.undo())
        val legacy = ChessGame(computer = false)
        assertEquals(legacy, ChessGame.decode(legacy.encode()))
    }
    @Test fun invalidCustomSavedOriginIsRejectedAndMateFromOriginIsCountedOnce() {
        val origin = custom("7k/5Q2/6K1/8/8/8/8/8 w - - 0 1")
        assertNull(origin.issue)
        val game = ChessGame(computer = false, position = origin.position, startFen = origin.fen())
        val mate = game.play(ChessMove(square("f7"), square("g7")))!!
        assertEquals(ChessOutcome.WHITE_WON, mate.position.outcome())
        assertEquals(mate, ChessGame.decode(mate.encode()))
        assertEquals(1, ChessStatistics().record(mate).record(mate).games.size)
        val bad = org.json.JSONObject(game.encode()).put("startFen", "8/8/8/8/8/8/8/8 w - - 0 1").toString()
        assertThrows(IllegalArgumentException::class.java) { ChessGame.decode(bad) }
    }
    @Test fun arrangingOneSideNeverReplacesAnOpponentOnItsHomeSquares() {
        val blocked=ChessCustomPosition().place(square("a1"),Piece(PieceKind.ROOK,false))
        assertNull(blocked.arrange(true))
        val base=ChessCustomPosition().place(square("d5"),Piece(PieceKind.KING,false))
        val arranged=base.arrange(true)!!
        assertEquals(base.board[square("d5")],arranged.board[square("d5")])
        assertEquals(16,arranged.board.count{it?.white==true})
        assertEquals(Piece(PieceKind.QUEEN,true),arranged.board[square("d1")])
    }
    @Test fun randomArmiesPreserveOpponentsKingsAndPawnRanks() {
        val base=ChessCustomPosition.standard()
        repeat(32) {seed ->
            val next=base.randomize(true,kotlin.random.Random(seed))!!
            assertEquals(base.board.filter{it?.white==false},next.board.filter{it?.white==false})
            val army=next.board.filterNotNull().filter{it.white}
            assertTrue(army.size in 1..16)
            assertEquals(1,army.count{it.kind==PieceKind.KING})
            assertTrue(army.count{it.kind==PieceKind.PAWN}<=8)
            assertFalse(next.board.indices.any{it/8 in listOf(0,7)&&next.board[it]==Piece(PieceKind.PAWN,true)})
            assertNotEquals("adjacent",next.issue)
            assertEquals(next,ChessCustomPosition.decode(next.encode()))
        }
    }
    @Test fun draggingMovesInsteadOfCopyingAndCanReplaceAnOccupiedSquare() {
        val original=ChessCustomPosition.standard()
        val next=original.move(square("a1"),square("a7"))
        assertNull(next.board[square("a1")])
        assertEquals(Piece(PieceKind.ROOK,true),next.board[square("a7")])
        assertEquals(31,next.board.count{it!=null})
        assertEquals(original,original.move(square("a1"),square("a1")))
    }
}
