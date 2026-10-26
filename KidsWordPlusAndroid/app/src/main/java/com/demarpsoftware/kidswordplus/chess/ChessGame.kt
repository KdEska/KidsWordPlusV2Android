package com.demarpsoftware.kidswordplus.chess

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ChessGame(val id: String = UUID.randomUUID().toString(), val computer: Boolean = true, val playerWhite: Boolean = true,
    val position: ChessPosition = ChessPosition.initial(), val history: List<ChessMove> = emptyList(), val startFen: String? = null, val clock: ChessClock? = null, val difficulty: ChessDifficulty = ChessDifficulty.NORMAL, val clockEnabled:Boolean=true) {
    val startingWhite get() = startFen?.let { ChessPosition.fromFen(it).whiteTurn } ?: true
    val outcome get() = position.outcome() ?: clock?.timedOutWhite?.let { if (it) ChessOutcome.BLACK_WON else ChessOutcome.WHITE_WON }
    val computerTurn get() = computer && position.whiteTurn != playerWhite && outcome == null
    fun play(move: ChessMove): ChessGame? = if (outcome != null) null else position.play(move)?.let { copy(position = it, history = history + move) }
    fun elapse(millis: Long): ChessGame = if (outcome != null || clock == null || !clockEnabled) this else copy(clock = clock.elapse(millis, position.whiteTurn))
    fun configureClock(enabled:Boolean,minutes:Int):ChessGame {
        require(minutes in 1..180)
        if(outcome!=null)return this
        val next=if(clock?.limitMillis==minutes*60000L)clock else ChessClock.minutes(minutes)
        return copy(clock=next,clockEnabled=enabled)
    }
    fun undo(): ChessGame? {
        if (history.isEmpty() || clock?.timedOutWhite != null) return null
        val amount = if (computer && position.whiteTurn == playerWhite && history.size >= 2) 2 else 1
        // If the computer opened, do not offer undo before the human has moved.
        if (computer && startingWhite != playerWhite && history.size == 1) return null
        return replay(id, computer, playerWhite, history.dropLast(amount), startFen).copy(clock = clock, difficulty = difficulty,clockEnabled=clockEnabled)
    }
    fun historyText(): String {
        var white = startingWhite
        var number = 1
        return history.mapIndexed { index, move ->
            val prefix = if (white) "$number. " else if (index == 0) "$number... " else ""
            if (!white) number++
            white = !white
            prefix + move.notation
        }.joinToString(" ")
    }
    fun encode(): String = JSONObject().put("version", if (!clockEnabled) 4 else if (clock != null) 3 else if (startFen == null) 1 else 2).apply { if (startFen != null) put("startFen", startFen) }.put("clockEnabled",clockEnabled).put("id", id).put("computer", computer).put("playerWhite", playerWhite).put("difficulty", difficulty.name)
        .put("moves", JSONArray(history.map { it.notation })).apply { clock?.let { put("clock", it.json()) } }.toString()
    companion object {
        private fun replay(id: String, computer: Boolean, playerWhite: Boolean, moves: List<ChessMove>, startFen: String? = null): ChessGame {
            var game = ChessGame(id, computer, playerWhite, position = startFen?.let { ChessCustomPosition.fromFen(it).position } ?: ChessPosition.initial(), startFen = startFen)
            moves.forEach { game = game.play(it) ?: error("Illegal saved move") }
            return game
        }
        fun decode(text: String): ChessGame {
            require(text.length <= 200_000)
            val root = JSONObject(text); require(root.getInt("version") in 1..4)
            val id = root.getString("id"); UUID.fromString(id)
            val array = root.getJSONArray("moves"); require(array.length() <= 10000)
            val moves = List(array.length()) { i ->
                val move = array.getString(i); require(move.length in 4..5)
                val promotion = if (move.length == 5) ChessPosition.promotions.first { it.code == move[4] } else null
                ChessMove(square(move.take(2)), square(move.substring(2, 4)), promotion)
            }
            return replay(id, root.getBoolean("computer"), root.getBoolean("playerWhite"), moves, if (root.getInt("version") == 2 || (root.getInt("version") >= 3 && root.has("startFen"))) root.getString("startFen") else null)
                .let { game -> if (root.getInt("version") >= 3) game.copy(clock = if(root.has("clock"))ChessClock.decode(root.getJSONObject("clock"))else null,clockEnabled=root.optBoolean("clockEnabled",true)) else game }
                .let { game -> game.copy(difficulty = if (root.has("difficulty")) ChessDifficulty.valueOf(root.getString("difficulty")) else ChessDifficulty.NORMAL) }
        }
    }
}
data class ChessSummary(val id: String, val outcome: ChessOutcome, val plies: Int, val computer: Boolean, val difficulty: ChessDifficulty = ChessDifficulty.NORMAL, val playerWhite: Boolean? = null)
data class ChessStatistics(val games: List<ChessSummary> = emptyList()) {
    fun record(game: ChessGame): ChessStatistics {
        val end = game.outcome ?: return this
        if (games.any { it.id == game.id }) return this
        return copy(games = games + ChessSummary(game.id, end, game.history.size, game.computer, game.difficulty, game.playerWhite))
    }
    fun encode(): String = JSONObject().put("version", 1).put("games", JSONArray(games.map {
        JSONObject().put("id", it.id).put("outcome", it.outcome.name).put("plies", it.plies).put("computer", it.computer).put("difficulty", it.difficulty.name).apply { it.playerWhite?.let { side -> put("playerWhite", side) } }
    })).toString()
    companion object {
        fun decode(text: String): ChessStatistics {
            val root = JSONObject(text); require(root.getInt("version") == 1)
            val array = root.getJSONArray("games"); require(array.length() <= 10000)
            val games = List(array.length()) { i ->
                val record = array.getJSONObject(i)
                ChessSummary(record.getString("id").also { UUID.fromString(it) }, ChessOutcome.valueOf(record.getString("outcome")), record.getInt("plies").also { require(it >= 0) }, record.getBoolean("computer"), if (record.has("difficulty")) ChessDifficulty.valueOf(record.getString("difficulty")) else ChessDifficulty.NORMAL, if(record.has("playerWhite")) record.getBoolean("playerWhite") else null)
            }
            require(games.map { it.id }.distinct().size == games.size)
            return ChessStatistics(games)
        }
    }
}
