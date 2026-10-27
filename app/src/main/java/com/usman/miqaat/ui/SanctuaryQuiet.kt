package com.usman.miqaat.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.usman.miqaat.R
import com.usman.miqaat.azaan.AzaanService.Phase
import com.usman.miqaat.data.*
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.math.*

private val SanctuaryArabic = FontFamily(Font(R.font.sanctuary_arabic))
@OptIn(ExperimentalTextApi::class)
private val SanctuaryDisplay = FontFamily(Font(R.font.cormorant_garamond_medium,FontWeight.Bold,
    variationSettings=FontVariation.Settings(FontVariation.weight(700))))
@OptIn(ExperimentalTextApi::class)
private val SanctuaryCaps = FontFamily(Font(R.font.cinzel,FontWeight.Normal,
    variationSettings=FontVariation.Settings(FontVariation.weight(500))))

/** Coordinates follow the selected A compositions; all prayer facts and controls remain native. */
@Composable internal fun SanctuaryQuietContent(p:Phase.Quiet,s:AppSettings,now:ZonedDateTime,onStop:()->Unit) {
    val c=quietPalette(s.theme);val ur=L10n.isUrdu(s)
    val gallery=s.theme==AppTheme.PRAYER_GALLERY;val kiswah=s.theme==AppTheme.KISWAH
    val celestial=s.theme==AppTheme.CELESTIAL_MERIDIAN
    val gold=Color(0xFFF4CE83);val ink=if(gallery)Color(0xFF102D22) else Color(0xFFFFE6B1)
    val secondary=if(gallery)Color(0xFF825126) else Color(0xFFF4E8D2)
    val times=quietPrayerTimes(s,p.prayer,now)
    val remaining=((p.endsAt-now.toInstant().toEpochMilli()).coerceAtLeast(0)+59_999)/60_000
    val progress=(1f-(p.endsAt-now.toInstant().toEpochMilli()).coerceAtLeast(0)/(s.quietMinutes.coerceAtLeast(1)*60_000f)).coerceIn(0f,1f)
    fun word(en:String,ar:String)=if(ur)ar else en
    fun name(prayer:Prayer)=L10n.prayer(s,prayer).let{if(ur)it else it.uppercase(Locale.ROOT)}
    val jumuah=s.jumuahEnabled && p.prayer==Prayer.DHUHR && times.current.dayOfWeek==java.time.DayOfWeek.FRIDAY
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
    BoxWithConstraints(Modifier.fillMaxSize().background(c.background).testTag("quiet-board")
        .clickable(role=Role.Button,onClickLabel=Str[R.string.s_quiet_exit],onClick=onStop)) {
    BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
        val w=maxWidth;val h=maxHeight;val wide=w>h;val tablet=minOf(w,h)>=600.dp
        fun region(x:Float,y:Float,ww:Float,hh:Float)=Modifier.offset(x=w*x,y=h*y).width(w*ww).height(h*hh)
        val art=when(s.theme) {
            AppTheme.MIQAAT->if(wide)R.drawable.sanctuary_miqaat_land else R.drawable.sanctuary_miqaat_port
            AppTheme.KISWAH->if(wide)R.drawable.sanctuary_kiswah_land else R.drawable.sanctuary_kiswah_port
            AppTheme.CELESTIAL_MERIDIAN->if(wide)R.drawable.sanctuary_celestial_land else R.drawable.sanctuary_celestial_port
            AppTheme.PRAYER_GALLERY->if(wide)R.drawable.sanctuary_gallery_land else R.drawable.sanctuary_gallery_port
        }
        Box(Modifier.matchParentSize().clip(RoundedCornerShape(0.dp)).testTag("quiet-art-${s.theme.name}")) {
            if(s.artTheme!=ArtTheme.MINIMAL) Image(painterResource(art),null,
                Modifier.fillMaxWidth().height(if(wide)h else h*1.055f).offset(y=if(!wide && kiswah)h*.055f else if(!wide && gallery)h*.025f else 0.dp),contentScale=ContentScale.FillBounds)
        }
        if(kiswah)Box(region(0f,if(wide).062f else .055f,1f,.04f).testTag("quiet-sanctuary-ornament").semantics{contentDescription=word("Gold embroidered ornament","سنہری کڑھائی")})
        // Header follows each reference: mode below the portrait brand row, with fine rules.
        val headerY=if(wide).015f else .010f
        val headerH=if(wide).048f else .031f
        Row(region(if(gallery && wide).15f else .045f,headerY,if(gallery && wide).82f else .91f,headerH),verticalAlignment=Alignment.CenterVertically) {
            if(gallery || celestial || kiswah) SanctuaryIcon(if(kiswah)"star" else "arch",if(gallery)secondary else gold,Modifier.height(h*headerH*.8f).aspectRatio(1f).padding(end=4.dp))
            SanctuaryText("Miqaat",s,h.value*(if(wide).043f else .030f),ink,Modifier.weight(1f).fillMaxHeight().testTag("quiet-brand"),SanctuaryDisplay,TextAlign.Start)
            SanctuaryText(L10n.iso(L10n.at(now,s.use24h)),s,h.value*(if(wide).035f else .025f),ink,Modifier.weight(1f).fillMaxHeight().testTag("quiet-live-clock"),SanctuaryDisplay,TextAlign.End)
        }
        Row(region(if(wide).40f else .29f,if(wide).026f else .045f,if(wide).20f else .42f,if(wide).035f else .026f),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f).height(.6.dp).background(secondary.copy(alpha=.7f)))
            SanctuaryText(Str[R.string.s_quiet_mode_title].uppercase(Locale.ROOT),s,h.value*(if(wide).018f else .012f),secondary,
                Modifier.weight(3.4f).fillMaxHeight().then(if(gallery)Modifier.background(Color(0xFFE8D6B9),RoundedCornerShape(30.dp)).border(.5.dp,secondary.copy(alpha=.25f),RoundedCornerShape(30.dp))else Modifier).testTag("quiet-mode-label"),SanctuaryCaps)
            Box(Modifier.weight(1f).height(.6.dp).background(secondary.copy(alpha=.7f)))
        }
        val cx=if(!wide).5f else if(kiswah).28f else if(gallery).35f else .33f
        val currentY=if(wide)if(kiswah).16f else .12f else if(kiswah).105f else .075f
        val currentH=if(wide)if(gallery).52f else .68f else if(gallery).315f else .325f
        val currentW=if(wide).46f else .76f
        BoxWithConstraints(region(cx-currentW/2,currentY,currentW,currentH).testTag("quiet-current-panel")) {
            val ph=maxHeight
            fun row(y:Float,hh:Float)=Modifier.offset(y=ph*y).fillMaxWidth().height(ph*hh)
            SanctuaryText(if(jumuah)"الجمعة" else p.prayer.arabic,s,ph.value*.43f,ink,row(0f,.31f).testTag("quiet-current-arabic").semantics{heading()},SanctuaryArabic,gold=!gallery)
            SanctuaryText(if(jumuah)L10n.word(s,"Jumuʿah").uppercase(Locale.ROOT)else name(p.prayer),s,ph.value*.085f,ink,row(.315f,.085f).testTag("quiet-current-name"),SanctuaryCaps)
            if(!gallery && !celestial)SanctuaryDivider(ink,row(.425f,.035f).padding(horizontal=maxWidth*.23f))
            val ribbon=if(gallery)Modifier.background(Color(0xFFA44825),RoundedCornerShape(30.dp))else Modifier
            Box(row(if(gallery).455f else .49f,.065f),contentAlignment=Alignment.Center) {
                SanctuaryText(Str[R.string.s_quiet_prayer_progress].uppercase(Locale.ROOT),s,ph.value*.047f,if(gallery)Color(0xFFFFF2DA)else ink,
                    Modifier.fillMaxWidth(if(gallery).75f else 1f).fillMaxHeight().then(ribbon).testTag("quiet-progress"),SanctuaryCaps)
            }
            SanctuaryText(word("AZAAN AT","اذان کا وقت"),s,ph.value*.043f,secondary,row(if(gallery).59f else .61f,.06f).testTag("quiet-current-label"),SanctuaryCaps)
            val stack=wide && s.theme==AppTheme.MIQAAT && !s.use24h && !ur
            SanctuaryClock(times.current,s,ink,row(if(gallery).665f else .68f,if(stack).30f else .245f).testTag("quiet-current-time"),gold=!gallery,stack=stack)
            SanctuaryText(Str[R.string.s_quiet_iqamah_complete],s,ph.value*.055f,secondary,row(if(stack).975f else .935f,.065f).testTag("quiet-iqamah-complete"),SanctuaryDisplay,italic=gallery)
        }
        val nx=if(!wide).5f else if(kiswah).707f else if(celestial).766f else .748f
        val nextY=if(wide)if(kiswah).32f else if(celestial).34f else .28f else if(gallery).55f else .585f
        val nextH=if(wide)if(celestial).42f else .50f else if(gallery).245f else .29f
        val nextW=if(wide).245f else .50f
        BoxWithConstraints(region(nx-nextW/2,nextY,nextW,nextH).testTag("quiet-next-panel")) {
            val ph=maxHeight
            fun row(y:Float,hh:Float)=Modifier.offset(y=ph*y).fillMaxWidth().height(ph*hh)
            SanctuaryText(Str[R.string.s_quiet_next_azaan].uppercase(Locale.ROOT),s,ph.value*.047f,secondary,row(0f,.09f).testTag("quiet-next-label"),SanctuaryCaps)
            if(gallery)SanctuaryDivider(secondary,row(.10f,.025f))
            SanctuaryText(times.next.arabic,s,ph.value*.35f,ink,row(.14f,.28f).testTag("quiet-next-arabic"),SanctuaryArabic,gold=!gallery)
            SanctuaryText(name(times.next),s,ph.value*.075f,ink,row(.425f,.10f).testTag("quiet-next-name"),SanctuaryCaps)
            if(!gallery && !celestial)SanctuaryDivider(ink,row(.55f,.025f).padding(horizontal=maxWidth*.15f))
            val stack=wide && s.theme==AppTheme.MIQAAT && !s.use24h && !ur
            SanctuaryClock(times.nextTime,s,ink,row(if(gallery || celestial).54f else .59f,if(stack).32f else .27f).testTag("quiet-next-time"),gold=!gallery,stack=stack)
            val mins=java.time.Duration.between(now,times.nextTime).toMinutes()
            val relative=if(!ur && mins>0 && mins%60==0L)"In ${mins/60} ${if(mins==60L)"hour" else "hours"}"else L10n.relative(s,times.nextTime,now).replaceFirstChar{it.uppercase()}
            SanctuaryText(relative,s,ph.value*.065f,secondary,row(if(stack).92f else .87f,.08f).testTag("quiet-next-relative"),SanctuaryDisplay,italic=gallery)
            if(times.nextTime.toLocalDate().isAfter(now.withZoneSameInstant(s.zone()).toLocalDate()))
                SanctuaryText(Str[R.string.s_quiet_tomorrow],s,ph.value*.04f,secondary,row(.96f,.05f).testTag("quiet-tomorrow"),SanctuaryDisplay)
        }
        val footerH=maxOf(h*(if(wide).105f else .095f),if(wide || tablet && !kiswah)56.dp else 100.dp)
        SanctuaryFooter(s,remaining,progress,wide,tablet,ink,secondary,Modifier.offset(y=h-footerH).fillMaxWidth().height(footerH),onStop)
        if(s.nightDim && p.prayer==Prayer.ISHA)Box(Modifier.matchParentSize().background(Color.Black.copy(alpha=.22f)))
    }
    }
    }
}

