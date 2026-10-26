package com.demarpsoftware.kidswordplus.games

import kotlin.random.Random
import java.util.UUID

enum class RecallKind { BAG }
enum class RecallPhase { PICK, CORRECT, COMPLETE }
data class RecallQuestion(val target:String,val pictures:List<String>,val answers:List<String>):java.io.Serializable
data class RecallRound(val kind:RecallKind,val questions:List<RecallQuestion>,val id:String=UUID.randomUUID().toString(),
    val index:Int=0,val phase:RecallPhase=RecallPhase.PICK,
    val heard:Boolean=false,val wrong:Set<String> = emptySet(),val reviewed:Set<String> = emptySet()):java.io.Serializable {
    val question get()=questions.getOrNull(index)
    val finished get()=phase==RecallPhase.COMPLETE
    val canAnswer get()=phase==RecallPhase.PICK && heard
    fun hear()=if(phase==RecallPhase.PICK)copy(heard=true)else this
    fun answer(word:String):RecallRound {
        val q=question ?: return this
        if(!canAnswer || word !in q.answers || word in wrong)return this
        return if(word==q.target)copy(phase=RecallPhase.CORRECT,reviewed=reviewed+word) else copy(wrong=wrong+word)
    }
    fun next():RecallRound {
        if(phase!=RecallPhase.CORRECT)return this
        return if(index==questions.lastIndex)copy(phase=RecallPhase.COMPLETE) else copy(index=index+1,
            phase=RecallPhase.PICK,heard=false,wrong=emptySet())
    }
    companion object {
        fun create(kind:RecallKind,ids:List<String>,random:Random=Random.Default,wagonCount:Int=6):RecallRound? {
            // Shared concept IDs identify alcoholic drinks independently of learning language.
            val pool=ids.distinct().filter{it !in setOf("food:792","food:830")}.shuffled(random)
            if(pool.size<4)return null
            val questions=(0 until wagonCount.coerceIn(3,12)).map { i ->
                val target=pool[i%pool.size]
                val count=4
                val pictures=(listOf(target)+pool.filter{it!=target}.shuffled(random).take(count-1)).shuffled(random)
                RecallQuestion(target,pictures,pictures.shuffled(random))
            }
            return RecallRound(kind,questions)
        }
    }
}
