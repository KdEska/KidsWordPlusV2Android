package com.demarpsoftware.kidswordplus

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.demarpsoftware.kidswordplus.family.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.*

@Composable internal fun IosProfile(state:AppState,model:AppModel,tablet:Boolean) {
    val tr=state.ui=="tr"
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    var name by rememberSaveable {mutableStateOf(state.name)}
    var editing by remember {mutableStateOf(false)}
    val focus=LocalFocusManager.current
    // Save after typing settles, and immediately on Done/focus loss; no separate Save step in iOS.
    LaunchedEffect(name,state.busy) {if(name!=state.name&&!state.busy){delay(600);model.updateDisplayName(name)}}
    val latestName by rememberUpdatedState(name)
    val latestState by rememberUpdatedState(state)
    DisposableEffect(model) {
        onDispose {
            if(latestName!=latestState.name&&!latestState.busy)model.updateDisplayName(latestName)
        }
    }
    val unlocked=state.earned+Companions.starters
    Column(Modifier.widthIn(max=if(tablet)960.dp else 600.dp).fillMaxWidth().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(24.dp)) {
        ProfileSurface(28,20) {
            DesignArt("companions/${state.companion}.png",Modifier.fillMaxWidth().height(130.dp),decodeLimit=512)
            Text(state.name.ifBlank{t("Merhaba, keşifçi!","Hello, explorer!")},Modifier.fillMaxWidth(),style=MaterialTheme.typography.titleLarge,color=KidHeading,textAlign=TextAlign.Center)
            Text("◎  "+LearningLanguages.title(state.language),Modifier.fillMaxWidth(),style=MaterialTheme.typography.bodyMedium,color=KidHeading,textAlign=TextAlign.Center)
        }
        ProfileSurface(24,20) {
            Text(t("Ad veya takma ad","Name or nickname"),style=MaterialTheme.typography.titleMedium,color=KidHeading)
            Row(Modifier.fillMaxWidth().heightIn(min=70.dp).background(Color.White,RoundedCornerShape(22.dp))
                .border(if(editing)2.5.dp else 1.5.dp,KidHeading.copy(alpha=if(editing).9f else .22f),RoundedCornerShape(22.dp)).padding(14.dp),
                verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(42.dp).background(KidHeading.copy(alpha=.12f),RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center) {
                    Canvas(Modifier.size(22.dp)) {
                        val w=size.width
                        drawLine(KidHeading,androidx.compose.ui.geometry.Offset(w*.2f,w*.8f),androidx.compose.ui.geometry.Offset(w*.8f,w*.2f),w*.19f,androidx.compose.ui.graphics.StrokeCap.Square)
                        drawLine(Color.White,androidx.compose.ui.geometry.Offset(w*.66f,w*.27f),androidx.compose.ui.geometry.Offset(w*.75f,w*.36f),w*.06f)
                    }
                }
                BasicTextField(name,{name=it.take(30)},modifier=Modifier.weight(1f).onFocusChanged {
                    val wasEditing=editing;editing=it.isFocused
                    if(wasEditing&&!it.isFocused&&name!=state.name&&!state.busy)model.updateDisplayName(name)
                }.semantics{contentDescription=t("Ad veya takma ad","Name or nickname")},singleLine=true,
                    textStyle=MaterialTheme.typography.titleLarge.copy(color=KidHeading),cursorBrush=SolidColor(KidHeading),
                    keyboardOptions=KeyboardOptions(capitalization=KeyboardCapitalization.Words,autoCorrectEnabled=false,imeAction=ImeAction.Done),
                    keyboardActions=KeyboardActions(onDone={if(name!=state.name&&!state.busy)model.updateDisplayName(name);focus.clearFocus()}),
                    decorationBox={field -> Box {if(name.isEmpty())Text(t("Adını yaz…","Your name…"),style=MaterialTheme.typography.titleLarge,color=KidSecondary);field()}})
            }
            Row(Modifier.fillMaxWidth()) {Text(t("İsteğe bağlı","Optional"),Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium,color=KidSecondary);Text("${name.length} / 30",style=MaterialTheme.typography.bodyMedium,color=KidSecondary)}
        }
        ProfileSurface(24,16) {
            Text(t("Yol arkadaşın","Your companion"),style=MaterialTheme.typography.titleLarge,color=KidHeading)
            CompanionPicker(state,model,tr)
        }
        ProfileSurface(20,16) {Text(t("Değişiklikler bu cihazda kaydedilir. Öğrenme ilerlemen korunur.","Changes are saved on this device. Your learning progress is kept."),fontSize=13.sp,color=KidSecondary)}
    }
}
@Composable internal fun CompanionPicker(state:AppState,model:AppModel,tr:Boolean) {
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    val unlocked=state.earned+Companions.starters
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(18.dp)) {
            ProfileSurface(22,16) {
                Text(t("Karakter Dünyası","Companion World"),style=MaterialTheme.typography.titleLarge,color=KidHeading)
                Text(t("Öğren, ilerle, yeni arkadaşlarını aç!","Learn, grow and meet new friends!"),style=MaterialTheme.typography.bodyMedium,color=KidSecondary)
                Row(Modifier.fillMaxWidth()) {
                    Text("${unlocked.size} / 21 ${t("arkadaş","friends")}",Modifier.weight(1f),fontSize=13.sp,fontWeight=FontWeight.Bold,color=KidHeading)
                    Text("${state.completed.size} ${t("kelime","words")}",fontSize=13.sp,fontWeight=FontWeight.Bold,color=KidHeading)
                }
                LinearProgressIndicator(progress={unlocked.size/21f},modifier=Modifier.fillMaxWidth(),color=KidSuccess)
                Text(t("Öğrendiğin tüm dillerdeki kelimeler sayılır. Tekrarlar sayıyı artırmaz.","Words from all learning languages count. Reviews do not add new words."),fontSize=12.sp,color=KidSecondary)
            }
            Text(t("Başlangıç arkadaşları","Starter friends"),style=MaterialTheme.typography.titleMedium,color=KidHeading)
            ProfileFriendGrid(Companions.all.take(6),unlocked,state,model,tr)
            Text(t("Yeni arkadaşlarını keşfet","Discover new friends"),style=MaterialTheme.typography.titleMedium,color=KidHeading)
            ProfileFriendGrid(Companions.all.drop(6).take(14),unlocked,state,model,tr)
            ProfileFriendCard(Companions.all.last(),"nova" in unlocked,state.companion=="nova",state,model,tr)
    }
}
@Composable private fun ProfileSurface(radius:Int,padding:Int,content:@Composable ColumnScope.()->Unit) {
    Column(Modifier.fillMaxWidth().background(Color.White,RoundedCornerShape(radius.dp)).padding(padding.dp),verticalArrangement=Arrangement.spacedBy(12.dp),content=content)
}
@Composable private fun ProfileFriendGrid(friends:List<Companion>,unlocked:Set<String>,state:AppState,model:AppModel,tr:Boolean) {
    val columns=if(LocalDensity.current.fontScale>=1.6f)1 else 2
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        friends.chunked(columns).forEach {row -> Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            row.forEach {friend -> Box(Modifier.weight(1f)){ProfileFriendCard(friend,friend.id in unlocked,friend.id==state.companion,state,model,tr)}}
        }}
    }
}
@Composable private fun ProfileFriendCard(friend:Companion,unlocked:Boolean,chosen:Boolean,state:AppState,model:AppModel,tr:Boolean) {
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    val secret=friend.id=="nova"
    val shape=RoundedCornerShape(22.dp)
    Column(Modifier.fillMaxWidth().clip(shape).background(Color.White)
        .border(if(chosen)3.dp else 1.dp,KidHeading.copy(alpha=if(chosen)1f else if(secret).4f else .12f),shape)
        .semantics(mergeDescendants=true){selected=chosen;stateDescription=if(chosen)t("Yol arkadaşın","Your companion") else if(unlocked)t("Seçilebilir","Available")else t("Kilitli","Locked")}
        .clickable(enabled=unlocked&&!state.busy,role=Role.Button){model.selectCompanion(friend.id)}.padding(12.dp),
        horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)) {
        if(secret)Text(t("GİZLİ KARAKTER","SECRET FRIEND"),fontSize=12.sp,fontWeight=FontWeight.Bold,color=KidHeading)
        if(secret&&!unlocked)SecretFriendOutline(friend.id)
        else DesignArt("companions/${friend.id}.png",Modifier.fillMaxWidth().height(145.dp).graphicsLayer{alpha=if(unlocked)1f else .42f},decodeLimit=512,
            colorFilter=if(unlocked)null else ColorFilter.colorMatrix(ColorMatrix().apply{setToSaturation(0f)}))
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(5.dp)) {
            Text(friend.name,style=MaterialTheme.typography.titleMedium,color=KidHeading)
            if(chosen)DesignArt("ui/navigation/green-tick.webp",Modifier.size(22.dp),decodeLimit=96)
            else if(!unlocked)Text("🔒",fontSize=12.sp)
        }
        Text(if(tr)friend.tr else friend.en,style=MaterialTheme.typography.bodyMedium,color=KidBody,textAlign=TextAlign.Center)
        if(unlocked)Text(if(chosen)t("Yol arkadaşın","Your companion")else t("Beni seç","Choose me"),Modifier.background(KidSuccess.copy(alpha=.1f),RoundedCornerShape(50)).padding(horizontal=12.dp,vertical=6.dp),fontSize=12.sp,fontWeight=FontWeight.Bold,color=KidSuccess,textAlign=TextAlign.Center)
        else Column(Modifier.fillMaxWidth().background(KidHeading.copy(alpha=.06f),RoundedCornerShape(12.dp)).padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(5.dp)) {
            if(friend.words>0) {
                Text("★ ${minOf(state.completed.size,friend.words)} / ${friend.words} ${t("kelime","words")}",fontSize=12.sp,fontWeight=FontWeight.Bold,color=KidSecondary)
                LinearProgressIndicator(progress={minOf(state.completed.size,friend.words).toFloat()/friend.words},modifier=Modifier.fillMaxWidth(),color=KidHeading)
            }
            if(friend.task!="none")Text(Companions.requirement(friend.copy(words=0),tr),fontSize=12.sp,color=KidSecondary,textAlign=TextAlign.Center)
            if(secret)Text("${(state.earned+Companions.starters).count{it!="nova"}} / 20",fontSize=12.sp,fontWeight=FontWeight.Bold,color=KidSecondary)
        }
    }
}
@Composable private fun SecretFriendOutline(id:String) {
    val context=LocalContext.current
    val bitmap by produceState<ImageBitmap?>(null,id) {value=withContext(Dispatchers.IO){context.assets.open("companions/$id.png").use {android.graphics.BitmapFactory.decodeStream(it)?.asImageBitmap()}}}
    Canvas(Modifier.fillMaxWidth().height(145.dp).graphicsLayer{compositingStrategy=CompositingStrategy.Offscreen}) {
        bitmap?.let {image ->
            val scale=minOf((size.width-8.dp.toPx())/image.width,(size.height-8.dp.toPx())/image.height)
            val target=IntSize((image.width*scale).roundToInt(),(image.height*scale).roundToInt())
            val origin=IntOffset(((size.width-target.width)/2).roundToInt(),((size.height-target.height)/2).roundToInt())
            repeat(8){index ->val angle=index*Math.PI/4
                drawImage(image,dstOffset=origin+IntOffset((cos(angle)*2.5.dp.toPx()).roundToInt(),(sin(angle)*2.5.dp.toPx()).roundToInt()),dstSize=target,colorFilter=ColorFilter.tint(KidHeading))
            }
            drawImage(image,dstOffset=origin,dstSize=target,blendMode=BlendMode.DstOut)
        }
    }
}
