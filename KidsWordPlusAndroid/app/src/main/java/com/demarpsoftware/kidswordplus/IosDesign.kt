package com.demarpsoftware.kidswordplus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.demarpsoftware.kidswordplus.content.*
import kotlin.math.max

internal val KidHeading = Color(0xFF5827A6)
internal val KidBody = Color(0xFF20284B)
internal val KidSecondary = Color(0xFF48516D)
internal val KidSuccess = Color(0xFF24713B)
internal val KidTypography = Typography().let { base ->
    base.copy(
        headlineLarge = base.headlineLarge.copy(fontFamily=FontFamily(android.graphics.Typeface.create("sans-serif-rounded",android.graphics.Typeface.BOLD)), fontSize=34.sp, lineHeight=40.sp, fontWeight=FontWeight.Bold),
        headlineMedium = base.headlineMedium.copy(fontFamily=FontFamily(android.graphics.Typeface.create("sans-serif-rounded",android.graphics.Typeface.BOLD)), fontSize=28.sp, lineHeight=34.sp, fontWeight=FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontFamily=FontFamily(android.graphics.Typeface.create("sans-serif-rounded",android.graphics.Typeface.BOLD)), fontSize=22.sp, lineHeight=28.sp, fontWeight=FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontFamily=FontFamily(android.graphics.Typeface.create("sans-serif-rounded",android.graphics.Typeface.BOLD)), fontSize=17.sp, lineHeight=22.sp, fontWeight=FontWeight.Bold),
        bodyLarge = base.bodyLarge.copy(fontSize=17.sp, lineHeight=23.sp),
        bodyMedium = base.bodyMedium.copy(fontSize=15.sp, lineHeight=20.sp),
        labelLarge = base.labelLarge.copy(fontFamily=FontFamily(android.graphics.Typeface.create("sans-serif-rounded",android.graphics.Typeface.BOLD)), fontSize=17.sp, lineHeight=22.sp, fontWeight=FontWeight.Bold))
}
internal val KidShapes = Shapes(extraSmall=RoundedCornerShape(10.dp),small=RoundedCornerShape(16.dp),medium=RoundedCornerShape(24.dp),large=RoundedCornerShape(26.dp),extraLarge=RoundedCornerShape(28.dp))
internal val MenuTones = listOf(0xFF85C933 to 0xFF295C17,0xFFFFB033 to 0xFF9C3B0A,0xFF45B0FA to 0xFF1447A3,0xFFA666FA to 0xFF4D1FA6,0xFFF56EB3 to 0xFF961F61,0xFF29C7CC to 0xFF055970)

