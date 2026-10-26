package com.demarpsoftware.kidswordplus

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.demarpsoftware.kidswordplus.content.*
import com.demarpsoftware.kidswordplus.games.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {
    private val model by viewModels<AppModel>()
    private val lessonAds by lazy { LessonBreakAds(this) }
    private var player: MediaPlayer? = null
    private val storeReview by lazy {StoreReviewCoordinator(this)}
    private val interactionAudio by lazy { InteractionAudio(this) {model.state.value.soundEnabled && activeWordAudio==null && player==null && !chessAudio.playing && lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)} }
    private var activeWordAudio by mutableStateOf<String?>(null)
    private val chessAudio by lazy { ChessAudioPlayer(this, ::stopAudio, {model.state.value.soundEnabled}, {model.soundOffMessage(true)}) }
    private val parentGallery=registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()) {uri -> if(uri!=null)model.saveParentPhoto(uri)}
    private val parentCamera=registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()) {bitmap -> if(bitmap!=null)model.saveParentPhoto(bitmap)}
    private var learningInvitation by mutableStateOf(false)
    private var pendingNotificationFeature: String? = null
    private val notificationPermission = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { granted ->
        val feature = pendingNotificationFeature
        pendingNotificationFeature = null
        AppNotifications.refresh()
        if (granted) when (feature) {
            NotificationFeature.PUSH.name -> AppNotifications.setPushEnabled(true)
            NotificationFeature.REMINDERS.name -> AppNotifications.setRemindersEnabled(true)
        }
    }
    private fun notificationChange(feature: NotificationFeature, enabled: Boolean) {
        val state = model.state.value
        if (!state.parentGate.allowsManagement(state.ready)) return
        if (enabled && android.os.Build.VERSION.SDK_INT >= 33 && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            pendingNotificationFeature = feature.name
            AppNotifications.permissionRequested()
            notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            if (enabled && !AppNotifications.canPost()) AppNotifications.permissionRequested()
            when (feature) {
                NotificationFeature.PUSH -> AppNotifications.setPushEnabled(enabled)
                NotificationFeature.REMINDERS -> AppNotifications.setRemindersEnabled(enabled)
            }
        }
    }
    private fun consumeNotification(intent: android.content.Intent?) {
        if (intent?.getBooleanExtra(AppNotifications.OPEN_LEARNING, false) == true || intent?.getStringExtra("destination") == "learning") {
            learningInvitation = true
            intent.removeExtra(AppNotifications.OPEN_LEARNING)
            intent.removeExtra("destination")
        }
    }
    override fun onNewIntent(intent: android.content.Intent) { super.onNewIntent(intent); setIntent(intent); consumeNotification(intent) }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("pendingNotificationFeature", pendingNotificationFeature)
        outState.putBoolean("learningInvitation", learningInvitation)
        super.onSaveInstanceState(outState)
    }
    private var launchArtworkReady by mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingNotificationFeature = savedInstanceState?.getString("pendingNotificationFeature")
        learningInvitation = savedInstanceState?.getBoolean("learningInvitation") ?: false
        consumeNotification(intent)
        if (BuildConfig.DEBUG) lessonAds.start()
        if(android.os.Build.VERSION.SDK_INT>=31)splashScreen.setOnExitAnimationListener {it.remove()}
        enableEdgeToEdge(
            statusBarStyle=SystemBarStyle.light(android.graphics.Color.TRANSPARENT,android.graphics.Color.TRANSPARENT),
            navigationBarStyle=SystemBarStyle.light(android.graphics.Color.TRANSPARENT,android.graphics.Color.TRANSPARENT))
        val preDraw=object:android.view.ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw():Boolean {
                if(launchArtworkReady)window.decorView.viewTreeObserver.removeOnPreDrawListener(this)
                return launchArtworkReady
            }
        }
        window.decorView.viewTreeObserver.addOnPreDrawListener(preDraw)
        lifecycleScope.launch {
            try {withContext(Dispatchers.IO) {
                loadDesignArtwork(this@MainActivity,"ui/backgrounds/garden.webp",2048)
                loadDesignArtwork(this@MainActivity,"ui/brand/logo.webp",1024)
                loadDesignArtwork(this@MainActivity,"ui/games/daily-star.webp",128)
                val character=getSharedPreferences("profile",0).getString("companion","piko-default") ?: "piko-default"
                loadGreetingFrames(this@MainActivity,character)
            }} finally {launchArtworkReady=true}
        }
        InterfaceStrings.load(this)
        InterfaceStrings.language=model.state.value.ui
        setContent { MaterialTheme(typography = KidTypography, shapes = KidShapes, colorScheme = lightColorScheme(primary = KidHeading, onBackground = KidBody, onSurface = KidBody, onSurfaceVariant = KidSecondary, background = Color(0xFFE4F5FF), surface = Color.White, surfaceVariant = Color(0xFFF6F3FA), primaryContainer = Color(0xFFF0E7FF))) { val preferences by model.state.collectAsState()
            LaunchedEffect(preferences.ui, preferences.soundEnabled) { AppNotifications.refresh() }
            LaunchedEffect(preferences.soundEnabled) {if(!preferences.soundEnabled){stopAudio();chessAudio.stop()}}
            KidMotionProvider(preferences.animationsEnabled) { ChessAppearanceProvider {
                CompositionLocalProvider(LocalInteractionSound provides interactionAudio::play, LocalGameAdBreak provides { action ->
                    stopAudio(); chessAudio.stop(); lessonAds.atBreak(action)
                }) { if(launchArtworkReady)App() }
            } }
            InternetRequired { stopAudio(); chessAudio.stop(); model.gameAudioStopped() } } }
    }
    override fun onStart() { super.onStart(); model.chessForeground(true); AppNotifications.refresh() }
    override fun onStop() { model.chessForeground(false); chessAudio.stop(); super.onStop(); stopAudio(); model.gameAudioStopped(); model.relockParent() }
    override fun onDestroy() { lessonAds.dispose(); interactionAudio.release(); chessAudio.release(); super.onDestroy() }
    private fun stopAudio() { interactionAudio.stop(); activeWordAudio=null; player?.release(); player = null }
    private fun playCelebration(ui:String) {
        stopAudio();chessAudio.stop()
        if(!model.state.value.soundEnabled)return
        interactionAudio.play("success")
    }

    private fun play(file: File, onReady: () -> Unit = {}, onError: () -> Unit) {
        chessAudio.stop()
        stopAudio()
        if(!model.state.value.soundEnabled){model.soundOffMessage();return}
        runCatching {
            player = MediaPlayer().apply {
                setDataSource(file.path)
                setOnPreparedListener { if (player === it) { it.start(); activeWordAudio=file.path; onReady() } }
                setOnCompletionListener { stopAudio() }
                setOnErrorListener { _, _, _ -> stopAudio(); onError(); true }
                prepareAsync()
            }
        }.onFailure { stopAudio(); onError() }
    }

    @Composable private fun App() {
        val state by model.state.collectAsState()
        var knownFriends by remember {mutableStateOf(state.earned)}
        var friendBaselineReady by remember {mutableStateOf(state.ready)}
        var showFriends by rememberSaveable {mutableStateOf(false)}
        var newFriend by remember {mutableStateOf<String?>(null)}
        LaunchedEffect(state.earned,state.ready) {
            if(friendBaselineReady && state.ready)(state.earned-knownFriends).firstOrNull()?.let {newFriend=it}
            if(state.ready)friendBaselineReady=true
            knownFriends=state.earned
        }
        LaunchedEffect(newFriend,showFriends){if(newFriend!=null || showFriends)storeReview.dismiss()}
        newFriend?.let {friend ->
            LaunchedEffect(friend){kotlinx.coroutines.delay(180);stopAudio();interactionAudio.play("celebration")}
            CompanionUnlockCelebration(friend,state.ui=="tr",close={newFriend=null},friends={newFriend=null;showFriends=true})
        }
        if(storeReview.needsParent)ReviewParentGate(state.ui=="tr",storeReview::dismiss,storeReview::parentVerified)
        if(showFriends) {
            androidx.compose.ui.window.Dialog(onDismissRequest={showFriends=false},properties=androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth=false)) {
                Box(Modifier.fillMaxSize()) {
                    LearningBackdrop()
                    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                            Text(InterfaceStrings.text(state.ui=="tr","Arkadaşlarım","My friends"),Modifier.weight(1f),style=MaterialTheme.typography.titleLarge,color=KidHeading)
                            TextButton(onClick={showFriends=false}){Text(InterfaceStrings.text(state.ui=="tr","Kapat","Close"))}
                        }
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {CompanionPicker(state,model,state.ui=="tr")}
                    }
                }
            }
        }

        if(model.welcomeVisible) {WelcomeSplash(state) {model.completeWelcome()};return}
        val screenTitle = remember { mutableStateOf<String?>(null) }
        val pageHelp=rememberSaveable(saver=androidx.compose.runtime.saveable.Saver<PageHelpState,Boolean>(save={it.showing},restore={PageHelpState(it)})){PageHelpState()}
        val parentBack=remember {mutableStateOf<(() -> Unit)?>(null)}
        val gamesBack=remember {mutableStateOf<(() -> Unit)?>(null)}
        val chessNavigation=remember{mutableStateOf<ChessNavigation?>(null)}
        var tab by rememberSaveable { mutableIntStateOf(0) }
        var packages by rememberSaveable { mutableStateOf(false) }
        var chessPage by rememberSaveable { mutableStateOf(false) }
        var chessEntry by rememberSaveable {mutableStateOf("world")}
        val parentPages=androidx.compose.runtime.saveable.rememberSaveableStateHolder()
        var parentArea by rememberSaveable { mutableStateOf(false) }
        var parentDestination by rememberSaveable { mutableIntStateOf(0) }
        var focusSpeechSettings by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(parentArea) {if(!parentArea)focusSpeechSettings=false}
        var category by rememberSaveable(state.language) { mutableStateOf<String?>(null) }
        var subcategory by rememberSaveable(state.language) { mutableStateOf<String?>(null) }
        var lessonId by rememberSaveable(state.language) { mutableStateOf<String?>(null) }
        var card by rememberSaveable(state.language) { mutableIntStateOf(0) }
        var finished by rememberSaveable(lessonId) { mutableStateOf(false) }
        var completionEarned by rememberSaveable(lessonId) { mutableIntStateOf(0) }
        var completionCelebrated by rememberSaveable(state.language, lessonId) { mutableStateOf(false) }
        var audioError by remember { mutableStateOf(false) }
        val telemetryScreen = when {
            !state.ready -> "setup"
            packages -> "language_packs"
            parentArea -> "parent_area"
            chessPage -> "chess"
            tab == 1 && lessonId != null -> "word_lesson"
            tab == 1 -> "learn"
            tab == 2 -> "learning_games"
            tab == 3 -> "chess"
            tab == 4 -> "profile"
            else -> "home"
        }
        LaunchedEffect(telemetryScreen) { AppTelemetry.screen(telemetryScreen) }
        val tr = state.ui == "tr"
        fun t(turkish: String, english: String) = InterfaceStrings.text(tr,turkish,english)
        fun back() {
            stopAudio()
            when { parentArea && !packages -> { parentArea = false; parentDestination=0; parentPages.removeState("parent"); model.relockParent() }; chessPage -> { chessPage = false; tab = 0 }; packages -> { packages = false; if (tab != 4) model.relockParent() }; tab == 2 && state.game != null -> model.endGame(); lessonId != null && tab == 1 -> { lessonId = null; finished = false }
                subcategory != null && tab == 1 -> subcategory = null; category != null && tab == 1 -> category = null; tab != 0 -> { tab = 0; model.relockParent() } }
        }
        fun navigateBack() {
            stopAudio();chessAudio.stop()
            when {
                packages -> back()
                parentArea && parentBack.value!=null -> parentBack.value?.invoke()
                !parentArea && tab==2 && gamesBack.value!=null -> gamesBack.value?.invoke()
                !parentArea && (chessPage || tab==3) && chessNavigation.value!=null -> chessNavigation.value?.back?.invoke()
                else -> back()
            }
        }
        BackHandler(chessPage || packages || parentArea || tab != 0) { navigateBack() }
        val pack = state.pack
        val lesson = pack?.lessons?.find { it.id == lessonId }
        var visitedPage by remember {mutableStateOf(false)}
        LaunchedEffect(tab, packages, category, subcategory, state.language, lessonId, card, state.game?.id, state.game?.index) {
            activeWordAudio=null; player?.release(); player=null; audioError = false
            if(visitedPage)interactionAudio.play("page")else visitedPage=true
        }
        val labels = listOf(t("Ana Sayfa", "Home"), t("Öğren", "Learn"), t("Oyunlar", "Games"), t("Satranç", "Chess"), t("Profil", "Profile"))
        fun openLesson(value: Lesson, resume: Boolean) {
            stopAudio()
            category = value.category; subcategory = value.subcategory; lessonId = value.id
            card = if (resume) value.words.indexOfFirst { it.id == state.lastConcept }.coerceAtLeast(0) else 0
            finished = false; completionCelebrated = false; tab = 1
            model.visit(value, value.words[card], false)
        }
        fun selectTab(index: Int) {
            chessEntry="world"
            stopAudio(); if(index != 2) model.endGame(); model.relockParent()
            parentArea = false; focusSpeechSettings=false; parentDestination=0; parentPages.removeState("parent"); chessPage = index == 3; tab = index; packages = false
        }
        if (learningInvitation && state.ready) LearningInvitation(state, dismiss = { learningInvitation = false }) { suggested ->
            learningInvitation = false
            selectTab(1)
            category = null; subcategory = null; lessonId = null
            if (suggested != null) openLesson(suggested, suggested.id == state.lastLesson)
        }
        LaunchedEffect(state.helpEnabled) {if(!state.helpEnabled)pageHelp.showing=false}
        CompositionLocalProvider(LocalStoreReviewTest provides storeReview, LocalCompletionReview provides {offer ->
            val latest=model.state.value
            val total=latest.gameProgress.lessonRewards.values.sumOf {it.size}+latest.gameProgress.days.values.sumOf {it.sessions.size}
            val unobstructed=!model.welcomeVisible && newFriend==null && !showFriends && !parentArea && !packages && !pageHelp.showing && latest.message==null && !latest.busy && lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)
            storeReview.completion(total,offer && unobstructed)
        }, LocalNotificationChange provides ::notificationChange, LocalPageHelp provides pageHelp, LocalWordAudio provides activeWordAudio,
            LocalParentPhotoActions provides ParentPhotoActions(
                gallery={if(state.parentGate.allowsManagement(state.ready))parentGallery.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly))},
                camera={if(state.parentGate.allowsManagement(state.ready))runCatching {parentCamera.launch(null)}.onFailure {android.widget.Toast.makeText(this@MainActivity,InterfaceStrings.text(tr,"Kamera açılamadı.","Camera could not open."),android.widget.Toast.LENGTH_SHORT).show()}})) {
            val topic=when {
                packages && (!state.ready || state.parentGate.unlocked) -> "packages"
                state.ready && !packages && !parentArea && tab==1 -> when {lesson!=null -> "word";subcategory!=null -> "lessons";category!=null -> "subcategories";else -> "categories"}
                else -> null
            }
            if(topic!=null)PageHelp(topic)
            if(state.helpEnabled)PageHelpSheet(pageHelp,tr)
        Surface(Modifier.fillMaxSize()) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val tablet = maxWidth >= 600.dp
                if(!state.ready&&!packages) Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFFDCF7FA),Color.White,Color(0xFFE2F5E5))))) else LearningBackdrop()
                val hasBackButton=(state.ready || packages) && (packages || parentArea ||
                    (tab==1 && category!=null) || (tab==2 && (state.game!=null || gamesBack.value!=null)) ||
                    ((chessPage || tab==3) && chessNavigation.value!=null))
                Column(Modifier.fillMaxSize().safeDrawingPadding()
                    .edgeBackGesture(hasBackButton && !pageHelp.showing, ::navigateBack)) {
                    val rootHome = tab == 0 && !chessPage && !packages && !parentArea
                    if (!rootHome && (state.ready || packages)) Row(Modifier.fillMaxWidth().heightIn(min = 44.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (packages || parentArea || (tab == 1 && category != null) || (tab == 2 && (state.game != null || gamesBack.value!=null)) || (tab==3&&chessNavigation.value!=null)) TextButton(onClick = ::navigateBack) {
                            DesignArt("ui/navigation/back.webp", Modifier.size(26.dp), decodeLimit = 128)
                            Text(t("Geri", "Back"))
                        } else Spacer(Modifier.width(52.dp))
                        Text(when { packages -> t("Dil paketleri", "Language packs"); parentArea -> screenTitle.value ?: t("Ebeveyn alanı", "Parent area"); tab == 1 && lesson != null -> "${t("Ders", "Lesson")} ${lesson.number}"; tab == 1 -> if(category == null)t("Kategoriler", "Categories") else label(subcategory ?: category!!, tr); tab == 2 -> screenTitle.value ?: t("Oyunlar", "Games"); tab == 3 -> screenTitle.value ?: t("Satranç dünyası", "Chess world"); else -> t("Profilim", "My profile") }, Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.titleMedium)
                        if(tab == 4 && !parentArea && !packages) IconButton(onClick = { parentArea = true }) { DesignArt("ui/parent/settings.webp", Modifier.size(32.dp), description = t("Ayarlar", "Settings"), decodeLimit = 128) }
                        else if(tab==3&&chessNavigation.value?.settings!=null)Row(verticalAlignment=Alignment.CenterVertically) {
                            IconButton(onClick={chessNavigation.value?.settings?.invoke()},modifier=Modifier.semantics{contentDescription=t("Maç ayarları","Match settings")}) {DesignArt("ui/parent/settings.webp",Modifier.size(32.dp),decodeLimit=128)}
                            if(state.helpEnabled&&pageHelp.topic!=null)PageHelpButton(!state.busy,tr){stopAudio();chessAudio.stop();pageHelp.showing=true}
                        }
                        else if(tab==3&&chessNavigation.value?.reset!=null)Row(verticalAlignment=Alignment.CenterVertically) {
                            IconButton(onClick={chessNavigation.value?.reset?.invoke()},modifier=Modifier.semantics{contentDescription=t("Baştan başla","Reset")}){Text("↶",color=KidHeading,fontSize=26.sp)}
                            if(state.helpEnabled&&pageHelp.topic!=null)PageHelpButton(!state.busy,tr){stopAudio();chessAudio.stop();pageHelp.showing=true}
                        }
                        else if(state.helpEnabled && pageHelp.topic!=null) PageHelpButton(!state.busy,tr) {stopAudio();chessAudio.stop();pageHelp.showing=true}
                        else Spacer(Modifier.width(52.dp))
                    }
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                        Column(Modifier.widthIn(max = if(tablet && (tab == 0 || tab == 1 || tab == 3))1120.dp else 960.dp).fillMaxWidth().padding(horizontal = 20.dp, vertical = if(rootHome) 12.dp else 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (state.message != null) Card { Column(Modifier.padding(12.dp)) {
                                Text(state.message!!, color = MaterialTheme.colorScheme.error)
                                TextButton(onClick = model::clearMessage) { Text(t("Kapat", "Dismiss")) }
                            } }
                            when {
                                state.ready && (packages || parentArea) && !state.parentGate.unlocked -> ParentGateScreen(state, model)
                                packages -> PackageScreen(state, model, tr)
                                !state.ready -> IosSetup(state, model)
                                chessPage -> CompositionLocalProvider(LocalChessAudio provides chessAudio, LocalScreenTitle provides screenTitle, LocalChessNavigation provides chessNavigation, LocalPageHelp provides pageHelp) { ChessScreen(state, model, tablet,initialDestination=chessEntry) }
                                tab == 4 && !parentArea -> IosProfile(state, model, tablet)
                                parentArea -> parentPages.SaveableStateProvider("parent") { CompositionLocalProvider(LocalScreenTitle provides screenTitle,LocalParentBack provides parentBack) { ParentDashboard(state, model, parentDestination, { parentDestination=it },focusSpeech=focusSpeechSettings) { packages = true } } }
                                tab == 0 -> IosHome(state, tablet, ::openLesson,
                                    { category = it; subcategory = null; lessonId = null; tab = 1 },
                                    { category = null; subcategory = null; lessonId = null; tab = 1 },
                                    { selectTab(4) }, { packages = true }, { word -> play(pack!!.audio(word)){audioError=true} }, {destination->selectTab(3);chessEntry=destination})
                                pack == null -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(t("Öğrenmeye başlamak için bir dil paketi kur.", "Install a language pack to start learning."))
                                    IosButton(onClick = { packages = true }) { Text(t("Dil paketleri", "Language packs")) }
                                }
                                tab == 2 -> CompositionLocalProvider(LocalScreenTitle provides screenTitle,LocalGamesBack provides gamesBack) { GamesScreen(state, model, tablet, { word, heard, failed -> play(pack.audio(word), heard, failed) }, ::stopAudio, { category=null;subcategory=null;lessonId=null;selectTab(1) }, {focusSpeechSettings=true;parentDestination=3;parentArea=true}, {playCelebration(state.ui)}) }
                                lesson != null -> {
                                    if (finished) LessonCompletionScreen(state,lesson,pack!!,completionEarned,completionCelebrated,
                                        LessonCompletionRules.following(pack.lessons,lesson),{ next, resume -> stopAudio(); lessonAds.atBreak { openLesson(next, resume) } },{ stopAudio(); lessonAds.atBreak { back() } },{
                                            if(completionCelebrated) false else {
                                                completionCelebrated=true
                                                playCelebration(state.ui)
                                                true
                                            }
                                        },::stopAudio)
                                    else {
                                        val index = card.coerceIn(0, lesson.words.lastIndex)
                                        val word = lesson.words[index]
                                        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                                            Column(Modifier.weight(1f).fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                                                Row(Modifier.fillMaxWidth()) {Text("${index+1} / ${lesson.words.size}",Modifier.weight(1f),fontWeight=FontWeight.Bold,fontSize=15.sp);Text(label(lesson.subcategory,tr),color=KidSecondary,fontSize=15.sp)}
                                                LinearProgressIndicator(progress={(index+1).toFloat()/lesson.words.size},modifier=Modifier.fillMaxWidth(),color=KidSuccess,drawStopIndicator={})
                                                KidSurface(Modifier.weight(1f)) {
                                                    WordImage(pack.image(word),word.text,tablet,tr,modifier=Modifier.weight(1f),stretch=true)
                                                    Text(word.text,Modifier.fillMaxWidth(),textAlign=androidx.compose.ui.text.style.TextAlign.Center,style=MaterialTheme.typography.headlineLarge,color=KidHeading)
                                                    KidAction(t("Dinle","Listen"),tone=1,audioActive=activeWordAudio==pack.audio(word).path){play(pack.audio(word)){audioError=true}}
                                                    if(audioError)Text(t("Ses oynatılamadı. Yeniden deneyebilirsin.","Audio could not play. Try again."),color=MaterialTheme.colorScheme.error)
                                                }
                                            }
                                            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                                                Box(Modifier.weight(1f),contentAlignment=Alignment.CenterStart) {
                                                    Box(Modifier.widthIn(max=160.dp).fillMaxWidth()) {
                                                        KidAction(t("Geri","Back"),enabled=index>0&&!state.busy,tone=1,icon="back"){stopAudio();model.visit(lesson,lesson.words[index-1],false){card=index-1}}
                                                    }
                                                }
                                                Box(Modifier.weight(1f),contentAlignment=Alignment.CenterEnd) {
                                                Box(Modifier.widthIn(max=280.dp).fillMaxWidth()) {KidAction(if(index==lesson.words.lastIndex)t("Bitir","Finish") else t("Devam","Next"),!state.busy,if(index==lesson.words.lastIndex)2 else 0){
                                                    val starsBefore=state.gameProgress.stars(state.language)
                                                    stopAudio();model.visit(lesson,word,true,lesson.words[(index+1).coerceAtMost(lesson.words.lastIndex)]) {if(index==lesson.words.lastIndex){completionEarned=LessonCompletionRules.earnedStars(starsBefore,model.state.value.gameProgress.stars(state.language));completionCelebrated=false;finished=true}else card=index+1}
                                                }}}
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    val choices: List<BrowseItem> = when {
                                        category == null -> pack.words.groupBy { it.category }.keys.sortedBy { LearningPack.categoryOrder.indexOf(it) }.map { c ->
                                            BrowseItem(c, label(c, tr), pack.words.filter { it.category == c }) }
                                        subcategory == null -> pack.words.filter { it.category == category }.groupBy { it.subcategory }.map { (key, words) -> BrowseItem(key, label(key, tr), words) }
                                        else -> pack.lessons.filter { it.category == category && it.subcategory == subcategory }.map { BrowseItem(it.id, "${t("Ders", "Lesson")} ${it.number}", it.words) }
                                    }
                                    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                        if(category == null) {
                                            Text(t("Bir dünya seç, keşfe çık!", "Choose a world to explore!"), style = MaterialTheme.typography.titleLarge, color = KidHeading)
                                            Text("${pack.words.count { LearningRules.progressKey(state.language,it.id) in state.completed }} / ${pack.words.size} ${t("kelime çalışıldı", "words studied")}", color = KidHeading, fontWeight = FontWeight.Bold)
                                            IosCategoryGrid(state, choices.map { it.id }) { category = it }
                                        } else {
                                            KidSurface {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                                    DesignArt("ui/categories/$category.webp", Modifier.size(64.dp), decodeLimit = 256)
                                                    Column { Text(label(subcategory ?: category!!,tr), style = MaterialTheme.typography.titleLarge, color = KidHeading)
                                                        val words = pack.words.filter { it.category == category && (subcategory == null || it.subcategory == subcategory) }
                                                        Text("${words.count { LearningRules.progressKey(state.language,it.id) in state.completed }} / ${words.size} ${t("kelime çalışıldı","words studied")}", color = KidSecondary)
                                                    }
                                                }
                                            }
                                            choices.forEach { item ->
                                                val studied = item.words.count { LearningRules.progressKey(state.language,it.id) in state.completed }
                                                Card(onClick = { if(subcategory == null)subcategory = item.id else openLesson(pack.lessons.first {it.id == item.id},false) }, enabled = !state.busy,
                                                    colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, if(studied > 0)KidSuccess.copy(alpha=.65f) else Color.Gray.copy(alpha=.18f)), elevation = CardDefaults.cardElevation(3.dp)) {
                                                    Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                                        if(subcategory == null) DesignArt("ui/subcategories/$category/${item.id}.webp",Modifier.size(104.dp,112.dp),decodeLimit=384)
                                                        else Box(Modifier.width(104.dp)) { WordImage(pack.image(item.words.first()),item.title,tablet,tr,112) }
                                                        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                                                            Text(item.title,style=MaterialTheme.typography.titleMedium)
                                                            if(subcategory == null)Text("${pack.lessons.count {it.category == category && it.subcategory == item.id}} ${t("ders","lessons")}",color=KidSecondary)
                                                            Text("$studied / ${item.words.size} ${t("kelime çalışıldı","words studied")}",color=KidSecondary,fontSize=15.sp)
                                                            LinearProgressIndicator(progress={studied.toFloat()/item.words.size},modifier=Modifier.fillMaxWidth(),color=KidSuccess,drawStopIndicator={})
                                                        }
                                                        val itemCompleted=item.words.isNotEmpty() && studied==item.words.size
                                                        DesignArt(if(itemCompleted)"ui/navigation/green-tick.webp" else "ui/navigation/next.webp",
                                                            Modifier.size(26.dp),description=if(itemCompleted)t("Tamamlandı","Completed") else null,decodeLimit=128)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(state.ready && !parentArea && (tab!=3||chessNavigation.value?.hideTabs!=true)) Box(Modifier.widthIn(max = 720.dp).fillMaxWidth().align(Alignment.CenterHorizontally)) { IosTabBar(labels,tab,::selectTab) }
                }
            }
        }
        }
    }
}
private data class BrowseItem(val id: String, val title: String, val words: List<Word>)

