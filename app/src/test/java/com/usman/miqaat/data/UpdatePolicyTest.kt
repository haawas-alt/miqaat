package com.usman.miqaat.data

import org.junit.Assert.*
import org.junit.Test

class UpdatePolicyTest {
    @Test fun debugAndPlayCannotSelfUpdate() {
        assertFalse(UpdatePolicy.selfUpdateEnabled(true, true))
        assertFalse(UpdatePolicy.selfUpdateEnabled(false, false))
        assertTrue(UpdatePolicy.selfUpdateEnabled(true, false))
    }
    @Test fun releaseCodeIsIndependentOfWorkflowRunNumber() {
        assertEquals(183, UpdatePolicy.releaseVersionCode("build=83", 83))
        assertEquals(1790905000, UpdatePolicy.releaseVersionCode("build=3\\nversion_code=1790905000", 3))
        assertNull(UpdatePolicy.releaseVersionCode("version_code=999999999999999999999999", 3))
        assertNull(UpdatePolicy.releaseVersionCode("version_code=2100000001", 3))
        assertNull(UpdatePolicy.releaseVersionCode("version_code=0", 3))
    }
    @Test fun installerOnlyReceivesSameAppNewerMatchingKey() {
        fun check(pkg: String?, code: Long, key: Set<String>) = UpdatePolicy.archiveProblem("com.usman.miqaat", pkg, 183, code, setOf("release"), key)
        assertNotNull(check("another.app", 184, setOf("release")))
        assertNotNull(check("com.usman.miqaat", 183, setOf("release")))
        assertNotNull(check("com.usman.miqaat", 184, setOf("debug")))
        assertNotNull(check("com.usman.miqaat", 184, emptySet()))
        assertNull(check("com.usman.miqaat", 184, setOf("release")))
    }
}