@Composable internal fun KidProgress(value: Float, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha=.10f)).border(1.5.dp,Color.White.copy(alpha=.8f),RoundedCornerShape(50))) {
        if(value>0) Box(Modifier.fillMaxHeight().fillMaxWidth(value.coerceIn(0f,1f)).background(Brush.verticalGradient(listOf(Color(0xFF99DB3B),Color(0xFF42A61A)))))
    }
}
@Composable internal fun KidAction(title: String, enabled: Boolean = true, tone: Int = 0, icon: String? = null, audioActive: Boolean? = null, onClick: () -> Unit) {
    val sound=LocalInteractionSound.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val offset by animateFloatAsState(if(pressed && enabled && LocalKidMotion.current)4f else 0f,if(LocalKidMotion.current)tween(120) else snap(),label="kidActionPress")
    val colors = when(tone) { 1 -> listOf(Color(0xFF1966B4),Color(0xFF15478C)); 2 -> listOf(Color(0xFF287F43),Color(0xFF1B6034)); 3 -> listOf(Color(0xFFB95014),Color(0xFF91370C)); else -> listOf(Color(0xFF7940C8),Color(0xFF54239B)) }
    BoxWithConstraints(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center) {
    Row(Modifier.widthIn(max=if(maxWidth>=600.dp)280.dp else maxWidth).fillMaxWidth().wordAudioEffect(audioActive==true).offset(y=offset.dp).padding(bottom=5.dp).shadow(4.dp,RoundedCornerShape(22.dp)).clip(RoundedCornerShape(22.dp))
        .background(if(enabled) Brush.verticalGradient(colors) else Brush.verticalGradient(listOf(Color(0xFFE8E8E8),Color(0xFFCECECE))))
        .border(1.5.dp,Color.White.copy(alpha=.5f),RoundedCornerShape(22.dp)).clickable(interactionSource=interaction,indication=null,enabled=enabled,role=Role.Button,onClick={onClick();if(tone!=1)sound("tap")}).padding(horizontal=14.dp,vertical=6.dp).heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically) {
        if(audioActive!=null)WordAudioIndicator(audioActive)
        if(icon=="back"||icon=="profile-edit")Box(Modifier.size(36.dp).background(Color.White,RoundedCornerShape(50)),contentAlignment=Alignment.Center) {DesignArt(if(icon=="profile-edit")"ui/parent/profile-edit.webp"else "ui/navigation/back.webp",Modifier.size(28.dp),decodeLimit=128)}
        Text(title,Modifier.weight(1f),color=if(enabled) Color.White else KidSecondary,textAlign=TextAlign.Center,style=MaterialTheme.typography.titleMedium)
        if(icon!="back"&&icon!="profile-edit")Box(Modifier.size(36.dp).background(Color.White,RoundedCornerShape(50)),contentAlignment=Alignment.Center) {
            if(icon=="hint")Canvas(Modifier.size(24.dp)) {
                val w=size.width
                drawCircle(KidHeading,w*.26f,androidx.compose.ui.geometry.Offset(w*.5f,w*.36f),style=androidx.compose.ui.graphics.drawscope.Stroke(w*.08f))
                drawLine(KidHeading,androidx.compose.ui.geometry.Offset(w*.4f,w*.68f),androidx.compose.ui.geometry.Offset(w*.6f,w*.68f),w*.08f,StrokeCap.Round)
                drawLine(KidHeading,androidx.compose.ui.geometry.Offset(w*.42f,w*.82f),androidx.compose.ui.geometry.Offset(w*.58f,w*.82f),w*.08f,StrokeCap.Round)
            }else if(icon=="undo")Canvas(Modifier.size(24.dp)) {
                val w=size.width
                drawArc(KidHeading,210f,270f,false,topLeft=androidx.compose.ui.geometry.Offset(w*.22f,w*.25f),size=androidx.compose.ui.geometry.Size(w*.6f,w*.6f),style=androidx.compose.ui.graphics.drawscope.Stroke(w*.09f))
                val arrow=Path().apply{moveTo(w*.12f,w*.15f);lineTo(w*.12f,w*.51f);lineTo(w*.46f,w*.43f);close()}
                drawPath(arrow,KidHeading)
            }else if(icon=="share")Canvas(Modifier.size(24.dp)) {
                val w=size.width
                val a=androidx.compose.ui.geometry.Offset(w*.22f,w*.5f)
                val b=androidx.compose.ui.geometry.Offset(w*.76f,w*.22f)
                val c=androidx.compose.ui.geometry.Offset(w*.76f,w*.78f)
                drawLine(KidHeading,a,b,w*.08f,StrokeCap.Round)
                drawLine(KidHeading,a,c,w*.08f,StrokeCap.Round)
                listOf(a,b,c).forEach { drawCircle(KidHeading,w*.13f,it) }
            }else if(icon=="edit")Canvas(Modifier.size(24.dp)) {
                val w=size.width
                drawLine(KidHeading,androidx.compose.ui.geometry.Offset(w*.25f,w*.75f),androidx.compose.ui.geometry.Offset(w*.75f,w*.25f),w*.16f,StrokeCap.Square)
                val tip=Path().apply {moveTo(w*.12f,w*.88f);lineTo(w*.17f,w*.66f);lineTo(w*.34f,w*.83f);close()}
                drawPath(tip,KidHeading)
            }else if(icon=="play")Canvas(Modifier.size(24.dp)) {
                val triangle=Path().apply{moveTo(size.width*.3f,size.height*.16f);lineTo(size.width*.8f,size.height*.5f);lineTo(size.width*.3f,size.height*.84f);close()}
                drawPath(triangle,KidHeading)
            }else if(icon=="download") Canvas(Modifier.size(24.dp)) {
                val w=size.width
                val a=androidx.compose.ui.geometry.Offset(w*.5f,w*.15f)
                val b=androidx.compose.ui.geometry.Offset(w*.5f,w*.65f)
                drawLine(KidHeading,a,b,w*.10f,StrokeCap.Round)
                drawLine(KidHeading,androidx.compose.ui.geometry.Offset(w*.28f,w*.45f),b,w*.10f,StrokeCap.Round)
                drawLine(KidHeading,androidx.compose.ui.geometry.Offset(w*.72f,w*.45f),b,w*.10f,StrokeCap.Round)
                drawLine(KidHeading,androidx.compose.ui.geometry.Offset(w*.2f,w*.85f),androidx.compose.ui.geometry.Offset(w*.8f,w*.85f),w*.10f,StrokeCap.Round)
            } else if(icon=="replay")DesignArt("ui/games/repeat-words.webp",Modifier.size(28.dp),decodeLimit=128)
            else DesignArt(if(icon=="back")"ui/navigation/back.webp" else if(tone==1) "ui/parent/sound.webp" else if(tone==2) "ui/navigation/green-tick.webp" else "ui/navigation/next.webp",Modifier.size(28.dp),decodeLimit=128)
        }
    }
    }
}
@Composable internal fun KidSurface(modifier: Modifier = Modifier, content: @Composable ColumnScope.()->Unit) {
    Column(modifier.fillMaxWidth().shadow(4.dp,RoundedCornerShape(24.dp),ambientColor=KidHeading.copy(alpha=.10f),spotColor=KidHeading.copy(alpha=.10f))
        .background(Color.White,RoundedCornerShape(24.dp)).border(1.dp,KidHeading.copy(alpha=.12f),RoundedCornerShape(24.dp)).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp),content=content)
}
@Composable internal fun MenuBackground(index: Int, modifier: Modifier = Modifier, content: @Composable BoxScope.()->Unit) {
    val (a,b)=MenuTones[index%MenuTones.size];val shape=RoundedCornerShape(28.dp)
    Box(modifier.shadow(7.dp,shape,ambientColor=Color(b).copy(alpha=.22f),spotColor=Color(b).copy(alpha=.22f)).clip(shape)
        .background(Brush.linearGradient(0f to Color(a),.65f to Color(b),1f to Color(b))).border(1.5.dp,Brush.linearGradient(listOf(Color.White.copy(alpha=.8f),Color.White.copy(alpha=.15f))),shape)) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(Color.White.copy(alpha=.12f),size.width*.525f,androidx.compose.ui.geometry.Offset(size.width*.105f,size.width*.105f))
            drawCircle(Color.White.copy(alpha=.09f),55.dp.toPx(),androidx.compose.ui.geometry.Offset(size.width,size.height-20.dp.toPx()),style=Stroke(18.dp.toPx()))
        };content()
    }
}
@Composable internal fun KidTabIcon(index: Int, tint: Color, modifier: Modifier=Modifier) {
    Canvas(modifier.size(27.dp)) {
        scale(size.width/28f,size.height/28f,pivot=androidx.compose.ui.geometry.Offset.Zero) {
            fun path(vararg points: Pair<Float,Float>) { val p=Path();points.forEachIndexed { i,xy -> if(i==0)p.moveTo(xy.first,xy.second) else p.lineTo(xy.first,xy.second) };p.close();drawPath(p,tint) }
            when(index) {
                0 -> {path(1f to 12f,14f to 1f,27f to 12f,25f to 15f,14f to 6f,3f to 15f);path(5f to 13f,14f to 6f,23f to 13f,23f to 26f,17f to 26f,17f to 18f,11f to 18f,11f to 26f,5f to 26f)}
                1 -> {path(1f to 3f,8f to 2f,13f to 5f,13f to 27f,7f to 24f,1f to 25f);path(15f to 5f,20f to 2f,27f to 3f,27f to 25f,21f to 24f,15f to 27f)}
                2 -> {drawRoundRect(tint,androidx.compose.ui.geometry.Offset(1f,6f),androidx.compose.ui.geometry.Size(26f,19f),androidx.compose.ui.geometry.CornerRadius(7f));drawLine(Color.White,androidx.compose.ui.geometry.Offset(5f,15f),androidx.compose.ui.geometry.Offset(13f,15f),2.5f);drawLine(Color.White,androidx.compose.ui.geometry.Offset(9f,11f),androidx.compose.ui.geometry.Offset(9f,19f),2.5f);drawCircle(Color.White,1.6f,androidx.compose.ui.geometry.Offset(20f,12f));drawCircle(Color.White,1.6f,androidx.compose.ui.geometry.Offset(23f,17f))}
                3 -> {path(2f to 8f,8f to 14f,14f to 3f,20f to 14f,26f to 8f,23f to 23f,5f to 23f);drawRect(tint,androidx.compose.ui.geometry.Offset(5f,25f),androidx.compose.ui.geometry.Size(18f,2f));listOf(2f to 6f,14f to 2f,26f to 6f).forEach {drawCircle(tint,2f,androidx.compose.ui.geometry.Offset(it.first,it.second))}}
                else -> {drawCircle(tint,13f,androidx.compose.ui.geometry.Offset(14f,14f));drawCircle(Color.White,4.3f,androidx.compose.ui.geometry.Offset(14f,10f));drawOval(Color.White,androidx.compose.ui.geometry.Offset(6f,17f),androidx.compose.ui.geometry.Size(16f,8f))}
            }
        }
    }
}
@Composable internal fun IosTabBar(labels: List<String>, selectedTab: Int, onSelect:(Int)->Unit) {
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize=10.sp, lineHeight=13.sp, fontWeight=FontWeight.SemiBold)
    val labelWidth = labels.maxOfOrNull { measurer.measure(it, labelStyle, maxLines=1).size.width } ?: 0
    val minimumWidth = maxOf(48.dp, with(density) { labelWidth.toDp() } + 20.dp)
    val scroll = rememberScrollState()
    val motion = LocalKidMotion.current
    BoxWithConstraints(Modifier.padding(horizontal=20.dp,vertical=8.dp).fillMaxWidth()
        .shadow(8.dp,RoundedCornerShape(34.dp)).clip(RoundedCornerShape(34.dp))
        .background(Color(0xFFE8F6B9).copy(alpha=.88f))
        .border(1.5.dp,Color.White.copy(alpha=.65f),RoundedCornerShape(34.dp)).padding(5.dp)) {
        val itemWidth = maxOf(minimumWidth, (maxWidth / labels.size.coerceAtLeast(1)))
        val itemPx = with(density) { itemWidth.toPx() }
        val viewportPx = with(density) { maxWidth.toPx() }
        // Keep the selected destination visible after navigation or a font/viewport change.
        LaunchedEffect(selectedTab, itemPx, viewportPx) {
            val target = ((selectedTab + .5f) * itemPx - viewportPx / 2).toInt().coerceAtLeast(0)
            if(motion) scroll.animateScrollTo(target) else scroll.scrollTo(target)
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(scroll).selectableGroup()) {
            labels.forEachIndexed { index,title ->
                val tint = if(index==selectedTab)KidHeading else Color(0xFF172000)
                Column(Modifier.width(itemWidth).heightIn(min=56.dp).clip(RoundedCornerShape(29.dp))
                    .background(if(index==selectedTab)Color.White.copy(alpha=.40f) else Color.Transparent)
                    .selectable(selected=index==selectedTab,role=Role.Tab,onClick={onSelect(index)})
                    .padding(horizontal=6.dp,vertical=6.dp),horizontalAlignment=Alignment.CenterHorizontally,
                    verticalArrangement=Arrangement.spacedBy(3.dp,Alignment.CenterVertically)) {
                    KidTabIcon(index,tint)
                    Text(title,style=labelStyle,color=tint,maxLines=1,softWrap=false)
                }
            }
        }
    }
}
@Composable internal fun IosCategoryGrid(state: AppState, categories: List<String>, onClick:(String)->Unit) {
    val pack=state.pack?:return;val tr=state.ui=="tr"
    val categorySound=LocalInteractionSound.current
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val large=androidx.compose.ui.platform.LocalDensity.current.fontScale>=1.6f
        val columns=if(large)1 else max(1,((maxWidth.value+16)/(if(maxWidth>=600.dp)206 else 161)).toInt())
        Column(verticalArrangement=Arrangement.spacedBy(16.dp)) {categories.chunked(columns).forEach { row ->
            Row(horizontalArrangement=Arrangement.spacedBy(16.dp)) {row.forEach { category ->
                val words=pack.words.filter {it.category==category};val count=words.count {LearningRules.progressKey(state.language,it.id) in state.completed}
                MenuBackground(LearningPack.categoryOrder.indexOf(category).coerceAtLeast(0),Modifier.weight(1f).categoryPress {categorySound("category");onClick(category)}) {
                    Column(Modifier.padding(14.dp).heightIn(min=208.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                        DesignArt("ui/categories/$category.webp",Modifier.fillMaxWidth().height(112.dp),decodeLimit=512)
                        Text(label(category,tr),color=Color.White,style=MaterialTheme.typography.titleMedium,minLines=2)
                        Text("$count / ${words.size} " + InterfaceStrings.text(tr,"çalışıldı","studied"),color=Color.White,fontSize=15.sp)
                        LinearProgressIndicator(progress={count.toFloat()/words.size.coerceAtLeast(1)},modifier=Modifier.fillMaxWidth(),color=Color.White,trackColor=Color.White.copy(alpha=.25f),drawStopIndicator={})
                    }
                }
            };repeat(columns-row.size){Spacer(Modifier.weight(1f))}}
        }}
    }
}
@Composable internal fun IosHome(state:AppState, tablet:Boolean, openLesson:(Lesson,Boolean)->Unit, category:(String)->Unit, allCategories:()->Unit, profile:()->Unit, packages:()->Unit, hear:(com.demarpsoftware.kidswordplus.content.Word)->Unit, chess:(String)->Unit) {
    val tr=state.ui=="tr";fun t(a:String,b:String)=InterfaceStrings.text(tr,a,b)
    Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(24.dp)) {
        Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center) {DesignArt("ui/brand/logo.webp",Modifier.width(230.dp).height(100.dp),"KidsWordPlus",decodeLimit=1024)}
        BoxWithConstraints(Modifier.fillMaxWidth().clickable(role=Role.Button,onClick=profile)) {
            val bubbleWidth = maxWidth * .55f
            val characterWidth = minOf(195.dp, maxWidth * .45f)
            val stacked = LocalDensity.current.fontScale >= 1.6f || maxWidth < 320.dp
            if(stacked) {
                Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    HomeGreeting(state.companion,Modifier.align(Alignment.End).size(175.dp))
                    HomeGreetingBubble(state,tr,Modifier.fillMaxWidth())
                }
            } else {
                Box(Modifier.fillMaxWidth().heightIn(min=155.dp)) {
                    HomeGreeting(state.companion,Modifier.align(Alignment.TopEnd).offset(y=(-7).dp).width(characterWidth).height(155.dp))
                    Canvas(Modifier.width(bubbleWidth+14.dp).height(120.dp)) {
                        val path=Path().apply {moveTo(size.width-16.dp.toPx(),size.height*.40f);lineTo(size.width,size.height*.55f);lineTo(size.width-16.dp.toPx(),size.height*.65f);close()};drawPath(path,Color.White)
                    }
                    HomeGreetingBubble(state,tr,Modifier.width(bubbleWidth))
                }
            }
        }
        val pack=state.pack
        if(pack==null && (state.contentLoading || state.busy)) {
            HomeLoadingCard(state)
        } else if(pack!=null) {
            val suggested=pack.lessons.find {it.id==state.lastLesson&&it.words.any {w->LearningRules.progressKey(state.language,w.id) !in state.completed}}
                ?:pack.lessons.firstOrNull {it.words.any {w->LearningRules.progressKey(state.language,w.id) !in state.completed}}?:pack.lessons.first()
            val count=suggested.words.count {LearningRules.progressKey(state.language,it.id) in state.completed}
            KidSurface {
                Text(if(count==0)t("Öğrenmeye başla","Start learning") else t("Kaldığın yerden","Continue learning"),style=MaterialTheme.typography.titleLarge,color=KidHeading)
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    DesignArt("ui/categories/${suggested.category}.webp",Modifier.size(84.dp,92.dp).background(Color(0xFFF0FAF1),RoundedCornerShape(18.dp)),decodeLimit=384)
                    Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
                        Text("${label(suggested.category,tr)} · ${t("Ders","Lesson")} ${suggested.number}",style=MaterialTheme.typography.titleMedium)
                        Text(label(suggested.subcategory,tr),color=KidSecondary,fontSize=15.sp)
                        Text("$count / ${suggested.words.size} ${t("kelime çalışıldı","words studied")}",color=KidSuccess,fontWeight=FontWeight.Bold,fontSize=15.sp)
                    }
                }
                KidProgress(count.toFloat()/suggested.words.size)
                Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.CenterEnd) {
                    Box(Modifier.widthIn(max=280.dp).fillMaxWidth()) {
                        KidAction(if(count==0)t("Derse başla","Start lesson") else t("Devam et","Continue"),!state.busy){openLesson(suggested,suggested.id==state.lastLesson)}
                    }
                }
            }
            KidSurface(Modifier.clickable(onClick=allCategories,role=androidx.compose.ui.semantics.Role.Button)) {Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Canvas(Modifier.size(46.dp).background(Color(0xFFF8EAFF),RoundedCornerShape(14.dp)).padding(10.dp)){repeat(4){val x=if(it%2==0)0f else size.width*.56f;val y=if(it<2)0f else size.height*.56f;drawRoundRect(KidHeading,androidx.compose.ui.geometry.Offset(x,y),androidx.compose.ui.geometry.Size(size.width*.43f,size.height*.43f),androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()))}}
                Column {Text(t("Kategoriler","Categories"),style=MaterialTheme.typography.titleLarge,color=KidHeading);Text(t("Bir dünya seç, keşfe çık!","Choose a world to explore!"),color=KidSecondary,style=MaterialTheme.typography.bodyMedium)}
            }}
            val featured=remember(pack){pack.words.map{it.category}.distinct().shuffled(kotlin.random.Random(HomePortalSeed.value)).take(6)}
            IosCategoryGrid(state,featured,category)
            Text(t("Bugünün kelime sürprizleri","Word surprises"),style=MaterialTheme.typography.titleLarge,color=KidHeading)
            val samples=remember(pack){pack.words.filter {pack.image(it).exists()&&pack.audio(it).exists()}.distinctBy {it.text}.shuffled(kotlin.random.Random(HomePortalSeed.value+1)).take(6)}
            samples.chunked(if(tablet)3 else 2).forEach {row ->
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    row.forEach {word ->
                        KidSurface(Modifier.weight(1f)) {
                            WordImage(pack.image(word),word.text,tablet,tr,if(tablet)170 else 130)
                            Text(word.text,Modifier.fillMaxWidth(),textAlign=TextAlign.Center,style=MaterialTheme.typography.titleMedium,color=KidHeading)
                            KidAction(t("Dinle","Listen"),tone=1){hear(word)}
                        }
                    }
                }
            }
            HomeChessPortalCard(tr,{chess("world")},{chess("pieces")},{chess("openings")})
        } else KidSurface {Text(t("Kelimelerin seni bekliyor","Your words are waiting"));KidAction(t("Dil paketlerini aç","Open language packs"),onClick=packages)}
    }
}

