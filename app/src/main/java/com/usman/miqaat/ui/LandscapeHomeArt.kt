package com.usman.miqaat.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.R
import com.usman.miqaat.data.*
import kotlin.math.*

internal val KiswahThuluth=FontFamily(Font(R.font.layla_thuluth))

/** Home art follows the approved composition, independent of Quiet/Iqamah scene framing. */
@Composable
internal fun LandscapeHomeArt(s:AppSettings,modifier:Modifier) {
    val c=quietPalette(s.theme)
    Box(modifier.background(c.background)) {
        if(s.artTheme!=ArtTheme.MINIMAL) {
            val art=when(s.theme) {
                AppTheme.MIQAAT->R.drawable.home_miqaat_v176
                AppTheme.KISWAH->R.drawable.home_kiswah_v172
                AppTheme.CELESTIAL_MERIDIAN->R.drawable.home_celestial_v172
                AppTheme.PRAYER_GALLERY->R.drawable.home_gallery_v176
            }
            LandscapePainting(art,s.theme,Modifier.matchParentSize())
            if(s.theme==AppTheme.MIQAAT)Canvas(Modifier.matchParentSize()) {
                // Keep architectural details quiet beneath the clock and long next-day countdown.
                drawRect(Brush.radialGradient(0f to Color(0xFF03162E).copy(alpha=.8f),.50f to Color(0xFF03162E).copy(alpha=.8f),1f to Color.Transparent,center=Offset(size.width*.81f,size.height*.49f),radius=size.width*.14f))
                // The arch starts above the heading; fade its apex behind the compact header.
                drawRect(Brush.verticalGradient(0f to Color(0xFF02162D).copy(alpha=.94f),.12f to Color(0xFF02162D).copy(alpha=.82f),.27f to Color.Transparent,1f to Color.Transparent))
                drawRect(Brush.radialGradient(0f to Color(0xFF02162D).copy(alpha=.76f),.55f to Color(0xFF02162D).copy(alpha=.58f),1f to Color.Transparent,center=Offset(size.width*.79f,size.height*.17f),radius=size.width*.13f))
                drawRect(Brush.horizontalGradient(0f to Color(0xFF02162D).copy(alpha=.55f),.56f to Color(0xFF02162D).copy(alpha=.42f),.65f to Color.Transparent,1f to Color.Transparent))
            }
            if(s.theme==AppTheme.KISWAH)Canvas(Modifier.matchParentSize()) {
                // Recede the large cloth folds while retaining the embroidered edge.
                drawRect(Brush.horizontalGradient(0f to Color.Black.copy(alpha=.62f),.80f to Color.Black.copy(alpha=.55f),.86f to Color.Transparent,1f to Color.Transparent))
            }
            if(s.theme==AppTheme.CELESTIAL_MERIDIAN)Canvas(Modifier.matchParentSize()) {
                drawRect(Brush.radialGradient(listOf(Color(0xFF03142A).copy(alpha=.42f),Color.Transparent),Offset(size.width*.8f,size.height*.43f),size.width*.18f))
            }
        }
        if(s.theme!=AppTheme.PRAYER_GALLERY)Box(Modifier.matchParentSize().background(Brush.verticalGradient(0f to Color.Black.copy(alpha=.24f),.17f to Color.Transparent,.85f to Color.Transparent,1f to Color.Black.copy(alpha=.12f))))
    }
}

