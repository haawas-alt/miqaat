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
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.R
import com.usman.miqaat.data.*
import kotlin.math.*

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

/** Double stitched rails and interlaced end medallions; Arabic remains real selectable text. */
@Composable
internal fun OrnateLandscapeBand() {
    val clearance=if(LocalConfiguration.current.screenHeightDp<390)6.dp else 12.dp
    Box(Modifier.fillMaxWidth().padding(vertical=1.dp).testTag("kiswah-calligraphy-frame").background(Color.Black.copy(alpha=.88f)),contentAlignment=Alignment.Center) {
        Canvas(Modifier.matchParentSize().testTag("kiswah-band-ornament")) {
            val gold=Kiswah.thread;val fine=.55.dp.toPx();val inset=4.dp.toPx();val side=37.dp.toPx()
            drawRect(gold,style=Stroke(.8.dp.toPx()))
            drawRect(gold.copy(alpha=.8f),Offset(0f,inset),Size(size.width,size.height-2*inset),style=Stroke(fine))
            for(x in 0..(size.width/7.dp.toPx()).toInt()) {
                val px=x*7.dp.toPx()
                rotate(45f,Offset(px,2.dp.toPx())){drawRect(gold,Offset(px-.6.dp.toPx(),1.4.dp.toPx()),Size(1.2.dp.toPx(),1.2.dp.toPx()))}
                rotate(45f,Offset(px,size.height-2.dp.toPx())){drawRect(gold,Offset(px-.6.dp.toPx(),size.height-2.6.dp.toPx()),Size(1.2.dp.toPx(),1.2.dp.toPx()))}
            }
            for(left in listOf(0f,size.width-side)) {
                drawRect(gold.copy(alpha=.75f),Offset(left,inset),Size(side,size.height-2*inset),style=Stroke(fine))
                val center=Offset(left+side/2,size.height/2);val radius=minOf(side*.43f,size.height*.38f)
                for(turn in listOf(0f,45f))rotate(turn,center) {
                    val half=radius*.69f
                    drawRoundRect(gold,Offset(center.x-half,center.y-half),Size(half*2,half*2),androidx.compose.ui.geometry.CornerRadius(radius*.12f),style=Stroke(.85.dp.toPx()))
                    drawRoundRect(gold.copy(alpha=.8f),Offset(center.x-half*.72f,center.y-half*.72f),Size(half*1.44f,half*1.44f),androidx.compose.ui.geometry.CornerRadius(radius*.09f),style=Stroke(fine))
                }
                repeat(8) { petal->rotate(petal*45f,center) {
                    val p=Path().apply {
                        moveTo(center.x,center.y-radius*.15f)
                        cubicTo(center.x-radius*.42f,center.y-radius*.55f,center.x-radius*.22f,center.y-radius*.84f,center.x,center.y-radius*.9f)
                        cubicTo(center.x+radius*.22f,center.y-radius*.84f,center.x+radius*.42f,center.y-radius*.55f,center.x,center.y-radius*.15f)
                        close()
                    }
                    drawPath(p,gold.copy(alpha=.8f),style=Stroke(fine))
                }}
                drawCircle(gold,radius*.18f,center,style=Stroke(fine))
                for(dx in listOf(-1,1))for(dy in listOf(-1,1)) {
                    val p=Offset(center.x+dx*radius*.78f,center.y+dy*radius*.63f)
                    rotate(45f,p){drawRect(gold,Offset(p.x-2.dp.toPx(),p.y-2.dp.toPx()),Size(4.dp.toPx(),4.dp.toPx()),style=Stroke(fine))}
                }
            }
        }
        Text(Kiswah.BAND,Modifier.fillMaxWidth().padding(horizontal=45.dp,vertical=clearance).testTag("kiswah-calligraphy-band")
            .semantics{contentDescription=Kiswah.BAND},style=TextStyle(platformStyle=PlatformTextStyle(includeFontPadding=false)),fontFamily=Amiri,fontWeight=FontWeight.Bold,fontSize=if(LocalConfiguration.current.screenHeightDp<390)16.sp else 22.sp,lineHeight=if(LocalConfiguration.current.screenHeightDp<390)26.sp else 34.sp,color=Kiswah.threadSoft,textAlign=TextAlign.Center)
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
