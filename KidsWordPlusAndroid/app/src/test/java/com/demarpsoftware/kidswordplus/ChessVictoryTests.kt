package com.demarpsoftware.kidswordplus

import org.junit.Assert.*
import org.junit.Test

class ChessVictoryTests {
    @Test fun restoringAWonGameNeverCelebratesEvenAfterReentry() {
        val seen=mutableSetOf<String>()
        val gate=ChessVictoryGate()
        assertFalse(gate.observe("old","WON",true,true,seen))
        assertFalse(ChessVictoryGate().observe("old","WON",true,true,seen))
        assertEquals(setOf("old"),seen)
    }
    @Test fun aNewVisibleVictoryCelebratesExactlyOnceAcrossUndoAndRecreation() {
        val seen=mutableSetOf<String>()
        val gate=ChessVictoryGate()
        assertFalse(gate.observe("live",null,false,true,seen))
        assertTrue(gate.observe("live","WON",true,true,seen))
        assertFalse(gate.observe("live","WON",true,true,seen))
        assertFalse(gate.observe("live",null,false,true,seen))
        assertFalse(gate.observe("live","WON",true,true,seen))
        assertFalse(ChessVictoryGate().observe("live","WON",true,true,seen))
    }
    @Test fun menusAndLosingResultsDoNotCelebrate() {
        val seen=mutableSetOf<String>();val gate=ChessVictoryGate()
        gate.observe("menu",null,false,false,seen)
        assertFalse(gate.observe("menu","WON",true,false,seen))
        assertFalse(gate.observe("loss",null,false,true,seen))
        assertFalse(gate.observe("loss","LOST",false,true,seen))
        assertFalse(gate.observe("draw","DRAW",false,true,seen))
    }
    @Test fun aDifferentNewGameCanCelebrateAfterAnOldGame() {
        val seen=mutableSetOf<String>();val gate=ChessVictoryGate()
        gate.observe("old","WON",true,true,seen)
        gate.observe("new",null,false,true,seen)
        assertTrue(gate.observe("new","WON",true,true,seen))
        assertFalse(gate.observe("restored","WON",true,true,seen))
    }
}
