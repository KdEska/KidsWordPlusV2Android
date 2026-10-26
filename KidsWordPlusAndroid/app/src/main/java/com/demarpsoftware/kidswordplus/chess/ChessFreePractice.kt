package com.demarpsoftware.kidswordplus.chess

/** A single-piece movement exercise, independent of matches and lesson progress. */
data class ChessFreePractice(val kind: PieceKind, val location: Int) {
    init { require(location in 0..63) }
    val board: List<Piece?> get() = List(64) { if (it == location) Piece(kind, true) else null }
    val destinations: Set<Int> get() = ChessPosition(board, castling = 0).movesFrom(location).map { it.to }.toSet()
    fun move(to: Int): ChessFreePractice? = if (to !in destinations) null else
        ChessFreePractice(if (kind == PieceKind.PAWN && to / 8 == 0) PieceKind.QUEEN else kind, to)
    companion object {
        fun start(kind: PieceKind) = ChessFreePractice(kind, square(if (kind == PieceKind.PAWN) "d2" else "d4"))
    }
}
