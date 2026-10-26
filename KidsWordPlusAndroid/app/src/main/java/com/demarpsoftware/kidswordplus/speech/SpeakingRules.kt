package com.demarpsoftware.kidswordplus.speech

import java.text.Normalizer
import java.util.Locale

internal object SpeakingRules {
    val locales = mapOf("tr" to "tr-TR", "en" to "en-US", "fr" to "fr-FR", "de" to "de-DE",
        "es" to "es-MX", "pt" to "pt-BR", "zh" to "zh-CN", "ja" to "ja-JP",
        "ko" to "ko-KR", "it" to "it-IT", "hi" to "hi-IN", "ar" to "ar-SA", "ru" to "ru-RU")
    fun normalized(text: String, language: String): String {
        val lower = Normalizer.normalize(text, Normalizer.Form.NFC).lowercase(Locale.forLanguageTag(locales[language] ?: language))
        val value = buildString {
            lower.forEach { char ->
                when {
                    language == "ar" && (char.code in 0x064B..0x0652 || char.code == 0x0670 || char.code == 0x0640) -> Unit
                    language == "ja" && char.code in 0x3041..0x3096 -> append((char.code + 0x60).toChar())
                    char.isLetterOrDigit() || (language == "hi" && Character.getType(char) in listOf(Character.NON_SPACING_MARK.toInt(), Character.COMBINING_SPACING_MARK.toInt(), Character.ENCLOSING_MARK.toInt())) -> append(char)
                    else -> append(' ')
                }
            }
        }
        return value.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.joinToString(if (language in listOf("ja","zh")) "" else " ")
    }
    fun matches(heard: String, expected: String, language: String): Boolean {
        val target = normalized(expected, language)
        return target.isNotEmpty() && normalized(heard, language) == target
    }
}
