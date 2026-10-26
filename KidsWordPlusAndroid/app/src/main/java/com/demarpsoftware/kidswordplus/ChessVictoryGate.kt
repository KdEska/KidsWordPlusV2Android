package com.demarpsoftware.kidswordplus

/** Restored results are consumed silently; only a visible ongoing game becoming won celebrates. */
internal class ChessVictoryGate {
    private var observedID:String?=null
    private var observedOutcome:String?=null
    fun observe(id:String?,outcome:String?,won:Boolean,visible:Boolean,consumed:MutableSet<String>):Boolean {
        val fresh=visible && id!=null && observedID==id && observedOutcome==null && won
        observedID=id;observedOutcome=outcome
        return if(won && id!=null)consumed.add(id) && fresh else false
    }
}
