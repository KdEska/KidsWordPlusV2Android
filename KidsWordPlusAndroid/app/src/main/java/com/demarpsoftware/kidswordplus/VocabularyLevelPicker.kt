package com.demarpsoftware.kidswordplus

import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable internal fun VocabularyLevelPicker(state:AppState,model:AppModel,ui:String=state.ui) {
    fun t(tr:String,en:String)=InterfaceStrings.text(ui,tr,en)
    val levels=listOf("beginner","intermediate","advanced")
    val titles=listOf(t("Basit","Simple"),t("Orta","Medium"),t("Zor","Hard"))
    val icons=listOf("🌱","🌿","🌳")
    var position by remember(state.vocabularyLevel) { mutableFloatStateOf(levels.indexOf(state.vocabularyLevel).coerceAtLeast(0).toFloat()) }
    val selected=position.roundToInt().coerceIn(0,2)
    val tint=listOf(Color(0xFF249269),KidHeading,Color(0xFFE06E1F))[selected]
    fun commit(index:Int) {position=index.toFloat();if(levels[index]!=state.vocabularyLevel)model.setVocabularyLevel(levels[index])}
    SettingsCard {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            SettingsIcon("target")
            Text(t("Kelime seviyesi","Vocabulary level"),style=MaterialTheme.typography.titleMedium,color=KidHeading)
        }
        Row(Modifier.fillMaxWidth().background(tint.copy(alpha=.08f),RoundedCornerShape(18.dp)).padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            Text(icons[selected],fontSize=30.sp,modifier=Modifier.clearAndSetSemantics {})
            Text(titles[selected],style=MaterialTheme.typography.titleLarge,color=tint,fontWeight=FontWeight.Bold)
        }
        Slider(value=position,onValueChange={position=it},valueRange=0f..2f,steps=1,
            onValueChangeFinished={commit(selected)},enabled=!state.busy,
            colors=SliderDefaults.colors(thumbColor=tint,activeTrackColor=tint,inactiveTrackColor=tint.copy(alpha=.12f)),
            modifier=Modifier.fillMaxWidth().semantics {contentDescription=t("Kelime seviyesi","Vocabulary level");stateDescription=titles[selected]})
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            levels.indices.forEach { index ->
                val chosen=selected==index
                TextButton(onClick={commit(index)},enabled=!state.busy,
                    shape=RoundedCornerShape(16.dp),contentPadding=PaddingValues(horizontal=4.dp,vertical=10.dp),
                    modifier=Modifier.weight(1f).heightIn(min=84.dp)
                        .background(if(chosen)tint.copy(alpha=.12f)else Color(0xFFF7F7FA),RoundedCornerShape(16.dp))
                        .border(2.dp,if(chosen)tint.copy(alpha=.6f)else Color.Transparent,RoundedCornerShape(16.dp))
                        .semantics {this.selected=chosen}) {
                    Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(7.dp)) {
                        Text(icons[index],fontSize=28.sp,modifier=Modifier.clearAndSetSemantics {})
                        Text(titles[index],color=if(chosen)tint else KidSecondary,style=MaterialTheme.typography.labelLarge,textAlign=TextAlign.Center)
                    }
                }
            }
        }
        if(state.vocabularyLevelChanging) {
            Column(verticalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.semantics {liveRegion=LiveRegionMode.Polite}) {
                Text(t("Hazırlanıyor…","Preparing…"),color=tint,style=MaterialTheme.typography.bodyMedium)
                LinearProgressIndicator(modifier=Modifier.fillMaxWidth(),color=tint)
            }
        }
        Text(titles.take(selected+1).joinToString("  ·  "),color=tint,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.bodyMedium)
        Text(t("Dersler ve kelime oyunları seçtiğiniz seviyeyi ve altındaki seviyeleri içerir. İlerlemeniz korunur.","Lessons and word games include your selected level and all easier levels. Your progress is kept."),style=MaterialTheme.typography.bodySmall,color=KidSecondary)
    }
}
