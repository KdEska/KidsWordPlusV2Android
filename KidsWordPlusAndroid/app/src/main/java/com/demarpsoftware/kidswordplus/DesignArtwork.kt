package com.demarpsoftware.kidswordplus

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private object ArtworkCache {
    val images = object : LruCache<String, ImageBitmap>(20 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ImageBitmap) = value.width * value.height * 4
    }
}
internal fun loadDesignArtwork(context:android.content.Context,path:String,decodeLimit:Int):ImageBitmap? {
    val key="$path:$decodeLimit"
    return ArtworkCache.images.get(key) ?: runCatching {
        val options=BitmapFactory.Options().apply {inJustDecodeBounds=true}
        context.assets.open(path).use {BitmapFactory.decodeStream(it,null,options)}
        options.inSampleSize=1
        while(maxOf(options.outWidth,options.outHeight)/options.inSampleSize>decodeLimit)options.inSampleSize*=2
        options.inJustDecodeBounds=false
        context.assets.open(path).use {BitmapFactory.decodeStream(it,null,options)}?.asImageBitmap()?.also {ArtworkCache.images.put(key,it)}
    }.getOrNull()
}
@Composable internal fun DesignArt(path: String, modifier: Modifier = Modifier, description: String? = null,
    scale: ContentScale = ContentScale.Fit, decodeLimit: Int = 768, colorFilter: androidx.compose.ui.graphics.ColorFilter? = null) {
    val context = LocalContext.current
    val bitmap by key(path, decodeLimit) { produceState<ImageBitmap?>(ArtworkCache.images.get("$path:$decodeLimit"), path, decodeLimit) {
        value = withContext(Dispatchers.IO) { loadDesignArtwork(context,path,decodeLimit) }
    } }
    if (bitmap == null) Box(modifier) else Image(bitmap!!, description, modifier, contentScale = scale, colorFilter = colorFilter)
}
@Composable internal fun LearningBackdrop() {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFD8F1FF), Color(0xFFF4FFF3))))) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            DesignArt("ui/backgrounds/garden.webp", Modifier.fillMaxSize(), scale = if (maxWidth >= 600.dp) ContentScale.Crop else ContentScale.FillBounds, decodeLimit = 2048)
        }
    }
}
@Composable internal fun BrandHeader(tablet: Boolean) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        DesignArt("ui/brand/logo.webp", Modifier.widthIn(max = if (tablet) 320.dp else 240.dp).fillMaxWidth().height(if (tablet) 76.dp else 62.dp), "KidsWordPlus", decodeLimit = 1024)
    }
}
@Composable internal fun NavigationArt(index: Int, companion: String) {
    val path = when(index) {
        0 -> "ui/categories/home.webp"
        1 -> "ui/games/new-words.webp"
        2 -> "ui/games/daily-controller.webp"
        3 -> "companions/$companion.png"
        else -> "ui/parent/settings.webp"
    }
    DesignArt(path, Modifier.size(34.dp), decodeLimit = 192)
}
@Composable internal fun FeatureTile(path: String, title: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            DesignArt(path, Modifier.size(76.dp).clip(RoundedCornerShape(16.dp)), decodeLimit = 384)
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            DesignArt("ui/navigation/next.webp", Modifier.size(28.dp), decodeLimit = 128)
        }
    }
}
@Composable internal fun ChessMenuAction(art: String, title: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        DesignArt("ui/chess/$art.webp", Modifier.size(44.dp), decodeLimit = 192)
        Spacer(Modifier.width(12.dp))
        Text(title, Modifier.weight(1f))
    }
}
