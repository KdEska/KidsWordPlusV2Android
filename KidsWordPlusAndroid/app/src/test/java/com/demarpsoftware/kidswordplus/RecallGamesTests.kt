package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.games.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random
import java.time.LocalDate

class RecallGamesTests {
    @Test fun randomRoundsHaveExactlyFourUnambiguousChoices() {
        for(kind in RecallKind.entries)for(seed in 0..60) {
            assertNull(RecallRound.create(kind,listOf("a","b","c"),Random(seed)))
            val r=RecallRound.create(kind,(0..12).map{it.toString()},Random(seed))!!
            assertEquals(6,r.questions.size)
            assertEquals(6,r.questions.map{it.target}.distinct().size)
            r.questions.forEach{q->
                assertEquals(4,q.pictures.size)
                assertEquals(q.pictures.size,q.pictures.distinct().size)
                assertEquals(q.pictures.toSet(),q.answers.toSet())
                assertEquals(1,q.pictures.count{it==q.target})
            }
            assertEquals(4,RecallRound.create(kind,listOf("a","a","b","c","d"),Random(seed))!!.questions.last().pictures.size)
        }
    }
    @Test fun adultDrinkConceptsNeverEnterChildGameChoices() {
        for(kind in RecallKind.entries) {
            assertNull(RecallRound.create(kind,listOf("a","b","c","food:792","food:830")))
            val r=RecallRound.create(kind,listOf("a","b","c","d","food:792","food:830"))!!
            assertTrue(r.questions.flatMap{it.pictures}.all{it !in setOf("food:792","food:830")})
        }
    }
    @Test fun bagRequiresPromptAndRejectsWrongDropsUntilSixSolvedTasks() {
        var r=RecallRound.create(RecallKind.BAG,(0..9).map{it.toString()},Random(2))!!
        repeat(6) {i->
            val q=r.question!!
            assertEquals(r,r.answer(q.target));assertEquals(r,r.next())
            r=r.hear()
            assertEquals(r,r.answer("foreign"))
            val wrong=q.answers.first{it!=q.target}
            r=r.answer(wrong)
            assertEquals(setOf(wrong),r.wrong)
            assertEquals(r,r.answer(wrong))
            r=r.answer(q.target)
            assertEquals(RecallPhase.CORRECT,r.phase)
            assertEquals(r,r.answer(q.target))
            r=r.next()
            assertEquals(i==5,r.finished)
            if(i<5)assertFalse(r.heard)
        }
        assertEquals(6,r.reviewed.size)
        assertEquals(r,r.next())
    }
    @Test fun wagonSettingClampsToThreeThroughTwelveAndFillsExactlyThatMany() {
        for(requested in listOf(-1,3,4,6,11,12,99)) {
            val count=requested.coerceIn(3,12)
            var round=RecallRound.create(RecallKind.BAG,(0..20).map{it.toString()},Random(11),wagonCount=requested)!!
            assertEquals(count,round.questions.size)
            assertTrue(round.questions.all{it.answers.size==4 && it.pictures.size==4})
            repeat(count) {index->
                round=round.hear().answer(round.question!!.target).next()
                assertEquals(index==count-1,round.finished)
            }
            assertEquals(count,round.reviewed.size)
        }
    }
    @Test fun rotationSnapshotRetainsTaskChoicesAndPhase() {
        val r=RecallRound.create(RecallKind.BAG,(0..9).map{it.toString()},Random(8))!!.hear()
        val wrong=r.question!!.answers.first{it!=r.question!!.target}
        val current=r.answer(wrong)
        val bytes=java.io.ByteArrayOutputStream().apply{java.io.ObjectOutputStream(this).use{it.writeObject(current)}}.toByteArray()
        val restored=java.io.ObjectInputStream(java.io.ByteArrayInputStream(bytes)).use{it.readObject() as RecallRound}
        assertEquals(current,restored)
        assertEquals(RecallPhase.CORRECT,restored.answer(restored.question!!.target).phase)
    }
    @Test fun bagCreditsOnlyOncePerRound() {
        var r=RecallRound.create(RecallKind.BAG,(0..9).map{it.toString()},Random(3))!!
        repeat(6) {
            val target=r.question!!.target
            assertEquals(r,r.answer(target))
            r=r.hear();assertTrue(r.canAnswer)
            r=r.answer(target).next()
        }
        assertTrue(r.finished)
        val today=LocalDate.of(2026,10,23)
        val p=GameProgress().recallRound("tr",r.kind,r.id,r.reviewed,today)
        assertEquals(1,p.activity("tr",today).sessions.size)
        assertEquals(r.reviewed,p.activity("tr",today).reviews)
        assertEquals(p,p.recallRound("tr",r.kind,r.id,r.reviewed,today))
        assertEquals(p,p.recallRound("tr",r.kind,r.id,r.reviewed,today.plusDays(1)))
        assertTrue(p.activity("en",today).sessions.isEmpty())
        val restored=GameProgress.decode(p.encode())
        assertEquals(p,restored.recallRound("tr",r.kind,r.id,r.reviewed,today))
    }
}
