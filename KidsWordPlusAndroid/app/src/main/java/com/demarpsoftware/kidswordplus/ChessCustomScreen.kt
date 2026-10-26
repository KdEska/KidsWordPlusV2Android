package com.demarpsoftware.kidswordplus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.demarpsoftware.kidswordplus.chess.*

@Composable internal fun ChessCustomScreen(state:AppState,model:AppModel,tablet:Boolean,back:()->Unit,started:()->Unit) {
    val tr=state.ui=="tr"
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    val custom=state.chessCustom
    val issue=remember(custom){custom.issue}
    var white by rememberSaveable {mutableStateOf(true)}
    var kindName by rememberSaveable {mutableStateOf(PieceKind.PAWN.name)}
    var erase by rememberSaveable {mutableStateOf(false)}
    val preferences=LocalContext.current.getSharedPreferences("chess-custom-options",0)
    var difficultyName by rememberSaveable {mutableStateOf(preferences.getString("difficulty",null)?.takeIf {name->ChessDifficulty.entries.any {it.name==name}} ?: ChessDifficulty.NORMAL.name)}
    var playerWhite by rememberSaveable {mutableStateOf(preferences.getBoolean("playerWhite",true))}
    var confirm by remember {mutableStateOf(false)}
    val enabled=!state.busy&&!state.chessCustomSaveError
    val placement=remember {ChessPlacementDrag()}
    SideEffect {placement.onDrop={piece,square->if(enabled)model.editCustom(square,piece)}}
    PageHelp("custom");ScreenTitle(t("Kendi Oyununu Kur","Create Your Game"));ChessPageNavigation(back)
    val header:@Composable ()->Unit={
        Text(t("İki tarafın taşlarını dokunarak veya sürükleyerek yerleştir. İstersen otomatik diz, sonra bilgisayara karşı oyna.","Place both sides by tapping or dragging pieces. You can arrange them automatically, then play against the computer."),style=MaterialTheme.typography.bodyMedium,textAlign=TextAlign.Center)
        if(state.chess?.startFen!=null&&!state.chessSaveError)KidAction(t("Oyuna devam et","Resume game"),tone=1,icon="play",onClick=started)
        if(state.chessCustomSaveError)Text(t("Konum taslağı okunamadı; mevcut kayıt korunuyor.","The draft could not be read; the existing record is preserved."),color=MaterialTheme.colorScheme.error)
    }
    val tools:@Composable (Boolean)->Unit={compact ->
        KidSurface {
            ChessSegmented(listOf(t("Beyaz","White"),t("Siyah","Black")),if(white)0 else 1){white=it==0;erase=false}
            ChessPieceStrip(if(erase)null else PieceKind.valueOf(kindName),white,tr,enabled,placement,columns=if(compact)3 else null){kindName=it.name;erase=false}
            if(compact||LocalDensity.current.fontScale>=1.6f) {
                OutlinedButton({erase=!erase},enabled=enabled){Text(if(erase)t("✓ Silgi","✓ Eraser")else t("Silgi","Eraser"))}
                OutlinedButton(model::undoCustom,enabled=enabled&&state.customUndo.isNotEmpty()){Text(t("Geri al","Undo"))}
                OutlinedButton({model.resetCustom(false)},enabled=enabled){Text(t("Tahtayı temizle","Clear board"))}
            }else Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                OutlinedButton({erase=!erase},Modifier.weight(1f),enabled=enabled){Text(if(erase)t("✓ Silgi","✓ Eraser")else t("Silgi","Eraser"))}
                OutlinedButton(model::undoCustom,Modifier.weight(1f),enabled=enabled&&state.customUndo.isNotEmpty()){Text(t("Geri al","Undo"))}
                OutlinedButton({model.resetCustom(false)},Modifier.weight(1f),enabled=enabled){Text(t("Tahtayı temizle","Clear board"))}
            }
            OutlinedButton({model.arrangeCustom(white,true)},enabled=enabled){Text(t("Seçili tarafı rastgele diz","Randomize selected side"))}
            if(compact||LocalDensity.current.fontScale>=1.6f) {
                OutlinedButton({model.arrangeCustom(white)},enabled=enabled){Text(t("Seçili tarafı otomatik diz","Auto-arrange selected side"))}
                OutlinedButton({model.resetCustom(true)},enabled=enabled){Text(t("İki tarafı otomatik diz","Auto-arrange both sides"))}
            }else Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                OutlinedButton({model.arrangeCustom(white)},Modifier.weight(1f),enabled=enabled){Text(t("Seçili tarafı otomatik diz","Auto-arrange selected side"))}
                OutlinedButton({model.resetCustom(true)},Modifier.weight(1f),enabled=enabled){Text(t("İki tarafı otomatik diz","Auto-arrange both sides"))}
            }
        }
    }
    val options:@Composable ()->Unit={
        KidSurface {
            ChessSegmented(listOf(t("Beyaz","White"),t("Siyah","Black")),if(playerWhite)0 else 1){value->if(preferences.edit().putBoolean("playerWhite",value==0).commit())playerWhite=value==0}
            Text(t("Oynayacağın taraf","Your side"),style=MaterialTheme.typography.bodySmall)
            ChessSegmented(listOf(t("Beyaz","White"),t("Siyah","Black")),if(custom.whiteTurn)0 else 1){if(enabled)model.customTurn(it==0)}
            Text(t("İlk kim oynasın?","Who moves first?"),style=MaterialTheme.typography.bodySmall)
            ChessDifficultyPicker(ChessDifficulty.valueOf(difficultyName),tr){value->if(preferences.edit().putString("difficulty",value.name).commit())difficultyName=value.name}
            Row(verticalAlignment=Alignment.CenterVertically){Text(t("Rok hakkı","Castling rights"),Modifier.weight(1f));Switch(custom.allowCastling,model::customCastling,enabled=enabled)}
            Text(t("Rok yalnız başlangıç karelerindeki şah ve kaleler için geçerlidir. Geçerken alma hakkı yoktur. Başlangıç sırasındaki piyon iki kare ilerleyebilir.","Castling applies only to kings and rooks on their home squares. There is no en passant right. Pawns on their starting rank may move two squares."),style=MaterialTheme.typography.bodySmall)
        }
    }
    val start:@Composable ()->Unit={
        Text(if(issue==null)t("✓ Konum hazır!","✓ Position ready!")else customIssue(issue,tr),color=if(issue==null)KidSuccess else KidSecondary,style=MaterialTheme.typography.titleMedium,textAlign=TextAlign.Center)
        KidAction(t("Oyunu başlat","Start game"),enabled=enabled&&issue==null&&!state.chessSaveError,tone=2,icon="play"){if(state.chess!=null)confirm=true else model.startCustom(true,playerWhite,0,ChessDifficulty.valueOf(difficultyName),started)}
        if(state.chessSaveError)Text(t("Mevcut oyun kaydı okunamadı. Ebeveyn alanından oyun kaydını kurtarabilirsin.","The saved match could not be read. Recover it in the parent area."),color=MaterialTheme.colorScheme.error)
    }
    val board:@Composable ()->Unit={
        IosChessBoard(custom.board,tablet,tr,enabled=enabled,placement=placement,drag={from,to->if(enabled)model.moveCustom(from,to)}) {square->model.editCustom(square,if(erase)null else Piece(PieceKind.valueOf(kindName),white))}
    }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val toolsWidth=minOf(350.dp,maxWidth*.36f)
        val boardWidth=(maxHeight-285.dp).coerceAtLeast(240.dp)
        if(tablet&&maxWidth>maxHeight&&LocalDensity.current.fontScale<1.6f) {
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(20.dp),verticalAlignment=Alignment.Top) {
                Column(Modifier.width(toolsWidth).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(14.dp),horizontalAlignment=Alignment.CenterHorizontally){header();tools(true);options()}
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp),horizontalAlignment=Alignment.CenterHorizontally){ChessAppearancePicker(tr);Box(Modifier.widthIn(max=boardWidth)){board()};start()}
            }
        }else Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.TopCenter){Column(Modifier.widthIn(max=if(tablet)960.dp else 600.dp).fillMaxWidth().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(16.dp),horizontalAlignment=Alignment.CenterHorizontally){header();tools(false);ChessAppearancePicker(tr);board();options();start()}}
    }
    if(confirm)AlertDialog(onDismissRequest={confirm=false},title={Text(t("Yeni oyun başlatılsın mı?","Start a new game?"))},text={Text(t("Önceki oyun kaydı bu konumla değiştirilecek. Taslak korunur.","This position replaces the previous saved match. The draft is kept."))},confirmButton={TextButton({confirm=false;model.startCustom(true,playerWhite,0,ChessDifficulty.valueOf(difficultyName),started)}){Text(t("Oyunu başlat","Start game"))}},dismissButton={TextButton({confirm=false}){Text(t("Vazgeç","Cancel"))}})
}
private fun customIssue(issue:String,tr:Boolean)=when(issue){
    "kings"->InterfaceStrings.text(tr,"Her renkten tam bir şah olmalı.","Each side needs exactly one king.")
    "material"->InterfaceStrings.text(tr,"Bir renkte en fazla 16 taş ve 8 piyon olabilir.","Each side can have at most 16 pieces and 8 pawns.")
    "pawns"->InterfaceStrings.text(tr,"İlk ve son sırada piyon olamaz.","Pawns cannot be on the first or last rank.")
    "adjacent"->InterfaceStrings.text(tr,"Şahlar yan yana olamaz.","Kings cannot be adjacent.")
    "wrongCheck"->InterfaceStrings.text(tr,"Sırası gelmeyen tarafın şahı tehdit altında olamaz. Konumu veya sırayı değiştir.","The side not to move cannot be in check. Change the position or side to move.")
    else->InterfaceStrings.text(tr,"Bu konumda oyun zaten bitmiş. Mat, pat veya yetersiz materyali değiştir.","This position is already finished. Change the checkmate, stalemate or insufficient material.")
}