@Composable private fun HomeLoadingCard(state:AppState) {
    fun t(a:String,b:String)=InterfaceStrings.text(state.ui,a,b)
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
        .background(Brush.linearGradient(listOf(Color(0xFFF0E5FF),Color.White)))
        .border(1.dp,Color(0xFFDCC8F5),RoundedCornerShape(28.dp)).padding(24.dp),
        horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(18.dp)) {
        Box(Modifier.size(132.dp).background(Color.White.copy(alpha=.6f),RoundedCornerShape(66.dp)),contentAlignment=Alignment.Center) {
            CompanionImage(state.companion,116)
        }
        Text(t("Dersler hazırlanıyor…","Preparing lessons…"),style=MaterialTheme.typography.titleLarge,color=KidHeading,textAlign=TextAlign.Center)
        Text(t("Kelimelerin ve görsellerin hazırlanıyor. Birazdan başlayacağız!","Getting your words and pictures ready. We’ll start soon!"),style=MaterialTheme.typography.bodyMedium,color=KidSecondary,textAlign=TextAlign.Center)
        LinearProgressIndicator(modifier=Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),color=KidHeading,trackColor=Color(0xFFE3D5F4))
    }
}

// Shared primary action surface follows KidActionButtonStyle in HomeViews.swift.
@Composable internal fun IosButton(onClick: ()->Unit, modifier:Modifier=Modifier, enabled:Boolean=true, content:@Composable RowScope.()->Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val offset by animateFloatAsState(if(pressed && enabled && LocalKidMotion.current)4f else 0f,if(LocalKidMotion.current)tween(120) else snap(),label="iosButtonPress")
    val shape=RoundedCornerShape(22.dp)
    val colors=if(enabled)listOf(Color(0xFF7940C8),Color(0xFF54239B)) else listOf(Color(0xFFE8E8E8),Color(0xFFCECECE))
    Row(modifier.offset(y=offset.dp).padding(bottom=5.dp).heightIn(min=54.dp).shadow(3.dp,shape).clip(shape).background(Brush.verticalGradient(colors)).border(1.5.dp,Color.White.copy(alpha=.5f),shape)
        .clickable(interactionSource=interaction,indication=null,enabled=enabled,role=Role.Button,onClick=onClick).padding(horizontal=14.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center) {
        CompositionLocalProvider(LocalContentColor provides if(enabled)Color.White else KidSecondary){ProvideTextStyle(MaterialTheme.typography.titleMedium){content()}}
    }
}

@Composable internal fun IosSetup(state:AppState, model:AppModel) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var ui by rememberSaveable { mutableStateOf(state.ui) }
    var language by rememberSaveable { mutableStateOf(state.language) }
    var name by rememberSaveable { mutableStateOf(state.name) }
    val tr=ui=="tr";fun t(a:String,b:String)=InterfaceStrings.text(ui,a,b)
    val titles=listOf(t("Hoş geldin!","Welcome!"),t("Hangi dili keşfedelim?","Which language shall we explore?"),t("Kelimelerini hazırlayalım","Let's get your words ready"),t("Sana nasıl seslenelim?","What shall we call you?"),t("Yol arkadaşını seç","Choose your companion"),t("Keşfe hazır mısın?","Ready to explore?"))
    androidx.activity.compose.BackHandler(step>0&&!state.busy){step--}
    val scroll=remember(step){androidx.compose.foundation.ScrollState(0)}
    Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(18.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {Box(Modifier.width(80.dp)){if(step>0)TextButton({step--},enabled=!state.busy){Text(t("Geri","Back"))}};Text("KidsWordPlus",Modifier.weight(1f),textAlign=TextAlign.Center,style=MaterialTheme.typography.titleMedium);Spacer(Modifier.width(80.dp))}
        LinearProgressIndicator(progress={(step+1)/6f},modifier=Modifier.fillMaxWidth().semantics{contentDescription=InterfaceStrings.render(tr, "Kurulum adımı {0} / 6", "Setup step {0} / 6", (step+1).toString())},color=KidHeading)
        Column(Modifier.weight(1f).widthIn(max=800.dp).fillMaxWidth().align(Alignment.CenterHorizontally).verticalScroll(scroll),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(18.dp)) {
            if(step!=4)CompanionImage(state.companion,if(LocalDensity.current.fontScale>=1.6f)80 else 120)
            Text(titles[step],Modifier.fillMaxWidth().semantics{heading()},textAlign=TextAlign.Center,style=MaterialTheme.typography.headlineLarge,color=KidHeading)
            when(step) {
                0,1->{if(step==1)VocabularyLevelPicker(state,model,ui)
                    Text(if(step==0)t("Ekranlarda hangi dili kullanalım?","Which language should the app use?") else t("Arayüz dilinden bağımsız olarak seçebilirsin.","Choose independently of the app language."),textAlign=TextAlign.Center)
                    (if(step==0)LearningLanguages.names.filterKeys{it in InterfaceStrings.codes}else LearningLanguages.names).forEach {(code,title)->
                        val selected=code==if(step==0)ui else language
                        Card(onClick={if(step==0)ui=code else language=code},enabled=!state.busy,modifier=Modifier.fillMaxWidth().semantics{this.selected=selected},shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=Color.White),border=androidx.compose.foundation.BorderStroke(if(selected)3.dp else 0.dp,if(selected)KidHeading else Color.Transparent)) {
                            Row(Modifier.fillMaxWidth().padding(24.dp),verticalAlignment=Alignment.CenterVertically){Text(title,Modifier.weight(1f),style=MaterialTheme.typography.titleLarge,color=KidBody);RadioButton(selected,onClick=null)}
                        }
                    }
                }
                2->{Text(t("İlk dilinle birlikte ortak görselleri içeren Temel dil paketi de otomatik kurulacak. Sonra internet olmadan da öğrenebilirsin.","The Base language pack with shared images installs with your first language. Then you can learn offline."),textAlign=TextAlign.Center)
                    val entry=state.entries.find{it.language==language}
                    if(language in state.installed) Text(t("✓ Dil paketin hazır","✓ Your language pack is ready"),color=KidSuccess,style=MaterialTheme.typography.titleLarge)
                    else if(state.busy) {
                        Text(t("Hazırlanıyor…","Preparing…"))
                        LinearProgressIndicator(progress={state.progress},modifier=Modifier.fillMaxWidth())
                        TextButton(model::cancel){Text(t("İptal et","Cancel"))}
                    } else if(entry!=null) {
                        Text("%.1f MB".format(entry.bytes/1_000_000.0),style=MaterialTheme.typography.titleLarge,color=KidHeading)
                        KidAction(t("Paketi hazırla","Prepare pack"),icon="download"){model.install(language)}
                    } else {
                        Text(t("Dil paketi şu anda kullanılamıyor. Bağlantını kontrol edip yeniden deneyebilirsin.","This language pack is currently unavailable. Check your connection and try again."))
                        TextButton({model.refreshCatalog(true)}){Text(t("Yeniden dene","Try again"))}
                    }
                }
                3->{Text(t("Bir ad veya takma ad yazabilirsin. İstersen boş bırak.","Use a name or nickname, or leave this blank."),textAlign=TextAlign.Center)
                    KidSurface {Text(t("Ad veya takma ad","Name or nickname"),color=KidHeading,style=MaterialTheme.typography.titleMedium);OutlinedTextField(name,{name=it.take(30)},Modifier.fillMaxWidth(),singleLine=true,shape=RoundedCornerShape(22.dp),placeholder={Text(t("Adını yaz…","Your name…"))});Text("${name.length} / 30",color=KidSecondary)}
                    Text(t("Profilin bu cihazda saklanır.","Your profile stays on this device."),color=KidSecondary)
                }
                4->CompanionPicker(state,model,tr)
                5->KidSurface {Text(t("Kelime seviyesi","Vocabulary level")+": "+when(state.vocabularyLevel){"beginner"->t("Basit","Simple");"intermediate"->t("Orta","Medium");else->t("Zor","Hard")});Text(t("Arayüz dili","App language")+": "+LearningLanguages.title(ui));Text(t("Öğrenme dili: ","Learning language: ")+LearningLanguages.title(language));Text(t("Adın: ","Name: ")+name.ifBlank{t("Keşifçi","Explorer")});Text(t("Çevrimdışı öğrenmeye hazır","Ready to learn offline"),color=KidSuccess)}
            }
        }
        KidAction(if(step==5)t("Öğrenmeye başla","Start learning") else t("Devam","Continue"),!state.busy&&(step!=2||language in state.installed)) {
            if(step==5)model.saveProfile(name,ui,language,true) else {if(step==1)model.saveProfile(name,ui,language,false);step++}
        }
    }
}

