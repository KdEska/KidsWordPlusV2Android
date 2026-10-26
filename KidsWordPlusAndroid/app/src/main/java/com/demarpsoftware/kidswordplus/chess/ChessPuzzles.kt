package com.demarpsoftware.kidswordplus.chess

enum class ChessEnding { CHECK, MATE, STALEMATE }
data class ChessEndingPuzzle(val id: String, val position: ChessPosition) {
    val answer: ChessEnding by lazy { when {
        position.legalMoves().isNotEmpty() -> ChessEnding.CHECK
        position.inCheck(position.whiteTurn) -> ChessEnding.MATE
        else -> ChessEnding.STALEMATE
    }
    }
    val escapeMoves: List<ChessMove> by lazy { if(answer==ChessEnding.CHECK) position.legalMoves() else emptyList() }
    fun accepts(choice: ChessEnding): Boolean = choice == answer
}
data class ChessMatePuzzle(val id: String, val position: ChessPosition, val referenceMove: ChessMove?=null) {
    private val winningOutcome get() = if (position.whiteTurn) ChessOutcome.WHITE_WON else ChessOutcome.BLACK_WON
    val solutions: List<ChessMove> get() = position.legalMoves().filter { accepts(it) }
    fun accepts(move: ChessMove): Boolean = position.play(move)?.outcome() == winningOutcome
}
object ChessPuzzles {
    // Original teaching positions from the iOS ending and mate seed catalog.
    val baseEndings = listOf(
        ChessEndingPuzzle("ending.1", ChessPosition.fromFen("8/8/8/8/8/q1k5/8/K7 w - - 0 1")),
        ChessEndingPuzzle("ending.2", ChessPosition.fromFen("8/8/8/8/8/2k5/1q6/K7 w - - 0 1")),
        ChessEndingPuzzle("ending.3", ChessPosition.fromFen("8/8/8/8/8/1qk5/8/K7 w - - 0 1"))
    )
    val baseMates = listOf(
        ChessMatePuzzle("mate.queen.left", ChessPosition.fromFen("8/8/8/8/8/2K5/2Q5/k7 w - - 0 1")),
        ChessMatePuzzle("mate.queen.right", ChessPosition.fromFen("8/8/8/8/8/5K2/5Q2/7k w - - 0 1")),
        ChessMatePuzzle("mate.rook", ChessPosition.fromFen("7k/R5pp/8/8/8/8/8/K7 w - - 0 1"))
    ).mapIndexed {index,task -> task.copy(referenceMove=listOf("c2b2","f2g2","a7a8")[index].let {ChessMove(square(it.take(2)),square(it.takeLast(2)))})}
    val endings=ChessPuzzleVariants.endings(baseEndings)
    val mates=ChessPuzzleVariants.mates(baseMates,endings)
    val endingById=endings.associateBy {it.id}
    val mateById=mates.associateBy {it.id}
    val endingPool=endings.map {it.id to it.answer.name}
    val matePool=mates.map {it.id to it.position.board[it.referenceMove!!.from]!!.kind.name}
    val ids = (endings.map { it.id } + mates.map { it.id }).toSet()
    fun finishEnding(completed: Set<String>, id: String, answer: ChessEnding): Set<String> =
        if (endingById[id]?.accepts(answer)==true) completed + id else completed
    fun finishMate(completed: Set<String>, id: String, move: ChessMove): Set<String> =
        if (mateById[id]?.accepts(move)==true) completed + id else completed
}
