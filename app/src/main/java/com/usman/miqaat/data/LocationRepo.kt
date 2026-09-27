package com.usman.miqaat.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.coroutines.resume

data class Place(val name: String, val lat: Double, val lng: Double, val zone: String? = null)

object LocationRepo {

    /** Offline fallback list so the app works with no network and no GPS. */
    val presets = listOf(
        Place("Gledswood Hills, NSW", -34.02, 150.77, "Australia/Sydney"),
        Place("Sydney, NSW", -33.87, 151.21, "Australia/Sydney"),
        Place("Lakemba, NSW", -33.92, 151.08, "Australia/Sydney"),
        Place("Parramatta, NSW", -33.82, 151.00, "Australia/Sydney"),
        Place("Melbourne, VIC", -37.81, 144.96, "Australia/Melbourne"),
        Place("Brisbane, QLD", -27.47, 153.03, "Australia/Brisbane"),
        Place("Perth, WA", -31.95, 115.86, "Australia/Perth"),
        Place("Adelaide, SA", -34.93, 138.60, "Australia/Adelaide"),
        Place("Canberra, ACT", -35.28, 149.13, "Australia/Sydney"),
        Place("Auckland, NZ", -36.85, 174.76, "Pacific/Auckland"),
        Place("Wellington, NZ", -41.29, 174.78, "Pacific/Auckland"),
        Place("Christchurch, NZ", -43.53, 172.64, "Pacific/Auckland"),
        Place("Lahore, Pakistan", 31.55, 74.34, "Asia/Karachi"),
        Place("Karachi, Pakistan", 24.86, 67.01, "Asia/Karachi"),
        Place("Islamabad, Pakistan", 33.69, 73.04, "Asia/Karachi"),
        Place("Makkah, Saudi Arabia", 21.39, 39.86, "Asia/Riyadh"),
        Place("Madinah, Saudi Arabia", 24.47, 39.61, "Asia/Riyadh"),
        Place("Dubai, UAE", 25.20, 55.27, "Asia/Dubai"),
        Place("London, UK", 51.51, -0.13, "Europe/London"),
        Place("Kuala Lumpur, Malaysia", 3.14, 101.69, "Asia/Kuala_Lumpur")
    )

    fun hasPermission(ctx: Context) =
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @Suppress("MissingPermission")
    suspend fun current(ctx: Context): Location? {
        if (!hasPermission(ctx)) return null
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        // Best cached fix first: instant, and tablets rarely move.
        val cached = providers.mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }.maxByOrNull { it.time }
        if (cached != null && System.currentTimeMillis() - cached.time < 6 * 3600_000L) return cached
        // Otherwise ask for one fresh fix, with a timeout.
        val provider = providers.firstOrNull { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) } ?: return cached
        val fresh = withTimeoutOrNull(20_000) {
            suspendCancellableCoroutine<Location?> { cont ->
                val exec = Executors.newSingleThreadExecutor()
                runCatching {
                    lm.getCurrentLocation(provider, null, exec) { loc -> if (cont.isActive) cont.resume(loc) }
                }.onFailure { if (cont.isActive) cont.resume(null) }
            }
        }
        return fresh ?: cached
    }

    suspend fun name(ctx: Context, lat: Double, lng: Double): String? = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext null
        runCatching {
            val g = Geocoder(ctx, Locale.ENGLISH)
            val list = if (Build.VERSION.SDK_INT >= 33) {
                withTimeoutOrNull(8_000) {
                    suspendCancellableCoroutine { cont ->
                        g.getFromLocation(lat, lng, 1, object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<android.location.Address>) { if (cont.isActive) cont.resume(addresses) }
                            override fun onError(errorMessage: String?) { if (cont.isActive) cont.resume(null) }
                        })
                    }
                }
            } else @Suppress("DEPRECATION") g.getFromLocation(lat, lng, 1)
            val a = list?.firstOrNull() ?: return@runCatching null
            val locality = a.subLocality ?: a.locality ?: a.subAdminArea ?: a.adminArea
            val region = a.adminArea?.let { abbreviate(it) } ?: a.countryCode
            listOfNotNull(locality, region).distinct().joinToString(", ")
        }.getOrNull()
    }

    suspend fun search(ctx: Context, query: String): List<Place> = withContext(Dispatchers.IO) {
        val local = presets.filter { it.name.contains(query, ignoreCase = true) }
        if (!Geocoder.isPresent()) return@withContext local
        val remote = runCatching {
            @Suppress("DEPRECATION")
            Geocoder(ctx, Locale.ENGLISH).getFromLocationName(query, 6)?.map { a ->
                val locality = a.subLocality ?: a.locality ?: a.subAdminArea ?: a.featureName ?: query
                val region = a.adminArea?.let { abbreviate(it) } ?: a.countryName
                Place(listOfNotNull(locality, region).distinct().joinToString(", "), a.latitude, a.longitude)
            }
        }.getOrNull().orEmpty()
        (local + remote).distinctBy { it.name }
    }

    private fun abbreviate(state: String) = when (state) {
        "New South Wales" -> "NSW"; "Victoria" -> "VIC"; "Queensland" -> "QLD"; "Western Australia" -> "WA"
        "South Australia" -> "SA"; "Tasmania" -> "TAS"; "Australian Capital Territory" -> "ACT"; "Northern Territory" -> "NT"
        else -> state
    }
}
