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

// Original Kiswah typography, retained from KiswahHome and the user-approved legacy screen.
internal val KiswahLandscapeArabic=ReemKufi

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
                drawRect(Brush.radialGradient(listOf(Color(0xFF03142A).copy(alpha=.58f),Color.Transparent),Offset(size.width*.8f,size.height*.43f),size.width*.18f))
            }
        }
        if(s.theme==AppTheme.MIQAAT||s.theme==AppTheme.CELESTIAL_MERIDIAN)Canvas(Modifier.matchParentSize().testTag("landscape-timeline-art-scrim")) {
            // Quiet bright water reflections behind the functional timeline without
            // dimming the entire painting or placing a hard opaque panel over it.
            drawRect(Brush.verticalGradient(0f to Color.Transparent,.70f to Color.Transparent,
                .76f to Color(0xFF03162E).copy(alpha=.58f),.84f to Color(0xFF03162E).copy(alpha=.62f),
                .90f to Color.Transparent,1f to Color.Transparent))
        }
        if(s.theme!=AppTheme.PRAYER_GALLERY)Box(Modifier.matchParentSize().background(Brush.verticalGradient(0f to Color.Black.copy(alpha=.24f),.17f to Color.Transparent,.85f to Color.Transparent,1f to Color.Black.copy(alpha=.12f))))
    }
}

/** Original Kufic lettering framed by native silver vine rails and antique-gold stitching. */
@Composable
internal fun OrnateLandscapeBand() {
    val short=LocalConfiguration.current.screenHeightDp<390
    val clearance=if(short)6.dp else 10.dp
    BoxWithConstraints(Modifier.fillMaxWidth().padding(vertical=1.dp).height(if(short)42.dp else 54.dp).testTag("kiswah-calligraphy-frame").background(Color(0xFF060605)),contentAlignment=Alignment.Center) {
        val measure=rememberTextMeasurer()
        val density=LocalDensity.current
        val baseSize=if(short)16f else 20f
        val measured=measure.measure(Kiswah.BAND,TextStyle(platformStyle=PlatformTextStyle(includeFontPadding=false),fontFamily=KiswahLandscapeArabic,fontWeight=FontWeight.Normal,fontSize=baseSize.sp),softWrap=false,maxLines=1)
        val nativeWidth=with(density){measured.size.width.toDp()}
        val availableHeight=with(density){minOf(maxHeight-clearance*2,if(short)26.dp else 34.dp).toPx()}
        val fit=minOf(1f,(maxWidth-144.dp)/nativeWidth,availableHeight/measured.size.height.coerceAtLeast(1))
        Canvas(Modifier.matchParentSize().testTag("kiswah-band-ornament")) {
            val gold=Brush.linearGradient(listOf(Color(0xFF7E5C25),Color(0xFFE5C678),Color(0xFFA77C36),Color(0xFFE6CC88)),Offset.Zero,Offset(0f,size.height))
            val silver=Brush.horizontalGradient(listOf(Color(0xFFBEC2B2),Color(0xFFF1F0DE),Color(0xFFA8AE9F)))
            val fine=.45.dp.toPx()
            drawRect(gold,style=Stroke(.8.dp.toPx()))
            // Silver scrolling foliage is characteristic of the supplied references:
            // broad leaves on a continuous vine, not a diamond braid or star rail.
            for(bottom in listOf(false,true)) {
                val y=if(bottom)size.height-5.dp.toPx()else 5.dp.toPx()
                val sign=if(bottom)-1f else 1f
                for(edge in listOf(1.4.dp.toPx(),8.6.dp.toPx())) {
                    val yy=if(bottom)size.height-edge else edge
                    drawLine(gold,Offset(0f,yy),Offset(size.width,yy),fine)
                }
                val pitch=16.dp.toPx()
                var x=0f
                while(x<size.width) {
                    val stem=Path().apply {
                        moveTo(x,y)
                        cubicTo(x+pitch*.28f,y-sign*3.dp.toPx(),x+pitch*.72f,y+sign*3.dp.toPx(),x+pitch,y)
                    }
                    drawPath(stem,silver,style=Stroke(.65.dp.toPx()))
                    for(side in listOf(-1f,1f)) {
                        val leaf=Path().apply {
                            moveTo(x+pitch*.46f,y)
                            cubicTo(x+pitch*.15f,y+side*sign*.8.dp.toPx(),x+pitch*.20f,y+side*sign*3.3.dp.toPx(),x+pitch*.64f,y+side*sign*2.8.dp.toPx())
                            cubicTo(x+pitch*.79f,y+side*sign*1.3.dp.toPx(),x+pitch*.57f,y+side*sign*.6.dp.toPx(),x+pitch*.46f,y)
                            close()
                        }
                        drawPath(leaf,silver)
                    }
                    x+=pitch
                }
            }
            // Floral end cartouches replace the generic geometric stars. They are
            // original vector ornament, not copied stock calligraphy or a false verse.
            val side=58.dp.toPx()
            for(left in listOf(1.dp.toPx(),size.width-side-1.dp.toPx())) {
                val top=10.dp.toPx()
                val h=size.height-2*top
                val center=Offset(left+side/2,size.height/2)
                drawRoundRect(gold,Offset(left+4.dp.toPx(),top),Size(side-8.dp.toPx(),h),androidx.compose.ui.geometry.CornerRadius(h*.48f),style=Stroke(.8.dp.toPx()))
                drawOval(gold,Offset(left+9.dp.toPx(),top+2.dp.toPx()),Size(side-18.dp.toPx(),h-4.dp.toPx()),style=Stroke(fine))
                val radius=minOf(side*.36f,h*.42f)
                repeat(12) { i->rotate(i*30f,center) {
                    val petal=Path().apply {
                        moveTo(center.x,center.y+radius*.12f)
                        cubicTo(center.x-radius*.78f,center.y-radius*.55f,center.x+radius*.48f,center.y-radius*1.25f,center.x+radius*.18f,center.y-radius*.50f)
                        cubicTo(center.x+radius*.09f,center.y-radius*.21f,center.x-radius*.14f,center.y-radius*.12f,center.x,center.y+radius*.12f)
                    }
                    drawPath(petal,Kiswah.thread.copy(alpha=.18f))
                    drawPath(petal,gold,style=Stroke(.55.dp.toPx()))
                }}
                drawCircle(gold,radius*.56f,center,style=Stroke(fine))
                drawCircle(Kiswah.threadSoft,radius*.12f,center,style=Stroke(fine))
            }
        }
        Text(Kiswah.BAND,Modifier.fillMaxWidth().padding(horizontal=68.dp,vertical=clearance).testTag("kiswah-calligraphy-band")
            .semantics{contentDescription=Kiswah.BAND},style=TextStyle(platformStyle=PlatformTextStyle(includeFontPadding=false),brush=Kiswah.goldText),
            fontFamily=KiswahLandscapeArabic,fontWeight=FontWeight.Normal,fontSize=(baseSize*fit).sp,
            lineHeight=((if(short)26f else 34f)/density.fontScale).sp,textAlign=TextAlign.Center,maxLines=1,softWrap=false)
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

