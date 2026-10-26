package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.chess.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class ChessLessonRoundTests {
    @Test fun practiceProgressUsesTenSequentialBlocks() {
        var round=ChessLessonRound.create(Random(13))
        repeat(100) { index ->
            assertEquals(index/10+1,round.block)
            assertEquals(index%10+1,round.blockTask)
            round=round.finish(round.task.origin,round.task.target)
            assertEquals(index%10==9,round.blockFinished)
            round=ChessLessonRound.restore(round.save())!!
            if(index<99)round=round.advance()
        }
        assertTrue(round.finished)
    }
    @Test fun roundsCoverAllPiecesCaptureAndAvoidPreviousRound() {
        var previous=emptySet<String>()
        repeat(200) { seed ->
            val round=ChessLessonRound.create(Random(seed),previous)
            assertEquals(100,round.tasks.size)
            assertEquals(100,round.tasks.map { it.id }.toSet().size)
            assertEquals(PieceKind.entries.toSet(),round.tasks.map { it.kind }.toSet())
            assertTrue(round.tasks.any { it.capture })
            assertTrue(round.tasks.groupingBy { it.kind }.eachCount().values.all { it>0 })
            val fresh=ChessLessons.tasks.filter {it.id !in previous}
            if(fresh.size>=100 && PieceKind.entries.all {kind -> fresh.any {it.kind==kind}} && fresh.any {it.capture})
                assertTrue(round.tasks.none {it.id in previous})
            previous=round.tasks.map { it.id }.toSet()
        }
    }
    @Test fun cannotSkipOrFinishWithWrongMoveAndStopsAtHundred() {
        var round=ChessLessonRound.create(Random(7))
        repeat(100) { index ->
            assertEquals(index,round.completed)
            assertEquals(round,round.advance())
            assertEquals(round,round.finish(round.task.origin,round.task.origin))
            round=round.finish(round.task.origin,round.task.target)
            assertEquals(index+1,round.completed)
            assertEquals(round,round.finish(round.task.origin,round.task.target))
            if(index<99) round=round.advance()
        }
        assertTrue(round.finished)
        assertEquals(round,round.advance())
    }
    @Test fun roundRestoresExactTasksIndexAndSolvedState() {
        var round=ChessLessonRound.create(Random(42))
        repeat(4) {round=round.finish(round.task.origin,round.task.target).advance()}
        round=round.finish(round.task.origin,round.task.target)
        assertEquals(round,ChessLessonRound.restore(round.save()))
        assertNull(ChessLessonRound.restore(emptyList()))
        assertNull(ChessLessonRound.restore(round.save().toMutableList().apply {this[0]="100"}))
        assertNull(ChessLessonRound.restore(round.save().toMutableList().apply {this[2]="missing"}))
        assertNull(ChessLessonRound.restore(round.save().toMutableList().apply {this[3]=this[2]}))
    }
    @Test fun exhaustedOrUnbalancedFreshPoolFallsBackToFullLibrary() {
        assertEquals(100,ChessLessonRound.create(Random(1),ChessLessons.ids).tasks.size)
        val sparse=PieceKind.entries.flatMap {kind -> ChessLessons.tasks.filter {it.kind==kind}.take(if(kind==PieceKind.QUEEN)20 else 1) }.map {it.id}.toSet()
        assertEquals(100,ChessLessonRound.create(Random(2),ChessLessons.ids-sparse).tasks.size)
    }
    @Test fun expandingGoalsPreservesLegacyCompletionsAndValidatesVariants() {
        val legacy=ChessLessons.baseTasks.map { it.id }.toSet()
        assertTrue(ChessLessons.ids.containsAll(legacy))
        val variant=ChessLessons.tasks.first {it.id !in legacy}
        assertEquals(legacy,ChessLessons.finish(legacy,variant.id,variant.origin,variant.origin))
        assertEquals(legacy+variant.id,ChessLessons.finish(legacy,variant.id,variant.origin,variant.target))
    }
}