@Composable internal fun WordImage(file: File, description: String, tablet: Boolean, tr: Boolean, height: Int = 0, modifier: Modifier = Modifier, stretch: Boolean = false) {
    val bitmap by key(file.path) { produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, file.path) {
        value = withContext(Dispatchers.IO) { runCatching {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.path, options)
            options.inSampleSize = 1
            while (options.outWidth / options.inSampleSize > 1024 || options.outHeight / options.inSampleSize > 1024) options.inSampleSize *= 2
            options.inJustDecodeBounds = false
            BitmapFactory.decodeFile(file.path, options)?.asImageBitmap()
        }.getOrNull() }
    } }
    BoxWithConstraints(modifier.then(if(stretch)Modifier.fillMaxWidth() else Modifier.fillMaxWidth().height((if(height>0)height else if(tablet)320 else 220).dp)),contentAlignment=Alignment.Center) {
        val image=bitmap
        if(image!=null) {
            val ratio=image.width.toFloat()/image.height
            val imageWidth=minOf(maxWidth,maxHeight*ratio)
            Image(image,description,Modifier.size(imageWidth,imageWidth/ratio).clip(RoundedCornerShape(20.dp)))
        } else Text(InterfaceStrings.text(tr,"Görsel yükleniyor veya kullanılamıyor.","Image is loading or unavailable."))
    }
}

