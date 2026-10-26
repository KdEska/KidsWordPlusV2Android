package com.demarpsoftware.kidswordplus
import org.junit.Assert.*
import org.junit.Test
class LearningRulesTest {
    @Test fun completionIsIdempotentAndLanguageScoped() {
        val first = LearningRules.complete(emptySet(), "tr", "animals:1")
        assertEquals(first, LearningRules.complete(first, "tr", "animals:1"))
        assertEquals(2, LearningRules.complete(first, "en", "animals:1").size)
    }
}
