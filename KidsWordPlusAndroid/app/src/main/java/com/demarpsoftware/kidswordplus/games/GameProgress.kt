package com.demarpsoftware.kidswordplus.games

import com.demarpsoftware.kidswordplus.LearningRules
import com.demarpsoftware.kidswordplus.content.objects
import com.demarpsoftware.kidswordplus.content.strings
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class AnswerStats(val attempts: Int = 0, val firstCorrect: Int = 0, val lastFirstCorrect: Boolean = true)
data class DayActivity(val newWords: Set<String> = emptySet(), val reviews: Set<String> = emptySet(),
    val sessions: Set<String> = emptySet(), val target: Int = 5, val rewarded: Boolean = false) {
    val complete get() = newWords.size >= target && reviews.size >= 10 && sessions.size >= 2
    fun reward() = if (complete) copy(rewarded = true) else this
}
data class GameProgress(val days: Map<String, DayActivity> = emptyMap(), val answers: Map<String, AnswerStats> = emptyMap(),
    val memoryCompleted: Map<String, Int> = emptyMap(), val memoryAttempts: Map<String, Int> = emptyMap(), val starBalances: Map<String,Int>? = null, val lessonRewards:Map<String,Set<String>> = emptyMap()) {
    fun activity(language: String, date: LocalDate) = days["$language|$date"] ?: DayActivity()
    fun stars(language: String) = if(starBalances!=null)starBalances[language] ?: 0 else legacyStars(language)
    private fun legacyStars(language:String) = days.filter { it.key.startsWith("$language|") }.values.count { it.rewarded } * 20
    fun earn(language:String,amount:Int):GameProgress {
        val balances=starBalances ?: days.keys.map { it.substringBefore("|") }.distinct().associateWith { legacyStars(it) }
        return copy(starBalances=balances+(language to ((balances[language] ?: 0)+amount)))
    }
    fun prepareLessonRewards(language:String,completed:Set<String>):GameProgress =
        if(language in lessonRewards)this else copy(lessonRewards=lessonRewards+(language to completed))
    fun finishLesson(language:String,id:String):GameProgress {
        val rewarded=lessonRewards[language] ?: emptySet()
        if(id in rewarded)return this
        return earn(language,5).copy(lessonRewards=lessonRewards+(language to (rewarded+id)))
    }
    fun prepare(language: String, active: Set<String>, completed: Set<String>, date: LocalDate): GameProgress {
        val day = activity(language, date)
        val available = active.count { LearningRules.progressKey(language, it) !in completed } + day.newWords.intersect(active).size
        return earn(language,0).withDay(language, date, day.copy(target = minOf(5, available)).reward())
    }
    private fun withDay(language: String, date: LocalDate, day: DayActivity) = copy(days = days + ("$language|$date" to day.reward()))
    fun study(language: String, word: String, fresh: Boolean, date: LocalDate): GameProgress {
        val day = activity(language, date)
        return withDay(language, date, if (fresh) day.copy(newWords = day.newWords + word) else day.copy(reviews = day.reviews + word))
    }
    fun transition(before: GameSession, after: GameSession, date: LocalDate): GameProgress {
        require(before.id == after.id && before.language == after.language && before.kind == after.kind)
        val language = before.language
        var progress = this
        val kindKey = if (before.kind == GameKind.MATCHING) "matching" else "listening"
        after.firstAnswers.filterKeys { it !in before.firstAnswers }.forEach { (id, correct) ->
            val key = "$kindKey|$language|$id"
            val stats = progress.answers[key] ?: AnswerStats()
            progress = progress.copy(answers = progress.answers + (key to AnswerStats(stats.attempts + 1, stats.firstCorrect + if (correct) 1 else 0, correct)))
        }
        if (before.kind != GameKind.MEMORY) {
            val day = progress.activity(language, date)
            progress = progress.withDay(language, date, day.copy(reviews = day.reviews + (after.matched - before.matched)))
        }
        if (!before.finished && after.finished) {
            val day = progress.activity(language, date)
            // A round token is credited once even if a completion callback is delivered twice.
            if (days.none { (key,value) -> key.startsWith("$language|") && after.id in value.sessions }) {
                progress = progress.earn(language,10).withDay(language, date, day.copy(sessions = day.sessions + after.id))
                if (after.kind == GameKind.MEMORY) progress = progress.copy(
                    memoryCompleted = progress.memoryCompleted + (language to ((progress.memoryCompleted[language] ?: 0) + 1)),
                    memoryAttempts = progress.memoryAttempts + (language to after.attempts))
            }
        }
        return progress
    }
    fun speakingRound(language: String, id: String, matched: Set<String>, date: LocalDate): GameProgress {
        val day=activity(language,date)
        val token="speaking|"+id
        if(days.any { (key,value) -> key.startsWith("$language|") && token in value.sessions }) return this
        return earn(language,10).withDay(language,date,day.copy(reviews=day.reviews+matched,sessions=day.sessions+token))
    }
    fun recallRound(language:String,kind:RecallKind,id:String,reviewed:Set<String>,date:LocalDate):GameProgress {
        val day=activity(language,date)
        val token="recall|${kind.name}|$id"
        if(reviewed.isEmpty() || days.any{(key,value)->key.startsWith("$language|") && token in value.sessions})return this
        return earn(language,10).withDay(language,date,day.copy(reviews=day.reviews+reviewed,sessions=day.sessions+token))
    }
    fun reviewIds(language: String, studied: Set<String>, active: Set<String>): Set<String> = active.filter { id ->
        LearningRules.progressKey(language, id) in studied && answers["listening|$language|$id"]?.lastFirstCorrect == false
    }.toSet()
    fun encode(): String = JSONObject().put("version", 1).put("days", JSONObject().apply {
        days.forEach { (key, day) -> put(key, JSONObject().put("newWords", JSONArray(day.newWords.toList())).put("reviews", JSONArray(day.reviews.toList()))
            .put("sessions", JSONArray(day.sessions.toList())).put("target", day.target).put("rewarded", day.rewarded)) }
    }).put("answers", JSONObject().apply { answers.forEach { (key, stats) -> put(key, JSONObject().put("attempts", stats.attempts).put("firstCorrect", stats.firstCorrect).put("lastFirstCorrect", stats.lastFirstCorrect)) } })
        .put("lessonRewards",JSONObject().apply {lessonRewards.forEach {(language,ids)->put(language,JSONArray(ids.toList()))}}).put("starBalances",starBalances?.let {JSONObject(it)}).put("memoryCompleted", JSONObject(memoryCompleted)).put("memoryAttempts", JSONObject(memoryAttempts)).toString()
    companion object {
        fun decode(text: String): GameProgress {
            val root = JSONObject(text); require(root.getInt("version") == 1)
            val days = root.getJSONObject("days").let { json -> json.keys().asSequence().associateWith { key ->
                val d = json.getJSONObject(key)
                DayActivity(d.getJSONArray("newWords").strings().toSet(), d.getJSONArray("reviews").strings().toSet(),
                    d.getJSONArray("sessions").strings().toSet(), d.getInt("target"), d.getBoolean("rewarded")).also { require(it.target in 0..5) }
            } }
            val answers = root.getJSONObject("answers").let { json -> json.keys().asSequence().associateWith { key ->
                val a = json.getJSONObject(key)
                AnswerStats(a.getInt("attempts"), a.getInt("firstCorrect"), a.getBoolean("lastFirstCorrect")).also { require(it.attempts >= 0 && it.firstCorrect in 0..it.attempts) }
            } }
            fun counts(name: String) = root.getJSONObject(name).let { json -> json.keys().asSequence().associateWith { json.getInt(it).also { count -> require(count >= 0) } } }
            return GameProgress(days, answers, counts("memoryCompleted"), counts("memoryAttempts"), if(root.has("starBalances"))counts("starBalances") else null, root.optJSONObject("lessonRewards")?.let {j->j.keys().asSequence().associateWith {j.getJSONArray(it).strings().toSet()}} ?: emptyMap())
        }
    }
}