/** Embroidered braid rails and arabesque end panels frame native Thuluth lettering. */
@Composable
internal fun OrnateLandscapeBand() {
    val short=LocalConfiguration.current.screenHeightDp<390
    val clearance=if(short)6.dp else 10.dp
    BoxWithConstraints(Modifier.fillMaxWidth().padding(vertical=1.dp).height(if(short)42.dp else 54.dp).testTag("kiswah-calligraphy-frame").background(Color(0xFF060605)),contentAlignment=Alignment.Center) {
        val measure=rememberTextMeasurer()
        val density=LocalDensity.current
        val baseSize=if(short)16f else 20f
        val measured=measure.measure(Kiswah.BAND,TextStyle(platformStyle=PlatformTextStyle(includeFontPadding=false),fontFamily=KiswahThuluth,fontWeight=FontWeight.Bold,fontSize=baseSize.sp),softWrap=false,maxLines=1)
        val nativeWidth=with(LocalDensity.current){measured.size.width.toDp()}
        // Fit the entire unwrapped phrase, including user font scaling, before stretching.
        val availableHeight=with(density){minOf(maxHeight-clearance*2,if(short)26.dp else 34.dp).toPx()}
        val fit=minOf(1f,(maxWidth-144.dp)/nativeWidth,availableHeight/measured.size.height.coerceAtLeast(1))
        val textScale=minOf(1.35f,((maxWidth-144.dp)/(nativeWidth*fit))).coerceAtLeast(1f)
        Canvas(Modifier.matchParentSize().testTag("kiswah-band-ornament")) {
            val gold=Kiswah.thread;val fine=.45.dp.toPx();val side=58.dp.toPx()
            val thread=Brush.linearGradient(listOf(Color(0xFF8B682D),Color(0xFFF2D784),Color(0xFFB58B43),Color(0xFFE4C978)),Offset.Zero,Offset(0f,size.height))
            drawRect(thread,style=Stroke(.8.dp.toPx()))
            for(y in listOf(3.dp.toPx(),size.height-3.dp.toPx())) {
                drawLine(gold.copy(alpha=.85f),Offset(0f,y),Offset(size.width,y),fine)
                // A continuous interwoven gold cord, rather than isolated diamond dots.
                for(phase in listOf(0.0,Math.PI)) {
                    val braid=Path()
                    val center=if(y<size.height/2)2.dp.toPx()else size.height-2.dp.toPx()
                    val step=1.dp.toPx()
                    var x=0f
                    while(x<=size.width) {
                        val yy=center+sin(x/9.dp.toPx()*2*Math.PI+phase).toFloat()*.65.dp.toPx()
                        if(x==0f)braid.moveTo(x,yy)else braid.lineTo(x,yy)
                        x+=step
                    }
                    drawPath(braid,thread,style=Stroke(fine))
                }
            }
            for(left in listOf(1.dp.toPx(),size.width-side-1.dp.toPx())) {
                val top=3.dp.toPx();val h=size.height-2*top
                drawRect(thread,Offset(left,top),Size(side,h),style=Stroke(.7.dp.toPx()))
                drawRect(gold.copy(alpha=.65f),Offset(left+3.dp.toPx(),top+2.dp.toPx()),Size(side-6.dp.toPx(),h-4.dp.toPx()),style=Stroke(fine))
                val center=Offset(left+side/2,size.height/2);val radius=minOf(side*.41f,h*.42f)
                // Square interlace in the end cartouche, with gold thread highlights.
                for(turn in listOf(0f,45f))rotate(turn,center) {
                    val r=radius*.70f
                    drawRoundRect(Color(0xFF6F5128),Offset(center.x-r,center.y-r),Size(2*r,2*r),androidx.compose.ui.geometry.CornerRadius(1.8.dp.toPx()),style=Stroke(1.6.dp.toPx()))
                    drawRoundRect(thread,Offset(center.x-r,center.y-r),Size(2*r,2*r),androidx.compose.ui.geometry.CornerRadius(1.8.dp.toPx()),style=Stroke(.7.dp.toPx()))
                    val inner=r*.64f
                    drawRoundRect(thread,Offset(center.x-inner,center.y-inner),Size(2*inner,2*inner),androidx.compose.ui.geometry.CornerRadius(1.dp.toPx()),style=Stroke(fine))
                }
                repeat(4) { i->rotate(i*90f,center) {
                    val vine=Path().apply {
                        moveTo(center.x-radius*.12f,center.y-radius*.26f)
                        cubicTo(center.x-radius*.48f,center.y-radius*.66f,center.x+radius*.42f,center.y-radius*.90f,center.x+radius*.24f,center.y-radius*.47f)
                        cubicTo(center.x+radius*.18f,center.y-radius*.30f,center.x,center.y-radius*.34f,center.x-radius*.12f,center.y-radius*.26f)
                    }
                    drawPath(vine,thread,style=Stroke(.7.dp.toPx()))
                }}
                repeat(8) { i->rotate(i*45f,center) {
                    val loop=Path().apply {
                        moveTo(center.x,center.y-radius*.30f)
                        cubicTo(center.x-radius*.50f,center.y-radius*.94f,center.x+radius*.50f,center.y-radius*.94f,center.x,center.y-radius*.30f)
                    }
                    drawPath(loop,thread,style=Stroke(fine))
                }}
                drawCircle(gold,radius*.13f,center,style=Stroke(fine))
                // Tiny corner arabesques fill the square cartouche instead of a large bare star.
                for(dx in listOf(-1,1))for(dy in listOf(-1,1)) {
                    val corner=Offset(center.x+dx*side*.37f,center.y+dy*h*.32f)
                    val vine=Path().apply {
                        moveTo(corner.x-dx*4.dp.toPx(),corner.y)
                        cubicTo(corner.x+dx*4.dp.toPx(),corner.y-dy*5.dp.toPx(),corner.x+dx*3.dp.toPx(),corner.y+dy*4.dp.toPx(),corner.x-dx*2.dp.toPx(),corner.y+dy*2.dp.toPx())
                    }
                    drawPath(vine,thread,style=Stroke(fine))
                }
            }
        }
        Text(Kiswah.BAND,Modifier.fillMaxWidth().padding(horizontal=68.dp,vertical=clearance).testTag("kiswah-calligraphy-band")
            .semantics{contentDescription=Kiswah.BAND},style=TextStyle(platformStyle=PlatformTextStyle(includeFontPadding=false),textGeometricTransform=TextGeometricTransform(scaleX=textScale)),fontFamily=KiswahThuluth,fontWeight=FontWeight.Bold,fontSize=(baseSize*fit).sp,lineHeight=((if(short)26f else 34f)/density.fontScale).sp,color=Kiswah.threadSoft,textAlign=TextAlign.Center,maxLines=1,softWrap=false)
    }
}

