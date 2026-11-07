package com.usman.miqaat.ui

import com.usman.miqaat.data.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Test

class FridayThemeIdentityTest {
    @Test fun ma2AndGa2UseTheirCorrectApprovedFridayVisualsOnly() {
        assertEquals(AppTheme.PRAYER_GALLERY, fridayApprovedVisualTheme(AppTheme.MIQAAT))
        assertEquals(AppTheme.MIQAAT, fridayApprovedVisualTheme(AppTheme.PRAYER_GALLERY))
        assertEquals(AppTheme.KISWAH, fridayApprovedVisualTheme(AppTheme.KISWAH))
        assertEquals(AppTheme.CELESTIAL_MERIDIAN, fridayApprovedVisualTheme(AppTheme.CELESTIAL_MERIDIAN))
    }
}
