package com.demarpsoftware.kidswordplus.chess

/** Reconstructed from the starting position; never changes the saved game. */
data class ChessHistoryEntry(val number:Int,val actor:Piece,val move:ChessMove)
data class ChessHistoryRow(val number:Int,val white:ChessHistoryEntry?,val black:ChessHistoryEntry?)
fun chessHistoryRows(moves:List<ChessMove>,startFen:String?=null):List<ChessHistoryRow> {
    var position=startFen?.let(ChessPosition::fromFen) ?: ChessPosition.initial()
    val rows=mutableListOf<ChessHistoryRow>()
    for(move in moves) {
        val actor=position.board[move.from] ?: break
        val next=position.play(move) ?: break
        val entry=ChessHistoryEntry(position.fullMove,actor,move)
        val row=rows.lastOrNull()?.takeIf {it.number==entry.number} ?: ChessHistoryRow(entry.number,null,null).also {rows+=it}
        rows[rows.lastIndex]=if(actor.white)row.copy(white=entry)else row.copy(black=entry)
        position=next
    }
    return rows
}

/** Actual captures only, including en passant and captures of promoted pieces. */
fun chessCapturedPieces(moves:List<ChessMove>,startFen:String?=null):List<Piece> {
    var position=startFen?.let(ChessPosition::fromFen) ?: ChessPosition.initial()
    val captures=mutableListOf<Piece>()
    for(move in moves) {
        val actor=position.board[move.from] ?: break
        val next=position.play(move) ?: break
        val captured=position.board[move.to] ?: if(actor.kind==PieceKind.PAWN && move.to==position.enPassant && move.from%8!=move.to%8)
            position.board[move.from/8*8+move.to%8] else null
        captured?.let(captures::add)
        position=next
    }
    return captures
}
