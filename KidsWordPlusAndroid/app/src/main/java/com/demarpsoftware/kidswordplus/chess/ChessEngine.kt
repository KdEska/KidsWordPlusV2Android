package com.demarpsoftware.kidswordplus.chess

import kotlin.math.abs
import kotlin.random.Random

enum class PieceKind(val code: Char, val value: Int) { KING('k', 0), QUEEN('q', 900), ROOK('r', 500), BISHOP('b', 310), KNIGHT('n', 310), PAWN('p', 100) }
data class Piece(val kind: PieceKind, val white: Boolean) {
    val symbol: String get() = (if (white) "♔♕♖♗♘♙" else "♚♛♜♝♞♟")[kind.ordinal].toString()
}
data class ChessMove(val from: Int, val to: Int, val promotion: PieceKind? = null) {
    val notation: String get() = squareName(from) + squareName(to) + (promotion?.code ?: "")
}
fun squareName(index: Int): String = "${('a'.code + index % 8).toChar()}${8 - index / 8}"
fun square(name: String): Int {
    require(name.length == 2 && name[0] in 'a'..'h' && name[1] in '1'..'8')
    return (8 - name[1].digitToInt()) * 8 + (name[0] - 'a')
}
enum class ChessDifficulty { EASY, NORMAL, CHALLENGE }
enum class ChessOutcome { WHITE_WON, BLACK_WON, STALEMATE, REPETITION, FIFTY_MOVES, INSUFFICIENT }

