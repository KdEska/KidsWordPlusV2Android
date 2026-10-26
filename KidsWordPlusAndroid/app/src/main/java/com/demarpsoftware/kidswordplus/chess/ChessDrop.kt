package com.demarpsoftware.kidswordplus.chess

/** Maps board-local pointer coordinates; outside drops never become edge moves. */
fun chessDropSquare(x: Float, y: Float, size: Float, flipped: Boolean): Int? {
    if (!size.isFinite() || size <= 0 || !x.isFinite() || !y.isFinite() || x < 0 || y < 0 || x >= size || y >= size) return null
    val index=(y / size * 8).toInt() * 8 + (x / size * 8).toInt()
    return if (flipped) 63-index else index
}
