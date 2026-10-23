package com.junkitout.app.data

import android.content.Context

/** Persists the login token and profile so the user stays signed in across launches. */
class Session(ctx: Context) {
    private val sp = ctx.applicationContext.getSharedPreferences("junkitout_session", Context.MODE_PRIVATE)

    var token: String
        get() = runCatching { sp.getString("token", "") }.getOrNull() ?: ""
        set(v) = runCatching { sp.edit().putString("token", v).apply() }.let { }

    var phone: String
        get() = runCatching { sp.getString("phone", "") }.getOrNull() ?: ""
        set(v) = runCatching { sp.edit().putString("phone", v).apply() }.let { }

    var name: String
        get() = runCatching { sp.getString("name", "") }.getOrNull() ?: ""
        set(v) = runCatching { sp.edit().putString("name", v).apply() }.let { }

    var area: String
        get() = runCatching { sp.getString("area", "") }.getOrNull() ?: ""
        set(v) = runCatching { sp.edit().putString("area", v).apply() }.let { }

    var pincode: String
        get() = runCatching { sp.getString("pincode", "") }.getOrNull() ?: ""
        set(v) = runCatching { sp.edit().putString("pincode", v).apply() }.let { }

    var lat: Double
        get() = runCatching { java.lang.Double.longBitsToDouble(sp.getLong("lat", 0L)) }.getOrDefault(0.0)
        set(v) = runCatching { sp.edit().putLong("lat", java.lang.Double.doubleToRawLongBits(v)).apply() }.let { }

    var lng: Double
        get() = runCatching { java.lang.Double.longBitsToDouble(sp.getLong("lng", 0L)) }.getOrDefault(0.0)
        set(v) = runCatching { sp.edit().putLong("lng", java.lang.Double.doubleToRawLongBits(v)).apply() }.let { }

    val loggedIn get() = token.isNotEmpty()
    fun clear() = runCatching { sp.edit().clear().apply() }.let { }
}
