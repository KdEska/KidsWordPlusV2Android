package com.demarpsoftware.kidswordplus.games

import com.demarpsoftware.kidswordplus.LearningRules
import com.demarpsoftware.kidswordplus.content.LearningPack
import com.demarpsoftware.kidswordplus.content.PackageSafety
import com.demarpsoftware.kidswordplus.content.Word
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

enum class GameKind { LISTENING, REVIEW, MATCHING, MEMORY }
data class Question(val target: Word, val options: List<Word>)
data class GameSession(
    val id: String = UUID.randomUUID().toString(), val language: String, val kind: GameKind,
    val words: List<Word>, val questions: List<Question> = emptyList(), val cards: List<Word> = emptyList(),
    val index: Int = 0, val heard: Boolean = false, val selected: String? = null,
    val firstAnswers: Map<String, Boolean> = emptyMap(), val wrong: Set<String> = emptySet(),
    val matched: Set<String> = emptySet(), val faceUp: List<Int> = emptyList(), val matchedCards: Set<Int> = emptySet(),
    val attempts: Int = 0, val finished: Boolean = false
) {
    val target: Word? get() = questions.getOrNull(index)?.target
    val solved: Boolean get() = target?.id in matched
    val waiting: Boolean get() = faceUp.size == 2
    fun hear(wordId: String): GameSession {
        if (finished) return this
        return when (kind) {
            GameKind.LISTENING, GameKind.REVIEW -> if (wordId == target?.id) copy(heard = true) else this
            GameKind.MATCHING -> if (words.any { it.id == wordId } && wordId !in matched) copy(selected = wordId, wrong = emptySet()) else this
            else -> this
        }
    }
    fun answer(picture: String): GameSession {
        if (finished) return this
        val sound = when (kind) {
            GameKind.LISTENING, GameKind.REVIEW -> if (heard && !solved) target?.id else null
            GameKind.MATCHING -> selected
            else -> null
        } ?: return this
        val options = if (kind == GameKind.MATCHING) words else questions[index].options
        if (options.none { it.id == picture } || (kind == GameKind.MATCHING && picture in matched) || picture in wrong) return this
        val correct = sound == picture
        val first = if (sound in firstAnswers) firstAnswers else firstAnswers + (sound to correct)
        val nextMatched = if (correct) matched + sound else matched
        return copy(firstAnswers = first, matched = nextMatched, wrong = if (correct) emptySet() else wrong + picture,
            selected = if (correct) null else selected, finished = kind == GameKind.MATCHING && nextMatched.size == words.size)
    }
    fun next(): GameSession {
        if (kind !in listOf(GameKind.LISTENING, GameKind.REVIEW) || finished || !solved) return this
        return if (index == questions.lastIndex) copy(finished = true)
            else copy(index = index + 1, heard = false, wrong = emptySet())
    }
    fun flip(position: Int): GameSession {
        if (kind != GameKind.MEMORY || finished || waiting || position !in cards.indices || position in faceUp || position in matchedCards) return this
        val revealed = faceUp + position
        if (revealed.size == 1) return copy(faceUp = revealed)
        val same = cards[revealed[0]].id == cards[revealed[1]].id
        val matched = if (same) matchedCards + revealed else matchedCards
        return copy(faceUp = if (same) emptyList() else revealed, matchedCards = matched, attempts = attempts + 1,
            finished = matched.size == cards.size)
    }
    fun hide(): GameSession = if (waiting) copy(faceUp = emptyList()) else this
}
object GameRules {
    // The same sound/image/text must never appear as two different answer choices.
    fun uniqueWords(pack: LearningPack, completed: Set<String>, targets: Set<String>? = null, random: Random = Random.Default): List<Word> {
        val candidates = pack.words.filter { LearningRules.progressKey(pack.language, it.id) in completed }.shuffled(random)
        val ordered = if (targets == null) candidates else candidates.filter { it.id in targets } + candidates.filter { it.id !in targets }
        val images = HashSet<String>(); val audio = HashSet<String>(); val texts = HashSet<String>()
        val result = ArrayList<Word>()
        for (word in ordered) {
            val signatures = runCatching { Triple(PackageSafety.sha(pack.image(word)), PackageSafety.sha(pack.audio(word)), word.text.lowercase(Locale.forLanguageTag(pack.language))) }.getOrNull() ?: continue
            if (signatures.first in images || signatures.second in audio || signatures.third in texts) continue
            images += signatures.first; audio += signatures.second; texts += signatures.third; result += word
            if (result.size == 40) break
        }
        return result
    }
    fun start(language: String, kind: GameKind, pool: List<Word>, count: Int, targets: Set<String>? = null, random: Random = Random.Default): GameSession? {
        val shuffled = pool.shuffled(random)
        return when (kind) {
            GameKind.LISTENING, GameKind.REVIEW -> {
                if (pool.size < 4) return null
                val chosen = (if (kind == GameKind.REVIEW) shuffled.filter { it.id in targets.orEmpty() } else shuffled).take(10)
                if (chosen.isEmpty()) return null
                GameSession(language = language, kind = kind, words = chosen, questions = chosen.map { target ->
                    Question(target, (listOf(target) + shuffled.filter { it.id != target.id }.shuffled(random).take(3)).shuffled(random))
                })
            }
            GameKind.MATCHING -> if (count in 2..4 && shuffled.size >= count) GameSession(language = language, kind = kind, words = shuffled.take(count), cards = shuffled.take(count).shuffled(random)) else null
            GameKind.MEMORY -> if (count in listOf(2, 4, 6, 8) && shuffled.size >= count) {
                val words = shuffled.take(count)
                GameSession(language = language, kind = kind, words = words, cards = (words + words).shuffled(random))
            } else null
        }
    }
}
