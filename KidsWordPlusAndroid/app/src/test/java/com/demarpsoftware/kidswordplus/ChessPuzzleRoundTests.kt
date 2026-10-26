package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.chess.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class ChessPuzzleRoundTests {
    private val pools get()=listOf(ChessSafety.pool,ChessPuzzles.endingPool,ChessPuzzles.matePool)
    @Test fun tenTaskBlocksUnlockSequentiallyAndResumeAtBoundary() {
        for(pool in pools) {
            var round=ChessPuzzleRound.create(pool,random=Random(77))
            repeat(100) { index ->
                assertEquals(index/10+1,round.block)
                assertEquals(index%10+1,round.blockTask)
                assertEquals(index%10,round.blockCompleted)
                assertFalse(round.blockFinished)
                round=round.finish(round.taskId)
                assertEquals(index%10==9,round.blockFinished)
                round=ChessPuzzleRound.restore(round.save(),pool)!!
                if(index<99)round=round.advance()
            }
            assertTrue(round.finished)
        }
    }
    @Test fun everyPoolBuildsBalancedUniqueRoundsAndAvoidsPreviousRound() {
        for(pool in pools) {
            var previous=emptySet<String>()
            repeat(100) {seed ->
                val round=ChessPuzzleRound.create(pool,previous,Random(seed))
                assertEquals(100,round.ids.size)
                assertEquals(100,round.ids.toSet().size)
                val fresh=pool.filter {it.first !in previous}.groupingBy {it.second}.eachCount()
                val minimum=(100+pool.map {it.second}.toSet().size-1)/pool.map {it.second}.toSet().size
                if(pool.all {fresh.getOrDefault(it.second,0)>=minimum})assertTrue(round.ids.none {it in previous})
                val groups=round.ids.map {pool.toMap().getValue(it)}.groupingBy {it}.eachCount()
                assertEquals(pool.map {it.second}.toSet(),groups.keys)
                assertTrue(groups.values.max()-groups.values.min()<=1)
                previous=round.ids.toSet()
            }
        }
    }
    @Test fun cannotSkipAndCompletedRoundStopsAtHundred() {
        var round=ChessPuzzleRound.create(ChessPuzzles.endingPool,random=Random(12))
        repeat(100) {index ->
            assertEquals(index,round.completed)
            assertEquals(round,round.advance())
            assertEquals(round,round.finish("wrong"))
            round=round.finish(round.taskId)
            assertEquals(index+1,round.completed)
            assertEquals(round,round.finish(round.taskId))
            if(index<99)round=round.advance()
        }
        assertTrue(round.finished)
        assertEquals(round,round.advance())
    }
    @Test fun saveRestoresExactOrderAndProgressAndRejectsInvalidIds() {
        for(pool in pools) {
            var round=ChessPuzzleRound.create(pool,random=Random(7))
            repeat(3) {round=round.finish(round.taskId).advance()}
            round=round.finish(round.taskId)
            assertEquals(round,ChessPuzzleRound.restore(round.save(),pool))
            assertNull(ChessPuzzleRound.restore(round.save().toMutableList().apply {this[0]="100"},pool))
            assertNull(ChessPuzzleRound.restore(round.save().toMutableList().apply {this[2]="unknown"},pool))
            assertNull(ChessPuzzleRound.restore(round.save().toMutableList().apply {this[3]=this[2]},pool))
            assertEquals(100,ChessPuzzleRound.create(pool,pool.map {it.first}.toSet(),Random(9)).ids.size)
        }
    }
    @Test fun generatedCatalogsAreUniqueAndEveryGoalIsGenuine() {
        assertEquals(240,ChessPuzzles.endings.size)
        assertEquals(240,ChessPuzzles.mates.size)
        assertEquals(480,ChessPuzzles.ids.size)
        for(kind in ChessEnding.entries)assertEquals(80,ChessPuzzles.endings.count {it.answer==kind})
        for(task in ChessPuzzles.endings) {
            assertFalse(task.position.inCheck(false))
            assertTrue(task.position.inCheck(true) || task.position.legalMoves().isEmpty())
            assertTrue(task.accepts(task.answer))
        }
        for(task in ChessPuzzles.mates) {
            assertFalse(task.position.inCheck(true))
            assertFalse(task.position.inCheck(false))
            assertTrue(task.id,task.accepts(task.referenceMove!!))
            val after=task.position.play(task.referenceMove!!)!!
            assertTrue(after.inCheck(false))
            assertTrue(after.legalMoves().isEmpty())
        }
        assertEquals(ChessSafety.tasks.size,ChessSafety.ids.size)
    }
    @Test fun eachPageRandomizesItsHundredGoalsAndPreservesTheirOrderOnRestore() {
        for(pool in pools) {
            val a=ChessPuzzleRound.create(pool,random=Random(1))
            val b=ChessPuzzleRound.create(pool,random=Random(2))
            assertNotEquals(a.ids,b.ids)
            assertEquals(a,ChessPuzzleRound.restore(a.save(),pool))
        }
        val a=ChessLessonRound.create(Random(1))
        val b=ChessLessonRound.create(Random(2))
        assertNotEquals(a.tasks.map {it.id},b.tasks.map {it.id})
        assertEquals(a,ChessLessonRound.restore(a.save()))
    }
    @Test fun allLegacyCompletionIdsSurviveCatalogExpansion() {
        assertTrue(ChessSafety.ids.containsAll(setOf("threat","escape","capture","block")))
        assertTrue(ChessPuzzles.ids.containsAll(setOf("ending.1","ending.2","ending.3","mate.queen.left","mate.queen.right","mate.rook")))
    }
}
