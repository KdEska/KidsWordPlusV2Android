package com.demarpsoftware.kidswordplus

import java.io.File
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.demarpsoftware.kidswordplus.chess.Piece
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable internal fun FamilyShareButton(title: String,detail: String,tr: Boolean,board: List<Piece?>?=null,symbol: FamilyShareSymbol=FamilyShareSymbol.CROWN,images:List<File> = emptyList(),parentArea:Boolean=false,appPromotion:Boolean=false,onOpen:()->Unit={}) {
    var showing by remember {mutableStateOf(false)}
    val audio=LocalChessAudio.current
    if(appPromotion) SettingsCard {
        Text(InterfaceStrings.text(tr,"Öğrenmenin mutluluğunu paylaş","Share the joy of learning"),style=MaterialTheme.typography.titleMedium,color=KidHeading)
        Text(InterfaceStrings.text(tr,"KidsWordPlus’ı sevdiklerinle tanıştır.","Introduce KidsWordPlus to your loved ones."),style=MaterialTheme.typography.bodyMedium,color=KidSecondary)
        SettingsAction(InterfaceStrings.text(tr,"Uygulamayı paylaş","Share the app")){audio?.stop();onOpen();showing=true}
    } else TextButton(onClick={audio?.stop();onOpen();showing=true}) {Text(if(appPromotion){InterfaceStrings.text(tr,"Uygulamayı paylaş","Share the app")}else if(parentArea){InterfaceStrings.text(tr,"Paylaş","Share")}else InterfaceStrings.text(tr,"Ailenle paylaş","Share with your family"))}
    if(showing) FamilyShareDialog(title,detail,tr,board,symbol,images,parentArea) {showing=false}
}

@Composable private fun FamilyShareDialog(title: String,detail: String,tr: Boolean,board: List<Piece?>?,symbol: FamilyShareSymbol,images:List<File>,parentArea:Boolean,close: ()->Unit) {
    val context=LocalContext.current
    val keyboard=LocalSoftwareKeyboardController.current
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    var challenge by remember {mutableStateOf(FamilyShareChallenge.create())}
    var answer by remember {mutableStateOf("")}
    var verified by remember {mutableStateOf(parentArea)}
    var image by remember {mutableStateOf<Bitmap?>(null)}
    var failed by remember {mutableStateOf(false)}
    val owner=LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer=LifecycleEventObserver {_,event -> if(event==Lifecycle.Event.ON_STOP) {
            verified=false;answer="";challenge=FamilyShareChallenge.create();image=null
        }}
        owner.lifecycle.addObserver(observer);onDispose {owner.lifecycle.removeObserver(observer)}
    }
    LaunchedEffect(verified,title,detail,board,symbol,images,tr) {
        image=null;failed=false
        if(verified) image=withContext(Dispatchers.IO) {runCatching {renderFamilyShareCard(context,title,detail,board,tr,symbol,images)}.getOrNull()}.also {failed=it==null}
    }
    Dialog(onDismissRequest=close,properties=DialogProperties(usePlatformDefaultWidth=false)) {
        BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment=Alignment.Center) {
            val dialogHeight = minOf(maxHeight * .78f, 560.dp)
            Surface(Modifier.widthIn(max=520.dp).fillMaxWidth(.92f).heightIn(max=dialogHeight),shape=MaterialTheme.shapes.large,color=MaterialTheme.colorScheme.background) {
                Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(14.dp)) {
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                        Text(t("Aile paylaşımı","Family sharing"),Modifier.weight(1f),style=MaterialTheme.typography.titleLarge,color=KidHeading)
                        TextButton(onClick=close) {Text(t("Kapat","Close"))}
                    }
                    if(!verified) {
                        Text(t("Yetişkin doğrulaması","Adult verification"),style=MaterialTheme.typography.headlineSmall,color=KidHeading)
                        Text(t("Paylaşmak için bir ebeveynden yardım iste.","Ask a parent to help you share."),textAlign=TextAlign.Center)
                        Text("${challenge.left} × ${challenge.right} = ?",style=MaterialTheme.typography.headlineLarge)
                        OutlinedTextField(answer,{answer=it.take(5)},label={Text(t("Cevap","Answer"))},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true,modifier=Modifier.widthIn(max=240.dp))
                        KidAction(t("Devam et","Continue"),enabled=challenge.accepts(answer)) {keyboard?.hide();verified=true}
                    } else {
                        image?.let {bitmap -> Image(bitmap.asImageBitmap(),t("Paylaşım kartı ön izlemesi","Share card preview"),Modifier.fillMaxWidth().height(220.dp))}
                        if(image==null && !failed) CircularProgressIndicator()
                        Text(t("Kart yalnız başarı bilgisini ve etkinlik görsellerini içerir. Çocuğun adı veya profil bilgileri eklenmez.","The card contains only the achievement and activity images. No child's name or profile information is included."),textAlign=TextAlign.Center,style=MaterialTheme.typography.bodySmall)
                        KidAction(t("Paylaş","Share"),enabled=image!=null,tone=2) {image?.let {bitmap -> failed=runCatching {launchFamilyShare(context,bitmap)}.isFailure}}
                        if(failed) Text(t("Kart hazırlanamadı. Kapatıp yeniden dene.","The card could not be prepared. Close and try again."),color=MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
