package com.demarpsoftware.kidswordplus

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.composed
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/** In-app edge swipe also works when Android uses three-button navigation. */
internal fun Modifier.edgeBackGesture(enabled: Boolean, back: () -> Unit): Modifier = composed {
    val latestBack = rememberUpdatedState(back)
    val density = LocalDensity.current
    val edge = with(density) { 48.dp.toPx() }
    val distance = with(density) { 72.dp.toPx() }
    pointerInput(enabled, edge, distance) {
        if (!enabled) return@pointerInput
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            if (down.position.x > edge) return@awaitEachGesture
            var claimed = false
            var rejected = false
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (event.changes.any { it.id != down.id && it.pressed }) rejected = true
                val dx = change.position.x - down.position.x
                val dy = abs(change.position.y - down.position.y)
                if (!claimed && !rejected) {
                    if (dy > viewConfiguration.touchSlop && dy >= abs(dx)) rejected = true
                    else if (dx < -viewConfiguration.touchSlop) rejected = true
                    else if (dx > viewConfiguration.touchSlop && dx > dy * 1.5f) claimed = true
                }
                if (claimed && !rejected) change.consume()
                if (!change.pressed) {
                    if (claimed && !rejected && dx >= distance && dx > dy * 1.5f) latestBack.value()
                    break
                }
            }
        }
    }
}