@Composable private fun SanctuaryFooter(s:AppSettings,remaining:Long,progress:Float,wide:Boolean,tablet:Boolean,ink:Color,secondary:Color,modifier:Modifier,onStop:()->Unit) {
    val gallery=s.theme==AppTheme.PRAYER_GALLERY;val kiswah=s.theme==AppTheme.KISWAH;val celestial=s.theme==AppTheme.CELESTIAL_MERIDIAN
    val ur=L10n.isUrdu(s);val hint=if(ur)"خاموش موڈ $remaining منٹ میں ختم ہوگا"else "Quiet mode ends in $remaining min"
    BoxWithConstraints(modifier.testTag("quiet-sanctuary-footer")) {
        val fw=maxWidth;val fh=maxHeight;val stacked=!wide && (!tablet || kiswah)
        val bg=if(gallery)Color(0xFFF6EBD7)else Color(0xFF041022).copy(alpha=if(kiswah).12f else .7f)
        val shape=RoundedCornerShape(if(gallery)0.dp else 40.dp)
        val rail=if(gallery)Modifier.fillMaxSize().background(bg)else Modifier.padding(horizontal=fw*.04f,vertical=if(stacked)0.dp else fh*.15f).fillMaxSize().background(bg,shape).then(if(!kiswah && !celestial)Modifier.border(.6.dp,secondary.copy(alpha=.55f),shape)else Modifier)
        Box(rail)
        val returnW=if(stacked)fw*(if(kiswah).65f else .46f)else fw*(if(gallery).23f else .22f)
        val buttonX=if(stacked)(fw-returnW)/2 else fw-returnW-fw*.035f
        val buttonY=if(stacked)fh*.48f else 0.dp
        Box(Modifier.offset(x=buttonX,y=buttonY).width(returnW).height(maxOf(48.dp,if(stacked)fh*.45f else fh)).testTag("quiet-return").clickable(role=Role.Button,onClick=onStop),contentAlignment=Alignment.Center) {
            Row(Modifier.fillMaxWidth().height(if(stacked)minOf(40.dp,fh*.42f)else minOf(48.dp,fh*.54f)).clip(RoundedCornerShape(30.dp))
                .background(if(gallery)ink else Color(0xFF030C16).copy(alpha=.65f)).border(.8.dp,secondary.copy(alpha=.8f),RoundedCornerShape(30.dp)).padding(horizontal=8.dp),
                verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center) {
                if(gallery || celestial)SanctuaryIcon("arch",if(gallery)Color(0xFFECC882)else ink,Modifier.size(if(tablet)22.dp else 15.dp).padding(end=4.dp))
                SanctuaryText(Str[R.string.s_quiet_exit],s,if(tablet)20f else if(wide)13f else 12f,if(gallery)Color(0xFFFFF0D3)else ink,Modifier.weight(1f).fillMaxHeight().testTag("quiet-exit-label"),SanctuaryDisplay)
                if(!gallery && !celestial && !kiswah)SanctuaryText("›",s,24f,ink,Modifier.width(13.dp).fillMaxHeight(),SanctuaryDisplay)
            }
        }
        val available=if(stacked)fw*.90f else buttonX-fw*.065f
        Row(Modifier.offset(x=fw*.05f,y=if(stacked)0.dp else fh*.13f).width(available).height(if(stacked)fh*.45f else fh*.74f),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(if(tablet)14.dp else 7.dp)) {
            if(gallery)SanctuaryRing(progress,ink,Modifier.height(if(stacked)fh*.34f else fh*.58f).aspectRatio(1f))
            else SanctuaryIcon(if(kiswah)"arch"else"moon",ink,Modifier.height(if(stacked)fh*.27f else fh*.5f).aspectRatio(1f))
            val inline=(!celestial && !kiswah && (!gallery || wide))
            if(inline && !gallery)SanctuaryProgress(progress,ink,Modifier.weight(.43f).height(if(tablet)7.dp else 4.dp))
            Column(Modifier.weight(if(inline && !gallery).57f else 1f),verticalArrangement=Arrangement.Center) {
                SanctuaryText(hint,s,if(tablet)18f else if(wide)12f else 10f,if(gallery)ink else secondary,Modifier.fillMaxWidth().height(if(tablet)27.dp else 17.dp).testTag("quiet-expiry-hint"),SanctuaryDisplay,if(inline)TextAlign.Start else TextAlign.Center)
                if(!inline)SanctuaryProgress(progress,if(gallery)ink else Color(0xFFF6D18B),Modifier.fillMaxWidth().height(if(tablet)7.dp else 4.dp))
            }
            if(inline && gallery)SanctuaryProgress(progress,ink,Modifier.weight(1.5f).height(if(tablet)9.dp else 5.dp))
        }
    }
}

