package com.demarpsoftware.kidswordplus.family

import com.demarpsoftware.kidswordplus.InterfaceStrings

import com.demarpsoftware.kidswordplus.LearningRules
import com.demarpsoftware.kidswordplus.content.LearningPack
import com.demarpsoftware.kidswordplus.games.GameProgress
import java.time.LocalDate

data class Companion(val id: String, val name: String, val tr: String, val en: String, val words: Int, val task: String)
data class CompanionEvidence(val words: Int = 0, val games: Int = 0, val bestStreak: Int = 0, val perfect: Boolean = false, val level: Boolean = false)
object Companions {
    val all = listOf(
        Companion("mia", "Mia", "Meraklı, neşeli", "Curious, cheerful", 0, "none"),
        Companion("max-dog", "Max", "Enerjik, sadık", "Energetic, loyal", 0, "none"),
        Companion("luna", "Luna", "Hızlı, tatlı", "Quick, sweet", 0, "none"),
        Companion("piko-default", "Piko", "Sakin, sevimli", "Calm, lovable", 0, "none"),
        Companion("foxy", "Foxy", "Zeki, meraklı", "Clever, curious", 0, "none"),
        Companion("pingu", "Pingu", "Komik, arkadaş canlısı", "Funny, friendly", 0, "none"),
        Companion("momo", "Momo", "Yaramaz, hareketli", "Mischievous, lively", 25, "tenGames"),
        Companion("nuki", "Nuki", "Çalışkan, koleksiyoncu", "Hardworking, a collector", 50, "none"),
        Companion("koko", "Koko", "Rahat, sevecen", "Relaxed, affectionate", 75, "none"),
        Companion("bibi", "Bibi", "Komik, cesur", "Funny, brave", 100, "none"),
        Companion("hugo", "Hugo", "Utangaç, dikkatli", "Shy, attentive", 150, "none"),
        Companion("toto", "Toto", "Sabırlı, kararlı", "Patient, determined", 200, "perfectLesson"),
        Companion("ollie", "Ollie", "Bilgili, sakin", "Knowledgeable, calm", 300, "sevenDays"),
        Companion("riko", "Riko", "Meraklı, yaramaz", "Curious, mischievous", 400, "none"),
        Companion("otti", "Otti", "Oyuncu, sosyal", "Playful, sociable", 500, "none"),
        Companion("bruno", "Bruno", "Güçlü, koruyucu", "Strong, protective", 650, "none"),
        Companion("gigi", "Gigi", "Nazik, dikkatli", "Gentle, attentive", 800, "none"),
        Companion("elly", "Elly", "Hafızası güçlü, bilge", "Great memory, wise", 1000, "none"),
        Companion("tigo", "Tigo", "Maceracı, kararlı", "Adventurous, determined", 1250, "none"),
        Companion("leo", "Leo", "Cesur, lider", "Brave, a leader", 1500, "finishedLevel"),
        Companion("nova", "Nova", "Efsanevi, büyülü", "Legendary, magical", 0, "allFriends")
    )
    val starters = all.take(6).map { it.id }.toSet()
    fun unlocked(e: CompanionEvidence, earned: Set<String>): Set<String> {
        val result = (earned.intersect(all.map { it.id }.toSet()) + starters).toMutableSet()
        result.remove("nova")
        all.filter { it.id != "nova" }.forEach { friend ->
            val task = when (friend.task) {
                "tenGames" -> e.games >= 10
                "perfectLesson" -> e.perfect
                "sevenDays" -> e.bestStreak >= 7
                "finishedLevel" -> e.level
                else -> true
            }
            if (e.words >= friend.words && task) result += friend.id
        }
        if (all.filter { it.id != "nova" }.all { it.id in result }) result += "nova"
        return result
    }
    fun evidence(completed: Set<String>, progress: GameProgress, perfect: Boolean, level: Boolean): CompanionEvidence {
        val dates = progress.days.filter { (_, day) -> day.newWords.isNotEmpty() || day.reviews.isNotEmpty() || day.sessions.isNotEmpty() }
            .keys.mapNotNull { runCatching { LocalDate.parse(it.substringAfter('|')) }.getOrNull() }.distinct().sorted()
        var best = 0; var streak = 0; var previous: LocalDate? = null
        dates.forEach { day -> streak = if (previous?.plusDays(1) == day) streak + 1 else 1; best = maxOf(best, streak); previous = day }
        return CompanionEvidence(completed.size, progress.days.values.sumOf { it.sessions.size }, best, perfect, level)
    }
    fun finishedLevel(pack: LearningPack?, completed: Set<String>): Boolean = pack?.words?.filter { it.difficulty != null }
        ?.groupBy { it.difficulty }?.values?.any { words -> words.isNotEmpty() && words.all { LearningRules.progressKey(pack.language, it.id) in completed } } ?: false
    fun requirement(friend: Companion, tr: Boolean): String {
        val task = when (friend.task) {
            "tenGames" -> InterfaceStrings.text(tr,"10 oyun tamamla","Complete 10 games")
            "perfectLesson" -> InterfaceStrings.text(tr,"Dinle ve Seç'i hatasız bitir","Finish Listen and Choose without mistakes")
            "sevenDays" -> InterfaceStrings.text(tr,"7 gün üst üste çalış","Learn 7 days in a row")
            "finishedLevel" -> InterfaceStrings.text(tr,"Bir seviyeyi tamamen bitir","Complete a learning level")
            "allFriends" -> InterfaceStrings.text(tr,"Diğer 20 arkadaşını aç","Unlock all 20 other friends")
            else -> ""
        }
        return listOfNotNull(if (friend.words > 0) "${friend.words} " + InterfaceStrings.text(tr,"kelime çalış","words studied") else null, task.takeIf { it.isNotBlank() }).joinToString(" • ")
    }
}
