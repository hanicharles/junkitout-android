package com.junkitout.app.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import com.app.junkitout.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Locale
import kotlin.coroutines.resume

/** Device location + geocoding (Google Geocoding API with the restricted Maps key, Android Geocoder as fallback). */
object Geo {
    @SuppressLint("MissingPermission")
    suspend fun lastKnown(ctx: Context): Location? = withContext(Dispatchers.IO) {
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.getProviders(true).mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
    }

    @SuppressLint("MissingPermission")
    suspend fun currentFix(ctx: Context): Location? {
        lastKnown(ctx)?.let { return it }
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val provider = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .firstOrNull { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) } ?: return null
        return suspendCancellableCoroutine { cont ->
            runCatching {
                lm.requestSingleUpdate(provider, { loc -> if (cont.isActive) cont.resume(loc) }, android.os.Looper.getMainLooper())
            }.onFailure { if (cont.isActive) cont.resume(null) }
        }
    }

    suspend fun reverse(ctx: Context, lat: Double, lng: Double): Place? = withContext(Dispatchers.IO) {
        google("latlng=$lat,$lng") ?: androidGeocode(ctx) { it.getFromLocation(lat, lng, 1) }
    }

    suspend fun search(ctx: Context, query: String): Place? = withContext(Dispatchers.IO) {
        google("address=${Api.enc("$query, Bengaluru")}&components=country:IN")
            ?: androidGeocode(ctx) { it.getFromLocationName("$query, Bengaluru", 1) }
    }

    private fun google(params: String): Place? {
        val key = BuildConfig.GOOGLE_MAPS_API_KEY
        if (key.isBlank()) return null
        return runCatching {
            val json = JSONObject(URL("https://maps.googleapis.com/maps/api/geocode/json?$params&key=$key").readText())
            val r = json.optJSONArray("results")?.optJSONObject(0) ?: return null
            val loc = r.getJSONObject("geometry").getJSONObject("location")
            var area = ""; var city = ""; var pin = ""
            val comps = r.getJSONArray("address_components")
            for (i in 0 until comps.length()) {
                val c = comps.getJSONObject(i); val types = c.getJSONArray("types").toString()
                when {
                    "sublocality_level_1" in types || "sublocality" in types -> if (area.isEmpty()) area = c.getString("long_name")
                    "locality" in types -> city = c.getString("long_name")
                    "postal_code" in types -> pin = c.getString("long_name")
                    "neighborhood" in types -> if (area.isEmpty()) area = c.getString("long_name")
                }
            }
            Place(area.ifEmpty { city.ifEmpty { "Selected location" } }, city.ifEmpty { "Bengaluru" }, pin, loc.getDouble("lat"), loc.getDouble("lng"))
        }.getOrNull()
    }

    @Suppress("DEPRECATION")
    private fun androidGeocode(ctx: Context, f: (Geocoder) -> List<android.location.Address>?): Place? = runCatching {
        val a = f(Geocoder(ctx, Locale.getDefault()))?.firstOrNull() ?: return null
        Place(a.subLocality ?: a.locality ?: "Selected location", a.locality ?: "Bengaluru",
            a.postalCode.orEmpty(), a.latitude, a.longitude)
    }.getOrNull()
}