@Composable private fun SanctuaryProgress(progress:Float,color:Color,modifier:Modifier) {
    Canvas(modifier.testTag("quiet-duration-progress").semantics{progressBarRangeInfo=ProgressBarRangeInfo(progress,0f..1f)}) {
        val radius=androidx.compose.ui.geometry.CornerRadius(size.height)
        drawRoundRect(color.copy(alpha=.23f),cornerRadius=radius)
        drawRoundRect(Brush.horizontalGradient(listOf(color.copy(alpha=.7f),color)),size=androidx.compose.ui.geometry.Size(size.width*progress,size.height),cornerRadius=radius)
    }
}
@Composable private fun SanctuaryRing(progress:Float,color:Color,modifier:Modifier) {
    Canvas(modifier){val stroke=size.width*.1f;drawCircle(color.copy(alpha=.18f),style=Stroke(stroke));drawArc(color,-90f,360f*progress,false,style=Stroke(stroke,cap=StrokeCap.Round))}
}
@Composable private fun SanctuaryIcon(kind:String,color:Color,modifier:Modifier) {
    Canvas(modifier) {
        val w=size.width;val h=size.height
        if(kind=="moon") {
            val p=Path().apply{moveTo(w*.72f,h*.05f);cubicTo(-w*.15f,h*.02f,-w*.15f,h*1.03f,w*.72f,h*.92f);cubicTo(w*.15f,h*.73f,w*.19f,h*.25f,w*.72f,h*.05f)};drawPath(p,color)
            drawCircle(color,w*.035f,Offset(w*.88f,h*.23f))
        }else if(kind=="arch") {
            for(inset in listOf(.05f,.22f)) {
                val p=Path().apply{moveTo(w*inset,h*.94f);lineTo(w*inset,h*.40f);cubicTo(w*inset,h*.24f,w*.40f,h*.17f,w*.5f,h*.04f);cubicTo(w*.60f,h*.17f,w*(1-inset),h*.24f,w*(1-inset),h*.40f);lineTo(w*(1-inset),h*.94f)};drawPath(p,color,style=Stroke(maxOf(1f,w*.035f)))
            }
        }else {
            val p=Path();for(i in 0..16){val a=i*PI/8-PI/2;val r=if(i%2==0).47f else .23f;val x=w*(.5f+cos(a).toFloat()*r);val y=h*(.5f+sin(a).toFloat()*r);if(i==0)p.moveTo(x,y)else p.lineTo(x,y)};p.close();drawPath(p,color,style=Stroke(maxOf(1f,w*.025f)))
        }
    }
}
@Composable private fun SanctuaryDivider(color:Color,modifier:Modifier) {
    Row(modifier,verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f).height(.6.dp).background(Brush.horizontalGradient(listOf(Color.Transparent,color))))
        SanctuaryIcon("star",color,Modifier.fillMaxHeight().aspectRatio(1f))
        Box(Modifier.weight(1f).height(.6.dp).background(Brush.horizontalGradient(listOf(color,Color.Transparent))))
    }
}

