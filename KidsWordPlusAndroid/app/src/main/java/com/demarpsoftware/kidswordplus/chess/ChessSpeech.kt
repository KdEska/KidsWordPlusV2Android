package com.demarpsoftware.kidswordplus.chess

import org.json.JSONObject
import java.text.Normalizer
import java.util.Locale

data class ChessSpeechClip(val text: String, val path: String)
class ChessSpeechCatalog(contentJson: String, audioJson: String) {
    private val content = JSONObject(contentJson)
    val clips: List<ChessSpeechClip> = JSONObject(audioJson).getJSONArray("clips").let { array ->
        List(array.length()) { index -> array.getJSONObject(index).let {
            val path = it.getString("file")
            require(Regex("audio/[a-f0-9]+\\.mp3").matches(path))
            ChessSpeechClip(it.getString("text"), path)
        } }.sortedByDescending { normalize(it.text).length }
    }
    fun text(key: String, values: Map<String, String> = emptyMap()): String {
        var result = content.getString(key)
        values.forEach { (key, value) -> result = result.replace("{$key}", value) }
        require(!result.contains('{') && !result.contains('}')) { "Missing speech value: $key" }
        return result
    }
    fun prompt(key:String,values:Map<String,String>):String {
        val piece=values["piece"]?.let {name->PieceKind.entries.firstOrNull {it.name.lowercase()==name}}
        return text(key,if(piece==null)values else values+("piece" to text(piece.name.lowercase()+".name")))
    }
    fun resolve(text: String): List<String> {
        var remaining = normalize(text)
        require(remaining.isNotEmpty())
        val paths = ArrayList<String>()
        while (remaining.isNotEmpty()) {
            val clip = clips.firstOrNull { val prefix = normalize(it.text); remaining == prefix || remaining.startsWith("$prefix ") }
                ?: error("Recording unavailable")
            paths += clip.path
            remaining = remaining.removePrefix(normalize(clip.text)).trim()
        }
        return paths
    }
    fun lastMove(game: ChessGame): String? {
        val move = game.history.lastOrNull() ?: return null
        var position = game.startFen?.let { ChessCustomPosition.fromFen(it).position } ?: ChessPosition.initial()
        game.history.dropLast(1).forEach { position = checkNotNull(position.play(it)) }
        val actor = checkNotNull(position.board[move.from])
        val captured = position.board[move.to] ?: if (actor.kind == PieceKind.PAWN && move.to == position.enPassant) Piece(PieceKind.PAWN, !actor.white) else null
        var result = text("speech.move", mapOf("color" to text(if (actor.white) "speech.white" else "speech.black"), "piece" to text(actor.kind.name.lowercase()+".name"), "from" to squareName(move.from), "to" to squareName(move.to)))
        if (captured != null) result += " " + text("speech.capture", mapOf("piece" to text(captured.kind.name.lowercase()+".name")))
        val promoted = game.position.board[move.to]?.kind
        if (actor.kind == PieceKind.PAWN && promoted != null && promoted != PieceKind.PAWN) result += " " + text("speech.promotion", mapOf("piece" to text(promoted.name.lowercase()+".name")))
        return result
    }
    companion object {
        fun normalize(text: String): String = Normalizer.normalize(text.lowercase(Locale.ROOT), Normalizer.Form.NFC)
            .replace("\u0307", "").replace(Regex("[^\\p{L}\\p{N}\\p{M}]+"), " ").trim().replace(Regex("\\s+"), " ")
    }
}
