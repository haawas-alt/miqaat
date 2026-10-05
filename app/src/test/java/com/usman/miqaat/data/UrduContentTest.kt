package com.usman.miqaat.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Coverage checks; translation and religious accuracy still require human review. */
class UrduContentTest {
    @Test fun everyLessonActionHasLocalizedCueAndPosition() {
        Learn.Lesson.entries.flatMap { Learn.actions(it) }.forEach {
            assertTrue(UrduContent.cue(it).any { ch -> ch in '\u0600'..'\u06ff' })
            assertTrue(UrduContent.position(it.step).any { ch -> ch in '\u0600'..'\u06ff' })
        }
    }
    @Test fun everyPostureAndWordHasLocalizedDescription() {
        assertEquals(Learn.Posture.entries.size, UrduContent.postureLabels.size)
        assertEquals(Learn.Posture.entries.size, UrduContent.postureDescriptions.size)
        assertEquals(Adhkar.salah.size, UrduContent.stepPositions.size)
        assertEquals(Adhkar.salah.size, UrduContent.stepMeanings.size)
        assertEquals(Learn.Posture.entries.size, UrduContent.postureDescriptions.toSet().size)
    }
}
