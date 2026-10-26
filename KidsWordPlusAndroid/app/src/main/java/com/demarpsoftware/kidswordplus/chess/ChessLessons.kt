package com.demarpsoftware.kidswordplus.chess

/** Movement-only lessons: no turn alternation, castling or en passant. */
data class ChessLessonTask(val id: String, val kind: PieceKind, val origin: Int, val target: Int,
    val capture: Boolean = false, val obstacles: Map<Int, Piece> = emptyMap()) {
    val position: ChessPosition get() = ChessPosition(List(64) { index ->
        if (index == origin) Piece(kind, true) else obstacles[index]
    }, castling = 0)
    fun accepts(from: Int, to: Int): Boolean = from == origin && to == target && position.movesFrom(from).any { it.to == to }
}
object ChessLessons {
    val baseTasks: List<ChessLessonTask> = PieceKind.entries.zip(listOf("e5", "g7", "g4", "g7", "e6", "d4")).map { (kind, target) ->
        ChessLessonTask("move.${kind.name.lowercase()}", kind, square(if (kind == PieceKind.PAWN) "d2" else "d4"), square(target))
    } + listOf(
        ChessLessonTask("capture.rook", PieceKind.ROOK, square("d4"), square("g4"), true, mapOf(square("g4") to Piece(PieceKind.PAWN, false))),
        ChessLessonTask("capture.pawn", PieceKind.PAWN, square("d4"), square("e5"), true, mapOf(square("e5") to Piece(PieceKind.ROOK, false))),
        ChessLessonTask("jump.knight", PieceKind.KNIGHT, square("d4"), square("e6"), obstacles = mapOf(square("d5") to Piece(PieceKind.PAWN, true), square("e4") to Piece(PieceKind.PAWN, true)))
    )
    // Keep the recorded goals and legacy identifiers while varying their boards.
    val tasks: List<ChessLessonTask> = buildList {
        addAll(baseTasks)
        for(goal in baseTasks) for(source in 0..63) {
            if(source==goal.origin || source==goal.target || goal.kind==PieceKind.PAWN && source/8 !in 1..6) continue
            val obstacles=mutableMapOf<Int,Piece>()
            if(goal.capture) obstacles[goal.target]=Piece(PieceKind.PAWN,false)
            val adjacent=(0..63).filter { it!=source && it!=goal.target && kotlin.math.abs(it/8-source/8)<=1 && kotlin.math.abs(it%8-source%8)<=1 }
            if(goal.id=="jump.knight") adjacent.forEach { obstacles[it]=Piece(PieceKind.PAWN,true) }
            val variant=goal.copy(id="${goal.id}.${squareName(source)}",origin=source,obstacles=obstacles.toMap())
            if(!variant.accepts(source,goal.target)) continue
            add(variant)
            if(goal.id!="jump.knight") {
                for(obstacle in adjacent) {
                    val blocked=variant.copy(id="${variant.id}.obstacle",obstacles=variant.obstacles+(obstacle to Piece(if(goal.kind==PieceKind.PAWN)PieceKind.ROOK else PieceKind.PAWN,true)))
                    if(blocked.accepts(source,goal.target)) { add(blocked);break }
                }
            }
        }
    }
    val byId=tasks.associateBy { it.id }
    val ids = byId.keys
    fun finish(completed: Set<String>, taskId: String, from: Int, to: Int): Set<String> =
        if (byId[taskId]?.accepts(from, to) == true) completed + taskId else completed
}

/** Immutable hundred-goal round; only a correct move permits advancing. */
@ConsistentCopyVisibility
data class ChessLessonRound private constructor(val tasks: List<ChessLessonTask>, val index: Int=0, val solved: Boolean=false) {
    val block get()=index/10+1
    val blockTask get()=index%10+1
    val blockCompleted get()=index%10+if(solved)1 else 0
    val blockFinished get()=solved && blockCompleted==10
    val task get()=tasks[index]
    val completed get()=index + if(solved)1 else 0
    val finished get()=completed==COUNT
    fun finish(from: Int,to: Int)=if(task.accepts(from,to)) copy(solved=true) else this
    fun advance()=if(solved && !finished) copy(index=index+1,solved=false) else this
    fun save(): List<String> = listOf(index.toString(),solved.toString()) + tasks.map { it.id }
    companion object {
        const val COUNT=100
        fun create(random: kotlin.random.Random=kotlin.random.Random.Default, excluding: Set<String> = emptySet()): ChessLessonRound {
            val fresh=ChessLessons.tasks.filter {it.id !in excluding}
            val pool=if(fresh.size>=COUNT && PieceKind.entries.all {kind -> fresh.any {it.kind==kind}} && fresh.any {it.capture})fresh else ChessLessons.tasks
            val ids=ChessPuzzleRound.create(pool.map {it.id to it.kind.name},random=random).ids
            val chosen=ids.map {ChessLessons.byId.getValue(it)}.toMutableList()
            if(chosen.none {it.capture}) {
                val capture=pool.filter {it.capture}.random(random)
                val replace=chosen.indexOfFirst {it.kind==capture.kind}
                chosen[replace]=capture
            }
            return ChessLessonRound(chosen.shuffled(random))
        }
        fun restore(saved: List<String>): ChessLessonRound? {
            if(saved.size!=COUNT+2)return null
            val index=saved[0].toIntOrNull()?.takeIf { it in 0 until COUNT } ?: return null
            val solved=saved[1].toBooleanStrictOrNull() ?: return null
            val tasks=saved.drop(2).map { ChessLessons.byId[it] ?: return null }
            if(tasks.map { it.id }.toSet().size!=COUNT || tasks.map { it.kind }.toSet()!=PieceKind.entries.toSet() || tasks.none { it.capture })return null
            return ChessLessonRound(tasks,index,solved)
        }
    }
}
