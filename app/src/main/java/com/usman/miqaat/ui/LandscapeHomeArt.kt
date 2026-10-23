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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
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
                AppTheme.MIQAAT->R.drawable.home_miqaat_v172
                AppTheme.KISWAH->R.drawable.home_kiswah_v172
                AppTheme.CELESTIAL_MERIDIAN->R.drawable.home_celestial_v172
                AppTheme.PRAYER_GALLERY->R.drawable.home_gallery_v172
            }
            LandscapePainting(art,s.theme,Modifier.matchParentSize().padding(top=if(s.theme==AppTheme.MIQAAT)32.dp else 0.dp))
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
    val clearance=if(short)6.dp else 12.dp
    Box(Modifier.fillMaxWidth().padding(vertical=1.dp).testTag("kiswah-calligraphy-frame").background(Color(0xFF060605)),contentAlignment=Alignment.Center) {
        Canvas(Modifier.matchParentSize().testTag("kiswah-band-ornament")) {
            val gold=Kiswah.thread;val fine=.45.dp.toPx();val side=46.dp.toPx()
            val thread=Brush.linearGradient(listOf(Color(0xFF8B682D),Color(0xFFF2D784),Color(0xFFB58B43),Color(0xFFE4C978)),Offset.Zero,Offset(0f,size.height))
            drawRect(thread,style=Stroke(.8.dp.toPx()))
            for(y in listOf(3.dp.toPx(),size.height-3.dp.toPx())) {
                drawLine(gold.copy(alpha=.85f),Offset(0f,y),Offset(size.width,y),fine)
                // A continuous interwoven gold cord, rather than isolated diamond dots.
                for(phase in listOf(0.0,Math.PI)) {
                    val braid=Path()
                    val center=if(y<size.height/2)7.dp.toPx()else size.height-7.dp.toPx()
                    val step=1.dp.toPx()
                    var x=0f
                    while(x<=size.width) {
                        val yy=center+sin(x/9.dp.toPx()*2*Math.PI+phase).toFloat()*1.6.dp.toPx()
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
                // Repeating palmettes and interlacing pointed leaves echo the woven Kiswah end cartouches.
                repeat(8) { i->rotate(i*45f,center) {
                    val leaf=Path().apply {
                        moveTo(center.x,center.y-radius*.12f)
                        cubicTo(center.x-radius*.48f,center.y-radius*.35f,center.x-radius*.35f,center.y-radius*.76f,center.x,center.y-radius)
                        cubicTo(center.x+radius*.35f,center.y-radius*.76f,center.x+radius*.48f,center.y-radius*.35f,center.x,center.y-radius*.12f)
                        moveTo(center.x,center.y-radius*.30f)
                        cubicTo(center.x-radius*.17f,center.y-radius*.60f,center.x+radius*.12f,center.y-radius*.71f,center.x,center.y-radius*.84f)
                    }
                    drawPath(leaf,thread,style=Stroke(.7.dp.toPx()))
                    val curl=Path().apply {
                        moveTo(center.x+radius*.16f,center.y-radius*.25f)
                        cubicTo(center.x+radius*.70f,center.y-radius*.34f,center.x+radius*.65f,center.y-radius*.88f,center.x+radius*.34f,center.y-radius*.65f)
                    }
                    drawPath(curl,gold.copy(alpha=.85f),style=Stroke(fine))
                }}
                for(turn in listOf(0f,45f))rotate(turn,center) {
                    val r=radius*.40f
                    drawRect(thread,Offset(center.x-r,center.y-r),Size(2*r,2*r),style=Stroke(.65.dp.toPx()))
                }
                drawCircle(gold,radius*.12f,center,style=Stroke(fine))
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
        Text(Kiswah.BAND,Modifier.fillMaxWidth().padding(horizontal=55.dp,vertical=clearance).testTag("kiswah-calligraphy-band")
            .semantics{contentDescription=Kiswah.BAND},style=TextStyle(platformStyle=PlatformTextStyle(includeFontPadding=false)),fontFamily=KiswahThuluth,fontWeight=FontWeight.Bold,fontSize=if(short)16.sp else 20.sp,lineHeight=26.sp,color=Kiswah.threadSoft,textAlign=TextAlign.Center)
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
    if(theme!=AppTheme.PRAYER_GALLERY&&theme!=AppTheme.KISWAH) {
        Image(painterResource(resource),null,modifier,contentScale=ContentScale.FillBounds)
        return
    }
    val bitmap=ImageBitmap.imageResource(resource)
    Canvas(modifier) {
        val source=if(theme==AppTheme.PRAYER_GALLERY)listOf(0f,.08f,.65f,1f)else listOf(0f,.80f,1f)
        val target=if(theme==AppTheme.PRAYER_GALLERY)listOf(0f,.08f,.80f,1f)else listOf(0f,.86f,1f)
        for(i in 0 until source.lastIndex) {
            val sx=(source[i]*bitmap.width).toInt();val ex=(source[i+1]*bitmap.width).toInt()
            val dx=(target[i]*size.width).toInt();val end=(target[i+1]*size.width).toInt()
            drawImage(bitmap,srcOffset=IntOffset(sx,0),srcSize=IntSize(ex-sx,bitmap.height),dstOffset=IntOffset(dx,0),dstSize=IntSize(end-dx,size.height.toInt()))
        }
    }
}
