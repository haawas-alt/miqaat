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
    data class Info(val build: Int, val apkUrl: String, val versionName: String, val sha256Url: String, val commit: String)
    val enabled: Boolean get() = BuildConfig.SELF_UPDATE

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

    val currentBuild: Int get() = BuildConfig.VERSION_CODE - 100
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
                Info(build, url, "1.$build", sha, commit)
            }.getOrNull()
        }
        prefs.edit().putLong("lastCheck", System.currentTimeMillis()).apply()
        _state.value = when {
            info == null -> State.Failed("Couldn't reach GitHub. Is the tablet online?")
            info.apkUrl.isEmpty() -> State.Failed("No APK in the latest release")
            info.sha256Url.isEmpty() -> State.Failed("Release has no checksum; not installing")
            info.build > currentBuild -> State.Available(info)
            else -> State.UpToDate
        }
    }

    fun canInstall(ctx: Context): Boolean = ctx.packageManager.canRequestPackageInstalls()

    fun openInstallPermission(ctx: Context) {
        runCatching { ctx.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    fun download(ctx: Context, info: Info) {
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
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.files", file)
        val i = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { ctx.startActivity(i) }.onFailure { _state.value = State.Failed("Couldn't open the installer") }
    }

    fun reset() { if (_state.value !is State.Downloading) _state.value = State.Idle }
}
