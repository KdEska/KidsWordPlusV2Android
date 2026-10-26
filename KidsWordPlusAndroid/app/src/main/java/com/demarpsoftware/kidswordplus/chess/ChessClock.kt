package com.demarpsoftware.kidswordplus.chess

import org.json.JSONObject

data class ChessClock(val limitMillis: Long, val whiteMillis: Long = limitMillis, val blackMillis: Long = limitMillis) {
    init {
        require(limitMillis in 60_000L..10_800_000L)
        require(whiteMillis in 0..limitMillis && blackMillis in 0..limitMillis)
        require(whiteMillis > 0 || blackMillis > 0)
    }
    val timedOutWhite: Boolean? get() = if (whiteMillis == 0L) true else if (blackMillis == 0L) false else null
    fun elapse(millis: Long, white: Boolean): ChessClock {
        if (millis <= 0 || timedOutWhite != null) return this
        return if (white) copy(whiteMillis = (whiteMillis - millis.coerceAtMost(whiteMillis)).coerceAtLeast(0))
        else copy(blackMillis = (blackMillis - millis.coerceAtMost(blackMillis)).coerceAtLeast(0))
    }
    fun json(): JSONObject = JSONObject().put("limit", limitMillis).put("white", whiteMillis).put("black", blackMillis)
    companion object {
        fun minutes(minutes: Int): ChessClock? { require(minutes in 0..180); return if (minutes == 0) null else ChessClock(minutes * 60_000L) }
        fun decode(root: JSONObject) = ChessClock(root.getLong("limit"), root.getLong("white"), root.getLong("black"))
    }
}
fun chessClockText(millis: Long): String {
    val seconds = (millis.coerceAtLeast(0) + 999) / 1000
    return "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
}
