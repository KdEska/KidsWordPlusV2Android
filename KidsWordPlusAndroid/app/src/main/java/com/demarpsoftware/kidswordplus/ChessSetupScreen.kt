package com.demarpsoftware.kidswordplus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.demarpsoftware.kidswordplus.chess.*

@Composable internal fun ChessSetupScreen(state:AppState,model:AppModel,tablet:Boolean,back:()->Unit) {
    val tr=state.ui=="tr"
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    val next=ChessBoardSetup.placements.getOrNull(state.chessSetupCount)
    var chosen by rememberSaveable {mutableStateOf(next?.second?.kind?.name ?: PieceKind.ROOK.name)}
    var feedback by rememberSaveable {mutableIntStateOf(0)}
    val audio=LocalChessAudio.current
    val placement=remember {ChessPlacementDrag()}
    fun place(kind:PieceKind,square:Int) {
        if(state.busy||next==null)return
        if(ChessBoardSetup.place(state.chessSetupCount,kind,square)==state.chessSetupCount)feedback=2
        else {feedback=1;model.placeChessPiece(state.chessSetupCount,kind,square)}
    }
    SideEffect {placement.onDrop={piece,square ->if(piece.white==next?.second?.white)place(piece.kind,square)}}
    LaunchedEffect(next?.second?.white){if(next!=null)chosen=next.second.kind.name}
    PageHelp("setup");ScreenTitle(t("Tahtayı Hazırla","Set Up the Board"))
    ChessStopSpeechOnChange(state.chessSetupCount,state.ui)
    ChessPageNavigation(back,reset={if(!state.busy){audio?.stop();chosen=PieceKind.ROOK.name;feedback=0;model.restartChessSetup()}})
    val complete=t("Tahta hazır! İki tarafta da 16 taş var. Oyuna beyaz başlar.","The board is ready! Each side has 16 pieces. White moves first.")
    val instruction=next?.let{InterfaceStrings.named(tr,"{piece} taşını {square} karesine yerleştir.","Place the {piece} on {square}.",mapOf("piece" to pieceTitle(it.second.kind,tr),"square" to squareName(it.first)))} ?: complete
    Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.TopCenter) {
        Column(Modifier.widthIn(max=if(tablet)820.dp else 600.dp).fillMaxWidth().verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                Text(if(next==null)t("✓ Tamamlandı","✓ Done")else if(next.second.white)"○ "+t("Beyaz taşlar","White pieces")else "● "+t("Siyah taşlar","Black pieces"),style=MaterialTheme.typography.titleMedium)
                Text("${state.chessSetupCount} / 32",style=MaterialTheme.typography.titleMedium)
            }
            LinearProgressIndicator(progress={state.chessSetupCount/32f},modifier=Modifier.fillMaxWidth(),color=KidHeading,drawStopIndicator={})
            if(next!=null)Text(chessDragInstruction(tr),style=MaterialTheme.typography.bodySmall,textAlign=TextAlign.Center)
            KidSurface {
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    Text(instruction,Modifier.weight(1f),style=MaterialTheme.typography.titleMedium)
                    FilledIconButton(onClick={if(next==null)audio?.playKey(state.ui,"setup.complete")else audio?.playSetup(state.ui,next.second.kind,next.first)},modifier=Modifier.size(48.dp).semantics{contentDescription=t("Dinle","Listen")},colors=IconButtonDefaults.filledIconButtonColors(containerColor=Color(0xFF1966B4))) {WordAudioIndicator(audio?.playing==true)}
                }
            }
            if(next!=null)ChessPieceStrip(PieceKind.valueOf(chosen),next.second.white,tr,!state.busy,placement){chosen=it.name}
            ChessAudioStatus(state.ui)
            ChessAppearancePicker(tr)
            val board=ChessBoardSetup.board(state.chessSetupCount)
            IosChessBoard(board,tablet,tr,target=next?.first,enabled=!state.busy&&next!=null,placement=placement,dashedTarget=true){place(PieceKind.valueOf(chosen),it)}
            KidSurface {
                if(next==null){Text("✓",style=MaterialTheme.typography.headlineLarge,color=KidSuccess);FamilyShareButton(t("Tahtayı Hazırla","Set Up the Board"),t("Tamamlandı","Completed"),tr,board,symbol=FamilyShareSymbol.BOARD)}
                Text(if(next==null)complete else when(feedback){1->t("Harika! Taş doğru yerde.","Great! The piece is in the right place.");2->t("Tekrar dene. Gösterilen taşın adını kontrol et ve işaretli kareye dokun.","Try again. Check the piece name and tap the marked square.");else->t("Tahtayı beyazın tarafından görüyoruz. Sağ alt köşe açık renktir. Önce beyaz taşları, sonra siyah taşları yerleştirelim.","We see the board from White’s side. The bottom-right square is light. Place the white pieces first, then the black pieces.")},textAlign=TextAlign.Center)
                if(next?.second?.kind==PieceKind.QUEEN)Text(t("Vezir kendi rengindeki karede başlar: beyaz vezir açık, siyah vezir koyu karede.","The queen starts on her own colour: the white queen on a light square, the black queen on a dark square."),style=MaterialTheme.typography.titleMedium,textAlign=TextAlign.Center)
            }
        }
    }
}
