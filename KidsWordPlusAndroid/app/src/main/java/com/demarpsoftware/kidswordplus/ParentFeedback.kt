package com.demarpsoftware.kidswordplus

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

internal val feedbackEndpoint: URL?
    get() = runCatching { URL(BuildConfig.FEEDBACK_API_URL).takeIf { it.protocol == "https" && it.host.isNotBlank() && it.userInfo == null } }.getOrNull()
internal data class FeedbackChoice(val id:String,val tr:String,val en:String)
internal val feedbackKinds=listOf(FeedbackChoice("problem","Sorun","Problem"),FeedbackChoice("suggestion","Öneri","Suggestion"),FeedbackChoice("appreciation","Beğendiğim bir şey","Something I liked"))
internal val feedbackTopics=listOf(FeedbackChoice("general","Genel","General"),FeedbackChoice("sound","Ses","Sound"),FeedbackChoice("images","Görseller","Images"),FeedbackChoice("lessons","Dersler","Lessons"),FeedbackChoice("games","Oyunlar","Games"),FeedbackChoice("chess","Satranç","Chess"),FeedbackChoice("notifications","Bildirimler","Notifications"),FeedbackChoice("other","Diğer","Other"))
internal val feedbackScreens=listOf(FeedbackChoice("general","Genel","General"),FeedbackChoice("home","Ana Sayfa","Home"),FeedbackChoice("learn","Öğren","Learn"),FeedbackChoice("games","Oyunlar","Games"),FeedbackChoice("chess","Satranç","Chess"),FeedbackChoice("profile","Profil","Profile"),FeedbackChoice("settings","Ayarlar","Settings"),FeedbackChoice("packages","Dil paketleri","Language packs"))

internal object FeedbackValidation {
    fun validEmail(email:String)=email.isBlank() || (email.trim().length<=254 && Regex("^[^\\s@<>]+@[^\\s@<>]+\\.[^\\s@<>]+$").matches(email.trim()))
    fun validMessage(message:String)=message.trim().length in 10..3000
}
internal suspend fun sendFeedback(payload:JSONObject):Int=withContext(Dispatchers.IO) {
    val connection=requireNotNull(feedbackEndpoint).openConnection() as HttpURLConnection
    try {
        connection.requestMethod="POST";connection.connectTimeout=15000;connection.readTimeout=20000
        connection.instanceFollowRedirects=false;connection.doOutput=true
        connection.setRequestProperty("Content-Type","application/json; charset=utf-8")
        connection.outputStream.use{it.write(payload.toString().toByteArray(Charsets.UTF_8))}
        val code=connection.responseCode
        if(code in 200..299) {
            val response=connection.inputStream.bufferedReader().use{it.readText()}
            check(JSONObject(response).optString("id")==payload.getString("id"))
        }
        code
    } finally {connection.disconnect()}
}

