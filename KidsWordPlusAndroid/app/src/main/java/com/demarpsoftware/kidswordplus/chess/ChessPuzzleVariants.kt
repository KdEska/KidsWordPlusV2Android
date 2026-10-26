package com.demarpsoftware.kidswordplus.chess

/** Board symmetries and translations, filtered by the actual chess rules. */
internal object ChessPuzzleVariants {
    private fun transforms(position: ChessPosition): Sequence<Pair<(Int)->Int,List<Piece?>>> = sequence {
        for(rotation in 0..3) for(mirror in listOf(false,true)) for(dr in -7..7) for(dc in -7..7) {
            val transform: (Int)->Int = {index ->
                var row=index/8;var col=if(mirror)7-index%8 else index%8
                repeat(rotation) {val old=row;row=col;col=7-old}
                row+=dr;col+=dc
                if(row in 0..7 && col in 0..7)row*8+col else -1
            }
            val occupied=position.board.indices.filter {position.board[it]!=null}
            if(occupied.any {transform(it)<0})continue
            val board=MutableList<Piece?>(64){null}
            occupied.forEach {board[transform(it)]=position.board[it]}
            yield(transform to board)
        }
    }
    private fun key(board: List<Piece?>)=board.joinToString("") {it?.let {p -> if(p.white)p.kind.code.uppercaseChar().toString() else p.kind.code.toString()} ?: "."}
    fun safety(seeds: List<ChessSafetyTask>): List<ChessSafetyTask> = buildList {
        addAll(seeds)
        val seen=seeds.map {it.group+key(it.position.board)}.toMutableSet()
        for(seed in seeds) for((transform,board) in transforms(seed.position)) {
            val position=ChessPosition(board,castling=0)
            if(!position.inCheck(true) || position.inCheck(false))continue
            val from=seed.source?.let(transform);val to=transform(seed.target)
            if(to<0 || from!=null && from<0)continue
            val task=ChessSafetyTask("${seed.group}.variant.${seed.source?.let(transform) ?: "threat"}.$to.${key(board)}",position,from,to)
            if(!task.accepts(from,to) || from!=null && task.after().inCheck(true))continue
            if(seen.add(seed.group+key(board)))add(task)
        }
        // Vary the opposing king while preserving the check and the valid defensive move.
        for(seed in seeds) {
            var generated=0
            val originalKing=seed.position.board.indexOfFirst {it?.kind==PieceKind.KING && !it.white}
            for((transform,baseBoard) in transforms(seed.position)) {
                for(king in 0..63) {
                    val oldKing=transform(originalKing)
                    if(baseBoard[king]!=null || king==oldKing)continue
                    val board=baseBoard.toMutableList().apply {this[oldKing]=null;this[king]=Piece(PieceKind.KING,false)}
                    val position=ChessPosition(board,castling=0)
                    if(!position.inCheck(true) || position.inCheck(false))continue
                    val from=seed.source?.let(transform);val to=transform(seed.target)
                    if(to<0 || from!=null && from<0)continue
                    val task=ChessSafetyTask("${seed.group}.kingvariant.${key(board)}",position,from,to)
                    if(!task.accepts(from,to) || from!=null && task.after().inCheck(true))continue
                    if(seen.add(seed.group+key(board))) {add(task);generated++}
                    if(generated>=100)break
                }
                if(generated>=100)break
            }
        }
    }
    fun endings(seeds: List<ChessEndingPuzzle>): List<ChessEndingPuzzle> = buildList {
        addAll(seeds)
        val seen=seeds.map {key(it.position.board)}.toMutableSet()
        val counts=ChessEnding.entries.associateWith {kind -> seeds.count {it.answer==kind}}.toMutableMap()
        val kings=listOf(square("a1"))+(0..63).filter {it/8 in listOf(0,7) || it%8 in listOf(0,7)}.filter {it!=square("a1")}
        val opponents=listOf(square("c3"))+(0..63).filter {it!=square("c3")}
        val firstQueens=listOf(square("b2"),square("b3"),square("a3"))
        for(king in kings) for(opponent in opponents) {
            if(maxOf(kotlin.math.abs(king/8-opponent/8),kotlin.math.abs(king%8-opponent%8))<=1)continue
            for(queen in firstQueens+(0..63).filter {it !in firstQueens}) {
                if(queen==king || queen==opponent)continue
                val board=MutableList<Piece?>(64){null}.apply {this[king]=Piece(PieceKind.KING,true);this[opponent]=Piece(PieceKind.KING,false);this[queen]=Piece(PieceKind.QUEEN,false)}
                val position=ChessPosition(board,castling=0)
                val checked=position.inCheck(true)
                if(!checked && counts.getValue(ChessEnding.STALEMATE)>=80)continue
                if(checked && counts.getValue(ChessEnding.CHECK)>=80 && counts.getValue(ChessEnding.MATE)>=80)continue
                val movable=position.movesFrom(king).isNotEmpty()
                if(!checked && movable)continue
                val kind=if(movable)ChessEnding.CHECK else if(checked)ChessEnding.MATE else ChessEnding.STALEMATE
                if(counts.getValue(kind)>=80 || !seen.add(key(board)))continue
                add(ChessEndingPuzzle("ending.generated.${kind.name.lowercase()}.$king.$opponent.$queen",position))
                counts[kind]=counts.getValue(kind)+1
                if(size==240)return@buildList
            }
        }
    }
    fun mates(seeds: List<ChessMatePuzzle>,endings: List<ChessEndingPuzzle>): List<ChessMatePuzzle> = buildList {
        addAll(seeds)
        val seen=seeds.map {key(it.position.board)}.toMutableSet()
        // Vary rook starting squares and the friendly king, retaining the back-rank trap.
        var generated=0
        loop@ for(king in 0..63) for(source in 0..63) {
            if(source/8==0 || source%8>=6 || king==source)continue
            val board=seeds.last().position.board.toMutableList().apply {this[square("a1")]=null;this[square("a7")]=null}
            if(board[king]!=null || board[source]!=null)continue
            board[king]=Piece(PieceKind.KING,true);board[source]=Piece(PieceKind.ROOK,true)
            val position=ChessPosition(board,castling=0)
            if(position.inCheck(true) || position.inCheck(false))continue
            val move=ChessMove(source,source%8)
            val task=ChessMatePuzzle("mate.rook.generated.$king.$source",position,move)
            if(task.accepts(move) && seen.add(key(board))) {add(task);generated++}
            if(generated==80)break@loop
        }
        // Work backwards from genuine mate positions to produce legal queen mates.
        for(ending in endings.filter {it.answer==ChessEnding.MATE}) {
            val final=ending.position.board.map {it?.let {piece -> piece.copy(white=!piece.white)}}
            val target=final.indexOfFirst {it?.kind==PieceKind.QUEEN}
            for(source in 0..63) {
                if(final[source]!=null)continue
                val board=final.toMutableList().apply {this[source]=this[target];this[target]=null}
                val position=ChessPosition(board,castling=0)
                if(position.inCheck(true) || position.inCheck(false))continue
                val move=ChessMove(source,target)
                val task=ChessMatePuzzle("mate.queen.generated.${key(board)}",position,move)
                if(task.accepts(move) && seen.add(key(board)))add(task)
                if(size==240)return@buildList
            }
        }
    }
}
