package com.demarpsoftware.kidswordplus

import org.junit.Assert.*
import org.junit.Test

class FamilyShareChallengeTests {
    @Test fun onlyCompleteNumericAnswersUnlockIncludingUnicodeDigits() {
        val challenge=FamilyShareChallenge(12,6)
        assertTrue(challenge.accepts("72"))
        assertTrue(challenge.accepts("٧٢"))
        assertTrue(challenge.accepts("７２"))
        for(answer in listOf("","7","73","-72","72x","72 ","7.2","999999")) assertFalse(answer,challenge.accepts(answer))
    }
    @Test fun randomizedChallengesRemainInReferenceRanges() {
        repeat(100) {
            val challenge=FamilyShareChallenge.create()
            assertTrue(challenge.left in 12..19)
            assertTrue(challenge.right in 6..9)
            assertTrue(challenge.accepts((challenge.left*challenge.right).toString()))
        }
    }
}
