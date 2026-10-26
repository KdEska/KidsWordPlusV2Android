package com.demarpsoftware.kidswordplus

import androidx.activity.compose.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.text.rememberTextMeasurer
import com.demarpsoftware.kidswordplus.family.*
import kotlinx.coroutines.delay

internal val LocalParentBack=staticCompositionLocalOf<MutableState<(() -> Unit)?>?>{null}

@Composable internal fun ParentDashboard(state:AppState,model:AppModel,tab:Int,selectTab:(Int)->Unit,focusSpeech:Boolean=false,packages:()->Unit) {
    val tr=state.ui=="tr"
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    var activity by rememberSaveable{mutableStateOf(false)}
    var language by rememberSaveable{mutableStateOf(false)}
    var privacy by rememberSaveable{mutableStateOf(false)}
    var feedback by rememberSaveable{mutableStateOf(false)}
    val parentBack=LocalParentBack.current
    DisposableEffect(activity,language,privacy,feedback) {
        parentBack?.value=when {feedback -> { {feedback=false} };privacy -> { {privacy=false} };language -> { {language=false} };activity -> { {activity=false} };else -> null}
        onDispose {parentBack?.value=null}
    }
    BackHandler(activity||language||privacy||feedback){when{feedback->feedback=false;activity->activity=false;language->language=false;privacy->privacy=false;else->selectTab(0)}}
    val titles=listOf(t("Ebeveyn paneli","Parent dashboard"),t("Öğrenme raporu","Learning report"),t("Profilim","My profile"),t("Ayarlar","Settings"))
    if(activity || language)PageHelp(if(language)"languageSettings"else "daily")
    ScreenTitle(when{activity->t("Bugünün görevleri","Today's tasks");language->t("Dil tercihleri","Language preferences");privacy->t("Gizlilik","Privacy");feedback->t("Geri bildirim","Feedback");else->titles[tab]})
    val report=remember(state.pack,state.completed,state.gameProgress,state.today){state.pack?.let{ParentReport.build(it,state.completed,state.gameProgress,state.today)}}
    LaunchedEffect(Unit){while(true){model.refreshDay();delay(60_000)}}
    Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            when {
                activity -> Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(16.dp)){TextButton({activity=false}){Text(t("Kapat","Close"))};GameDailyTasks(state)}
                language -> ParentLanguages(state,model,packages)
                privacy -> ParentPrivacyScreen(tr)
                feedback -> ParentFeedback(state)
                tab==2 -> IosProfile(state,model,maxWidth>=600.dp)
                tab==3 -> ParentSettings(state,model,packages,{language=true},{privacy=true},{feedback=true},focusSpeech)
                report==null -> KidSurface{Text(t("Raporu görmek için dil paketini kurun. Önceki ilerleme korunur.","Install a language pack to view its report. Previous progress is kept."));KidAction(t("Dil paketleri","Language packs"),onClick=packages)}
                tab==1 -> ParentReportPage(state,report,tr)
                else -> ParentHome(state,report,tr){activity=true}
            }
        }
        ParentNavigation(tab,state.companion,tr){selectTab(it);activity=false;language=false;privacy=false;feedback=false}
    }
}
@Composable private fun ParentNavigation(tab:Int,companion:String,tr:Boolean,select:(Int)->Unit) {
    val labels=listOf("Ana Sayfa" to "Home","Raporlar" to "Reports","Profil" to "Profile","Ayarlar" to "Settings").map{InterfaceStrings.text(tr,it.first,it.second)}
    Row(Modifier.fillMaxWidth().background(Color.White.copy(alpha=.86f),RoundedCornerShape(topStart=28.dp,topEnd=28.dp)).padding(horizontal=12.dp,vertical=10.dp),horizontalArrangement=Arrangement.spacedBy(4.dp),verticalAlignment=Alignment.CenterVertically) {
        labels.forEachIndexed {index,title ->
            Column(Modifier.weight(1f).clipParentTab(tab==index).semantics(mergeDescendants=true){selected=tab==index;contentDescription=InterfaceStrings.text(tr,"Ebeveyn sekmesi: $title","Parent tab: $title")}
                .clickable(role=Role.Tab){select(index)}.padding(6.dp).heightIn(min=52.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(2.dp,Alignment.CenterVertically)) {
                if(index==2)DesignArt("companions/$companion.png",Modifier.size(52.dp).border(if(tab==2)3.dp else 2.dp,KidHeading.copy(alpha=if(tab==2)1f else .25f),RoundedCornerShape(50)),decodeLimit=192)
                else {ParentArt(when(index){0->"home";1->"words-learned";else->"settings"},36);Text(title,fontSize=12.sp,fontWeight=FontWeight.Bold,color=if(tab==index)KidHeading else KidSecondary,textAlign=TextAlign.Center)}
            }
        }
    }
}
private fun Modifier.clipParentTab(chosen:Boolean)=background(if(chosen)Color(0xFFEDE4FA)else Color.Transparent,RoundedCornerShape(16.dp))
@Composable private fun ParentArt(name:String,size:Int=60) {DesignArt(if(name=="home")"ui/categories/home.webp"else if(name=="daily-star"||name=="repeat-words")"ui/games/$name.webp"else "ui/parent/$name.webp",Modifier.size(size.dp),decodeLimit=256)}
@Composable private fun ParentHome(state:AppState,report:ParentReport,tr:Boolean,activity:()->Unit) {
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(18.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            DesignArt("companions/${state.companion}.png",Modifier.size(54.dp).background(Color.White,RoundedCornerShape(50)),decodeLimit=192)
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {Text(t("Merhaba, Ebeveyn!","Hello, Parent!"),style=MaterialTheme.typography.titleLarge,color=KidHeading);Text(if(state.name.isBlank())t("Çocuğunuzun öğrenme yolculuğu","Your child's learning journey")else InterfaceStrings.render(tr,"{0} ile öğrenme yolculuğu","{0}'s learning journey",state.name),style=MaterialTheme.typography.bodyMedium)}
            IconButton(activity,Modifier.semantics{contentDescription=t("Bugünün görevlerini görüntüle","View today's tasks")}){ParentArt("notifications",42)}
        }
        Text(t("Bu Hafta","This Week"),style=MaterialTheme.typography.titleLarge,color=KidHeading)
        val metrics=listOf(Triple(report.weeklyNew,t("yeni kelime","new words"),"words-learned"),Triple(report.lessons,t("toplam tamamlanan ders","total completed lessons"),"mastered"),Triple(report.weeklyReviews,t("kelime tekrarı","word reviews"),"repeat-words"),Triple(report.activeDays,t("çalışma günü","active days"),"learning-days"))
        val columns=if(LocalDensity.current.fontScale>=1.6f)1 else 2
        val measurer=rememberTextMeasurer()
        val density=LocalDensity.current
        val labelStyle=MaterialTheme.typography.bodyMedium
        val valueStyle=MaterialTheme.typography.headlineMedium
        BoxWithConstraints(Modifier.fillMaxWidth()) {
        val textWidth=with(density){((maxWidth-12.dp*(columns-1))/columns-76.dp).coerceAtLeast(30.dp).roundToPx()}
        val tallest=metrics.maxOf {m -> measurer.measure(m.second,labelStyle,constraints=Constraints(maxWidth=textWidth)).size.height+measurer.measure("${m.first}",valueStyle,constraints=Constraints(maxWidth=textWidth)).size.height}
        val cardHeight=maxOf(92.dp,with(density){tallest.toDp()}+24.dp)
        Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        metrics.chunked(columns).forEach{row -> Row(Modifier.height(cardHeight),horizontalArrangement=Arrangement.spacedBy(12.dp)) {row.forEachIndexed{index,m ->
            Row(Modifier.weight(1f).fillMaxHeight().heightIn(min=92.dp).background(Color.White,RoundedCornerShape(20.dp)).padding(10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                ParentArt(m.third,50)
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)){Text("${m.first}",style=MaterialTheme.typography.headlineMedium,color=KidHeading);Text(m.second,style=MaterialTheme.typography.bodyMedium,color=when(m.third){"mastered"->KidSuccess;"repeat-words"->Color(0xFF945000);"learning-days"->Color(0xFF1765A7);else->KidHeading})}
            }
        }}}
        }
        }
        KidSurface {
            Text(t("Haftalık İlerleme","Weekly Progress"),style=MaterialTheme.typography.titleMedium,color=KidHeading)
            Text(t("Her gün çalışılan yeni kelimeler","New words studied each day"),style=MaterialTheme.typography.bodyMedium,color=KidSecondary)
            WeeklyChart(report,tr)
        }
        KidSurface {
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                ParentArt("daily-star",72)
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)){Text(if(report.weeklyNew>0)t("Harika gidiyorsunuz!","You're doing great!")else t("Yeni bir başlangıç!","A fresh start!"),style=MaterialTheme.typography.titleMedium);Text(if(report.weeklyNew>0)InterfaceStrings.render(tr,"Bu hafta {0} yeni kelime çalışıldı. Birlikte öğrenmeye devam edin!","{0} new words studied this week. Keep learning together!",report.weeklyNew.toString())else t("Bu haftanın ilk kelimelerini birlikte keşfedin.","Discover this week's first words together."),style=MaterialTheme.typography.bodyMedium)}
            }
        }
    }
}
@Composable private fun WeeklyChart(report:ParentReport,tr:Boolean) {
    var visible by remember{mutableStateOf(false)}
    LaunchedEffect(Unit){visible=true}
    val motion=LocalKidMotion.current
    val fraction by animateFloatAsState(if(visible||!motion)1f else 0f,if(motion)tween(700)else snap(),label="parentWeeklyChart")
    val max=maxOf(5,(report.daily.maxOfOrNull{it.second}?:0)+2)
    val labels=if(tr)listOf("Pzt","Sal","Çar","Per","Cum","Cmt","Paz")else listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
    Row(Modifier.fillMaxWidth().heightIn(min=200.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
        Column(Modifier.height(170.dp),verticalArrangement=Arrangement.SpaceBetween){Text("$max",fontSize=11.sp,color=KidSecondary);Text("${max/2}",fontSize=11.sp,color=KidSecondary);Text("0",fontSize=11.sp,color=KidSecondary)}
        report.daily.forEachIndexed{index,(_,count) ->
            Column(Modifier.weight(1f).semantics(mergeDescendants=true){contentDescription="${labels[index]}, $count ${InterfaceStrings.text(tr,"kelime","words")}"},horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)) {
                Column(Modifier.fillMaxWidth().height(170.dp),verticalArrangement=Arrangement.Bottom,horizontalAlignment=Alignment.CenterHorizontally) {
                    Text("$count",fontSize=11.sp,fontWeight=FontWeight.Bold)
                    Box(Modifier.fillMaxWidth(.7f).height((140f*count/max*fraction).dp).background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFFAF52DE),KidHeading)),RoundedCornerShape(topStart=7.dp,topEnd=7.dp)))
                }
                Text(labels[index],fontSize=11.sp,color=KidSecondary,textAlign=TextAlign.Center)
            }
        }
    }
}
@Composable private fun ParentReportPage(state:AppState,report:ParentReport,tr:Boolean) {
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    val day=state.gameProgress.activity(state.language,state.today)
    Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        Text(LearningLanguages.title(state.language)+" · "+t("Öğrenme özeti","Learning summary"),style=MaterialTheme.typography.titleLarge,color=KidHeading)
        ParentCategorySection(t("Güçlü Olduğu Kategoriler","Strong categories"),report.strongest,KidSuccess,tr)
        ParentCategorySection(t("Gelişime Açık Kategoriler","Categories to improve"),report.developing,Color(0xFF945000),tr)
        ParentReportCard(t("Çalışma Süresi","Study time"),"${day.newWords.size+day.reviews.size} ${t("kelime etkinliği","word activities")}","study-time")
        ParentReportCard(t("Çalışılan kelime","Words studied"),"${report.studied}","words-learned")
        ParentReportCard(t("Tamamlanan ders","Lessons completed"),"${report.lessons}","mastered")
        ParentReportCard(t("Bugünün çalışma günü","Today's activity"),"${day.newWords.size+day.reviews.size} ${t("kelime","words")}","learning-days")
        ParentReportCard(t("Kazanılan yıldız","Stars earned"),"${report.stars}","daily-star")
        FamilyShareButton(t("Öğrenme özeti","Learning summary"),"${LearningLanguages.title(state.language)} · ${report.studied} ${t("kelime","words")}",tr,symbol=FamilyShareSymbol.REPORT,parentArea=true)
    }
}
@Composable private fun ParentReportCard(title:String,value:String,art:String) {KidSurface{Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)){ParentArt(art);Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.titleMedium,color=KidHeading);Text(value,style=MaterialTheme.typography.titleLarge)}}}}
@Composable private fun ParentCategorySection(title:String,rows:List<CategoryReport>,color:Color,tr:Boolean) {
    KidSurface{Text(title,style=MaterialTheme.typography.titleLarge,color=KidHeading)}
    if(rows.isEmpty())Text(InterfaceStrings.text(tr,"Henüz yeterli veri yok","Not enough data yet"),color=KidSecondary)
    rows.forEach {category ->
        KidSurface {Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            DesignArt("ui/categories/${category.id}.webp",Modifier.size(58.dp,50.dp),decodeLimit=192)
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)){Text(ContentLabels.title(category.id,tr),style=MaterialTheme.typography.titleMedium);LinearProgressIndicator(progress={category.studied.toFloat()/category.total.coerceAtLeast(1)},modifier=Modifier.fillMaxWidth(),color=color)}
            Text("${category.studied*100/category.total.coerceAtLeast(1)}%",fontWeight=FontWeight.Bold,color=color)
        }}
    }
}