data class ChessPosition(val board: List<Piece?>, val whiteTurn: Boolean = true, val castling: Int = 15,
    val enPassant: Int? = null, val quietPlies: Int = 0, val fullMove: Int = 1, val repetitions: Map<String, Int> = emptyMap()) {
    init { require(board.size == 64) }
    fun attacked(target: Int, byWhite: Boolean): Boolean = board.indices.any { from ->
        val piece = board[from]
        piece != null && piece.white == byWhite && attacks(from, target, piece)
    }
    private fun attacks(from: Int, to: Int, piece: Piece): Boolean {
        if (from == to || from !in 0..63 || to !in 0..63) return false
        val dr = to / 8 - from / 8; val dc = to % 8 - from % 8
        return when (piece.kind) {
            PieceKind.KING -> maxOf(abs(dr), abs(dc)) == 1
            PieceKind.KNIGHT -> abs(dr) * abs(dc) == 2
            PieceKind.PAWN -> dr == (if (piece.white) -1 else 1) && abs(dc) == 1
            else -> {
                val shape = when (piece.kind) {
                    PieceKind.BISHOP -> abs(dr) == abs(dc)
                    PieceKind.ROOK -> dr == 0 || dc == 0
                    else -> abs(dr) == abs(dc) || dr == 0 || dc == 0
                }
                if (!shape) false else {
                    val r = dr.compareTo(0); val c = dc.compareTo(0)
                    var row = from / 8 + r; var column = from % 8 + c
                    var clear = true
                    while (row * 8 + column != to) {
                        if (board[row * 8 + column] != null) { clear = false; break }
                        row += r; column += c
                    }
                    clear
                }
            }
        }
    }
    fun inCheck(white: Boolean): Boolean {
        val king = board.indexOfFirst { it?.kind == PieceKind.KING && it.white == white }
        return king >= 0 && attacked(king, !white)
    }
    private fun pseudo(from: Int): List<ChessMove> {
        val piece = board.getOrNull(from) ?: return emptyList()
        if (piece.white != whiteTurn) return emptyList()
        val targets = ArrayList<Int>()
        for (to in 0..63) {
            if (to == from || board[to]?.white == piece.white || board[to]?.kind == PieceKind.KING) continue
            if (piece.kind != PieceKind.PAWN) {
                if (attacks(from, to, piece)) targets += to
            } else {
                val direction = if (piece.white) -1 else 1
                val row = from / 8; val col = from % 8
                val dr = to / 8 - row; val dc = to % 8 - col
                if (dc == 0 && board[to] == null && (dr == direction || (row == (if (piece.white) 6 else 1) && dr == direction * 2 && board[from + direction * 8] == null))) targets += to
                if (abs(dc) == 1 && dr == direction && (board[to] != null || (to == enPassant && board[to] == null && board[row * 8 + to % 8] == Piece(PieceKind.PAWN, !piece.white)))) targets += to
            }
        }
        if (piece.kind == PieceKind.KING && from == (if (piece.white) 60 else 4) && !inCheck(piece.white)) {
            val row = from / 8
            for (kingSide in listOf(true, false)) {
                val bit = if (piece.white) { if (kingSide) 1 else 2 } else if (kingSide) 4 else 8
                val rook = row * 8 + if (kingSide) 7 else 0
                val transit = row * 8 + if (kingSide) 5 else 3
                val target = row * 8 + if (kingSide) 6 else 2
                val empty = if (kingSide) listOf(row * 8 + 5, row * 8 + 6) else listOf(row * 8 + 1, row * 8 + 2, row * 8 + 3)
                if (castling and bit != 0 && board[rook] == Piece(PieceKind.ROOK, piece.white) && empty.all { board[it] == null }) {
                    val crossing = board.toMutableList().apply { this[from] = null; this[transit] = piece }
                    if (!copy(board = crossing).inCheck(piece.white)) targets += target
                }
            }
        }
        return targets.flatMap { to ->
            if (piece.kind == PieceKind.PAWN && to / 8 in listOf(0, 7)) promotions.map { ChessMove(from, to, it) }
            else listOf(ChessMove(from, to))
        }
    }
    fun movesFrom(from: Int): List<ChessMove> = if (from !in 0..63) emptyList() else pseudo(from).filter { !applyRaw(it).inCheck(whiteTurn) }
    fun legalMoves(): List<ChessMove> = board.indices.flatMap(::movesFrom)
    private fun applyRaw(move: ChessMove): ChessPosition {
        val piece = board[move.from] ?: error("No piece")
        val next = board.toMutableList()
        var captured = next[move.to] != null
        next[move.from] = null
        if (piece.kind == PieceKind.PAWN && move.to == enPassant && move.from % 8 != move.to % 8 && next[move.to] == null) {
            next[move.from / 8 * 8 + move.to % 8] = null; captured = true
        }
        if (piece.kind == PieceKind.KING && abs(move.to % 8 - move.from % 8) == 2) {
            val row = move.from / 8
            val rookFrom = row * 8 + if (move.to % 8 == 6) 7 else 0
            val rookTo = row * 8 + if (move.to % 8 == 6) 5 else 3
            next[rookTo] = next[rookFrom]; next[rookFrom] = null
        }
        next[move.to] = if (piece.kind == PieceKind.PAWN && move.to / 8 in listOf(0, 7)) Piece(move.promotion ?: PieceKind.QUEEN, piece.white) else piece
        var rights = castling
        if (piece.kind == PieceKind.KING) rights = rights and (if (piece.white) 12 else 3)
        for ((corner, bit) in listOf(63 to 1, 56 to 2, 7 to 4, 0 to 8)) if (move.from == corner || move.to == corner) rights = rights and bit.inv()
        return copy(board = next, whiteTurn = !whiteTurn, castling = rights,
            enPassant = if (piece.kind == PieceKind.PAWN && abs(move.to - move.from) == 16) (move.to + move.from) / 2 else null,
            quietPlies = if (piece.kind == PieceKind.PAWN || captured) 0 else quietPlies + 1,
            fullMove = fullMove + if (whiteTurn) 0 else 1)
    }
    fun key(): String = board.joinToString("") { if (it == null) "." else (if (it.white) it.kind.code.uppercaseChar() else it.kind.code).toString() } + ":$whiteTurn:$castling:${enPassant ?: "-"}"
    fun outcome(): ChessOutcome? {
        if (legalMoves().isEmpty()) return if (inCheck(whiteTurn)) { if (whiteTurn) ChessOutcome.BLACK_WON else ChessOutcome.WHITE_WON } else ChessOutcome.STALEMATE
        if (quietPlies >= 100) return ChessOutcome.FIFTY_MOVES
        if ((repetitions[key()] ?: 0) >= 3) return ChessOutcome.REPETITION
        val material = board.filterNotNull().filter { it.kind != PieceKind.KING }
        if (material.isEmpty() || (material.size == 1 && material[0].kind in listOf(PieceKind.BISHOP, PieceKind.KNIGHT))) return ChessOutcome.INSUFFICIENT
        return null
    }
    fun play(move: ChessMove): ChessPosition? {
        if (outcome() != null) return null
        val normalized = if (board.getOrNull(move.from)?.kind == PieceKind.PAWN && move.to in 0..63 && move.to / 8 in listOf(0, 7) && move.promotion == null) move.copy(promotion = PieceKind.QUEEN) else move
        if (normalized !in movesFrom(move.from)) return null
        val after = applyRaw(normalized)
        return after.copy(repetitions = repetitions + (after.key() to ((repetitions[after.key()] ?: 0) + 1)))
    }
    fun perft(depth: Int): Long = if (depth == 0) 1 else legalMoves().sumOf { applyRaw(it).perft(depth - 1) }
    private fun normalMove(random: Random): ChessMove? {
        if (outcome() != null) return null
        val side = whiteTurn
        return legalMoves().shuffled(random).maxByOrNull { move ->
            val next = applyRaw(move)
            val end = next.outcome()
            if (end == (if (side) ChessOutcome.WHITE_WON else ChessOutcome.BLACK_WON)) 100000
            else {
                val capture = board[move.to]?.kind?.value ?: if (board[move.from]?.kind == PieceKind.PAWN && move.to == enPassant) 100 else 0
                val risk = if (next.attacked(move.to, !side)) board[move.from]!!.kind.value else 0
                capture * 10 - risk * 6 + (move.promotion?.value ?: 0) + (7 - abs(3 - move.to / 8) - abs(3 - move.to % 8)) * 3
            }
        }
    }
    fun computerMove(random: Random = Random.Default, difficulty: ChessDifficulty = ChessDifficulty.NORMAL,
        searchBudgetMillis: Long = 1000, checkCancellation: () -> Unit = {}): ChessMove? {
        require(searchBudgetMillis in 0..5000)
        checkCancellation()
        if (outcome() != null) return null
        if (difficulty == ChessDifficulty.EASY) return legalMoves().randomOrNull(random)
        val fallback = normalMove(random) ?: return null
        if (difficulty == ChessDifficulty.NORMAL || searchBudgetMillis == 0L) return fallback
        val side = whiteTurn
        val winning = if (side) ChessOutcome.WHITE_WON else ChessOutcome.BLACK_WON
        val fallbackPosition = play(fallback)!!
        if (fallbackPosition.outcome() == winning) return fallback
        val deadline = System.nanoTime() + searchBudgetMillis * 1_000_000
        var nodes = 0
        fun exhausted(): Boolean {
            checkCancellation()
            return nodes >= 2000 || System.nanoTime() >= deadline
        }
        fun evaluate(state: ChessPosition): Int {
            val end = state.outcome()
            if (end != null) return when (end) {
                winning -> 100000
                ChessOutcome.WHITE_WON, ChessOutcome.BLACK_WON -> -100000
                else -> 0
            }
            return state.board.mapIndexed { index, piece ->
                if (piece == null) 0 else (if (piece.white == side) 1 else -1) *
                    (piece.kind.value + (7 - abs(3 - index / 8) - abs(3 - index % 8)) * 3)
            }.sum()
        }
        var best = fallback
        var bestScore = Int.MIN_VALUE
        val candidates = listOf(fallback) + legalMoves().filter { it != fallback }.shuffled(random)
        for (move in candidates) {
            if (exhausted()) break
            val next = play(move) ?: continue
            nodes++
            val replies = if (next.outcome() == null) next.legalMoves() else emptyList()
            var worst = if (replies.isEmpty()) evaluate(next) else Int.MAX_VALUE
            var complete = true
            for (reply in replies) {
                if (exhausted()) { complete = false; break }
                val after = next.play(reply) ?: continue
                nodes++
                worst = minOf(worst, evaluate(after))
                if (worst <= bestScore) break // This candidate cannot improve the best complete reply score.
            }
            if (complete && worst > bestScore) { best = move; bestScore = worst }
        }
        return best
    }
    companion object {
        val promotions = listOf(PieceKind.QUEEN, PieceKind.ROOK, PieceKind.BISHOP, PieceKind.KNIGHT)
        fun initial() = fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
        fun fromFen(fen: String): ChessPosition {
            val parts = fen.trim().split(Regex("\\s+")); require(parts.size == 6)
            val ranks = parts[0].split('/'); require(ranks.size == 8)
            val board = ranks.flatMap { rank ->
                rank.flatMap { c ->
                    if (c in '1'..'8') List<Piece?>(c.digitToInt()) { null }
                    else listOf(Piece(PieceKind.entries.first { it.code == c.lowercaseChar() }, c.isUpperCase()))
                }.also { require(it.size == 8) }
            }
            require(board.count { it == Piece(PieceKind.KING, true) } == 1 && board.count { it == Piece(PieceKind.KING, false) } == 1)
            require(parts[1] in listOf("w", "b"))
            require(parts[2] == "-" || (parts[2].all { it in "KQkq" } && parts[2].toSet().size == parts[2].length))
            val rights = if (parts[2] == "-") 0 else parts[2].fold(0) { total, right -> total + when (right) { 'K' -> 1; 'Q' -> 2; 'k' -> 4; else -> 8 } }
            val ep = if (parts[3] == "-") null else square(parts[3]).also { require(it / 8 in listOf(2, 5)) }
            require(parts[4].toInt() >= 0 && parts[5].toInt() >= 1)
            val state = ChessPosition(board, parts[1] == "w", rights, ep, parts[4].toInt(), parts[5].toInt())
            return state.copy(repetitions = mapOf(state.key() to 1))
        }
    }
}
