package com.demarpsoftware.kidswordplus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable internal fun ChessSecretsScreen(state:AppState,tablet:Boolean,back:()->Unit) {
    fun t(tr:String,en:String)=InterfaceStrings.text(state.ui,tr,en)
    ChessPageNavigation(back)
    ScreenTitle(t("Satranç Sırları","Chess secrets"))
    val columns=if(tablet&&androidx.compose.ui.platform.LocalDensity.current.fontScale<1.6f)2 else 1
    LazyColumn(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(14.dp),contentPadding=PaddingValues(16.dp)) {
        item {
            KidSurface {
                DesignArt("ui/chess/secrets.webp",Modifier.fillMaxWidth().height(if(tablet)260.dp else 190.dp),scale=ContentScale.Crop,decodeLimit=1024)
                Text(t("Satranç Sırları","Chess secrets"),style=MaterialTheme.typography.headlineLarge,color=KidHeading)
                Text(t("Akıllıca hamleler için küçük ipuçları.","Little tips for clever moves."),color=KidSecondary)
                Text(chessSecretCopies.size.toString(),color=KidHeading,fontWeight=FontWeight.Bold)
            }
        }
        items(chessSecretCopies.withIndex().toList().chunked(columns),key={it.first().value.id}) { row ->
            Row(horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                row.forEach {(index,secret)->
                    Box(Modifier.weight(1f)) {
                        KidSurface {
                            Row(horizontalArrangement=Arrangement.spacedBy(14.dp),verticalAlignment=Alignment.Top) {
                                Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                    Text("💡",fontSize=28.sp)
                                    Text("%02d".format(index+1),color=KidHeading,fontWeight=FontWeight.Bold)
                                }
                                Text(t(secret.turkish,secret.english),Modifier.weight(1f),style=MaterialTheme.typography.bodyLarge,color=KidBody)
                            }
                        }
                    }
                }
                if(row.size<columns)Spacer(Modifier.weight(1f))
            }
        }
    }
}
