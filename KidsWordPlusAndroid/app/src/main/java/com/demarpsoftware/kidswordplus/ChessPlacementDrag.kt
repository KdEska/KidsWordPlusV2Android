package com.demarpsoftware.kidswordplus

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import com.demarpsoftware.kidswordplus.chess.Piece
import com.demarpsoftware.kidswordplus.chess.chessDropSquare

internal class ChessPlacementDrag {
    var bounds:Rect?=null
    var hovered by mutableStateOf<Int?>(null)
    var onDrop:(Piece,Int)->Unit={_,_->}
    fun target(point:Offset):Int?=bounds?.takeIf{it.contains(point)}?.let{chessDropSquare(point.x-it.left,point.y-it.top,it.width,false)}
}
internal fun Modifier.chessPlacementSource(state:ChessPlacementDrag?,piece:Piece,enabled:Boolean,choose:()->Unit)=composed {
    if(state==null||!enabled)return@composed this
    var origin by remember {mutableStateOf(Offset.Zero)}
    val latestChoose by rememberUpdatedState(choose)
    onGloballyPositioned{origin=it.boundsInRoot().topLeft}.pointerInput(state,piece,enabled) {
        var point=Offset.Zero
        detectDragGesturesAfterLongPress(onDragStart={point=origin+it;latestChoose();state.hovered=state.target(point)},
            onDrag={change,amount->change.consume();point+=amount;state.hovered=state.target(point)},
            onDragCancel={state.hovered=null},onDragEnd={val target=state.target(point);state.hovered=null;if(target!=null)state.onDrop(piece,target)})
    }
}
