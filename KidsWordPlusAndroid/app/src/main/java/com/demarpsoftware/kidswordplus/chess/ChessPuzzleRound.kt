package com.demarpsoftware.kidswordplus.chess

import kotlin.random.Random

/** Round-local progress is separate from lifetime completion records. */
@ConsistentCopyVisibility
data class ChessPuzzleRound private constructor(val ids: List<String>,val index: Int=0,val solved: Boolean=false) {
    val block get()=index/10+1
    val blockTask get()=index%10+1
    val blockCompleted get()=index%10+if(solved)1 else 0
    val blockFinished get()=solved && blockCompleted==10
    val taskId get()=ids[index]
    val completed get()=index+if(solved)1 else 0
    val finished get()=completed==ids.size
    fun finish(id: String)=if(id==taskId)copy(solved=true)else this
    fun advance()=if(solved && !finished)copy(index=index+1,solved=false)else this
    fun save()=listOf(index.toString(),solved.toString())+ids
    companion object {
        const val COUNT=100
        fun create(pool: List<Pair<String,String>>,excluding: Set<String> = emptySet(),random: Random=Random.Default): ChessPuzzleRound {
            require(pool.size>=COUNT && pool.map {it.first}.toSet().size==pool.size)
            val fresh=pool.filter {it.first !in excluding}
            val minimumPerGroup=(COUNT+pool.map {it.second}.toSet().size-1)/pool.map {it.second}.toSet().size
            val freshCounts=fresh.groupingBy {it.second}.eachCount()
            val available=if(fresh.size>=COUNT && pool.all {freshCounts.getOrDefault(it.second,0)>=minimumPerGroup})fresh else pool
            val groups=available.groupBy {it.second}.mapValues {(_,items)->items.shuffled(random).toMutableList()}
            val chosen=mutableListOf<String>()
            while(chosen.size<COUNT) {
                for(group in groups.keys.sorted().shuffled(random)) {
                    val items=groups.getValue(group)
                    if(items.isNotEmpty())chosen+=items.removeAt(items.lastIndex).first
                    if(chosen.size==COUNT)break
                }
            }
            return ChessPuzzleRound(chosen.shuffled(random))
        }
        fun restore(saved: List<String>,pool: List<Pair<String,String>>): ChessPuzzleRound? {
            if(saved.size!=COUNT+2)return null
            val index=saved[0].toIntOrNull()?.takeIf {it in 0 until COUNT} ?: return null
            val solved=saved[1].toBooleanStrictOrNull() ?: return null
            val ids=saved.drop(2)
            if(ids.toSet().size!=COUNT || !pool.map {it.first}.toSet().containsAll(ids))return null
            return ChessPuzzleRound(ids,index,solved)
        }
    }
}
