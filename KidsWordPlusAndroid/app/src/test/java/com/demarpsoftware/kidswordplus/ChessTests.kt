package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.chess.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class ChessTests {
    private fun move(from: String, to: String, promotion: PieceKind? = null) = ChessMove(square(from), square(to), promotion)
    private fun fen(text: String) = ChessPosition.fromFen(text)
    private fun rejects(block: () -> Unit) { try { block(); fail("Expected rejection") } catch (_: Exception) { } }
    @Test fun initialPositionPerftAndTurns() {
        val start = ChessPosition.initial()
        assertEquals(32, start.board.count { it != null })
        assertEquals(20L, start.perft(1))
        assertEquals(400L, start.perft(2))
        assertEquals(8902L, start.perft(3))
        assertNull(start.play(move("a7", "a6")))
        val after = start.play(move("e2", "e4"))!!
        assertFalse(after.whiteTurn)
        assertEquals(square("e3"), after.enPassant)
        assertEquals(0, after.quietPlies)
        assertNull(after.play(move("e4", "e5")))
    }
    @Test fun castlingRichAndPinnedPositionsMatchKnownPerftCounts() {
        // Standard positions also used by official Stockfish tests/perft.sh.
        val castle = fen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1")
        assertEquals(48L, castle.perft(1))
        assertEquals(2039L, castle.perft(2))
        val pinned = fen("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1")
        assertEquals(14L, pinned.perft(1))
        assertEquals(191L, pinned.perft(2))
        assertEquals(2812L, pinned.perft(3))
    }
    @Test fun castlingMovesRookAndCannotCrossAttackedSquare() {
        val castle = fen("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 1")
        assertTrue(move("e1", "g1") in castle.legalMoves())
        assertTrue(move("e1", "c1") in castle.legalMoves())
        val after = castle.play(move("e1", "g1"))!!
        assertEquals(Piece(PieceKind.ROOK, true), after.board[square("f1")])
        assertNull(after.board[square("h1")])
        assertEquals(0, after.castling and 3)
        val attacked = fen("k4r2/8/8/8/8/8/8/4K2R w K - 0 1")
        assertNull(attacked.play(move("e1", "g1")))
        val checked = fen("k3r3/8/8/8/8/8/8/4K2R w K - 0 1")
        assertNull(checked.play(move("e1", "g1")))
        val missingRook = fen("4k3/8/8/8/8/8/8/4K3 w K - 0 1")
        assertFalse(move("e1", "g1") in missingRook.legalMoves())
    }
    @Test fun enPassantRemovesVictimAndCannotExposeKing() {
        val ep = fen("4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1")
        val after = ep.play(move("e5", "d6"))!!
        assertNull(after.board[square("d5")])
        assertEquals(Piece(PieceKind.PAWN, true), after.board[square("d6")])
        assertNull(after.enPassant)
        val pinned = fen("k3r3/8/8/3pP3/8/8/8/4K3 w - d6 0 1")
        assertNull(pinned.play(move("e5", "d6")))
        val expired = ep.play(move("e1", "f1"))!!
        assertNull(expired.enPassant)
    }
    @Test fun bothSidesPromoteToFourChoicesOnly() {
        for (white in listOf(true, false)) {
            val position = if (white) fen("7k/P7/8/8/8/8/8/4K3 w - - 0 1") else fen("7k/8/8/8/8/8/p7/4K3 b - - 0 1")
            val from = if (white) "a7" else "a2"; val to = if (white) "a8" else "a1"
            assertEquals(4, position.legalMoves().count { it.from == square(from) && it.to == square(to) })
            ChessPosition.promotions.forEach { kind -> assertEquals(Piece(kind, white), position.play(move(from, to, kind))!!.board[square(to)]) }
            assertNull(position.play(move(from, to, PieceKind.KING)))
            assertEquals(PieceKind.QUEEN, position.play(move(from, to))!!.board[square(to)]!!.kind)
        }
        assertNull(ChessPosition.initial().play(move("e2", "e4", PieceKind.ROOK)))
    }
    @Test fun checkmateStalemateAndInsufficientMaterialStopGame() {
        var game = ChessPosition.initial()
        for (m in listOf(move("f2", "f3"), move("e7", "e5"), move("g2", "g4"), move("d8", "h4"))) game = game.play(m)!!
        assertEquals(ChessOutcome.BLACK_WON, game.outcome())
        assertTrue(game.inCheck(true))
        assertNull(game.play(move("a2", "a3")))
        assertNull(game.computerMove())
        assertEquals(ChessOutcome.STALEMATE, fen("7k/5K2/6Q1/8/8/8/8/8 b - - 0 1").outcome())
        assertEquals(ChessOutcome.INSUFFICIENT, fen("7k/8/8/8/8/8/8/K7 w - - 0 1").outcome())
        assertEquals(ChessOutcome.INSUFFICIENT, fen("7k/8/8/8/8/8/8/KB6 w - - 0 1").outcome())
        assertEquals(ChessOutcome.INSUFFICIENT, fen("7k/8/8/8/8/8/8/KN6 w - - 0 1").outcome())
    }
    @Test fun repetitionsAndQuietMoveCounterArePreserved() {
        var game = ChessPosition.initial()
        repeat(2) {
            for (m in listOf(move("g1", "f3"), move("g8", "f6"), move("f3", "g1"), move("f6", "g8"))) game = game.play(m)!!
        }
        assertEquals(ChessOutcome.REPETITION, game.outcome())
        assertEquals(ChessOutcome.FIFTY_MOVES, fen("4k2r/8/8/8/8/8/8/R3K3 w - - 100 1").outcome())
        val quiet = ChessPosition.initial().copy(quietPlies = 99).play(move("g1", "f3"))!!
        assertEquals(ChessOutcome.FIFTY_MOVES, quiet.outcome())
        assertEquals(0, ChessPosition.initial().copy(quietPlies = 99).play(move("a2", "a3"))!!.quietPlies)
    }
    @Test fun rookReturnDoesNotRestoreCastlingAndKingsCannotBeCaptured() {
        var game = fen("4k3/8/8/8/8/8/8/4K2R w K - 0 1")
        game = game.play(move("h1", "h2"))!!.play(move("e8", "e7"))!!.play(move("h2", "h1"))!!.play(move("e7", "e8"))!!
        assertFalse(move("e1", "g1") in game.legalMoves())
        val aimed = fen("4k3/8/8/8/8/8/4R3/4K3 w - - 0 1")
        assertFalse(aimed.legalMoves().any { it.to == square("e8") })
        val pinned = fen("k3r3/8/8/8/8/8/4R3/4K3 w - - 0 1")
        assertNull(pinned.play(move("e2", "a2")))
    }
    @Test fun saveRestorePendingComputerAndUndoTurn() {
        val start = ChessGame()
        val pending = start.play(move("e2", "e4"))!!
        val restored = ChessGame.decode(pending.encode())
        assertEquals(pending, restored)
        assertTrue(restored.computerTurn)
        val after = restored.play(restored.position.computerMove(Random(2))!!)!!
        assertEquals(after, ChessGame.decode(after.encode()))
        assertEquals(start, after.undo())
        val twoPlayers = ChessGame(computer = false).play(move("e2", "e4"))!!.play(move("e7", "e5"))!!
        assertEquals(1, twoPlayers.undo()!!.history.size)
        val black = ChessGame(playerWhite = false)
        assertTrue(black.computerTurn)
        assertNull(black.play(move("e2", "e4"))!!.undo())
    }
    @Test fun corruptAndIllegalSavedDataAreRejected() {
        rejects { ChessGame.decode("bad") }
        rejects { ChessGame.decode(ChessGame().encode().replace("\"version\":1", "\"version\":99")) }
        val bad = org.json.JSONObject(ChessGame().encode()).put("moves", org.json.JSONArray().put("e2e5")).toString()
        rejects { ChessGame.decode(bad) }
        rejects { square("z9") }
        rejects { fen("8/8/8/8/8/8/8/8 w - - 0 1") }
    }
    @Test fun computerAndHintAlwaysChooseLegalMovesAndFindMate() {
        var game = ChessPosition.initial()
        repeat(12) {
            val move = game.computerMove(Random(it)) ?: return@repeat
            assertTrue(move in game.legalMoves())
            game = game.play(move)!!
        }
        val mate = fen("7k/5Q2/6K1/8/8/8/8/8 w - - 0 1")
        val selected = mate.computerMove(Random(1))!!
        assertEquals(ChessOutcome.WHITE_WON, mate.play(selected)!!.outcome())
    }
    @Test fun completedGameStatisticsCreditAnIdOnce() {
        var game = ChessGame(computer = false)
        for (m in listOf(move("f2", "f3"), move("e7", "e5"), move("g2", "g4"), move("d8", "h4"))) game = game.play(m)!!
        val stats = ChessStatistics().record(game).record(game)
        assertEquals(1, stats.games.size)
        assertEquals(ChessOutcome.BLACK_WON, stats.games.single().outcome)
        assertEquals(stats, ChessStatistics.decode(stats.encode()))
        assertEquals(stats, stats.record(ChessGame()))
    }
}
