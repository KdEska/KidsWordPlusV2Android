package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.chess.*
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class ChessFirstStepsTests {
    @Test fun generatedRoundsAreProgressiveLegalSafeAndNeverRepeatPreviousRound() {
        var previous=emptySet<String>()
        val seen=mutableSetOf<String>()
        repeat(60) {seed ->
            val moves=ChessGuideGenerator.generate(Random(seed),previous)
            assertEquals(36,moves.size)
            val ids=moves.map{it.signature}.toSet()
            assertEquals(36,ids.size)
            assertTrue(ids.intersect(previous).isEmpty())
            assertEquals((0..2).flatMap{stage->List(12){stage}},moves.map{it.stage})
            for(stage in 0..2) {
                assertEquals(PieceKind.entries.toSet(),moves.filter{it.stage==stage}.map{it.piece}.toSet())
            }
            moves.forEachIndexed {index,task ->
                val position=ChessPosition(task.board,castling=0)
                assertEquals(Piece(task.piece,true),task.board[task.from])
                assertFalse(position.inCheck(true))
                assertTrue(position.movesFrom(task.from).any{it.to==task.to})
                val after=ChessPosition(task.board.toMutableList().apply{this[task.from]=null;this[task.to]=Piece(task.piece,true)},castling=0)
                assertFalse(after.attacked(task.to,false))
                assertEquals(if(task.stage==2)false else null,task.board[task.to]?.white)
                val count=task.board.count{it?.white==false}
                assertEquals(when(task.stage){0->0;1->if(index%12<6)1 else 3;else->if(index%12<6)1 else 4},count)
                assertFalse(task.board.any{it==Piece(PieceKind.KING,false)})
                assertTrue(task.board.indices.filter{task.board[it]?.kind==PieceKind.PAWN}.all{it/8 in 1..6})
                if(task.piece==PieceKind.PAWN && task.stage==2)assertEquals(1,kotlin.math.abs(task.to%8-task.from%8))
            }
            previous=ids;seen.addAll(ids)
        }
        assertTrue(seen.size>1000)
    }
    @Test fun everyMoveRequiresSelectionAndOnlyAcceptsItsTargetAndSupportsBack() {
        val moves=ChessGuideGenerator.generate(Random(42))
        var exercise=ChessFirstSteps(moves)
        assertEquals(exercise,exercise.tap(moves.first().from))
        exercise=exercise.begin()
        moves.forEach {move ->
            assertTrue(exercise.selecting)
            assertEquals(exercise,exercise.tap(move.to))
            exercise=exercise.tap(move.from)
            assertEquals(setOf(move.to),exercise.targets)
            assertEquals(exercise,exercise.tap(move.from))
            val wrong=(0..63).first{it!=move.to}
            assertEquals(exercise,exercise.tap(wrong))
            exercise=exercise.tap(move.to)
        }
        assertTrue(exercise.complete)
        assertTrue(exercise.targets.isEmpty())
        assertEquals(exercise,exercise.tap(moves.last().to))
        assertTrue(exercise.back().tap(moves.last().to).complete)
        repeat(exercise.finalStep){exercise=exercise.back()}
        assertEquals(ChessFirstSteps(moves),exercise)
        assertEquals(exercise,exercise.back())
    }
}
