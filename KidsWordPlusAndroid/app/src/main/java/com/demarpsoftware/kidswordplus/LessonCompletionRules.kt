package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.content.Lesson

internal object LessonCompletionRules {
    fun following(lessons:List<Lesson>,current:Lesson):Lesson? {
        val index=lessons.indexOfFirst {it.id==current.id}
        return if(index>=0)lessons.getOrNull(index+1)else null
    }
    fun earnedStars(before:Int,after:Int)=(after-before).coerceAtLeast(0)
}
