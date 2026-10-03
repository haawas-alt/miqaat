package com.usman.miqaat.data

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.usman.miqaat.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Direct-download edition only (BuildConfig.SELF_UPDATE). Checks the repo's "latest" GitHub release,
 * downloads the APK with DownloadManager, verifies it against the SHA-256 published alongside it,
 * and only then hands it to the system installer. Android additionally refuses an APK whose
 * signing certificate differs from the installed app's.
 */
object Updater {
    data class Info(val build: Int, val apkUrl: String, val versionName: String, val sha256Url: String, val commit: String, val versionCode: Int = 100 + build)
    val enabled: Boolean get() = UpdatePolicy.selfUpdateEnabled(BuildConfig.SELF_UPDATE, BuildConfig.DEBUG)

    sealed class State {
        object Idle : State()
        object Checking : State()
        object UpToDate : State()
        data class Available(val info: Info) : State()
        data class Downloading(val info: Info) : State()
        data class Ready(val file: File) : State()
        data class Failed(val reason: String) : State()
    }

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state
    private const val PREFS = "miqaat_updater"

    val currentBuild: Int get() = BuildConfig.BUILD_NUMBER
    val currentName: String get() = BuildConfig.VERSION_NAME

    suspend fun check(ctx: Context, force: Boolean = false) {
        if (!enabled) return
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val lastCheck = prefs.getLong("lastCheck", 0L)            // survives process death
        if (!force && System.currentTimeMillis() - lastCheck < 6 * 3600_000L) return
        if (_state.value is State.Downloading) return
        _state.value = State.Checking
        val info = withContext(Dispatchers.IO) {
            runCatching {
                val c = URL("https://api.github.com/repos/${BuildConfig.REPO}/releases/tags/latest").openConnection() as HttpURLConnection
                c.connectTimeout = 8000; c.readTimeout = 8000
                c.setRequestProperty("Accept", "application/vnd.github+json")
                val json = JSONObject(c.inputStream.bufferedReader().readText())
                val body = json.optString("body")
                val build = Regex("build=(\\d+)").find(body)?.groupValues?.get(1)?.toInt() ?: 0
                val commit = Regex("commit=([0-9a-f]{7,40})").find(body)?.groupValues?.get(1) ?: ""
                val assets = json.getJSONArray("assets")
                var url = ""; var sha = ""
                for (i in 0 until assets.length()) {
                    val a = assets.getJSONObject(i); val name = a.getString("name")
                    if (name.endsWith(".apk")) url = a.getString("browser_download_url")
                    if (name.endsWith(".sha256")) sha = a.getString("browser_download_url")
                }
                Info(build, url, "1.$build", sha, commit, UpdatePolicy.releaseVersionCode(body, build) ?: error("Invalid release version code"))
            }.getOrNull()
        }
        prefs.edit().putLong("lastCheck", System.currentTimeMillis()).apply()
        _state.value = when {
            info == null -> State.Failed("Couldn't reach GitHub. Is the tablet online?")
            info.apkUrl.isEmpty() -> State.Failed("No APK in the latest release")
            info.sha256Url.isEmpty() -> State.Failed("Release has no checksum; not installing")
            info.versionCode > BuildConfig.VERSION_CODE -> State.Available(info)
            else -> State.UpToDate
        }
    }

    /** False on the Play edition, which neither declares REQUEST_INSTALL_PACKAGES nor may ask the system about it (that call crashed About in 1.78). */
    fun canInstall(ctx: Context): Boolean = enabled && runCatching { ctx.packageManager.canRequestPackageInstalls() }.getOrDefault(false)

    fun openInstallPermission(ctx: Context) {
        if (!enabled) return
        runCatching { ctx.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    fun download(ctx: Context, info: Info) {
        if (!enabled) return
        val dir = File(ctx.getExternalFilesDir(null), "updates").apply { mkdirs() }
        val file = File(dir, "Miqaat-${info.build}.apk")
        if (file.exists()) { verifyThenInstall(ctx, info, file); return }
        dir.listFiles()?.forEach { it.delete() }
        val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val req = DownloadManager.Request(Uri.parse(info.apkUrl))
            .setTitle("Miqaat ${info.versionName}")
            .setDescription("Downloading update")
            .setMimeType("application/vnd.android.package-archive")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalFilesDir(ctx, null, "updates/${file.name}")
        val id = dm.enqueue(req)
        _state.value = State.Downloading(info)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, i: Intent) {
                if (i.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) != id) return
                runCatching { c.unregisterReceiver(this) }
                if (file.exists() && file.length() > 1_000_000) verifyThenInstall(c, info, file)
                else _state.value = State.Failed("Download failed")
            }
        }
        ContextCompat.registerReceiver(ctx.applicationContext, receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), ContextCompat.RECEIVER_EXPORTED)
    }

    /** Compares the file's SHA-256 with the digest published in the release; a mismatch deletes the file. */
    private fun verifyThenInstall(ctx: Context, info: Info, file: File) {
        Thread {
            val ok = runCatching {
                val c = URL(info.sha256Url).openConnection() as HttpURLConnection
                c.connectTimeout = 8000; c.readTimeout = 8000
                val expected = c.inputStream.bufferedReader().readText().trim().split(Regex("\\s+")).first().lowercase()
                val md = java.security.MessageDigest.getInstance("SHA-256")
                file.inputStream().use { ins -> val buf = ByteArray(65536); var n: Int; while (ins.read(buf).also { n = it } > 0) md.update(buf, 0, n) }
                val actual = md.digest().joinToString("") { "%02x".format(it) }
                expected.length == 64 && expected == actual
            }.getOrDefault(false)
            if (ok) { _state.value = State.Ready(file); install(ctx, file) }
            else { file.delete(); _state.value = State.Failed("Checksum did not match the published release — the download was discarded") }
        }.start()
    }

    fun install(ctx: Context, file: File) {
        if (!enabled) return
        val problem = archiveProblem(ctx, file)
        if (problem != null) { _state.value = State.Failed(problem); return }
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.files", file)
        val i = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { ctx.startActivity(i) }.onFailure { _state.value = State.Failed("Couldn't open the installer") }
    }

    /** Refuse mismatched signatures/downgrades before opening Android's otherwise vague installer. */
    @Suppress("DEPRECATION")
    private fun archiveProblem(ctx: Context, file: File): String? = runCatching {
        val flags = if (Build.VERSION.SDK_INT >= 28) android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES else android.content.pm.PackageManager.GET_SIGNATURES
        val pm = ctx.packageManager
        val installed = pm.getPackageInfo(ctx.packageName, flags)
        val archive = pm.getPackageArchiveInfo(file.absolutePath, flags) ?: return@runCatching "Cannot read the downloaded APK; not installing"
        fun signers(p: android.content.pm.PackageInfo): Set<String> {
            val values = if (Build.VERSION.SDK_INT >= 28) p.signingInfo?.apkContentsSigners else p.signatures
            return values.orEmpty().map { sig ->
                java.security.MessageDigest.getInstance("SHA-256").digest(sig.toByteArray()).joinToString("") { "%02x".format(it) }
            }.toSet()
        }
        val code = if (Build.VERSION.SDK_INT >= 28) archive.longVersionCode else archive.versionCode.toLong()
        UpdatePolicy.archiveProblem(ctx.packageName, archive.packageName, BuildConfig.VERSION_CODE.toLong(), code, signers(installed), signers(archive))
    }.getOrElse { "Cannot verify APK compatibility; not installing" }

    fun reset() { if (_state.value !is State.Downloading) _state.value = State.Idle }
}
