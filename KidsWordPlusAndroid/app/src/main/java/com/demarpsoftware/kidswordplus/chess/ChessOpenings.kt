package com.demarpsoftware.kidswordplus.chess

import org.json.JSONObject

data class ChessOpeningStep(val coordinates: String, val idea: String) {
    val move get() = ChessMove(square(coordinates.take(2)), square(coordinates.drop(2)))
}
data class ChessOpening(val id: String, val tr: String, val en: String, val notation: String, val steps: List<ChessOpeningStep>) {
    fun position(count: Int): ChessPosition = steps.take(count.coerceIn(0, steps.size)).fold(ChessPosition.initial()) { position, step ->
        checkNotNull(position.play(step.move)) { "Invalid opening: $id" }
    }
}
object ChessOpenings {
    private fun line(id: String, tr: String, en: String, notation: String, moves: String, ideas: String) =
        ChessOpening(id, tr, en, notation, moves.split(' ').zip(ideas.split(' ')).map { (move, idea) -> ChessOpeningStep(move, idea) })
    val all = listOf(
        line("italian", "İtalyan Açılışı", "Italian Game", "1. e4 e5 2. Nf3 Nc6 3. Bc4 Bc5", "e2e4 e7e5 g1f3 b8c6 f1c4 f8c5", "center center develop develop pressure develop"),
        line("spanish", "İspanyol Açılışı", "Ruy Lopez", "1. e4 e5 2. Nf3 Nc6 3. Bb5 a6", "e2e4 e7e5 g1f3 b8c6 f1b5 a7a6", "center center develop develop pressure pressure"),
        line("scotch", "İskoç Açılışı", "Scotch Game", "1. e4 e5 2. Nf3 Nc6 3. d4 exd4 4. Nxd4", "e2e4 e7e5 g1f3 b8c6 d2d4 e5d4 f3d4", "center center develop develop center exchange exchange"),
        line("queens", "Vezir Gambiti", "Queen's Gambit", "1. d4 d5 2. c4 e6 3. Nc3 Nf6", "d2d4 d7d5 c2c4 e7e6 b1c3 g8f6", "center center pressure support develop develop"),
        line("london", "Londra Sistemi", "London System", "1. d4 d5 2. Nf3 Nf6 3. Bf4 e6", "d2d4 d7d5 g1f3 g8f6 c1f4 e7e6", "center center develop develop develop support"),
        line("sicilian", "Sicilya Savunması", "Sicilian Defence", "1. e4 c5 2. Nf3 d6 3. d4 cxd4 4. Nxd4", "e2e4 c7c5 g1f3 d7d6 d2d4 c5d4 f3d4", "center pressure develop support center exchange exchange"),
        line("french", "Fransız Savunması", "French Defence", "1. e4 e6 2. d4 d5 3. e5 c5", "e2e4 e7e6 d2d4 d7d5 e4e5 c7c5", "center support center pressure space pressure"),
        line("caro", "Caro–Kann Savunması", "Caro–Kann Defence", "1. e4 c6 2. d4 d5 3. Nc3 dxe4 4. Nxe4", "e2e4 c7c6 d2d4 d7d5 b1c3 d5e4 c3e4", "center support center pressure develop exchange exchange")
    )
    val ids = all.map { it.id }.toSet()
}
data class ChessOpeningProgress(val counts: Map<String, Int> = emptyMap(), val completed: Set<String> = emptySet()) {
    fun count(id: String) = counts[id] ?: 0
    fun advance(id: String, expectedCount: Int, move: ChessMove): ChessOpeningProgress {
        val opening = ChessOpenings.all.find { it.id == id } ?: return this
        val count = count(id)
        if (count != expectedCount || opening.steps.getOrNull(count)?.move != move || opening.position(count).play(move) == null) return this
        val next = count + 1
        return copy(counts = counts + (id to next), completed = if (next == opening.steps.size) completed + id else completed)
    }
    fun rewind(id: String, restart: Boolean = false): ChessOpeningProgress =
        if (id !in ChessOpenings.ids) this else copy(counts = counts + (id to if (restart) 0 else (count(id) - 1).coerceAtLeast(0)))
    fun encode(): String = JSONObject().put("version", 1).put("counts", JSONObject(counts))
        .put("completed", org.json.JSONArray(completed.sorted())).toString()
    companion object {
        fun decode(text: String): ChessOpeningProgress {
            val root = JSONObject(text)
            require(root.getInt("version") == 1)
            val counts = root.getJSONObject("counts")
            val values = ChessOpenings.all.associate { opening ->
                val count = if (counts.has(opening.id)) counts.getInt(opening.id) else 0
                require(count in 0..opening.steps.size)
                opening.id to count
            }
            val completed = root.getJSONArray("completed")
            return ChessOpeningProgress(values, (0 until completed.length()).map { completed.getString(it) }.toSet().intersect(ChessOpenings.ids))
        }
    }
}
