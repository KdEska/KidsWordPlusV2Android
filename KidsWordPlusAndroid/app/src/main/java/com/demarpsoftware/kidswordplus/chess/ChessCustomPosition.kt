package com.demarpsoftware.kidswordplus.chess

import org.json.JSONObject
import kotlin.math.abs
import kotlin.random.Random

data class ChessCustomPosition(val board: List<Piece?> = List(64) { null }, val whiteTurn: Boolean = true, val allowCastling: Boolean = false) {
    init { require(board.size == 64) }
    fun place(index: Int, piece: Piece?): ChessCustomPosition {
        if (index !in 0..63) return this
        val next = board.toMutableList()
        if (piece?.kind == PieceKind.KING) next.indices.filter { next[it] == piece }.forEach { next[it] = null }
        next[index] = piece
        return copy(board = next)
    }
    fun move(from:Int,to:Int):ChessCustomPosition {
        if(from !in 0..63||to !in 0..63||from==to)return this
        val actor=board[from] ?: return this
        return place(from,null).place(to,actor)
    }
    fun arrange(white:Boolean):ChessCustomPosition? {
        val placements=ChessBoardSetup.placements.filter{it.second.white==white}
        if(placements.any{(square,_)->board[square]?.white==!white})return null
        val next=board.map{if(it?.white==white)null else it}.toMutableList()
        placements.forEach{(square,piece)->next[square]=piece}
        return copy(board=next)
    }
    fun randomize(white:Boolean,random:Random=Random.Default):ChessCustomPosition? {
        val opponent=board.map{if(it?.white==white)null else it}
        val free=opponent.indices.filter{opponent[it]==null}
        if(free.isEmpty())return null
        val count=random.nextInt(1,minOf(16,free.size)+1)
        val army=ChessBoardSetup.placements.filter{it.second.white==white&&it.second.kind!=PieceKind.KING}.map{it.second.kind}
        val kinds=listOf(PieceKind.KING)+army.shuffled(random).take(count-1)
        val otherKings=opponent.indices.filter{opponent[it]?.kind==PieceKind.KING}
        var fallback:ChessCustomPosition?=null
        repeat(64) {
            val next=opponent.toMutableList();val available=free.toMutableList()
            for(kind in kinds) {
                val choices=available.filter{square ->
                    (kind!=PieceKind.PAWN||square/8 !in listOf(0,7))&&
                    (kind!=PieceKind.KING||otherKings.none{maxOf(abs(it/8-square/8),abs(it%8-square%8))<=1})
                }
                val square=choices.randomOrNull(random) ?: break
                next[square]=Piece(kind,white);available.remove(square)
            }
            if(next.count{it?.white==white}==count) {
                val candidate=copy(board=next)
                if(fallback==null)fallback=candidate
                if(candidate.issue==null)return candidate
            }
        }
        return fallback
    }
    val position: ChessPosition get() {
        var rights = 0
        if (allowCastling) for (white in listOf(true, false)) {
            val row = if (white) 7 else 0
            if (board[row * 8 + 4] == Piece(PieceKind.KING, white)) {
                if (board[row * 8 + 7] == Piece(PieceKind.ROOK, white)) rights = rights or if (white) 1 else 4
                if (board[row * 8] == Piece(PieceKind.ROOK, white)) rights = rights or if (white) 2 else 8
            }
        }
        val state = ChessPosition(board, whiteTurn, castling = rights)
        return state.copy(repetitions = mapOf(state.key() to 1))
    }
    val issue: String? get() {
        for (white in listOf(true, false)) {
            val pieces = board.filterNotNull().filter { it.white == white }
            if (pieces.count { it.kind == PieceKind.KING } != 1) return "kings"
            if (pieces.size > 16 || pieces.count { it.kind == PieceKind.PAWN } > 8) return "material"
        }
        if (board.indices.any { it / 8 in listOf(0, 7) && board[it]?.kind == PieceKind.PAWN }) return "pawns"
        val a = board.indexOf(Piece(PieceKind.KING, true)); val b = board.indexOf(Piece(PieceKind.KING, false))
        if (maxOf(abs(a / 8 - b / 8), abs(a % 8 - b % 8)) <= 1) return "adjacent"
        if (position.inCheck(!whiteTurn)) return "wrongCheck"
        if (position.outcome() != null) return "finished"
        return null
    }
    fun fen(): String {
        val ranks = board.chunked(8).joinToString("/") { rank -> buildString {
            var empty = 0
            rank.forEach { piece -> if (piece == null) empty++ else {
                if (empty > 0) { append(empty); empty = 0 }
                append(if (piece.white) piece.kind.code.uppercaseChar() else piece.kind.code)
            } }
            if (empty > 0) append(empty)
        } }
        val rights = listOf(1 to 'K', 2 to 'Q', 4 to 'k', 8 to 'q').filter { position.castling and it.first != 0 }.map { it.second }.joinToString("").ifEmpty { "-" }
        return "$ranks ${if (whiteTurn) "w" else "b"} $rights - 0 1"
    }
    fun encode(): String = JSONObject().put("version", 1).put("board", board.joinToString("") { piece ->
        if (piece == null) "." else (if (piece.white) piece.kind.code.uppercaseChar() else piece.kind.code).toString()
    }).put("whiteTurn", whiteTurn).put("castling", allowCastling).toString()
    companion object {
        fun standard() = ChessCustomPosition(ChessPosition.initial().board, allowCastling = true)
        fun fromFen(fen: String): ChessCustomPosition {
            val state = ChessPosition.fromFen(fen)
            require(state.enPassant == null && state.quietPlies == 0 && state.fullMove == 1)
            val custom = ChessCustomPosition(state.board, state.whiteTurn, state.castling != 0)
            require(custom.position.castling == state.castling && custom.issue == null)
            return custom
        }
        fun decode(text: String): ChessCustomPosition {
            require(text.length <= 2048)
            val root = JSONObject(text); require(root.getInt("version") == 1)
            val encoded = root.getString("board"); require(encoded.length == 64)
            val board = encoded.map { c -> if (c == '.') null else Piece(PieceKind.entries.first { it.code == c.lowercaseChar() }, c.isUpperCase()) }
            return ChessCustomPosition(board, root.getBoolean("whiteTurn"), root.getBoolean("castling"))
        }
    }
}