@Composable private fun ProfileScreen(state: AppState, model: AppModel, tr: Boolean, openPackages: () -> Unit) {
    var name by rememberSaveable(state.name) { mutableStateOf(state.name) }
    var ui by rememberSaveable(state.ui) { mutableStateOf(state.ui) }
    var language by rememberSaveable(state.language) { mutableStateOf(state.language) }
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(t("Birlikte öğrenmeye başlayalım", "Let's start learning"), style = MaterialTheme.typography.titleLarge)
        CompanionImage(state.companion, 120)
        Text(t("Yol arkadaşın", "Your companion"))
        com.demarpsoftware.kidswordplus.family.Companions.all.take(6).chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { pair.forEach { friend ->
                FilterChip(selected = state.companion == friend.id, enabled = !state.busy, onClick = { model.selectCompanion(friend.id) }, label = { Text(friend.name) })
            } }
        }
        OutlinedTextField(name, { name = it.take(30) }, label = { Text(t("Adın (isteğe bağlı)", "Name (optional)")) }, modifier = Modifier.fillMaxWidth())
        Text(t("Arayüz dili", "Interface language"))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("tr" to "Türkçe", "en" to "English").forEach { (code, title) ->
            FilterChip(selected = ui == code, onClick = { ui = code }, label = { Text(title) }) } }
        Text(t("Öğrenme dili", "Learning language"))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("tr" to "Türkçe", "en" to "English").forEach { (code, title) ->
            FilterChip(selected = language == code, onClick = { language = code }, label = { Text(title) }) } }
        OutlinedButton(enabled = !state.busy, onClick = openPackages) { Text(t("Dil paketleri", "Language packs")) }
        if (language !in state.installed) Text(t("Devam etmeden önce seçtiğin dil paketini kur.", "Install the selected language before continuing."))
        IosButton(enabled = !state.busy && language in state.installed, onClick = { model.saveProfile(name, ui, language, true) }) { Text(t("Kaydet ve devam et", "Save and continue")) }
        if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
    }
}

