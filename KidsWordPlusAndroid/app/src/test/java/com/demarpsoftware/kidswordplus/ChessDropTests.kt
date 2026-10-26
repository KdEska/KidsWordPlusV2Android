package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.chess.*
import org.junit.Assert.*
import org.junit.Test

class ChessDropTests {
    @Test fun everySquareMapsInBothOrientationsAndSizes() {
        for(size in listOf(320f,974f,1680f)) for(index in 0..63) {
            val x=(index%8+.5f)*size/8
            val y=(index/8+.5f)*size/8
            assertEquals(index,chessDropSquare(x,y,size,false))
            assertEquals(63-index,chessDropSquare(x,y,size,true))
        }
    }
    @Test fun outsideAndInvalidCoordinatesCancelRatherThanClamp() {
        for(flipped in listOf(false,true)) {
            for((x,y) in listOf(-1f to 20f,20f to -1f,800f to 20f,20f to 800f,Float.NaN to 20f,20f to Float.POSITIVE_INFINITY))
                assertNull(chessDropSquare(x,y,800f,flipped))
            assertNull(chessDropSquare(0f,0f,0f,flipped))
            assertEquals(if(flipped)63 else 0,chessDropSquare(0f,0f,800f,flipped))
        }
    }
}
