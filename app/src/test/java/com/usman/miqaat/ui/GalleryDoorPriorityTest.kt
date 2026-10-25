package com.usman.miqaat.ui

import org.junit.Assert.*
import org.junit.Test

class GalleryDoorPriorityTest {
    @Test fun adhkarKeepsPriorityAndCallbackInBothLanguagesWithCompetingDoors() {
        for (label in listOf("Evening adhkār", "شام کے اذکار")) for (warnings in 0..2) {
            var tapped=0
            val adhkar=Door(label,kind=DoorKind.ADHKAR) { tapped++ }
            val alerts=(1..warnings).map { Door("Warning $it",warn=true) }
            val qibla=Door("Qibla")
            val items=listOf(qibla,Door("Friday"),adhkar)+alerts
            val shown=galleryVisibleDoors(items)
            assertEquals(2,shown.size)
            assertTrue(shown.contains(adhkar))
            if(warnings>0) assertTrue(shown.first().warn)
            else assertSame(adhkar,shown.first())
            shown.single { it.kind==DoorKind.ADHKAR }.onClick?.invoke()
            assertEquals(1,tapped)
        }
    }
    @Test fun shortListsAndEmptyListsRetainTheirActions() {
        val items=listOf(Door("One"),Door("Two"))
        assertEquals(items,galleryVisibleDoors(items))
        assertTrue(galleryVisibleDoors(emptyList()).isEmpty())
    }
}
