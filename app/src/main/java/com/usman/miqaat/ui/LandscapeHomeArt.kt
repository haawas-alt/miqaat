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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
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
        if(s.artTheme!=ArtTheme.MINIMAL)when(s.theme) {
            AppTheme.MIQAAT->{
                Image(painterResource(R.drawable.quiet_miqaat_landscape),null,Modifier.matchParentSize(),contentScale=ContentScale.Crop)
                Box(Modifier.matchParentSize().background(Brush.horizontalGradient(0f to Color(0xFF03162D),.55f to Color(0xFF03162D).copy(alpha=.96f),.66f to Color.Transparent)))
                Image(painterResource(R.drawable.approved_miqaat_arch),null,Modifier.align(Alignment.CenterEnd).fillMaxHeight().fillMaxWidth(.39f),contentScale=ContentScale.Crop,alignment=Alignment.TopCenter)
            }
            AppTheme.KISWAH->{
                Weave(Modifier.matchParentSize())
                Image(painterResource(R.drawable.approved_kiswah_silk),null,Modifier.align(Alignment.CenterEnd).fillMaxHeight().fillMaxWidth(.18f),contentScale=ContentScale.Crop,alignment=Alignment.CenterEnd)
            }
            AppTheme.CELESTIAL_MERIDIAN->Image(painterResource(R.drawable.home_celestial_landscape_parity),null,Modifier.matchParentSize(),contentScale=ContentScale.Crop)
            AppTheme.PRAYER_GALLERY->Image(painterResource(R.drawable.quiet_gallery_landscape),null,Modifier.matchParentSize(),contentScale=ContentScale.Crop)
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
                fun star(r:Float,phase:Float):Path=Path().apply {
                    repeat(16){i->val angle=(i*PI/8+phase).toFloat();val rr=if(i%2==0)r else r*.58f
                        val x=center.x+cos(angle)*rr;val y=center.y+sin(angle)*rr
                        if(i==0)moveTo(x,y)else lineTo(x,y)};close()
                }
                drawPath(star(radius,0f),gold,style=Stroke(.8.dp.toPx()))
                drawPath(star(radius*.72f,(PI/8).toFloat()),gold,style=Stroke(fine))
                drawCircle(gold,radius*.17f,center,style=Stroke(fine))
                for(dx in listOf(-1,1))for(dy in listOf(-1,1)) {
                    val p=Offset(center.x+dx*radius*.78f,center.y+dy*radius*.63f)
                    rotate(45f,p){drawRect(gold,Offset(p.x-2.dp.toPx(),p.y-2.dp.toPx()),Size(4.dp.toPx(),4.dp.toPx()),style=Stroke(fine))}
                }
            }
        }
        Text(Kiswah.BAND,Modifier.fillMaxWidth().padding(horizontal=45.dp,vertical=clearance).testTag("kiswah-calligraphy-band")
            .semantics{contentDescription=Kiswah.BAND},fontFamily=Amiri,fontSize=14.sp,lineHeight=22.sp,color=Kiswah.threadSoft,textAlign=TextAlign.Center)
    }
}
