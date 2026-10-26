package com.demarpsoftware.kidswordplus

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.demarpsoftware.kidswordplus.content.Word
import com.demarpsoftware.kidswordplus.speech.SpeakingRules
import com.demarpsoftware.kidswordplus.speech.SpeakingStartRequest
import androidx.compose.ui.layout.onGloballyPositioned
import java.util.UUID
import androidx.compose.foundation.relocation.bringIntoViewRequester

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable internal fun SpeakingPracticeSettings(state: AppState, model: AppModel,focusOnArrival:Boolean=false) {
    val target=remember {androidx.compose.foundation.relocation.BringIntoViewRequester()}
    var targetPlaced by remember {mutableStateOf(false)}
    LaunchedEffect(focusOnArrival,targetPlaced) {
        if(focusOnArrival && targetPlaced) {withFrameNanos {};target.bringIntoView()}
    }
    fun t(a:String,b:String)=InterfaceStrings.text(state.ui,a,b)
    SettingsCard {
        Row(Modifier.bringIntoViewRequester(target).onGloballyPositioned {targetPlaced=true}.then(if(focusOnArrival)Modifier.background(KidHeading.copy(alpha=.08f),RoundedCornerShape(12.dp)).padding(8.dp)else Modifier),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            SettingsIcon("speech")
            Text(t("Konuşma pratiği","Speaking practice"),Modifier.weight(1f),style=MaterialTheme.typography.titleMedium,color=KidHeading)
            Switch(state.speakingEnabled,model::setSpeakingEnabled,enabled=!state.busy,modifier=Modifier.semantics {contentDescription=t("Konuşma pratiğine izin ver","Allow speaking practice")})
        }
        Text(t("Konuşma pratiğine izin ver","Allow speaking practice"),style=MaterialTheme.typography.titleSmall)
        Text(t("Mikrofon yalnızca çocuk konuşma düğmesine dokunduğunda açılır. En fazla 5 saniye dinlenir. Ses cihazda değerlendirilir; uygulama ses kaydı saklamaz veya sunucuya göndermez.",
            "The microphone opens only when your child taps the speaking button. Listening lasts up to 5 seconds. Audio is processed on the device; the app does not store recordings or upload audio."),color=KidSecondary)
        Text(t("Yerel tanıma desteği telefona ve yüklü dil modeline bağlıdır. Yüzde telaffuz puanı verilmez.",
            "Local recognition depends on the phone and installed language model. No pronunciation percentage is given."),style=MaterialTheme.typography.bodySmall,color=KidSecondary)
        SpeakingModelSettings(state,model)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun SpeakingPractice(state: AppState, model: AppModel, play:(Word,()->Unit,()->Unit)->Unit,
    stop:()->Unit, success:()->Unit, parent:()->Unit, back:()->Unit, helpOpen:Boolean) {
    val adBreak=LocalGameAdBreak.current
    val context=LocalContext.current
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    val pack=state.pack ?: return
    fun t(a:String,b:String)=InterfaceStrings.text(state.ui,a,b)
    var roundID by remember(pack){mutableStateOf(UUID.randomUUID().toString())}
    val startingStars=remember(pack,roundID){state.gameProgress.stars(state.language)}
    val words=remember(pack,roundID) {
        pack.words.filter {it.category!="alphabet" && it.subcategory!="alphabet" && it.text.isNotBlank()}
            .shuffled().sortedByDescending {LearningRules.progressKey(pack.language,it.id) in state.completed}.take(10)
    }
    var index by remember(pack){mutableIntStateOf(-1)}
    var matched by remember(pack){mutableStateOf(emptySet<String>())}
    var evaluated by remember(pack){mutableStateOf(emptySet<String>())}
    var permission by remember {mutableStateOf(context.checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED)}
    var foreground by remember {mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))}
    var request by remember {mutableStateOf(SpeakingStartRequest())}
    var audioFailed by remember {mutableStateOf(false)}
    var presentedResult by remember {mutableStateOf<SpeakingSession.Phase?>(null)}
    val session=remember(context){SpeakingSession(context)}
    val word=words.getOrNull(index)
    val finished=index>=words.size && words.isNotEmpty()
    val density=LocalDensity.current
    var nonImageHeight by remember { mutableStateOf(330.dp) }
    val permissionLauncher=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permission=granted
        request=request.permissionResult(granted)
        if(!granted)presentedResult=SpeakingSession.Phase.PERMISSION
    }
    DisposableEffect(lifecycle,session) {
        val observer=LifecycleEventObserver { _,event ->
            when(event) {
                Lifecycle.Event.ON_PAUSE -> {
                    foreground=false;session.reset();stop();presentedResult=null
                    if(!request.waitingForPermission)request=SpeakingStartRequest()
                }
                Lifecycle.Event.ON_STOP -> {request=SpeakingStartRequest()}
                Lifecycle.Event.ON_RESUME -> {
                    permission=context.checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED
                    foreground=true
                }
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {lifecycle.removeObserver(observer);session.reset();stop()}
    }
    LaunchedEffect(state.speakingEnabled,helpOpen) {
        if(!state.speakingEnabled || helpOpen){request=SpeakingStartRequest();presentedResult=null;session.reset();stop()}
    }
    LaunchedEffect(request,permission,foreground,state.speakingEnabled,helpOpen,word?.id) {
        if(permission && request.canStart(word?.id,foreground,state.speakingEnabled,helpOpen) && word!=null) {
            request=SpeakingStartRequest()
            stop();audioFailed=false;presentedResult=null
            session.start(word.text,pack.language)
        }
    }
    LaunchedEffect(session.phase) {
        val phase=session.phase
        if(phase !in listOf(SpeakingSession.Phase.READY,SpeakingSession.Phase.CHECKING,SpeakingSession.Phase.LISTENING,SpeakingSession.Phase.RECOGNIZING)) {
            if(word!=null && phase in listOf(SpeakingSession.Phase.MATCHED,SpeakingSession.Phase.DIFFERENT)) {
                evaluated=evaluated+word.id
                if(phase==SpeakingSession.Phase.MATCHED){matched=matched+word.id;success()}
            }
            presentedResult=phase
        }
    }
    fun advance() {
        request=SpeakingStartRequest();presentedResult=null;session.reset();stop();audioFailed=false
        if(index+1==words.size && evaluated.isNotEmpty())model.completeSpeakingRound(pack.language,roundID,matched)
        index++
    }
    fun speak() {
        stop();audioFailed=false;presentedResult=null
        if(session.phase==SpeakingSession.Phase.LISTENING){session.stop();return}
        if(!session.available){session.start(word?.text.orEmpty(),pack.language);return}
        request=SpeakingStartRequest(word?.id,waitingForPermission=!permission)
        if(!permission)permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
    if(foreground && !helpOpen && session.phase in listOf(SpeakingSession.Phase.CHECKING,SpeakingSession.Phase.RECOGNIZING)) {
        AlertDialog(onDismissRequest={},title={Text(t("İşleminiz yapılıyor","Processing your request"))},text={
            Column(verticalArrangement=Arrangement.spacedBy(14.dp)) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Text(if(session.phase==SpeakingSession.Phase.CHECKING)t("Konuşma tanıma hazırlanıyor. Lütfen bekleyin…","Preparing speech recognition. Please wait…")else t("Konuşman değerlendiriliyor. Sonucun birazdan hazır olacak.","Your speech is being checked. Your result will be ready shortly."))
            }
        },confirmButton={},dismissButton={TextButton({request=SpeakingStartRequest();session.reset();stop()}){Text(t("Vazgeç","Cancel"))}})
    }
    Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
        // Use the space left by the real header, footer, text and controls on this device.
        val imageHeight=(maxHeight-nonImageHeight).coerceAtLeast(0.dp)
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).onSizeChanged { size ->
            if(word!=null && !finished) {
                val measured=with(density) { size.height.toDp() }-imageHeight
                if(kotlin.math.abs((measured-nonImageHeight).value)>1f)nonImageHeight=measured
            }
        },
            horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(14.dp)) {
            when {
                words.isEmpty() -> KidSurface {Text(t("Önce dil paketini hazırlayalım","Let's prepare the language pack first"))}
                finished -> KidSurface {
                    LessonCelebration(state.companion,state.gameProgress.stars(state.language),(state.gameProgress.stars(state.language)-startingStars).coerceAtLeast(0),state.ui=="tr",false){true}
                    DesignArt("ui/games/congrats-ribbon.webp",Modifier.fillMaxWidth().height(130.dp),decodeLimit=512)
                    Text(t("Harika pratik!","Great practice!"),style=MaterialTheme.typography.headlineMedium,color=KidHeading)
                    Text(matched.size.toString()+" / "+words.size+" "+t("kelime eşleşti","words matched"),style=MaterialTheme.typography.titleLarge,color=KidSuccess)
                    KidAction(t("Yeni tur","New round"),enabled=!state.busy){session.reset();stop();adBreak { index=0;matched=emptySet();evaluated=emptySet();roundID=UUID.randomUUID().toString() }}
                    KidAction(t("Oyunlara dön","Back to games"),onClick={session.reset();stop();adBreak(back)})
                }
                index<0 -> {
                    Text(t("Şimdi sen söyle","Your turn to speak"),style=MaterialTheme.typography.headlineLarge,color=KidHeading,textAlign=TextAlign.Center)
                    KidSurface {
                        DesignArt("ui/games/speak-microphone.webp",Modifier.fillMaxWidth().height(140.dp),decodeLimit=512)
                        Text(words.size.toString()+" "+t("kelimelik konuşma turu","words in this speaking round"),Modifier.fillMaxWidth(),textAlign=TextAlign.Center,style=MaterialTheme.typography.headlineSmall,color=KidHeading)
                        Text(t("Önce kelimeyi dinle, sonra sen söyle. İstersen yeniden dene veya kelimeyi geç. Hazır olduğunda başlayalım!",
                            "Listen to the word, then say it yourself. You can try again or skip a word. Let's begin when you're ready!"),textAlign=TextAlign.Center)
                        KidAction(t("Konuşmaya başla","Let's speak"),enabled=state.speakingEnabled&&!state.busy){index=0}
                    }
                }
                word!=null -> {
                    KidSurface {
                        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                            Text((index+1).toString()+" / "+words.size,Modifier.weight(1f),style=MaterialTheme.typography.titleSmall,color=KidSecondary)
                            Text(LearningLanguages.title(pack.language),Modifier.background(KidHeading.copy(alpha=.08f),RoundedCornerShape(50)).padding(horizontal=12.dp,vertical=5.dp),style=MaterialTheme.typography.labelLarge,color=KidSecondary)
                        }
                        KidProgress(index.toFloat()/words.size)
                        GameImage(pack.image(word),Modifier.fillMaxWidth().height(imageHeight))
                        Text(word.text,Modifier.fillMaxWidth().testTag("speech.word"),textAlign=TextAlign.Center,style=MaterialTheme.typography.headlineLarge,color=KidHeading,fontWeight=FontWeight.Bold)
                        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                                SpeakingActionSurface(Modifier.fillMaxWidth(),1,!session.busy&&state.soundEnabled,{
                                    session.reset();audioFailed=false;play(word,{}, {audioFailed=true})
                                }) {
                                    Text(t("Dinle","Listen"),Modifier.weight(1f),textAlign=TextAlign.Center)
                                    DesignArt("ui/parent/sound.webp",Modifier.size(28.dp),decodeLimit=128)
                                }
                                SpeakingActionSurface(Modifier.fillMaxWidth(),2,!session.busy&&!state.busy&&!helpOpen,{
                                    advance()
                                }) {
                                    Text(if(word.id in matched)t(if(index+1==words.size)"Turu bitir" else "Sonraki kelime",if(index+1==words.size)"Finish round" else "Next word") else t("Şimdilik geç","Skip for now"),
                                        Modifier.weight(1f),textAlign=TextAlign.Center)
                                    DesignArt("ui/navigation/next.webp",Modifier.size(24.dp),decodeLimit=128)
                                }
                            }
                            SpeakingActionSurface(Modifier.weight(1f).fillMaxHeight().testTag("speech.record"),0,
                                state.speakingEnabled&&!state.busy&&!helpOpen&&session.phase !in listOf(SpeakingSession.Phase.CHECKING,SpeakingSession.Phase.RECOGNIZING),::speak) {
                                Column(Modifier.fillMaxWidth().padding(vertical=8.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)) {
                                    Box(Modifier.size(58.dp).background(Color.White,CircleShape),contentAlignment=Alignment.Center) {
                                        if(session.phase==SpeakingSession.Phase.LISTENING)Text("■",color=KidHeading,style=MaterialTheme.typography.headlineLarge)
                                        else DesignArt("ui/games/speak-microphone.webp",Modifier.size(50.dp),decodeLimit=192)
                                    }
                                    Text(if(session.phase==SpeakingSession.Phase.LISTENING)t("Bitirdim","I'm done") else t("Konuş","Speak"),
                                        style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black,textAlign=TextAlign.Center)
                                }
                            }
                        }
                        if(audioFailed)Text(t("Ses açılamadı. Yeniden deneyebilirsin.","Could not play audio. Try again."),color=MaterialTheme.colorScheme.error)
                    }
                    if(session.phase==SpeakingSession.Phase.LISTENING) {
                        Text(t("Seni dinliyorum…","Listening to you…"),style=MaterialTheme.typography.titleMedium,color=KidHeading)
                        LinearProgressIndicator(progress={session.seconds/5f},modifier=Modifier.fillMaxWidth())
                        Row(Modifier.height(45.dp),horizontalArrangement=Arrangement.spacedBy(5.dp),verticalAlignment=Alignment.CenterVertically) {
                            repeat(13){bar->Box(Modifier.width(8.dp).height((6+session.level*(18+(bar%4)*7)).dp).background(KidHeading,RoundedCornerShape(50)))}
                        }
                        Text(session.seconds.toInt().coerceAtMost(5).toString()+" / 5 "+t("saniye","seconds"),style=MaterialTheme.typography.bodySmall)
                    }
                    if(session.busy && session.phase!=SpeakingSession.Phase.LISTENING) {
                        CircularProgressIndicator();Text(t("Kelimeyi dinliyorum…","Recognizing your word…"))
                    }
                    if(session.phase !in listOf(SpeakingSession.Phase.READY,SpeakingSession.Phase.CHECKING,SpeakingSession.Phase.LISTENING,SpeakingSession.Phase.RECOGNIZING)) {
                        TextButton({presentedResult=session.phase}){Text(t("Sonucu göster","Show result"))}
                    }
                }
            }
            if(!state.speakingEnabled)KidSurface {
                Text(t("Önce ebeveyninden yardım iste","Ask your parent first"),style=MaterialTheme.typography.titleMedium,color=KidHeading)
                Text(t("Ebeveyn alanı → Ayarlar → Konuşma pratiği bölümünden mikrofonu açabilirsiniz.",
                    "Enable the microphone in Parent area → Settings → Speaking practice."))
                KidAction(t("Ebeveyn alanı","Parent area"),onClick=parent)
            }
        }
        }
        if(index>=0 && !finished)Column(Modifier.fillMaxWidth().background(Color.White.copy(alpha=.96f),RoundedCornerShape(16.dp)).padding(10.dp),
            horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)) {
            Text(if(session.available && session.phase!=SpeakingSession.Phase.UNSUPPORTED)t("Cihazda tanıma desteği var","On-device recognition is supported") else t("Bu dilde cihazda tanıma hazır değil","On-device recognition isn't ready for this language"),
                color=Color(0xFF1966B4),style=MaterialTheme.typography.titleSmall,textAlign=TextAlign.Center)
            Text(t("Duyulan kelimeyi karşılaştırıyoruz; telaffuz yüzdesi vermiyoruz.","We compare the recognized word; no pronunciation percentage is given."),style=MaterialTheme.typography.bodySmall,color=KidSecondary,textAlign=TextAlign.Center)
        }
    }
    val result=presentedResult
    if(result!=null)ModalBottomSheet(onDismissRequest={presentedResult=null},containerColor=Color.White) {
        Column(Modifier.fillMaxWidth().heightIn(max=600.dp).verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(14.dp)) {
            if(result==SpeakingSession.Phase.MATCHED)DesignArt("companions/"+state.companion+".png",Modifier.height(85.dp).fillMaxWidth(),decodeLimit=384)
            else DesignArt("ui/games/speak-microphone.webp",Modifier.size(65.dp),decodeLimit=192)
            Text(when(result) {
                SpeakingSession.Phase.MATCHED -> t("Kelimeyi söyledin!","You said the word!")
                SpeakingSession.Phase.DIFFERENT -> t("Farklı bir kelime duydum","I heard a different word")
                SpeakingSession.Phase.NO_SPEECH -> t("Kelimeyi duyamadım","I couldn't hear a word")
                SpeakingSession.Phase.UNSUPPORTED -> t("Bu dilde cihazda tanıma hazır değil","On-device recognition isn't ready for this language")
                SpeakingSession.Phase.PERMISSION -> t("Konuşma izni gerekiyor","Speech permission is needed")
                else -> t("Ses tanınamadı","Speech recognition couldn't finish")
            },style=MaterialTheme.typography.headlineSmall,color=if(result==SpeakingSession.Phase.MATCHED)KidSuccess else KidHeading,textAlign=TextAlign.Center)
            Text(t("Ekrandaki kelime: ","Target word: ")+(word?.text.orEmpty()),style=MaterialTheme.typography.titleMedium,textAlign=TextAlign.Center)
            if(session.transcript.isNotBlank())Text(t("Duyduğum: ","I heard: ")+session.transcript,style=MaterialTheme.typography.titleMedium,textAlign=TextAlign.Center)
            Text(when(result) {
                SpeakingSession.Phase.MATCHED -> t("Duyduğum kelime ekrandakiyle aynı. Harika!","The word I heard matches the picture. Great!")
                SpeakingSession.Phase.DIFFERENT,SpeakingSession.Phase.NO_SPEECH -> t("Seni yanlış duymuş olabilirim. Örnek sesi dinleyip yeniden söyleyelim.","I may have misheard you. Let's listen to the example and try again.")
                SpeakingSession.Phase.PERMISSION -> t("Mikrofon izni gerekli.","Microphone permission is required.")
                SpeakingSession.Phase.UNSUPPORTED -> t("Bu dilin cihazda çalışan konuşma modeli hazır değil. Kelimeyi dinleyip tekrar edebilirsin.","The on-device speech model for this language is not ready. You can listen and repeat the word.")
                else -> t("Konuşma tanıma şu anda kullanılamıyor. Yeniden deneyebilirsin.","Speech recognition is unavailable right now. You can try again.")
            },textAlign=TextAlign.Center,color=KidBody)
            if(result==SpeakingSession.Phase.UNSUPPORTED)TextButton({
                request=SpeakingStartRequest();presentedResult=null;session.reset();parent()
            }){Text(t("Dil modelini hazırla","Prepare language model"))}
            if(result==SpeakingSession.Phase.PERMISSION)TextButton({
                request=SpeakingStartRequest()
                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+context.packageName)))
            }){Text(t("Android ayarlarını aç","Open Android Settings"))}
            KidAction(if(result==SpeakingSession.Phase.MATCHED)t(if(index+1==words.size)"Turu bitir" else "Sonraki kelime",if(index+1==words.size)"Finish round" else "Next word") else t("Yeniden dene","Try again")) {
                presentedResult=null
                if(result==SpeakingSession.Phase.MATCHED)advance() else session.reset()
            }
            if(result!=SpeakingSession.Phase.MATCHED)TextButton({advance()}){Text(t("Şimdilik geç","Skip for now"))}
        }
    }
}