/** Measure before drawing: font variation, line metrics and native glyphs use the same style. */
@Composable private fun SanctuaryText(value:String,s:AppSettings,size:Float,color:Color,modifier:Modifier,family:FontFamily,align:TextAlign=TextAlign.Center,gold:Boolean=false,italic:Boolean=false) {
    val ur=L10n.isUrdu(s);val measurer=rememberTextMeasurer();val density=LocalDensity.current
    var layout by remember{mutableStateOf<TextLayoutResult?>(null)}
    BoxWithConstraints(modifier.semantics{text=AnnotatedString(value);getTextLayoutResult{out->layout?.let{out.add(it);true}?:false}},contentAlignment=Alignment.Center) {
        fun style(fs:Float)=TextStyle(fontFamily=if(ur && family!=SanctuaryArabic)Amiri else family,fontSize=fs.sp,
            fontWeight=if(family==SanctuaryDisplay)FontWeight.Bold else FontWeight.Normal,fontStyle=if(italic)FontStyle.Italic else FontStyle.Normal,
            lineHeight=(fs*(if(ur && family!=SanctuaryArabic)1.4f else 1f)).sp,
            letterSpacing=if(!ur && family==SanctuaryCaps)(fs*.22f).sp else 0.sp,
            platformStyle=PlatformTextStyle(includeFontPadding=false),textAlign=align,textDirection=if(ur)TextDirection.ContentOrRtl else TextDirection.ContentOrLtr,color=color)
        val fitted=remember(value,size,maxWidth,maxHeight,density.fontScale){val m=measurer.measure(value,style(size),softWrap=false,maxLines=1).size;minOf(1f,with(density){maxWidth.toPx()}/m.width.coerceAtLeast(1),with(density){maxHeight.toPx()}/m.height.coerceAtLeast(1))*size}
        val st=style(fitted)
        Text(value,Modifier.fillMaxWidth(),style=if(gold)st.copy(brush=Brush.verticalGradient(listOf(Color(0xFFFFEAC0),Color(0xFFE9AD50))))else st,maxLines=1,onTextLayout={layout=it})
    }
}

