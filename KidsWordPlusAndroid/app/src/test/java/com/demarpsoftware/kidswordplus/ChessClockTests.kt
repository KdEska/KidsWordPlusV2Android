package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.chess.*
import org.junit.Assert.*
import org.junit.Test

class ChessClockTests {
    @Test fun independentClocksSwitchAfterLegalMove() {
        val game = ChessGame(computer = false, clock = ChessClock.minutes(1))
        val spent = game.elapse(12500)
        assertEquals(47500L, spent.clock!!.whiteMillis)
        assertEquals(60000L, spent.clock.blackMillis)
        val moved = spent.play(ChessMove(square("e2"), square("e4")))!!.elapse(4000)
        assertEquals(47500L, moved.clock!!.whiteMillis)
        assertEquals(56000L, moved.clock.blackMillis)
        assertEquals(moved, moved.elapse(-1))
        assertEquals(moved, ChessGame.decode(moved.encode()))
    }
    @Test fun timeoutEndsGameBlocksPlayUndoAndCreditsOnce() {
        val game = ChessGame(computer = false, clock = ChessClock.minutes(1)).elapse(Long.MAX_VALUE)
        assertEquals(ChessOutcome.BLACK_WON, game.outcome)
        assertEquals(true, game.clock!!.timedOutWhite)
        assertNull(game.play(ChessMove(square("e2"), square("e4"))))
        assertNull(game.undo())
        assertEquals(game, game.elapse(1000))
        assertEquals(game, ChessGame.decode(game.encode()))
        assertEquals(1, ChessStatistics().record(game).record(game).games.size)
    }
    @Test fun undoDoesNotRefundTimeAndCustomBlackOriginWorks() {
        val origin = ChessCustomPosition.standard().copy(whiteTurn = false)
        val game = ChessGame(computer = false, position = origin.position, startFen = origin.fen(), clock = ChessClock.minutes(5))
        val moved = game.elapse(1200).play(ChessMove(square("e7"), square("e5")))!!.elapse(1500)
        val undone = moved.undo()!!
        assertEquals(origin.position, undone.position)
        assertEquals(moved.clock, undone.clock)
        assertEquals(undone, ChessGame.decode(undone.encode()))
        val ended = game.elapse(300000)
        assertEquals(ChessOutcome.WHITE_WON, ended.outcome)
        assertEquals(false, ended.clock!!.timedOutWhite)
    }
    @Test fun legacySavesStayUntimedAndInvalidClocksAreRejected() {
        val legacy = ChessGame()
        assertNull(ChessGame.decode(legacy.encode()).clock)
        assertNull(ChessClock.minutes(0))
        for (minutes in listOf(1,5,10,15,30,180)) assertEquals(minutes * 60000L, ChessClock.minutes(minutes)!!.limitMillis)
        assertThrows(IllegalArgumentException::class.java) { ChessClock.minutes(181) }
        assertThrows(IllegalArgumentException::class.java) { ChessClock(60000, -1, 60000) }
        assertThrows(IllegalArgumentException::class.java) { ChessClock(60000, 0, 0) }
        assertEquals("1:00", chessClockText(60000))
        assertEquals("0:01", chessClockText(1))
        assertEquals("0:00", chessClockText(0))
    }
    @Test fun clockTogglePreservesRemainingTimeAndSurvivesSave() {
        val game=ChessGame(computer=false,clock=ChessClock.minutes(5)).elapse(1200)
        val paused=game.configureClock(false,5)
        assertEquals(game.clock,paused.clock)
        assertEquals(paused,paused.elapse(8000))
        assertEquals(paused,ChessGame.decode(paused.encode()))
        assertEquals(game.clock,paused.configureClock(true,5).clock)
        val changed=paused.configureClock(true,10)
        assertEquals(600000L,changed.clock!!.whiteMillis)
        assertEquals(game.position,changed.position)
        assertEquals(game.history,changed.history)
    }
    @Test fun finishedClockCannotBeReopenedAndUndoKeepsDisabledClock() {
        val game=ChessGame(computer=false,clock=ChessClock.minutes(1))
        val ended=game.elapse(60000)
        assertEquals(ended,ended.configureClock(true,10))
        val moved=game.play(ChessMove(square("e2"),square("e4")))!!.configureClock(false,1)
        assertFalse(moved.undo()!!.clockEnabled)
    }
}