@Composable private fun SpeakingActionSurface(modifier:Modifier,tone:Int,enabled:Boolean,click:()->Unit,content:@Composable RowScope.()->Unit) {
    val shape=RoundedCornerShape(22.dp)
    val colors=if(!enabled)listOf(Color(0xFFE8E8E8),Color(0xFFCECECE)) else when(tone) {
        1->listOf(Color(0xFF1966B4),Color(0xFF15478C))
        2->listOf(Color(0xFF287F43),Color(0xFF1B6034))
        else->listOf(Color(0xFF7940C8),Color(0xFF54239B))
    }
    Row(modifier.padding(bottom=5.dp).heightIn(min=56.dp).shadow(4.dp,shape).clip(shape).background(Brush.verticalGradient(colors))
        .border(1.5.dp,Color.White.copy(alpha=.5f),shape).clickable(enabled=enabled,role=Role.Button,onClick=click)
        .padding(horizontal=12.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center) {
        CompositionLocalProvider(LocalContentColor provides if(enabled)Color.White else KidSecondary) {
            ProvideTextStyle(MaterialTheme.typography.titleMedium){content()}
        }
    }
}


@Composable private fun SpeakingModelSettings(state:AppState,model:AppModel) {
    val context=LocalContext.current
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    fun t(a:String,b:String)=InterfaceStrings.text(state.ui,a,b)
    fun authorized()=model.state.value.parentGate.allowsManagement(model.state.value.ready)
    val setup=remember(context,model){SpeakingModelSetup(context){model.state.value.parentGate.allowsManagement(model.state.value.ready)}}
    var showDownloadProgress by remember {mutableStateOf(true)}
    LaunchedEffect(setup.status) {if(setup.status==SpeakingModelSetup.Status.DOWNLOADING)showDownloadProgress=true}
    if(setup.status==SpeakingModelSetup.Status.DOWNLOADING && showDownloadProgress) {
        AlertDialog(onDismissRequest={showDownloadProgress=false},title={Text(t("Konuşma tanıma hazırlanıyor","Preparing speech recognition"))},text={
            Column(verticalArrangement=Arrangement.spacedBy(14.dp)) {
                val percent=setup.progress
                if(percent!=null) {
                    LinearProgressIndicator(progress={percent/100f},modifier=Modifier.fillMaxWidth())
                    Text("%$percent",fontWeight=FontWeight.Bold,color=KidHeading)
                } else LinearProgressIndicator(Modifier.fillMaxWidth())
                Text(t("İşleminiz yapılıyor. Dil modeli hazırlanırken lütfen bekleyin…","Processing your request. Please wait while the language model is prepared…"))
            }
        },confirmButton={TextButton({showDownloadProgress=false}){Text(t("Arka planda sürdür","Continue in background"))}})
    }
    LaunchedEffect(state.language){setup.check(state.language)}
    DisposableEffect(setup,lifecycle) {
        val observer=LifecycleEventObserver {_,event->
            if(event==Lifecycle.Event.ON_RESUME && !setup.busy && authorized())setup.check(model.state.value.language)
        }
        lifecycle.addObserver(observer)
        onDispose {lifecycle.removeObserver(observer);setup.close()}
    }
    HorizontalDivider()
    Text(t("Konuşma tanıma kurulumu","Speech recognition setup"),style=MaterialTheme.typography.titleMedium,color=KidHeading)
    if(setup.locale.isNotBlank())Text(java.util.Locale.forLanguageTag(setup.locale).getDisplayName(java.util.Locale.forLanguageTag(state.ui)),
        style=MaterialTheme.typography.bodyMedium,color=KidSecondary)
    Text(when(setup.status) {
        SpeakingModelSetup.Status.PERMISSION->t("Mikrofon izni gerekli.","Microphone permission is required.")
        SpeakingModelSetup.Status.CHECKING->t("Tanıma desteği kontrol ediliyor…","Checking recognition support…")
        SpeakingModelSetup.Status.READY->t("Dil modeli hazır. Konuşma pratiğini açabilirsiniz.","The language model is ready. You can open speaking practice.")
        SpeakingModelSetup.Status.MISSING->t("Bu dil destekleniyor; modeli henüz indirilmemiş.","This language is supported; its model has not been downloaded yet.")
        SpeakingModelSetup.Status.PENDING->t("Hazırlama isteği iletildi. Tamamlandığında yeniden kontrol edin.","The preparation request was sent. Check again when it is complete.")
        SpeakingModelSetup.Status.UNSUPPORTED->t("Bu cihazın tanıma servisi seçili dili cihaz içinde desteklemiyor.","This device's recognition service does not support the selected language on-device.")
        SpeakingModelSetup.Status.UNKNOWN->t("Servis model durumunu bildirmiyor. Modeli hazırlamayı deneyebilirsiniz.","The service does not report model status. You can try preparing the model.")
        SpeakingModelSetup.Status.LEGACY->t("Bu Android sürümünde model kurulumu uygulamadan yönetilemiyor. Ses girişi ayarlarını kontrol edin.","Model installation cannot be managed in the app on this Android version. Check voice input settings.")
        SpeakingModelSetup.Status.UNAVAILABLE->t("Bu cihazda cihaz içi tanıma servisi bulunamadı.","No on-device recognition service was found on this device.")
        SpeakingModelSetup.Status.DOWNLOADING->t("Dil modeli hazırlanıyor…","Preparing the language model…")
        SpeakingModelSetup.Status.FAILED->t("İşlem tamamlanamadı. Bağlantıyı kontrol edip yeniden deneyin.","The operation could not finish. Check your connection and try again.")
    },color=if(setup.status==SpeakingModelSetup.Status.READY)KidSuccess else KidSecondary)
    if(setup.busy || setup.status==SpeakingModelSetup.Status.PENDING) {
        KidSurface(Modifier.semantics {liveRegion=LiveRegionMode.Polite}) {
            val percent=setup.progress
            if(setup.status==SpeakingModelSetup.Status.PENDING) {
                Text(t("Hazırlama isteği beklemede","Preparation request pending"),fontWeight=FontWeight.Bold,color=KidHeading)
                Text(t("Cihaz hazırlama isteğini aldı; tamamlanma bilgisini henüz bildirmedi. Biraz bekleyip Tanımayı kontrol et düğmesine dokunun.","The device received the preparation request but has not reported completion yet. Wait a moment, then tap Check recognition."))
            } else {
                Text(t("İşleminiz yapılıyor…","Processing your request…"),fontWeight=FontWeight.Bold,color=KidHeading)
                if(setup.status==SpeakingModelSetup.Status.DOWNLOADING && percent!=null) {
                    LinearProgressIndicator(progress={percent/100f},modifier=Modifier.fillMaxWidth())
                    Text("%$percent",color=KidHeading)
                } else LinearProgressIndicator(Modifier.fillMaxWidth())
                Text(if(setup.status==SpeakingModelSetup.Status.DOWNLOADING)t("Dil modeli hazırlanıyor. Lütfen bekleyin.","The language model is being prepared. Please wait.")else t("Cihazın konuşma tanıma desteği kontrol ediliyor.","Checking this device's speech recognition support."))
            }
        }
    }
    Text(t("Model hazırlığı internet bağlantısı kullanabilir. Bu işlem mikrofonu açmaz ve ses göndermez.",
        "Preparing a model may use an internet connection. This does not open the microphone or send audio."),style=MaterialTheme.typography.bodySmall,color=KidSecondary)
    TextButton({if(authorized())setup.check(state.language)},enabled=!state.busy&&!setup.busy) {
        Text(t("Tanımayı kontrol et","Check recognition"))
    }
    if(setup.canDownload)KidAction(t("Dil modelini hazırla","Prepare language model"),enabled=!state.busy&&!setup.busy) {
        if(authorized())setup.download(state.language)
    }
    if(setup.status==SpeakingModelSetup.Status.PERMISSION)TextButton({
        if(authorized())context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+context.packageName)))
    }){Text(t("Android ayarlarını aç","Open Android Settings"))}
    if(setup.status!=SpeakingModelSetup.Status.READY)TextButton({
        if(authorized()) {
            val intent=Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)
            runCatching {context.startActivity(intent)}.onFailure {context.startActivity(Intent(Settings.ACTION_SETTINGS))}
        }
    }){Text(t("Ses girişi ayarlarını aç","Open voice input settings"))}
}