/** Six distinct reference-matched paintings: Fajr/Sunrise/Dhuhr, then Asr/Maghrib/Isha. */
@Composable
internal fun PhoneLandscapePeriodArt(index:Int,modifier:Modifier) {
    val bitmap=ImageBitmap.imageResource(R.drawable.home_gallery_periods_v172)
    Canvas(modifier) {
        val i=index.coerceIn(0,5);val sideW=bitmap.width/3;val sideH=bitmap.height/2
        val w=minOf(sideW,(sideH*size.width/size.height).toInt()).coerceAtLeast(1)
        val h=minOf(sideH,(sideW*size.height/size.width).toInt()).coerceAtLeast(1)
        drawImage(bitmap,srcOffset=IntOffset((i%3)*sideW+(sideW-w)/2,(i/3)*sideH+(sideH-h)/2),srcSize=IntSize(w,h),dstSize=IntSize(size.width.toInt(),size.height.toInt()))
    }
}

/** Keep decorative scene edges out of the live hero typography without cutting off the artwork. */
@Composable
private fun LandscapePainting(resource:Int,theme:AppTheme,modifier:Modifier) {
    if(theme==AppTheme.CELESTIAL_MERIDIAN) {
        Image(painterResource(resource),null,modifier,contentScale=ContentScale.FillBounds)
        return
    }
    val bitmap=ImageBitmap.imageResource(resource)
    Canvas(modifier) {
        val source=if(theme==AppTheme.PRAYER_GALLERY)listOf(0f,.08f,.65f,1f)else if(theme==AppTheme.MIQAAT)listOf(0f,.565f,1f)else listOf(0f,.80f,1f)
        val target=if(theme==AppTheme.PRAYER_GALLERY)listOf(0f,.08f,.75f,1f)else if(theme==AppTheme.MIQAAT)listOf(0f,.59f,1f)else listOf(0f,.86f,1f)
        val sourceY=if(theme==AppTheme.MIQAAT)listOf(0f,.60f,.80f,1f)else listOf(0f,1f)
        val targetY=if(theme==AppTheme.MIQAAT)listOf(0f,.60f,.72f,1f)else listOf(0f,1f)
        for(i in 0 until source.lastIndex)for(j in 0 until sourceY.lastIndex) {
            val sx=(source[i]*bitmap.width).toInt();val ex=(source[i+1]*bitmap.width).toInt()
            val sy=(sourceY[j]*bitmap.height).toInt();val ey=(sourceY[j+1]*bitmap.height).toInt()
            val dx=(target[i]*size.width).toInt();val end=(target[i+1]*size.width).toInt()
            val dy=(targetY[j]*size.height).toInt();val bottom=(targetY[j+1]*size.height).toInt()
            drawImage(bitmap,srcOffset=IntOffset(sx,sy),srcSize=IntSize(ex-sx,ey-sy),dstOffset=IntOffset(dx,dy),dstSize=IntSize(end-dx,bottom-dy))
        }
    }
}

