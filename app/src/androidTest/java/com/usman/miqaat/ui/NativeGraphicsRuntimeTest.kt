package com.usman.miqaat.ui

import android.graphics.Path
import android.system.Os
import android.system.OsConstants
import androidx.graphics.path.PathIterator
import androidx.graphics.path.PathSegment
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Runtime coverage for the source-built native payload, beyond ELF/ZIP inspection. */
@RunWith(AndroidJUnit4::class)
class NativeGraphicsRuntimeTest {
    @Test fun nativeLibraryLoadsOnTheExpectedMemoryPageSize() {
        val expected = InstrumentationRegistry.getArguments().getString("expectedPageSize")!!.toLong()
        assertEquals("Verify that the test actually uses the required page size", expected, Os.sysconf(OsConstants._SC_PAGESIZE))
        System.loadLibrary("androidx.graphics.path")
    }

    @Test fun nativeFallbackPreservesPathGeometryAndConicConversion() {
        System.loadLibrary("androidx.graphics.path")
        val path = Path().apply {
            moveTo(2f, 3f)
            lineTo(12f, 13f)
            quadTo(20f, 5f, 30f, 15f)
            cubicTo(32f, 5f, 38f, 25f, 40f, 15f)
            close()
        }
        val iterator = PathIterator(path)
        val points = FloatArray(8)
        val types = mutableListOf<PathSegment.Type>()
        while (iterator.hasNext()) {
            assertTrue("Iterator must terminate", types.size < 32)
            types += iterator.next(points)
            assertTrue("Coordinates remain finite", points.all { it.isFinite() })
        }
        assertTrue(types.containsAll(listOf(PathSegment.Type.Move, PathSegment.Type.Line,
            PathSegment.Type.Quadratic, PathSegment.Type.Cubic, PathSegment.Type.Close)))
        val oval = Path().apply { addOval(0f, 0f, 40f, 20f, Path.Direction.CW) }
        val curves = PathIterator(oval, PathIterator.ConicEvaluation.AsQuadratics)
        var count = 0
        while (curves.hasNext()) {
            curves.next(points)
            assertTrue(points.all { it.isFinite() })
            assertTrue("Conic conversion must terminate", ++count < 64)
        }
        assertTrue(count >= 4)
    }
}
