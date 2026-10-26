package com.demarpsoftware.kidswordplus.notifications

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

data class LearningReminder(val id: String = UUID.randomUUID().toString(), val hour: Int = 18, val minute: Int = 0, val enabled: Boolean = true) {
    val valid: Boolean get() = runCatching { UUID.fromString(id).toString() == id.lowercase() }.getOrDefault(false) && hour in 0..23 && minute in 0..59
    fun nextOccurrence(now: Instant, zone: ZoneId): Instant {
        require(valid)
        val today = now.atZone(zone).toLocalDate()
        val candidate = today.atTime(hour, minute).atZone(zone).toInstant()
        // Resolve each local date independently so DST changes never shift the chosen clock time.
        return if (candidate > now) candidate else today.plusDays(1).atTime(hour, minute).atZone(zone).toInstant()
    }
    /** Preserve an undelivered alarm when Android batches it past its requested time. */
    fun scheduledOccurrence(now: Instant, zone: ZoneId, previous: Long, delivered: Long): Instant {
        if (previous > 0 && previous != delivered) {
            val due = Instant.ofEpochMilli(previous)
            val age = java.time.Duration.between(due, now)
            if (age < java.time.Duration.ofDays(1) && nextOccurrence(due.minusMillis(1), zone) == due) return due
        }
        return nextOccurrence(now, zone)
    }
    companion object {
        fun encode(entries: List<LearningReminder>): String {
            require(entries.all { it.valid } && entries.map { it.id }.toSet().size == entries.size)
            return JSONArray().apply { entries.forEach { put(JSONObject().put("id", it.id).put("hour", it.hour).put("minute", it.minute).put("enabled", it.enabled)) } }.toString()
        }
        fun decode(raw: String): List<LearningReminder>? = runCatching {
            val array = JSONArray(raw)
            List(array.length()) { i -> array.getJSONObject(i).let { LearningReminder(it.getString("id"), it.getInt("hour"), it.getInt("minute"), it.getBoolean("enabled")) } }
                .also { require(it.all { item -> item.valid } && it.map { item -> item.id }.toSet().size == it.size) }
        }.getOrNull()
    }
}
