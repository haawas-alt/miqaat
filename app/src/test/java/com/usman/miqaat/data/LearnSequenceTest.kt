package com.usman.miqaat.data

import org.junit.Assert.*
import org.junit.Test

class LearnSequenceTest {
    @Test fun eachRecitingRakahHasBasmalaWithoutRepeatingFirstOnlyIstiadhah() {
        val expected=mapOf(Learn.Lesson.FAJR to 21,Learn.Lesson.MAGHRIB to 29,Learn.Lesson.FOUR to 36)
        for (lesson in Learn.Lesson.entries) {
            val actions=Learn.actions(lesson)
            assertEquals("Keep saved progress indices stable",expected.getValue(lesson),actions.size)
            assertEquals(1,actions.count { it.step.arabic==Adhkar.salah[2].arabic })
            for (r in 1..lesson.rakat) {
                val f=actions.single { it.rakah==r && it.step.arabic==Adhkar.salah[3].arabic }
                assertEquals(r>1,f.step.reciteBasmalaBefore)
                assertEquals(Adhkar.salah[3].arabic,f.step.arabic)
                assertEquals(Adhkar.salah[3].transliteration,f.step.transliteration)
                assertTrue(UrduContent.note(f.step).isNotBlank())
            }
            assertEquals(lesson.rakat*2,actions.count { it.posture==Learn.Posture.PROSTRATING })
            assertEquals(Learn.Posture.SALAM,actions.last().posture)
        }
    }
}
