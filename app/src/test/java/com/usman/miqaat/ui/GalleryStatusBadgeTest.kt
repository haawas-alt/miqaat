package com.usman.miqaat.ui

import androidx.compose.ui.test.*
import com.usman.miqaat.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.ZonedDateTime
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34])
class GalleryStatusBadgeTest:ComposeSupport() {
    @Test fun localizedStatusPillsFitAndRemainDistinctAcrossResponsiveHomes() {
        for(dev in Dev.entries) for(lang in Language.entries) for(scale in listOf(1f,1.3f,2f)) {
            val s=AppSettings(theme=AppTheme.PRAYER_GALLERY,language=lang,latitude=-33.93,longitude=151.19,locationName="Mascot, NSW",zoneId="Australia/Sydney",locationSet=true,setupDone=true)
            app.settings.update { s }
            val state=PrayerEngine.state(s,ZonedDateTime.of(2026,10,18,18,53,0,0,ZoneId.of("Australia/Sydney")))
            try {
                show(dev,fontScale=scale,rtl=lang==Language.UR,language=lang) { HomeRouter(state,s,HomeActions({}, {}, {}, {}, {}, {}, {}, false, {}, {}),false,{}) }
                for(now in listOf(true,false)) {
                    val tag=if(now) "gallery-status-now" else "gallery-status-next"
                    val nodes=rule.onAllNodesWithTag(tag,useUnmergedTree=true).fetchSemanticsNodes()
                    assertTrue("$dev $lang $scale: $tag must be present",nodes.isNotEmpty())
                    val status=rule.onAllNodesWithTag(tag,useUnmergedTree=true).onFirst()
                    if(scale>1f) status.performScrollTo()
                    status.assertIsDisplayed().assertTextEquals(L10n.word(s,if(now) "NOW" else "NEXT"))
                    val layouts=mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
                    status.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { it(layouts) }
                    assertEquals(1,layouts.single().lineCount)
                    assertFalse(layouts.single().multiParagraph.didExceedMaxLines)
                    val bounds=status.fetchSemanticsNode().boundsInRoot
                    assertTrue(bounds.width>0 && bounds.height>0)
                }
            } finally { scenario.close() }
        }
    }
}