@Composable private fun PackageScreen(state: AppState, model: AppModel, tr: Boolean) {
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    var deleting by remember {mutableStateOf<String?>(null)}
    Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        KidSurface {
            Text(t("Ortak görseller Temel dil paketindedir ve ilk dilinle birlikte otomatik kurulur. Her dil kendi kelimelerini, seslerini ve dile özel görsellerini içerir. Kurulumdan sonra çevrimdışı çalışır.","Shared images are in the Base language pack, installed automatically with your first language. Each language contains its words, audio and language-specific images. Installed packs work offline."),style=MaterialTheme.typography.bodyMedium)
            if(BuildConfig.DEBUG&&state.localCatalog)Text(t("Geliştirme sürümü · Yerel test paketleri","Development build · Local test packs"),style=MaterialTheme.typography.bodyMedium,color=KidSecondary)
        }
        state.entries.find{it.language=="base"}?.let {base ->
            KidSurface {
                Text(t("Temel dil paketi","Base language pack"),style=MaterialTheme.typography.titleMedium,color=KidHeading)
                Text(t("Tüm dillerin kullandığı ortak görseller","Shared images used by every language"))
                Text(if("base" in state.installedDetails)t("Kurulu · Diğer dillerle ortak kullanılır","Installed · Shared with other languages")else t("İlk dil paketiyle otomatik kurulur.","Installed automatically with the first language pack."),color=KidSecondary)
                PackageSizeRow(t("Paket boyutu","Package size"),base.bytes)
            }
        }
        val entries=state.entries.filter{it.language in LearningLanguages.codes}
        val codes=(entries.map{it.language}+state.installed.filter{it in LearningLanguages.codes}).distinct()
        codes.forEach {code ->
            val entry=entries.find{it.language==code}
            KidSurface {
                Text(LearningLanguages.title(code),style=MaterialTheme.typography.titleLarge,color=KidHeading)
                entry?.let {
                    PackageSizeRow(t("Paket boyutu","Package size"),it.bytes)
                    val missingBase=state.entries.find {base->base.language=="base" && it.dependencies.any {dep->dep.language=="base" && dep.hash!=state.installedDetails["base"]?.manifestHash}}
                    if(missingBase!=null)PackageSizeRow(t("Temel paketle toplam indirme","Download including base pack"),it.bytes+missingBase.bytes)
                }
                if(code in state.installed) {
                    Text(t("✓ Kurulu · Çevrimdışı hazır","✓ Installed · Available offline"),color=KidSuccess)
                    state.installedDetails[code]?.let {PackageSizeRow(t("Cihazda","On device"),it.bytes)}
                    KidAction(if(code==state.language)t("Seçili öğrenme dili","Selected learning language")else t("Bu dili öğren","Learn this language"),enabled=!state.busy&&code!=state.language){model.saveProfile(state.name,state.ui,code,state.ready)}
                    TextButton({deleting=code},enabled=!state.busy){Text(t("Paketi sil","Remove pack"),color=MaterialTheme.colorScheme.error)}
                }
                if(entry!=null && !(state.busy&&state.operationLanguage==code) && state.installedDetails[code]?.manifestHash!=entry.manifestHash)KidAction(if(code in state.installed)t("Güncelle","Update")else t("Paketi kur","Install pack"),enabled=!state.busy,tone=1,icon="download"){model.install(code)}
                if(state.busy&&state.operationLanguage==code) {
                    Text(t("Kuruluyor ve doğrulanıyor…","Installing and verifying…"))
                    LinearProgressIndicator(progress={state.progress},modifier=Modifier.fillMaxWidth())
                    TextButton(model::cancel){Text(t("İptal et","Cancel"))}
                }
            }
        }
        if(codes.isEmpty()&&!state.busy)Text(t("Dil paketi listesi şu anda alınamıyor. Listeyi yenileyerek tekrar deneyebilirsin.","The language pack list is currently unavailable. Refresh the list to try again."))
        TextButton({model.refreshCatalog(true)},enabled=!state.busy){Text(t("Listeyi yenile","Refresh list"))}
        if(BuildConfig.DEBUG)TextButton({model.refreshCatalog(false)},enabled=!state.busy){Text(t("Yerel paketlere dön","Use local packages"))}
    }
    deleting?.let {code -> AlertDialog(onDismissRequest={deleting=null},title={Text(t("Paketi sil?","Remove pack?"))},text={Text(t("Bu dil paketi cihazdan kaldırılacak. Öğrenme ilerlemesi korunur.","This language pack will be removed from this device. Learning progress is kept."))},confirmButton={TextButton({deleting=null;model.remove(code)}){Text(t("Paketi sil","Remove pack"))}},dismissButton={TextButton({deleting=null}){Text(t("Vazgeç","Cancel"))}})}
}

@Composable private fun PackageSizeRow(title:String,bytes:Long) {
    val size=android.text.format.Formatter.formatShortFileSize(LocalContext.current,bytes)
    if(androidx.compose.ui.platform.LocalDensity.current.fontScale>=1.6f)Column {Text(title,color=KidBody);Text(size,color=KidSecondary)}
    else Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(16.dp)) {Text(title,Modifier.weight(1f),color=KidBody);Text(size,color=KidSecondary)}
}

internal fun label(id: String, tr: Boolean) = ContentLabels.title(id, tr)
