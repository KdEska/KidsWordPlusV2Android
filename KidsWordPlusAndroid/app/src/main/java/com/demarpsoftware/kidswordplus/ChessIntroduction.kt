package com.demarpsoftware.kidswordplus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable internal fun ChessIntroduction(tr:Boolean,back:()->Unit,pieces:()->Unit) {
    val context=LocalContext.current
    val uri=LocalUriHandler.current
    val data by produceState<Triple<JSONObject,JSONObject,JSONObject>?>(null,tr){
        value=withContext(Dispatchers.IO){
            fun read(path:String)=JSONObject(context.assets.open(path).bufferedReader().use{it.readText()})
            val language=InterfaceStrings.text(tr,"tr","en")
            Triple(read("chess/history.json").getJSONObject(language),read("chess/reference-content.json").getJSONObject(language),read("chess/history/credits.json"))
        }
    }
    var section by rememberSaveable{mutableIntStateOf(0)}
    var era by rememberSaveable{mutableIntStateOf(0)}
    var sources by rememberSaveable{mutableStateOf(false)}
    Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(20.dp)) {
        ChessPageNavigation(back,hideTabs=false)
        data?.let{(history,content,credits) ->
            fun text(key:String)=history.optString(key,key)
            Text(text("title"),style=MaterialTheme.typography.headlineLarge,color=KidHeading)
            Text(text("intro"),style=MaterialTheme.typography.titleMedium,color=KidBody)
            ChessSegmented(listOf(text("history"),text("basics"),text("masters")),section){section=it}
            when(section) {
                0 -> {
                    listOf("origin","journey","rules","championship").forEachIndexed{index,key ->
                        KidSurface{Row(horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                            Text(listOf("≈ 600","↗","≈ 1500","1886 · 1924")[index],Modifier.width(74.dp).background(KidHeading.copy(alpha=.1f),RoundedCornerShape(14.dp)).padding(vertical=12.dp),color=KidHeading,fontSize=12.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center)
                            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(10.dp)){Text(text("$key.title"),style=MaterialTheme.typography.titleLarge,color=KidHeading);Text(text("$key.body"))}
                        }}
                    }
                    KidAction(text("masters"),tone=2){section=2}
                }
                1 -> {
                    listOf("board","turn","goal").forEachIndexed{index,key ->
                        KidSurface{
                            Text((if(tr)listOf("Tahta","Hamle sırası","Amaç")else listOf("Board","Turns","Goal"))[index],style=MaterialTheme.typography.titleLarge,color=KidHeading)
                            Text(content.optString("$key.body"))
                            ChessListenButton(InterfaceStrings.text(tr,"tr","en"),"$key.body")
                        }
                    }
                    KidAction(InterfaceStrings.text(tr,"Taşları tanı","Meet the pieces"),onClick=pieces)
                }
                else -> {
                    ChessSegmented(listOf(text("modern"),text("pioneers")),era){era=it}
                    val people=credits.getJSONArray("people")
                    repeat(people.length()){index ->val person=people.getJSONObject(index)
                        if(person.getString("era")==if(era==0)"modern"else "pioneers")KidSurface {
                            DesignArt("chess/history/${person.getString("image")}",Modifier.fillMaxWidth().height(230.dp).background(Color(0xFFF5EDDB),RoundedCornerShape(18.dp)).padding(12.dp),text(person.getString("id")+".name"),decodeLimit=768)
                            Text(text(person.getString("id")+".name"),style=MaterialTheme.typography.titleLarge,color=KidHeading)
                            Text(person.getString("years"),style=MaterialTheme.typography.bodyMedium,color=KidSecondary)
                            Text(text(person.getString("id")+".body"))
                            Text(person.getString("author"),style=MaterialTheme.typography.bodyMedium,color=KidSecondary)
                            TextButton({uri.openUri(person.getString("source"))}){Text(text("photoSource"))}
                            TextButton({uri.openUri(person.getString("licenseURL"))}){Text(person.getString("license"))}
                        }
                    }
                }
            }
            TextButton({sources=!sources}){Text(text("sources")+if(sources)" ⌃"else " ⌄")}
            if(sources)KidSurface{
                val links=credits.getJSONArray("sources")
                repeat(links.length()){i ->val link=links.getJSONObject(i);TextButton({uri.openUri(link.getString("url"))}){Text(link.getString("title"))}}
            }
        }?:CircularProgressIndicator()
    }
}
