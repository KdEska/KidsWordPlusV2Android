package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.content.Lesson
import org.junit.Assert.*
import org.junit.Test

class LessonCompletionTests {
    private fun lesson(id:String,category:String="animals",topic:String="pets",number:Int=1)=Lesson(id,category,topic,number,emptyList())
    @Test fun nextLessonCrossesTopicsAndCategoriesInCatalogOrder() {
        val first=lesson("first",number=5)
        val next=lesson("next",number=7)
        val other=lesson("other",topic="wild")
        val otherCategory=lesson("other-category",category="home")
        val lessons=listOf(first,next,other,otherCategory)
        assertEquals(next,LessonCompletionRules.following(lessons,first))
        assertEquals(other,LessonCompletionRules.following(lessons,next))
        assertEquals(otherCategory,LessonCompletionRules.following(lessons,other))
        assertNull(LessonCompletionRules.following(lessons,otherCategory))
        assertNull(LessonCompletionRules.following(lessons,lesson("unknown")))
    }
    @Test fun celebrationOnlyReportsAnActualStarIncrease() {
        assertEquals(20,LessonCompletionRules.earnedStars(40,60))
        assertEquals(0,LessonCompletionRules.earnedStars(40,40))
        assertEquals(0,LessonCompletionRules.earnedStars(40,20))
    }
}
