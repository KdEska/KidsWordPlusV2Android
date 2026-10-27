package com.demarpsoftware.kidswordplus.chess

/** The reference app places White's back rank, White pawns, then Black's pieces. */
object ChessBoardSetup {
    val placements: List<Pair<Int, Piece>> = listOf(true, false).flatMap { white ->
        val back = if (white) 7 else 0
        val pawns = if (white) 6 else 1
        val order = listOf(PieceKind.ROOK, PieceKind.KNIGHT, PieceKind.BISHOP, PieceKind.QUEEN,
            PieceKind.KING, PieceKind.BISHOP, PieceKind.KNIGHT, PieceKind.ROOK)
        order.mapIndexed { col, kind -> back * 8 + col to Piece(kind, white) } +
            (0..7).map { pawns * 8 + it to Piece(PieceKind.PAWN, white) }
    }
    fun board(count: Int): List<Piece?> {
        require(count in 0..32)
        val placed = placements.take(count).toMap()
        return List(64) { placed[it] }
    }
    fun place(count: Int, kind: PieceKind, target: Int): Int {
        require(count in 0..32)
        val next = placements.getOrNull(count) ?: return count
        return if (next.first == target && next.second.kind == kind) count + 1 else count
    }
}

data class ChessSafetyTask(val id: String, val position: ChessPosition, val source: Int?, val target: Int) {
    val group get()=id.substringBefore('.')
    val audioKey get()=if(source==null)"safety.$group"else position.board[source]!!.kind.name.lowercase()+".move"
    fun accepts(from: Int?, to: Int): Boolean {
        if(source==null)return from==null && to==target
        if(from==null || from !in 0..63 || position.board[from]?.white!=true)return false
        return position.play(ChessMove(from,to))?.inCheck(true)==false
    }
    fun after(): ChessPosition = if (source == null) position else position.play(ChessMove(source, target))!!
}
object ChessSafety {
    val baseTasks = listOf(
        ChessSafetyTask("threat", ChessPosition.fromFen("k3r3/8/8/8/8/8/8/4K3 w - - 0 1"), null, square("e8")),
        ChessSafetyTask("escape", ChessPosition.fromFen("k3r3/8/8/8/8/8/8/4K3 w - - 0 1"), square("e1"), square("d1")),
        ChessSafetyTask("capture", ChessPosition.fromFen("k7/8/8/8/4r3/8/8/4K2B w - - 0 1"), square("h1"), square("e4")),
        ChessSafetyTask("block", ChessPosition.fromFen("4r2k/8/8/8/8/8/R7/4K3 w - - 0 1"), square("a2"), square("e2"))
    )
    val tasks=ChessPuzzleVariants.safety(baseTasks)
    val byId=tasks.associateBy {it.id}
    val pool=tasks.map {it.id to it.group}
    val ids=byId.keys
    fun finish(completed: Set<String>, id: String, from: Int?, to: Int): Set<String> =
        if (byId[id]?.accepts(from,to)==true) completed + id else completed
}
