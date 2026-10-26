package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.chess.*
import org.junit.Assert.*
import org.junit.Test

class ChessMoveVisualTests {
    private fun game(fen: String)=ChessGame(computer=false,position=ChessPosition.fromFen(fen))
    @Test fun ordinaryMoveAndCaptureKeepSourceArtUntilLanding() {
        val before=ChessGame(computer=false)
        val move=ChessMove(square("e2"),square("e4"))
        val next=before.play(move)!!
        val visual=ChessMoveVisual.between(before,next)!!
        assertEquals(listOf(ChessMovingPiece(move.from,move.to,Piece(PieceKind.PAWN,true))),visual.movers)
        assertEquals(before.position.board,visual.before)
        val capture=game("4k3/8/8/8/3r4/8/8/3RK3 w - - 0 1")
        val after=capture.play(ChessMove(square("d1"),square("d4")))!!
        assertEquals(Piece(PieceKind.ROOK,false),ChessMoveVisual.between(capture,after)!!.before[square("d4")])
    }
    @Test fun bothCastlingSidesAnimateKingAndRookForBothColors() {
        for(white in listOf(true,false)) for(kingSide in listOf(true,false)) {
            val before=game("r3k2r/8/8/8/8/8/8/R3K2R ${if(white)"w" else "b"} KQkq - 0 1")
            val rank=if(white)"1" else "8"
            val move=ChessMove(square("e$rank"),square((if(kingSide)"g" else "c")+rank))
            val movers=ChessMoveVisual.between(before,before.play(move)!!)!!.movers
            assertEquals(2,movers.size)
            assertEquals(ChessMovingPiece(square((if(kingSide)"h" else "a")+rank),square((if(kingSide)"f" else "d")+rank),Piece(PieceKind.ROOK,white)),movers[1])
        }
    }
    @Test fun promotionAndEnPassantUseOriginalPieceAndCapturedBoard() {
        val before=game("4k3/P7/8/8/8/8/8/4K3 w - - 0 1")
        for(kind in ChessPosition.promotions) {
            val next=before.play(ChessMove(square("a7"),square("a8"),kind))!!
            assertEquals(PieceKind.PAWN,ChessMoveVisual.between(before,next)!!.movers.single().piece.kind)
            assertEquals(kind,next.position.board[square("a8")]!!.kind)
        }
        val passant=game("4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1")
        val next=passant.play(ChessMove(square("e5"),square("d6")))!!
        assertEquals(Piece(PieceKind.PAWN,false),ChessMoveVisual.between(passant,next)!!.before[square("d5")])
        assertNull(next.position.board[square("d5")])
    }
    @Test fun reopensUndoNewGameClockTicksAndSkippedMovesDoNotReplay() {
        val before=ChessGame(computer=false)
        val next=before.play(ChessMove(square("e2"),square("e4")))!!
        assertNull(ChessMoveVisual.between(null,next))
        assertNull(ChessMoveVisual.between(next,next))
        assertNull(ChessMoveVisual.between(next,before))
        assertNull(ChessMoveVisual.between(before,next.copy(id="different")))
        val twice=next.play(ChessMove(square("e7"),square("e5")))!!
        assertNull(ChessMoveVisual.between(before,twice))
        assertNull(ChessMoveVisual.between(before,next.copy(position=before.position)))
        assertNull(ChessMoveVisual.between(before,before.copy(clock=ChessClock.minutes(3))))
    }
}