internal val LocalScreenTitle = staticCompositionLocalOf<MutableState<String?>?> { null }
@Composable internal fun ScreenTitle(title: String) {
    val holder = LocalScreenTitle.current
    DisposableEffect(holder, title) {
        holder?.value = title
        onDispose { if (holder?.value == title) holder?.value = null }
    }
}

@Composable private fun HomeGreetingBubble(state: AppState, tr: Boolean, modifier: Modifier) {
    KidSurface(modifier) {
        Text(if(state.name.isBlank()) {
            InterfaceStrings.text(tr,"Merhaba, keşifçi!","Hello, explorer!")
        } else {
            InterfaceStrings.render(tr,"Merhaba, {0}!","Hello, {0}!",state.name)
        },style=MaterialTheme.typography.titleMedium,color=KidHeading)
        Text(InterfaceStrings.text(tr,"Bugün neler keşfedelim?","What shall we discover today?"),style=MaterialTheme.typography.bodyMedium)
        Text("◎  "+LearningLanguages.title(state.language),fontSize=13.sp,fontWeight=FontWeight.Bold,color=KidHeading)
    }
}

@Composable private fun Modifier.categoryPress(action:()->Unit):Modifier {
    val source=remember {MutableInteractionSource()}
    val pressed by source.collectIsPressedAsState()
    val motion=LocalKidMotion.current
    val scale by animateFloatAsState(if(pressed&&motion)1.04f else 1f,tween(120),label="categoryPop")
    return this.graphicsLayer {scaleX=scale;scaleY=scale}.clickable(interactionSource=source,indication=null,role=Role.Button,onClick=action)
}

private object HomePortalSeed { val value=kotlin.random.Random.nextInt(0,Int.MAX_VALUE-1) }
