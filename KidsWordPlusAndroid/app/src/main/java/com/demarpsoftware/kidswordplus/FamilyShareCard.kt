package com.demarpsoftware.kidswordplus

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.demarpsoftware.kidswordplus.chess.*
import java.io.File
import java.util.UUID
import kotlin.random.Random

internal enum class FamilyShareSymbol { CROWN, BOARD, FLAG, LESSON, LISTENING, MEMORY, MATCHING, DAILY, REPORT, HEART }

internal data class FamilyShareChallenge(val left: Int,val right: Int) {
    fun accepts(answer: String): Boolean {
        if(answer.isEmpty() || answer.length>5)return false
        val digits=answer.map { Character.digit(it,10).takeIf {n -> n>=0} ?: return false }.joinToString("")
        return digits.toIntOrNull()==left*right
    }
    companion object {fun create()=FamilyShareChallenge(Random.nextInt(12,20),Random.nextInt(6,10))}
}

/** Receives only the public achievement copy and board, never a profile. */
internal fun renderFamilyShareCard(context: Context,title: String,detail: String,board: List<Piece?>?,tr: Boolean,symbol: FamilyShareSymbol=FamilyShareSymbol.CROWN,images:List<File> = emptyList()): Bitmap {
    require(board==null || board.size==64)
    val purple=Color.rgb(88,39,166)
    fun layout(text: String,size: Float,color: Int,bold: Boolean=false)=StaticLayout.Builder.obtain(text,0,text.length,TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize=size;this.color=color;typeface=Typeface.create("sans-serif-rounded",if(bold)Typeface.BOLD else Typeface.NORMAL)
    },924).setAlignment(Layout.Alignment.ALIGN_CENTER).setIncludePad(false).build()
    val titleText=layout(title,75f,Color.rgb(49,33,82),true)
    val detailText=layout(detail,54f,Color.rgb(76,66,102),true)
    val tagline=layout(InterfaceStrings.text(tr,"Birlikte öğren, birlikte büyü.","Learn together, grow together."),45f,purple)
    val titleY=520f
    val detailY=titleY+titleText.height+36
    val contentY=detailY+detailText.height+48
    val thumbnails=images.take(3).mapNotNull {file -> runCatching {
        val options=BitmapFactory.Options().apply {inJustDecodeBounds=true}
        BitmapFactory.decodeFile(file.path,options)
        options.inSampleSize=1
        while(maxOf(options.outWidth,options.outHeight)/options.inSampleSize>256)options.inSampleSize*=2
        options.inJustDecodeBounds=false
        BitmapFactory.decodeFile(file.path,options)
    }.getOrNull()}
    val thumbnailHeight=if(thumbnails.isEmpty())0 else 300
    val footerY=contentY+thumbnailHeight+(if(board!=null)768 else 0)
    val height=(footerY+tagline.height+126).toInt()
    val image=Bitmap.createBitmap(1080,height,Bitmap.Config.ARGB_8888)
    val canvas=Canvas(image)
    val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    paint.shader=LinearGradient(0f,0f,1080f,height.toFloat(),intArrayOf(Color.rgb(250,245,255),Color.rgb(235,245,255)),null,Shader.TileMode.CLAMP)
    canvas.drawRect(0f,0f,1080f,height.toFloat(),paint);paint.shader=null
    fun art(path: String,rect: RectF) {
        val bitmap=context.assets.open(path).use {BitmapFactory.decodeStream(it)} ?: error("Missing share artwork")
        val scale=minOf(rect.width()/bitmap.width,rect.height()/bitmap.height)
        val w=bitmap.width*scale;val h=bitmap.height*scale
        canvas.drawBitmap(bitmap,null,RectF(rect.centerX()-w/2,rect.centerY()-h/2,rect.centerX()+w/2,rect.centerY()+h/2),paint)
        bitmap.recycle()
    }
    art("ui/brand/logo.webp",RectF(210f,60f,870f,270f))
    paint.color=Color.rgb(236,224,251);canvas.drawCircle(540f,390f,112f,paint)
    paint.color=purple
    when(symbol) {
    FamilyShareSymbol.BOARD -> for(row in 0..2) for(col in 0..2) canvas.drawRoundRect(465f+col*52,315f+row*52,507f+col*52,357f+row*52,6f,6f,paint)
    FamilyShareSymbol.FLAG -> {
        canvas.drawRoundRect(477f,315f,486f,465f,4f,4f,paint)
        for(row in 0..2) for(col in 0..3) {
            paint.color=if((row+col)%2==0)purple else Color.WHITE
            canvas.drawRect(486f+col*30,315f+row*30,516f+col*30,345f+row*30,paint)
        }
    }
    FamilyShareSymbol.LESSON -> {
        val cap=Path().apply {moveTo(440f,365f);lineTo(540f,315f);lineTo(640f,365f);lineTo(540f,415f);close()}
        canvas.drawPath(cap,paint)
        canvas.drawRoundRect(485f,398f,595f,440f,10f,10f,paint)
        paint.strokeWidth=8f;canvas.drawLine(633f,366f,633f,445f,paint)
        canvas.drawCircle(633f,450f,9f,paint)
    }
    FamilyShareSymbol.HEART -> {
        val heart=Path().apply {moveTo(540f,465f);cubicTo(380f,365f,470f,270f,540f,340f);cubicTo(610f,270f,700f,365f,540f,465f);close()};canvas.drawPath(heart,paint)
    }
    FamilyShareSymbol.REPORT -> {
        canvas.drawRoundRect(460f,395f,500f,462f,8f,8f,paint)
        canvas.drawRoundRect(520f,345f,560f,462f,8f,8f,paint)
        canvas.drawRoundRect(580f,310f,620f,462f,8f,8f,paint)
    }
    FamilyShareSymbol.LISTENING -> {
        paint.style=Paint.Style.STROKE;paint.strokeWidth=18f;paint.strokeCap=Paint.Cap.ROUND
        val ear=Path().apply {moveTo(492f,367f);cubicTo(468f,298f,614f,291f,604f,369f);cubicTo(600f,400f,561f,405f,554f,438f);cubicTo(549f,465f,513f,469f,502f,442f)}
        canvas.drawPath(ear,paint)
        val inner=Path().apply {moveTo(516f,363f);cubicTo(515f,329f,580f,331f,575f,365f);cubicTo(571f,385f,546f,382f,539f,407f)}
        canvas.drawPath(inner,paint);paint.style=Paint.Style.FILL
    }
    FamilyShareSymbol.MEMORY -> for(row in 0..1)for(col in 0..1)canvas.drawRoundRect(468f+col*82,318f+row*82,530f+col*82,380f+row*82,12f,12f,paint)
    FamilyShareSymbol.MATCHING -> {
        val puzzle=Path().apply {
            moveTo(465f,330f);lineTo(520f,330f);cubicTo(497f,275f,583f,275f,560f,330f);lineTo(610f,330f)
            lineTo(610f,380f);cubicTo(665f,357f,665f,443f,610f,420f);lineTo(610f,465f)
            lineTo(560f,465f);cubicTo(583f,410f,497f,410f,520f,465f);lineTo(465f,465f);close()
        };canvas.drawPath(puzzle,paint)
    }
    FamilyShareSymbol.DAILY -> {
        paint.style=Paint.Style.STROKE;paint.strokeWidth=10f;canvas.drawCircle(540f,390f,80f,paint);paint.style=Paint.Style.FILL
        val star=Path()
        for(i in 0..9) {
            val angle=-Math.PI/2+i*Math.PI/5;val radius=if(i%2==0)56 else 25
            val x=540f+(kotlin.math.cos(angle)*radius).toFloat();val y=390f+(kotlin.math.sin(angle)*radius).toFloat()
            if(i==0)star.moveTo(x,y)else star.lineTo(x,y)
        };star.close();canvas.drawPath(star,paint)
    }
    FamilyShareSymbol.CROWN -> {
    val crown=Path().apply {moveTo(465f,355f);lineTo(500f,390f);lineTo(540f,335f);lineTo(580f,390f);lineTo(615f,355f);lineTo(600f,430f);lineTo(480f,430f);close()}
    canvas.drawPath(crown,paint);canvas.drawRoundRect(480f,440f,600f,452f,6f,6f,paint)
    }
    }
    fun text(block: StaticLayout,y: Float) {canvas.save();canvas.translate(78f,y);block.draw(canvas);canvas.restore()}
    text(titleText,titleY);text(detailText,detailY)
    if(thumbnails.isNotEmpty()) {
        val start=(1080f-(thumbnails.size*240+(thumbnails.size-1)*30))/2
        thumbnails.forEachIndexed {index,thumbnail ->
            val x=start+index*270
            paint.color=Color.WHITE;canvas.drawRoundRect(x,contentY,x+240,contentY+240,42f,42f,paint)
            val scale=minOf(224f/thumbnail.width,224f/thumbnail.height)
            val w=thumbnail.width*scale;val h=thumbnail.height*scale
            canvas.drawBitmap(thumbnail,null,RectF(x+120-w/2,contentY+120-h/2,x+120+w/2,contentY+120+h/2),paint)
            thumbnail.recycle()
        }
    }
    if(board!=null) {
        val boardY=contentY+thumbnailHeight
        canvas.save();canvas.clipPath(Path().apply {addRoundRect(RectF(180f,boardY,900f,boardY+720),36f,36f,Path.Direction.CW)})
        val pieces=mutableMapOf<Piece,Bitmap>()
        try {for(index in 0..63) {
            val x=180f+index%8*90;val y=boardY+index/8*90
            paint.color=if((index/8+index%8)%2==0)Color.rgb(247,232,201)else Color.rgb(112,79,158)
            canvas.drawRect(x,y,x+90,y+90,paint)
            board[index]?.let {piece ->
                val bitmap=pieces.getOrPut(piece) {context.assets.open("chess/pieces/"+(if(piece.white)""else "black/")+piece.kind.name.lowercase()+".png").use {BitmapFactory.decodeStream(it)} ?: error("Missing piece")}
                canvas.drawBitmap(bitmap,null,RectF(x+4,y+4,x+86,y+86),paint)
            }
        }} finally {pieces.values.forEach {it.recycle()};canvas.restore()}
    }
    paint.color=Color.rgb(221,210,239);canvas.drawRect(78f,footerY,1002f,footerY+3,paint)
    text(tagline,footerY+48)
    return image
}

internal fun launchFamilyShare(context: Context,image: Bitmap) {
    val directory=File(context.cacheDir,"family-share").apply {check(mkdirs() || isDirectory)}
    // Keep recent URI grants valid while the receiving app reads its attachment.
    directory.listFiles()?.filter {System.currentTimeMillis()-it.lastModified()>24*60*60*1000L}?.forEach {it.delete()}
    val file=File(directory,"card-${UUID.randomUUID()}.png")
    file.outputStream().use {check(image.compress(Bitmap.CompressFormat.PNG,100,it))}
    val uri=FileProvider.getUriForFile(context,context.packageName+".familyshare",file)
    val intent=Intent(Intent.ACTION_SEND).apply {
        type="image/png";putExtra(Intent.EXTRA_STREAM,uri)
        clipData=ClipData.newRawUri("KidsWordPlus",uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent,null))
}