@Composable private fun SanctuaryClock(time:ZonedDateTime,s:AppSettings,color:Color,modifier:Modifier,gold:Boolean,stack:Boolean=false) {
    val full=L10n.iso(L10n.at(time,s.use24h));val digits=PrayerEngine.clock(time,s.use24h)
    val ur=L10n.isUrdu(s);val measurer=rememberTextMeasurer();val density=LocalDensity.current
    var layout by remember{mutableStateOf<TextLayoutResult?>(null)}
    BoxWithConstraints(modifier.semantics{text=AnnotatedString(full);getTextLayoutResult{out->layout?.let{out.add(it);true}?:false}},contentAlignment=Alignment.Center) {
        fun annotated(fs:Float)=buildAnnotatedString {
            val start=full.indexOf(digits)
            if(start<0)append(full)else {
                if(stack) {
                    withStyle(ParagraphStyle(lineHeight=(fs*.95f).sp)) {withStyle(SpanStyle(fontSize=fs.sp)){append(digits)}}
                    withStyle(ParagraphStyle(lineHeight=(fs*.35f).sp)) {append(full.substring(start+digits.length).trim())}
                } else {
                    append(full.substring(0,start));withStyle(SpanStyle(fontSize=fs.sp)){append(digits)};append(full.substring(start+digits.length))
                }
            }
        }
        fun style(fs:Float)=TextStyle(fontFamily=if(ur)Amiri else SanctuaryDisplay,fontWeight=if(ur)FontWeight.Normal else FontWeight.Bold,
            fontSize=(fs*.30f).sp,fontFeatureSettings="'lnum' 1, 'tnum' 1",lineHeight=(fs*(if(ur)1.35f else .95f)).sp,
            platformStyle=PlatformTextStyle(includeFontPadding=false),textAlign=TextAlign.Center,color=color)
        val size=maxHeight.value*(if(stack)1.05f else 1.30f)
        val fitted=remember(full,size,maxWidth,maxHeight,density.fontScale,stack){val m=measurer.measure(annotated(size),style(size),softWrap=false,maxLines=if(stack)2 else 1).size;minOf(1f,with(density){maxWidth.toPx()}/m.width.coerceAtLeast(1),with(density){maxHeight.toPx()}/m.height.coerceAtLeast(1))*size}
        val st=style(fitted)
        Text(annotated(fitted),Modifier.fillMaxWidth(),style=if(gold)st.copy(brush=Brush.verticalGradient(listOf(Color(0xFFFFEDC9),Color(0xFFEAB45F))))else st,maxLines=if(stack)2 else 1,onTextLayout={layout=it})
    }
}
