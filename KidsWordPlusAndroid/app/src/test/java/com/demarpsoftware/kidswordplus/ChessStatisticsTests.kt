package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.chess.*
import org.junit.Assert.*
import org.junit.Test

class ChessStatisticsTests {
    @Test fun completedGameKeepsPlayerColorAndRecordsOnlyOnce() {
        val position=ChessPosition.fromFen("7k/6Q1/5K2/8/8/8/8/8 b - - 0 1")
        val game=ChessGame(playerWhite=false,position=position)
        val statistics=ChessStatistics().record(game).record(game)
        assertEquals(1,statistics.games.size)
        assertEquals(false,statistics.games.single().playerWhite)
        assertEquals(statistics,ChessStatistics.decode(statistics.encode()))
    }
    @Test fun legacyRecordDoesNotInventPlayerColor() {
        val old="""{"version":1,"games":[{"id":"00000000-0000-0000-0000-000000000001","outcome":"WHITE_WON","plies":12,"computer":true}]}"""
        val decoded=ChessStatistics.decode(old)
        assertNull(decoded.games.single().playerWhite)
        assertEquals(decoded,ChessStatistics.decode(decoded.encode()))
    }
}
