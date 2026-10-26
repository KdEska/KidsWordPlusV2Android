package com.demarpsoftware.kidswordplus.chess

import kotlin.math.abs
import kotlin.random.Random

/** Teaching diagrams, not full matches: no enemy king, castling or promotion. */
data class ChessGuideMove(val piece: PieceKind, val from: Int, val to: Int, val stage: Int, val board: List<Piece?>) {
    val signature get() = "$from:$to:" + board.joinToString("") { it?.let { p -> if(p.white)p.kind.code.uppercaseChar().toString() else p.kind.code.toString() } ?: "." }
}
object ChessGuideGenerator {
    private val order=listOf(PieceKind.PAWN,PieceKind.ROOK,PieceKind.BISHOP,PieceKind.KNIGHT,PieceKind.QUEEN,PieceKind.KING)
    fun generate(random:Random=Random.Default, excluding:Set<String> = emptySet()):List<ChessGuideMove> {
        val used=excluding.toMutableSet()
        return buildList {
            for(stage in 0..2)for(level in 0..1)for(kind in order) {
                var chosen:ChessGuideMove?=null
                for(attempt in 0 until 10000) {
                    val from=random.nextInt(64);val to=random.nextInt(64)
                    val dr=to/8-from/8;val dc=to%8-from%8;val distance=maxOf(abs(dr),abs(dc))
                    if(from==to || kind==PieceKind.PAWN && (from/8 !in 2..6 || to/8 !in 1..5))continue
                    val shape=when(kind) {
                        PieceKind.PAWN -> if(stage==2)dr == -1 && abs(dc)==1 else dc==0 && (if(stage==0 && level==1)from/8==6 && dr == -2 else dr == -1)
                        PieceKind.ROOK -> dr==0 || dc==0
                        PieceKind.BISHOP -> abs(dr)==abs(dc)
                        PieceKind.KNIGHT -> abs(dr)*abs(dc)==2
                        PieceKind.QUEEN -> dr==0 || dc==0 || abs(dr)==abs(dc)
                        PieceKind.KING -> distance==1
                    }
                    if(!shape)continue
                    if(kind in listOf(PieceKind.ROOK,PieceKind.BISHOP,PieceKind.QUEEN) && (if(level==0)distance!=1 else distance<3))continue
                    val board=MutableList<Piece?>(64){null};board[from]=Piece(kind,true)
                    if(stage==2)board[to]=Piece(order.dropLast(1).filter{it!=PieceKind.PAWN || to/8 in 1..6}.random(random),false)
                    val enemies=when(stage){0->0;1->if(level==0)1 else 3;else->if(level==0)0 else 3}
                    val spaces=(0..63).filter{it!=from && it!=to && it/8 in 1..6}.shuffled(random)
                    spaces.take(enemies).forEach {board[it]=Piece(order.dropLast(1).random(random),false)}
                    if(stage>0 && level==1)board[spaces[enemies]]=Piece(PieceKind.PAWN,true)
                    val position=ChessPosition(board,castling=0)
                    if(position.inCheck(true))continue
                    if(position.movesFrom(from).none{it.to==to})continue
                    // Match play() ends single-piece diagrams as insufficient material.
                    // The legal move was checked above; apply only this teaching move.
                    val afterBoard=board.toMutableList().apply { this[from]=null;this[to]=Piece(kind,true) }
                    if(ChessPosition(afterBoard,castling=0).attacked(to,false))continue
                    val task=ChessGuideMove(kind,from,to,stage,board.toList())
                    if(!used.add(task.signature))continue
                    chosen=task;break
                }
                add(checkNotNull(chosen){"Could not generate a new legal guide exercise"})
            }
        }
    }
}
data class ChessFirstSteps(val moves:List<ChessGuideMove>,val step:Int=0) {
    val finalStep get()=moves.size*2+1
    val complete get()=step==finalStep
    val move get()=moves[((step-1).coerceAtLeast(0)/2).coerceAtMost(moves.lastIndex)]
    val selecting get()=step in 1 until finalStep && step%2==1
    val targets:Set<Int> get()=if(step in 1 until finalStep && !selecting)setOf(move.to)else emptySet()
    fun begin()=if(step==0)copy(step=1)else this
    fun tap(square:Int):ChessFirstSteps=when {
        selecting && square==move.from -> copy(step=step+1)
        square in targets -> copy(step=step+1)
        else -> this
    }
    fun back()=copy(step=(step-1).coerceAtLeast(0))
}
