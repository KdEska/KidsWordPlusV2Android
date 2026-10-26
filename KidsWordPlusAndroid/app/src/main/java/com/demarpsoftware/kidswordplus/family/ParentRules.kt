package com.demarpsoftware.kidswordplus.family

import com.demarpsoftware.kidswordplus.LearningRules
import com.demarpsoftware.kidswordplus.content.LearningPack
import com.demarpsoftware.kidswordplus.games.GameProgress
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class ParentGate(val started: Long? = null, val unlocked: Boolean = false) {
    fun begin(now: Long) = if (unlocked) this else copy(started = now)
    fun fraction(now: Long): Float = started?.let { ((now - it).coerceAtLeast(0) / 3000f).coerceAtMost(1f) } ?: 0f
    fun tick(now: Long) = if (started != null && now - started >= 3000) copy(unlocked = true) else this
    fun release() = copy(started = null)
    fun allowsManagement(ready: Boolean) = !ready || unlocked
}
data class CategoryReport(val id: String, val studied: Int, val total: Int)
data class ParentReport(val studied: Int, val total: Int, val lessons: Int, val weeklyNew: Int, val weeklyReviews: Int,
    val activeDays: Int, val games: Int, val stars: Int, val streak: Int, val pending: Int, val categories: List<CategoryReport>, val daily: List<Pair<LocalDate, Int>>) {
    val strongest get() = categories.filter { it.studied > 0 }.sortedByDescending { it.studied.toDouble() / it.total }.take(2)
    val developing get() = categories.filter { it.studied < it.total }.sortedBy { it.studied.toDouble() / it.total }.take(2)
    companion object {
        fun build(pack: LearningPack, completed: Set<String>, progress: GameProgress, today: LocalDate): ParentReport {
            val language = pack.language
            val active = pack.words.map { it.id }.toSet()
            val studied = active.filter { LearningRules.progressKey(language, it) in completed }.toSet()
            val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val days = (0L..6L).map { monday.plusDays(it) }
            val activity = days.map { progress.activity(language, it) }
            fun isActive(day: LocalDate) = progress.activity(language, day).let { it.newWords.isNotEmpty() || it.reviews.isNotEmpty() || it.sessions.isNotEmpty() }
            var cursor = if (isActive(today)) today else today.minusDays(1)
            var streak = 0
            while (isActive(cursor)) { streak++; cursor = cursor.minusDays(1) }
            return ParentReport(studied.size, active.size, pack.lessons.count { lesson -> lesson.words.all { it.id in studied } },
                activity.flatMap { it.newWords }.toSet().size, activity.sumOf { it.reviews.size },
                days.count { isActive(it) }, activity.sumOf { it.sessions.size }, progress.stars(language), streak,
                progress.reviewIds(language, completed, active).size,
                pack.words.groupBy { it.category }.map { (id, words) -> CategoryReport(id, words.count { it.id in studied }, words.size) },
                days.map { it to progress.activity(language, it).newWords.size })
        }
    }
}
