package com.demarpsoftware.kidswordplus.chess

/** A visual description of one already validated and saved move. */
data class ChessMovingPiece(val from: Int, val to: Int, val piece: Piece)
data class ChessMoveVisual(val before: List<Piece?>, val movers: List<ChessMovingPiece>) {
    companion object {
        fun between(before: ChessGame?, after: ChessGame?): ChessMoveVisual? {
            if(before==null || after==null || before.id!=after.id || after.history.size!=before.history.size+1 || after.history.dropLast(1)!=before.history) return null
            val move=after.history.last()
            val expected=before.position.play(move) ?: return null
            if(expected!=after.position)return null
            val piece=before.position.board[move.from] ?: return null
            val movers=mutableListOf(ChessMovingPiece(move.from,move.to,piece))
            if(piece.kind==PieceKind.KING && kotlin.math.abs(move.to%8-move.from%8)==2) {
                val row=move.from/8
                val rookFrom=row*8+if(move.to%8==6)7 else 0
                val rookTo=row*8+if(move.to%8==6)5 else 3
                movers+=ChessMovingPiece(rookFrom,rookTo,before.position.board[rookFrom] ?: return null)
            }
            return ChessMoveVisual(before.position.board,movers)
        }
    }
}
