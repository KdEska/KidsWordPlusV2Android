package com.demarpsoftware.kidswordplus

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.runtime.staticCompositionLocalOf
import java.io.File

internal data class ParentPhotoActions(val gallery:()->Unit,val camera:()->Unit)
internal val LocalParentPhotoActions=staticCompositionLocalOf {ParentPhotoActions({},{})}

/** Keep only a small decoded copy in private storage; do not retain picker access or EXIF. */
internal object ParentPhotoStore {
    fun save(context:Context,uri:Uri) {
        val input=File(context.cacheDir,"parent-photo-input")
        try {
            context.contentResolver.openInputStream(uri).use {source ->
                requireNotNull(source)
                input.outputStream().use {target ->
                    val buffer=ByteArray(8192);var total=0
                    while(true) {val n=source.read(buffer);if(n<0)break;total+=n;require(total<=20*1024*1024);target.write(buffer,0,n)}
                }
            }
            val options=BitmapFactory.Options().apply {inJustDecodeBounds=true}
            BitmapFactory.decodeFile(input.path,options)
            require(options.outWidth>0&&options.outHeight>0)
            options.inSampleSize=1
            while(maxOf(options.outWidth,options.outHeight)/options.inSampleSize>1024)options.inSampleSize*=2
            options.inJustDecodeBounds=false
            val bitmap=requireNotNull(BitmapFactory.decodeFile(input.path,options))
            try {
                val orientation=runCatching {ExifInterface(input.path).getAttributeInt(ExifInterface.TAG_ORIENTATION,1)}.getOrDefault(1)
                val matrix=Matrix().apply {
                    when(orientation) {3->postRotate(180f);6->postRotate(90f);8->postRotate(270f);2->postScale(-1f,1f);4->postScale(1f,-1f);5->{postRotate(90f);postScale(-1f,1f)};7->{postRotate(270f);postScale(-1f,1f)}}
                }
                val rotated=Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,matrix,true)
                try {save(context,rotated)}finally {if(rotated!==bitmap)rotated.recycle()}
            }finally {bitmap.recycle()}
        }finally {input.delete()}
    }
    fun save(context:Context,bitmap:Bitmap) {
        val side=minOf(bitmap.width,bitmap.height)
        val cropped=Bitmap.createBitmap(bitmap,(bitmap.width-side)/2,(bitmap.height-side)/2,side,side)
        val resized=Bitmap.createScaledBitmap(cropped,512,512,true)
        val file=File(context.filesDir,"parent-photo.jpg")
        val atomic=android.util.AtomicFile(file)
        val output=atomic.startWrite()
        try {check(resized.compress(Bitmap.CompressFormat.JPEG,90,output));atomic.finishWrite(output)}catch(e:Exception){atomic.failWrite(output);throw e}finally {if(resized!==cropped)resized.recycle();if(cropped!==bitmap)cropped.recycle()}
    }
}
