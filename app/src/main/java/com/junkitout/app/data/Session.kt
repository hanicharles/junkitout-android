package com.junkitout.app.data

import android.content.Context

/** Persists the login token and profile so the user stays signed in across launches. */
class Session(ctx: Context) {
    private val sp = ctx.applicationContext.getSharedPreferences("junkitout_session", Context.MODE_PRIVATE)

    var token: String
        get() = sp.getString("token", "") ?: ""
        set(v) = sp.edit().putString("token", v).apply()
    var phone: String
        get() = sp.getString("phone", "") ?: ""
        set(v) = sp.edit().putString("phone", v).apply()
    var name: String
        get() = sp.getString("name", "") ?: ""
        set(v) = sp.edit().putString("name", v).apply()
    var area: String
        get() = sp.getString("area", "") ?: ""
        set(v) = sp.edit().putString("area", v).apply()
    var pincode: String
        get() = sp.getString("pincode", "") ?: ""
        set(v) = sp.edit().putString("pincode", v).apply()
    var lat: Double
        get() = java.lang.Double.longBitsToDouble(sp.getLong("lat", 0L))
        set(v) = sp.edit().putLong("lat", java.lang.Double.doubleToRawLongBits(v)).apply()
    var lng: Double
        get() = java.lang.Double.longBitsToDouble(sp.getLong("lng", 0L))
        set(v) = sp.edit().putLong("lng", java.lang.Double.doubleToRawLongBits(v)).apply()

    val loggedIn get() = token.isNotEmpty()
    fun clear() = sp.edit().clear().apply()
}
