package com.demarpsoftware.kidswordplus

// Language is part of the key: learning one language never completes another.
object LearningRules {
    fun progressKey(language: String, conceptId: String) = "$language|$conceptId"
    fun complete(previous: Set<String>, language: String, conceptId: String): Set<String> =
        previous + progressKey(language, conceptId)
}
