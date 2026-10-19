package com.junkitout.driver

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object DriverApi {
    private const val BASE_URL = "https://junkitout-android.onrender.com"

    private suspend fun call(
        method: String,
        path: String,
        token: String = "",
        body: JSONObject? = null
    ): DriverResult<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val url = URL(if (path.startsWith("http")) path else "$BASE_URL$path")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Content-Type", "application/json")
                if (token.isNotBlank()) setRequestProperty("Authorization", "Bearer $token")
                if (body != null) {
                    doOutput = true
                    outputStream.use { it.write(body.toString().toByteArray()) }
                }
            }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
            val json = if (text.startsWith("{")) JSONObject(text) else JSONObject()

            if (code in 200..299) {
                DriverResult.Ok(json)
            } else {
                val msg = json.optString("error").takeIf { it.isNotBlank() }
                    ?: json.optString("message").takeIf { it.isNotBlank() }
                    ?: "Driver server error ($code)"
                DriverResult.Err(msg)
            }
        } catch (e: Exception) {
            DriverResult.Err("Network connection failed. Check your internet.")
        }
    }

    suspend fun login(phone: String, name: String, vehicleNumber: String): DriverResult<Pair<String, DriverInfo>> =
        when (val r = call("POST", "/api/driver/auth/login", body = JSONObject().put("phone", phone).put("name", name).put("vehicleNumber", vehicleNumber))) {
            is DriverResult.Ok -> {
                val t = r.value.optString("token")
                val d = r.value.optJSONObject("driver") ?: JSONObject()
                DriverResult.Ok(
                    Pair(
                        t,
                        DriverInfo(
                            id = d.optString("id"),
                            phone = d.optString("phone"),
                            name = d.optString("name", "Collector Ramesh"),
                            vehicleNumber = d.optString("vehicle_number", "KA-01-EA-2026"),
                            status = d.optString("status", "online")
                        )
                    )
                )
            }
            is DriverResult.Err -> r
        }

    suspend fun getPickups(token: String): DriverResult<List<PickupOrder>> =
        when (val r = call("GET", "/api/driver/pickups", token)) {
            is DriverResult.Ok -> {
                val arr = r.value.optJSONArray("pickups") ?: JSONArray()
                val list = (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    PickupOrder(
                        id = o.optString("id"),
                        service = o.optString("service"),
                        date = o.optString("date"),
                        slot = o.optString("slot"),
                        estimatedKg = o.optInt("estimatedKg", 10),
                        amount = o.optDouble("amount", 0.0),
                        status = o.optString("status", "Scheduled"),
                        paid = o.optBoolean("paid", false),
                        addressText = o.optString("addressText"),
                        customerName = o.optString("customerName", "Customer"),
                        customerPhone = o.optString("customerPhone"),
                        isAssignedToMe = o.optBoolean("isAssignedToMe", false),
                        pickupOtp = o.optString("pickupOtp", "1234")
                    )
                }
                DriverResult.Ok(list)
            }
            is DriverResult.Err -> r
        }

    suspend fun acceptPickup(orderId: String, token: String): DriverResult<Unit> =
        when (val r = call("PUT", "/api/driver/pickups/$orderId/accept", token)) {
            is DriverResult.Ok -> DriverResult.Ok(Unit)
            is DriverResult.Err -> r
        }

    suspend fun updateStatus(orderId: String, status: String, token: String): DriverResult<Unit> =
        when (val r = call("PUT", "/api/driver/pickups/$orderId/status", token, JSONObject().put("status", status))) {
            is DriverResult.Ok -> DriverResult.Ok(Unit)
            is DriverResult.Err -> r
        }

    suspend fun verifyOtp(orderId: String, otp: String, token: String): DriverResult<Unit> =
        when (val r = call("POST", "/api/driver/pickups/$orderId/verify-otp", token, JSONObject().put("otp", otp))) {
            is DriverResult.Ok -> DriverResult.Ok(Unit)
            is DriverResult.Err -> r
        }

    suspend fun completePickup(orderId: String, actualKg: Int, token: String): DriverResult<Unit> =
        when (val r = call("POST", "/api/driver/pickups/$orderId/complete", token, JSONObject().put("actualKg", actualKg))) {
            is DriverResult.Ok -> DriverResult.Ok(Unit)
            is DriverResult.Err -> r
        }

    suspend fun sendLocation(lat: Double, lng: Double, token: String): DriverResult<Unit> =
        when (val r = call("POST", "/api/driver/location", token, JSONObject().put("lat", lat).put("lng", lng))) {
            is DriverResult.Ok -> DriverResult.Ok(Unit)
            is DriverResult.Err -> r
        }
}