@Composable private fun ParentSettings(state:AppState,model:AppModel,packages:()->Unit,languages:()->Unit,privacy:()->Unit,feedback:()->Unit,focusSpeech:Boolean=false) {
    val tr=state.ui=="tr"
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    val actions=LocalParentPhotoActions.current
    var photoOptions by remember {mutableStateOf(false)}
    var goalOptions by remember {mutableStateOf(false)}
    val context=androidx.compose.ui.platform.LocalContext.current
    val photo by produceState<android.graphics.Bitmap?>(null,state.parentPhotoVersion) {
        value=kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO){android.graphics.BitmapFactory.decodeFile(java.io.File(context.filesDir,"parent-photo.jpg").path)}
    }
    if(photoOptions)AlertDialog(onDismissRequest={photoOptions=false},title={Text(t("Profil fotoğrafı","Profile photo"))},text={Column {
        TextButton({photoOptions=false;actions.gallery()}){Text(t("Galeriden seç","Choose from gallery"))}
        TextButton({photoOptions=false;actions.camera()}){Text(t("Kamerayla çek","Take with camera"))}
    }},confirmButton={TextButton({photoOptions=false}){Text(t("Vazgeç","Cancel"))}})
    if(goalOptions)AlertDialog(onDismissRequest={goalOptions=false},title={Text(t("Günlük hedef","Daily goal"))},text={Column {
        listOf(20,40,60,80,100).forEach {goal -> Row(Modifier.fillMaxWidth().clickable(enabled=!state.busy,role=Role.RadioButton){goalOptions=false;model.setDailyGoal(goal)},verticalAlignment=Alignment.CenterVertically){RadioButton(state.dailyGoal==goal,null);Text("$goal ${t("kelime","words")}")}}
    }},confirmButton={TextButton({goalOptions=false}){Text(t("Vazgeç","Cancel"))}})
    Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        SettingsCard {
            Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){
                val avatar=Modifier.size(104.dp).background(Color.White,RoundedCornerShape(50)).then(Modifier.border(3.dp,Color(0xFF007AFF).copy(alpha=.3f),RoundedCornerShape(50)))
                if(photo!=null)androidx.compose.foundation.Image(photo!!.asImageBitmap(),t("Profil fotoğrafı","Profile photo"),avatar.then(Modifier.clip(RoundedCornerShape(50))),contentScale=androidx.compose.ui.layout.ContentScale.Crop)
                else DesignArt("companions/${state.companion}.png",avatar,decodeLimit=384)
            }
            ParentSettingsRow("profile-edit",t("Profil fotoğrafını değiştir","Change profile photo"),t("Galeriden veya kameradan","From gallery or camera")){photoOptions=true}
        }
        Text(t("Profil ve Ayarlar","Profile and Settings"),style=MaterialTheme.typography.titleLarge,color=KidHeading)
        ParentSettingsRow("download",t("Dil paketleri","Language packs"),t("Dilleri indir ve yönet","Download and manage languages"),packages)
        ParentSettingsRow("target",t("Günlük hedef","Daily goal"),"${state.dailyGoal} ${t("kelime","words")}"){goalOptions=true}
        ParentSettingsRow("learning-language",t("Öğrenme dili","Learning language"),LearningLanguages.title(state.language),languages)
        ParentSettingsRow("learning-language",t("Dil tercihlerini yönet","Manage language preferences"),t("Arayüz ve öğrenme dili","Interface and learning language"),languages)
        ParentPreferenceToggle("sound",t("Ses","Sound"),state.soundEnabled,!state.busy,model::setSoundEnabled)
        ParentPreferenceToggle("daily-star",t("Animasyon","Animation"),state.animationsEnabled,!state.busy,model::setAnimationsEnabled)
        SettingsCard {
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                SettingsIcon("help")
                Column(Modifier.weight(1f)) {
                    Text(t("Yardımı göster","Show help"),style=MaterialTheme.typography.titleMedium,color=KidHeading)
                    Text(t("Öğren, Oyunlar ve Satranç ekranlarındaki yardım simgelerini gösterir.","Show help icons in Learn, Games and Chess."),style=MaterialTheme.typography.bodySmall,color=KidSecondary)
                }
                Switch(checked=state.helpEnabled,onCheckedChange=model::setHelpEnabled,enabled=!state.busy,modifier=Modifier.semantics {contentDescription=t("Yardımı göster","Show help")})
            }
        }
        SpeakingPracticeSettings(state, model,focusSpeech)
        ParentNotificationSettings(state, model)
        ParentSettingsRow("notifications",t("Geri bildirim","Feedback"),t("Bir sorunu, önerinizi veya beğendiğiniz bir şeyi bizimle paylaşın.","Share a problem, a suggestion or something you liked."),feedback)
        if(BuildConfig.DEBUG)LocalStoreReviewTest.current?.let {review ->
            SettingsCard {
                Text(t("Mağaza değerlendirme testi","Store review test"),style=MaterialTheme.typography.titleMedium,color=KidHeading)
                Text(t("Bekleme koşullarını atlar; normal kullanım kayıtlarını değiştirmez. Gerçek pencere Google Play koşullarına bağlıdır.","Skips waiting rules without changing normal usage history. The real dialog depends on Google Play conditions."),color=KidSecondary)
                SettingsAction(t("Mağaza değerlendirmesini test et","Test store review"),enabled=!state.busy && !review.needsParent){review.test(tr)}
                review.testStatus?.let {Text(it,color=KidSecondary)}
            }
        }
        FamilyShareButton("KidsWordPlus","",tr,symbol=FamilyShareSymbol.HEART,parentArea=true,appPromotion=true)
        ParentSettingsRow("privacy",t("Gizlilik","Privacy"),t("Bu cihazdaki veriler","Data on this device"),privacy)
        if(state.chessSaveError)SettingsAction(t("Satranç kaydını kurtar","Recover chess data"),enabled=!state.busy){model.recoverChessSave()}
        SettingsAction(t("Ebeveyn alanını kilitle","Lock parent area"),click=model::relockParent)
    }
}
@Composable private fun ParentPreferenceToggle(art:String,title:String,checked:Boolean,enabled:Boolean,change:(Boolean)->Unit) {
    SettingsCard {Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        SettingsIcon(art)
        Text(title,Modifier.weight(1f),style=MaterialTheme.typography.titleMedium,color=KidHeading)
        Switch(checked,change,enabled=enabled,modifier=Modifier.semantics{contentDescription=title})
    }}
}
@Composable private fun ParentSettingsRow(art:String,title:String,detail:String,click:()->Unit) {
    SettingsCard(Modifier.clickable(role=Role.Button,onClick=click)) {
        Row(Modifier.fillMaxWidth().heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
            SettingsIcon(art)
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                Text(title,style=MaterialTheme.typography.titleMedium,color=KidHeading)
                Text(detail,style=MaterialTheme.typography.bodyMedium,color=KidSecondary)
            }
            Box(Modifier.size(28.dp).background(Color(0xFFF0F2F8),RoundedCornerShape(10.dp)),contentAlignment=Alignment.Center){Text("›",fontSize=22.sp,color=KidSecondary)}
        }
    }
}
@Composable private fun ParentLanguages(state:AppState,model:AppModel,packages:()->Unit) {
    val tr=state.ui=="tr"
    fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    var choosing by remember {mutableStateOf<Int?>(null)}
    Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        Text(t("Dil tercihleri","Language preferences"),style=MaterialTheme.typography.labelLarge,color=KidSecondary)
        KidSurface {
            listOf(true,false).forEachIndexed {index,interfaceLanguage ->
                Box {
                    TextButton(onClick={choosing=index},enabled=!state.busy,modifier=Modifier.fillMaxWidth()) {
                        Text(if(interfaceLanguage)t("Arayüz","Interface")else t("Öğrendiğim dil","Learning language"),Modifier.weight(1f),color=KidBody)
                        Text(LearningLanguages.title(if(interfaceLanguage)state.ui else state.language),color=KidSecondary)
                        Text("  ⌄",color=KidHeading)
                    }
                    DropdownMenu(expanded=choosing==index,onDismissRequest={choosing=null}) {
                        (if(interfaceLanguage)LearningLanguages.names.filterKeys{it in InterfaceStrings.codes}else LearningLanguages.names).forEach {(code,title)->
                            DropdownMenuItem(text={Text(title)},onClick={
                                choosing=null
                                if(interfaceLanguage)model.saveProfile(state.name,code,state.language,false)
                                else model.saveProfile(state.name,state.ui,code,false)
                            },trailingIcon={if(code==if(interfaceLanguage)state.ui else state.language)Text("✓",color=KidHeading)})
                        }
                    }
                }
                HorizontalDivider(color=KidHeading.copy(alpha=.1f))
            }
            TextButton(packages,enabled=!state.busy,modifier=Modifier.fillMaxWidth()) {
                Text(t("Dil paketleri","Language packs"),Modifier.weight(1f),color=KidBody)
                Text("›",color=KidHeading)
            }
        }
    }
}