@Composable internal fun ParentFeedback(state:AppState) {
    fun t(a:String,b:String)=InterfaceStrings.text(state.ui,a,b)
    var kind by rememberSaveable{mutableStateOf("problem")}
    var topic by rememberSaveable{mutableStateOf("general")}
    var screen by rememberSaveable{mutableStateOf("general")}
    var message by rememberSaveable{mutableStateOf("")}
    var email by rememberSaveable{mutableStateOf("")}
    var id by rememberSaveable{mutableStateOf(UUID.randomUUID().toString())}
    var reviewing by rememberSaveable{mutableStateOf(false)}
    var sent by rememberSaveable{mutableStateOf(false)}
    var sending by remember{mutableStateOf(false)}
    var error by rememberSaveable{mutableStateOf<Int?>(null)}
    val scope=rememberCoroutineScope()
    val technical=listOf("Android", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", "${Build.MANUFACTURER} ${Build.MODEL}".take(100), "Android ${Build.VERSION.RELEASE}", state.ui)
    fun edited(){id=UUID.randomUUID().toString();error=null}
    val scroll = rememberScrollState()
    val focus = LocalFocusManager.current
    LaunchedEffect(reviewing,sent) { scroll.scrollTo(0) }
    Column(Modifier.fillMaxWidth().verticalScroll(scroll).imePadding(),verticalArrangement=Arrangement.spacedBy(18.dp)) {
        FeedbackHero(state, if(sent)t("Geri bildiriminiz için teşekkürler!","Thanks for your feedback!")else if(reviewing)t("Gözden geçir","Review and send")else t("Geri bildirim","Feedback"),
            if(sent)t("Geri bildiriminiz kaydedildi. Yanıt için e-posta adresi bıraktıysanız sizinle iletişime geçebiliriz.","Your feedback was saved. If you left a reply email, we may contact you.")
            else t("Bir sorunu, önerinizi veya beğendiğiniz bir şeyi bizimle paylaşın.","Share a problem, a suggestion or something you liked."),sent,reviewing)
        if(sent) {
            FeedbackPanel { Text(id,style=MaterialTheme.typography.bodySmall,color=KidSecondary) }
            KidAction(t("Yeni geri bildirim","New feedback")){sent=false;reviewing=false;message="";email="";edited()}
        } else {
            if(!reviewing) {
                FeedbackSection(1,t("Geri bildirim türü","Feedback type")) {
                    FeedbackKindCards(kind,state.ui){kind=it;edited()}
                }
                FeedbackSection(2,t("Konu","Topic")) {
                    FeedbackTopics(topic,state.ui){topic=it;edited()}
                    HorizontalDivider(color=Color(0xFFEEEAF5))
                    FeedbackPicker(t("Ekran","Screen"),feedbackScreens,screen,state.ui){screen=it;edited()}
                }
                FeedbackSection(3,t("Mesajınız","Your message")) {
                OutlinedTextField(message,{if(it.length<=3000){message=it;edited()}},Modifier.fillMaxWidth(),label={Text(t("Mesajınız","Your message"))},minLines=4,
                    supportingText={Text("${message.length} / 3000")},isError=message.isNotBlank()&&!FeedbackValidation.validMessage(message))
                if(message.isNotBlank()&&!FeedbackValidation.validMessage(message))Text(t("Lütfen 10–3000 karakter yazın.","Please enter 10–3000 characters."),color=MaterialTheme.colorScheme.error)
                }
                FeedbackSection(4,t("Yanıt için e-posta (isteğe bağlı)","Reply email (optional)")) {
                OutlinedTextField(email,{if(it.length<=254){email=it;edited()}},Modifier.fillMaxWidth(),label={Text(t("Yanıt için e-posta (isteğe bağlı)","Reply email (optional)"))},singleLine=true,
                    keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email),isError=!FeedbackValidation.validEmail(email))
                if(!FeedbackValidation.validEmail(email))Text(t("Geçerli bir e-posta adresi yazın.","Enter a valid email address."),color=MaterialTheme.colorScheme.error)
                }
                FeedbackPanel {
                Row(horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically) {
                    DesignArt("ui/parent/privacy.webp",Modifier.size(30.dp),decodeLimit=128)
                Text(t("Lütfen çocuğunuzun adı, fotoğrafı veya başka özel bilgileri mesajınıza eklemeyin.","Please do not include your child's name, photo or other private information."),style=MaterialTheme.typography.bodySmall)
                }
                }
                KidAction(t("Gözden geçir","Review and send"),enabled=FeedbackValidation.validMessage(message)&&FeedbackValidation.validEmail(email)){focus.clearFocus();reviewing=true}
            } else {
                FeedbackPanel {
                    Row(horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically) {
                        FeedbackKindArt(kind,40)
                        Text(t(feedbackKinds.first{it.id==kind}.tr,feedbackKinds.first{it.id==kind}.en),style=MaterialTheme.typography.titleLarge,color=KidHeading)
                    }
                    val selectedTopic=feedbackTopics.first{it.id==topic}
                    val selectedScreen=feedbackScreens.first{it.id==screen}
                    Text(t(selectedTopic.tr,selectedTopic.en)+" · "+t(selectedScreen.tr,selectedScreen.en),style=MaterialTheme.typography.bodyMedium,color=KidSecondary)
                    HorizontalDivider(color=Color(0xFFEEEAF5))
                    Text(message.trim(),style=MaterialTheme.typography.bodyLarge,color=KidBody)
                    if(email.isNotBlank())Text(email.trim())
                }
                FeedbackPanel {
                Row(horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically) {
                    DesignArt("ui/parent/privacy.webp",Modifier.size(30.dp),decodeLimit=128)
                    Text(t("Teknik bilgiler","Technical details"),style=MaterialTheme.typography.titleMedium,color=KidHeading)
                }
                Text(technical.joinToString(" · "),style=MaterialTheme.typography.bodySmall)
                Text(t("Gönderdiğinizde mesajınız ve bu teknik bilgiler destek sunucumuzda saklanır, info@kidswordplus.com adresine e-postayla iletilir.","When you send, your message and these technical details are saved on our support server and emailed to info@kidswordplus.com."),style=MaterialTheme.typography.bodySmall)
                }
                if(error!=null)Text(if(error==429)t("Çok fazla geri bildirim gönderdiniz. Lütfen daha sonra deneyin.","Too many submissions. Please try again later.")else t("Gönderilemedi. Mesajınız burada duruyor; yeniden deneyebilirsiniz.","Could not send. Your message is still here; you can try again."),color=MaterialTheme.colorScheme.error)
                if(feedbackEndpoint==null)Text(t("Geri bildirim gönderimi henüz etkin değil.","Feedback sending is not enabled yet."),color=KidSecondary)
                KidAction(if(sending)t("Gönderiliyor…","Sending…")else t("Geri bildirimi gönder","Send feedback"),enabled=!sending&&feedbackEndpoint!=null){
                    // Capture only the reviewed fields; profile data and identifiers are never added.
                    val payload=JSONObject().put("id",id).put("kind",kind).put("topic",topic).put("screen",screen).put("message",message.trim()).put("email",email.trim())
                        .put("platform","android").put("source","MOBIL").put("version",technical[1]).put("device",technical[2]).put("os",technical[3]).put("language",state.ui)
                    sending=true;error=null
                    scope.launch {
                        try {val code=sendFeedback(payload);if(code in 200..299)sent=true else error=code}
                        catch(cancelled:kotlinx.coroutines.CancellationException){throw cancelled}
                        catch(_:Exception){error=0}
                        finally{sending=false}
                    }
                }
                KidAction(t("Düzenle","Edit"),enabled=!sending,tone=1){reviewing=false}
            }
        }
    }
}
@Composable private fun FeedbackPanel(content:@Composable ColumnScope.()->Unit) {
    Column(Modifier.fillMaxWidth().background(Color.White,RoundedCornerShape(24.dp)).border(1.dp,Color(0xFFE9E3F3),RoundedCornerShape(24.dp)).padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp),content=content)
}
@Composable private fun FeedbackSection(number:Int,title:String,content:@Composable ColumnScope.()->Unit) {
    FeedbackPanel {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(30.dp).background(Color(0xFFF0E9FF),RoundedCornerShape(10.dp)),contentAlignment=Alignment.Center){Text("$number",fontWeight=FontWeight.Bold,color=KidHeading)}
            Text(title,style=MaterialTheme.typography.titleMedium,color=KidHeading,modifier=Modifier.weight(1f).semantics{heading()})
        }
        content()
    }
}
@Composable private fun FeedbackHero(state:AppState,title:String,detail:String,sent:Boolean,reviewing:Boolean) {
    Column(Modifier.fillMaxWidth().background(Brush.linearGradient(if(sent)listOf(Color(0xFFE5F7ED),Color(0xFFF4FCF6))else listOf(Color(0xFFF0E7FF),Color(0xFFEAF3FF))),RoundedCornerShape(26.dp)).border(1.dp,Color.White.copy(alpha=.8f),RoundedCornerShape(26.dp)).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text(if(sent)"✓" else if(reviewing)"2 / 2" else "1 / 2",color=if(sent)KidSuccess else KidHeading,fontWeight=FontWeight.Bold)
                Text(title,style=MaterialTheme.typography.headlineSmall,color=KidHeading,modifier=Modifier.semantics{heading()})
            }
            DesignArt("companions/${state.companion}.png",Modifier.size(80.dp),decodeLimit=256)
        }
        Text(detail,style=MaterialTheme.typography.bodyMedium,color=KidBody)
    }
}
@Composable private fun FeedbackKindArt(id:String,size:Int=42) {
    DesignArt(when(id){"problem"->"ui/parent/need-practice.webp";"suggestion"->"ui/parent/target.webp";else->"ui/games/daily-star.webp"},Modifier.size(size.dp),decodeLimit=192)
}
@Composable private fun FeedbackKindCards(value:String,language:String,select:(String)->Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val stacked=maxWidth<270.dp||LocalDensity.current.fontScale>=1.5f
        @Composable fun option(choice:FeedbackChoice,modifier:Modifier) {
            val selected=value==choice.id
            val tint=when(choice.id){"problem"->Color(0xFFB65B31);"suggestion"->Color(0xFF4964B4);else->Color(0xFF358361)}
            Column(modifier.background(if(selected)tint.copy(alpha=.09f)else Color(0xFFFAF9FD),RoundedCornerShape(18.dp))
                .border(if(selected)2.dp else 1.dp,if(selected)tint else Color(0xFFEEEAF5),RoundedCornerShape(18.dp))
                .selectable(selected,role=Role.RadioButton,onClick={select(choice.id)}).padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)) {
                FeedbackKindArt(choice.id)
                Text(InterfaceStrings.text(language,choice.tr,choice.en),style=MaterialTheme.typography.bodyMedium,fontWeight=FontWeight.SemiBold,color=if(selected)tint else KidBody,textAlign=TextAlign.Center)
                if(!stacked)Spacer(Modifier.weight(1f))
                Text(if(selected)"●"else"○",color=if(selected)tint else KidSecondary,fontSize=16.sp)
            }
        }
        if(stacked)Column(verticalArrangement=Arrangement.spacedBy(10.dp)){feedbackKinds.forEach{option(it,Modifier.fillMaxWidth())}}
        else Row(Modifier.height(IntrinsicSize.Min),horizontalArrangement=Arrangement.spacedBy(8.dp)){feedbackKinds.forEach{option(it,Modifier.weight(1f).fillMaxHeight())}}
    }
}
@Composable private fun FeedbackChoiceArt(id:String) {
    if(id=="chess")Text("♞",fontSize=24.sp,color=KidHeading,modifier=Modifier.size(26.dp))
    else DesignArt(when(id){"sound"->"ui/parent/sound.webp";"images"->"ui/parent/strongest-category.webp";"lessons","learn"->"ui/parent/words-learned.webp";"games"->"ui/games/daily-controller.webp";"notifications"->"ui/parent/notifications.webp";"home"->"ui/categories/home.webp";"profile"->"ui/parent/profile-edit.webp";"packages"->"ui/parent/learning-language.webp";else->"ui/parent/settings.webp"},Modifier.size(26.dp),decodeLimit=128)
}
@OptIn(ExperimentalLayoutApi::class)
@Composable private fun FeedbackTopics(value:String,language:String,select:(String)->Unit) {
    FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        feedbackTopics.forEach {choice ->
            val selected=choice.id==value
            Row(Modifier.background(if(selected)Color(0xFFF0E9FF)else Color(0xFFF8F8FC),RoundedCornerShape(13.dp))
                .border(if(selected)1.5.dp else 1.dp,if(selected)KidHeading else Color(0xFFECE8F2),RoundedCornerShape(13.dp))
                .selectable(selected,role=Role.RadioButton,onClick={select(choice.id)}).padding(horizontal=12.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                FeedbackChoiceArt(choice.id)
                Text(InterfaceStrings.text(language,choice.tr,choice.en),color=KidHeading,style=MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
@Composable private fun FeedbackPicker(title:String,choices:List<FeedbackChoice>,value:String,language:String,select:(String)->Unit) {
    var expanded by remember{mutableStateOf(false)}
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text(title,style=MaterialTheme.typography.titleSmall,color=KidSecondary)
        Box {
            OutlinedButton({expanded=true},Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)) {
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    FeedbackChoiceArt(value)
                    val choice=choices.first{it.id==value}
                    Text(InterfaceStrings.text(language,choice.tr,choice.en),Modifier.weight(1f),textAlign=TextAlign.Start)
                    Text("⌄")
                }
            }
            DropdownMenu(expanded,{expanded=false}){choices.forEach{choice->DropdownMenuItem(text={Text(InterfaceStrings.text(language,choice.tr,choice.en))},leadingIcon={FeedbackChoiceArt(choice.id)},onClick={select(choice.id);expanded=false})}}
        }
    }
}
