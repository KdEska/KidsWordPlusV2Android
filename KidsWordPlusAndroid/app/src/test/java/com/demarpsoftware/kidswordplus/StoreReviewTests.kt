package com.demarpsoftware.kidswordplus

import org.junit.Assert.*
import org.junit.Test

class StoreReviewTests {
    private val day=ReviewHistory.DAY
    private fun ready()=ReviewHistory(0).observe(1,0).observe(3,day).observe(5,2*day)
    @Test fun waitsForExperienceAndThreeDistinctCompletionDays() {
        assertFalse(ready().eligible(6*day,"1"))
        assertTrue(ready().eligible(7*day,"1"))
        assertFalse(ReviewHistory(0).observe(5,0).eligible(7*day,"1"))
        assertFalse(ReviewHistory(0).observe(1,0).observe(2,day).observe(4,2*day).eligible(7*day,"1"))
    }
    @Test fun requiresNewActivityCooldownAndNewVersion() {
        val offered=ready().reserve(7*day,"1")
        assertFalse(offered.eligible(127*day,"2"))
        val engaged=offered.observe(10,126*day)
        assertFalse(engaged.eligible(126*day,"2"))
        assertTrue(engaged.eligible(127*day,"2"))
        assertFalse(engaged.eligible(500*day,"1"))
        assertFalse(engaged.eligible(6*day,"2"))
    }
    @Test fun annualCapIsRollingAndPersisted() {
        val capped=ready().reserve(7*day,"1").observe(10,127*day).reserve(127*day,"2")
            .observe(15,247*day).reserve(247*day,"3").observe(20,367*day)
        assertFalse(capped.eligible(367*day,"4"))
        assertTrue(capped.eligible(373*day,"4"))
        assertEquals(capped,ReviewHistory.decode(capped.encode()))
    }
    @Test fun repeatedResultViewsDoNotCreateEngagement() {
        val original=ready()
        assertEquals(original,original.observe(5,50*day))
        assertEquals(original,original.observe(3,50*day))
        assertEquals(3,original.days.size)
    }
}
