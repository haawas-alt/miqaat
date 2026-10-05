package com.usman.miqaat.data

/** Pure compatibility rules; Android remains the final signature authority. */
internal object UpdatePolicy {
    fun selfUpdateEnabled(directDownload: Boolean, debug: Boolean) = directDownload && !debug

    fun releaseVersionCode(body: String, build: Int): Int? {
        val raw = Regex("version_code=(\\d+)").find(body)?.groupValues?.get(1)
        val value = if (raw != null) raw.toLongOrNull() else 100L + build
        return value?.takeIf { it in 1L..2_100_000_000L }?.toInt()
    }

    fun archiveProblem(installedPackage: String, archivePackage: String?, installedCode: Long, archiveCode: Long, installedSigners: Set<String>, archiveSigners: Set<String>): String? = when {
        archivePackage != installedPackage -> "Downloaded APK belongs to a different app; not installing"
        archiveCode <= installedCode -> "Downloaded APK is not newer than this installation; not installing"
        installedSigners.isEmpty() || archiveSigners.isEmpty() || installedSigners != archiveSigners -> "Downloaded APK has a different signing key. Export your settings before changing installations; not installing"
        else -> null
    }
}
