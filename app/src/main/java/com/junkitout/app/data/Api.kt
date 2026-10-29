package com.junkitout.app.data

import com.app.junkitout.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Single place that knows the backend contract (see /backend/server.js).
 * Base URL comes from API_BASE_URL in local.properties.
 */
object Api {
    private suspend fun call(
        method: String, path: String, token: String = "", body: JSONObject? = null,
    ): Result<JSONObject> = withContext(Dispatchers.IO) {
        if (BuildConfig.API_BASE_URL.isBlank())
            return@withContext Result.Err("API_BASE_URL is not set")

        var lastErr: Exception? = null
        for (attempt in 1..3) {
            try {
                val conn = (URL(BuildConfig.API_BASE_URL.trimEnd('/') + path).openConnection() as HttpURLConnection).apply {
                    requestMethod = method
                    connectTimeout = 60000
                    readTimeout = 60000
                    setRequestProperty("Accept", "application/json")
                    if (token.isNotEmpty()) setRequestProperty("Authorization", "Bearer $token")
                    if (body != null) {
                        doOutput = true
                        setRequestProperty("Content-Type", "application/json")
                        outputStream.use { it.write(body.toString().toByteArray()) }
                    }
                }
                val code = conn.responseCode
                val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                    ?.bufferedReader()?.use { it.readText() }.orEmpty()
                val json = runCatching { JSONObject(text) }.getOrElse { JSONObject().put("raw", text) }

                return@withContext if (code in 200..299) {
                    Result.Ok(json)
                } else {
                    Result.Err(json.optString("error", json.optString("message", "Request failed ($code)")))
                }
            } catch (e: Exception) {
                lastErr = e
                if (attempt < 3) kotlinx.coroutines.delay(2000L)
            }
        }
        Result.Err("Server is waking up. Please try tapping Continue once more.")
    }

    // ── Auth ──
    suspend fun ping() {
        runCatching { call("GET", "/health") }
    }

    suspend fun sendOtp(phone: String): Result<Unit> =
        when (val r = call("POST", "/api/auth/send-otp", body = JSONObject().put("phone", phone))) {
            is Result.Ok -> Result.Ok(Unit)
            is Result.Err -> r
        }

    suspend fun verifyOtp(phone: String, otp: String): Result<UserLoginData> =
        when (val r = call("POST", "/api/auth/verify-otp", body = JSONObject().put("phone", phone).put("otp", otp))) {
            is Result.Ok -> {
                val token = r.value.optString("token")
                val u = r.value.optJSONObject("user")
                if (token.isEmpty()) Result.Err("Login failed: no token returned")
                else Result.Ok(
                    UserLoginData(
                        token = token,
                        name = u?.optString("name").orEmpty(),
                        area = u?.optString("area").orEmpty(),
                        pincode = u?.optString("pincode").orEmpty(),
                        lat = u?.optDouble("lat", 0.0) ?: 0.0,
                        lng = u?.optDouble("lng", 0.0) ?: 0.0
                    )
                )
            }
            is Result.Err -> r
        }

    suspend fun updateName(name: String, token: String): Result<String> =
        when (val r = call("PUT", "/api/me", token, JSONObject().put("name", name))) {
            is Result.Ok -> Result.Ok(r.value.optJSONObject("user")?.optString("name") ?: name)
            is Result.Err -> r
        }

    // ── Serviceability ──
    suspend fun isServiceable(p: Place, token: String): Result<Boolean> {
        val q = "?pincode=${enc(p.pincode)}&area=${enc(p.area)}&lat=${p.lat}&lng=${p.lng}"
        return when (val r = call("GET", "/api/serviceability$q", token)) {
            is Result.Ok -> Result.Ok(r.value.optBoolean("serviceable", false))
            is Result.Err -> r
        }
    }

    // ── Addresses ──
    private fun JSONObject.toAddress() = Address(
        id = optString("id"),
        name = optString("name"),
        phone = optString("phone"),
        house = optString("house"),
        street = optString("street"),
        landmark = optString("landmark"),
        city = optString("city", "Bengaluru"),
        state = optString("state", "Karnataka"),
        type = optString("type", "Home"),
        pincode = optString("pincode"),
        isDefault = optBoolean("isDefault"),
        lat = optDouble("lat", 0.0),
        lng = optDouble("lng", 0.0)
    )

    private fun Address.toJson() = JSONObject()
        .put("name", name)
        .put("phone", phone)
        .put("house", house)
        .put("street", street)
        .put("landmark", landmark)
        .put("city", city)
        .put("state", state)
        .put("type", type)
        .put("pincode", pincode)
        .put("isDefault", isDefault)
        .put("lat", lat)
        .put("lng", lng)

    suspend fun saveAddress(a: Address, token: String): Result<Address> {
        return when (val r = call("POST", "/api/addresses", token, a.toJson())) {
            is Result.Ok -> Result.Ok((r.value.optJSONObject("address") ?: r.value).toAddress())
            is Result.Err -> r
        }
    }

    suspend fun updateAddress(a: Address, token: String): Result<Address> {
        return when (val r = call("PUT", "/api/addresses/${a.id}", token, a.toJson())) {
            is Result.Ok -> Result.Ok((r.value.optJSONObject("address") ?: r.value).toAddress())
            is Result.Err -> r
        }
    }

    suspend fun deleteAddress(addressId: String, token: String): Result<Unit> {
        return when (val r = call("DELETE", "/api/addresses/$addressId", token)) {
            is Result.Ok -> Result.Ok(Unit)
            is Result.Err -> r
        }
    }

    suspend fun setDefaultAddress(addressId: String, token: String): Result<Address> {
        return when (val r = call("PUT", "/api/addresses/$addressId/default", token)) {
            is Result.Ok -> Result.Ok((r.value.optJSONObject("address") ?: r.value).toAddress())
            is Result.Err -> r
        }
    }

    suspend fun addresses(token: String): Result<List<Address>> =
        when (val r = call("GET", "/api/addresses", token)) {
            is Result.Ok -> Result.Ok(r.value.optJSONArray("addresses").toList { it.toAddress() })
            is Result.Err -> r
        }

    // ── Bookings & payments ──
    suspend fun createBooking(
        service: String, date: String, slot: String, kg: Int, token: String,
        addressId: String = "", addressText: String = "",
        couponCode: String = "", discountAmount: Double = 0.0
    ): Result<Booking> {
        val body = JSONObject()
            .put("service", service)
            .put("date", date)
            .put("slot", slot)
            .put("estimatedKg", kg)
            .put("addressId", addressId)
            .put("addressText", addressText)
            .put("couponCode", couponCode)
            .put("discountAmount", discountAmount)
        return when (val r = call("POST", "/api/bookings", token, body)) {
            is Result.Ok -> Result.Ok((r.value.optJSONObject("booking") ?: r.value).toBooking())
            is Result.Err -> r
        }
    }

    // ── Offers & Coupons ──
    suspend fun getOffers(token: String): Result<List<Offer>> =
        when (val r = call("GET", "/api/offers", token)) {
            is Result.Ok -> {
                val arr = r.value.optJSONArray("offers")
                Result.Ok((0 until (arr?.length() ?: 0)).map { i ->
                    val o = arr!!.getJSONObject(i)
                    Offer(
                        code = o.optString("code"),
                        title = o.optString("title"),
                        description = o.optString("description"),
                        discountType = o.optString("discountType", "percentage"),
                        discountValue = o.optDouble("discountValue", 0.0),
                        minOrderValue = o.optDouble("minOrderValue", 0.0),
                        validTill = o.optString("validTill", "31 Dec 2026"),
                        tag = o.optString("tag")
                    )
                })
            }
            is Result.Err -> r
        }

    suspend fun validateCoupon(code: String, amount: Double, token: String): Result<Pair<Double, Double>> =
        when (val r = call("POST", "/api/coupons/validate", token, JSONObject().put("code", code).put("amount", amount))) {
            is Result.Ok -> Result.Ok(Pair(r.value.optDouble("discountAmount"), r.value.optDouble("finalAmount")))
            is Result.Err -> r
        }

    suspend fun bookings(token: String): Result<List<Booking>> =
        when (val r = call("GET", "/api/bookings", token)) {
            is Result.Ok -> Result.Ok(r.value.optJSONArray("bookings").toList { it.toBooking() })
            is Result.Err -> r
        }

    suspend fun cancelBooking(bookingId: String, token: String): Result<Booking> =
        when (val r = call("PUT", "/api/bookings/$bookingId/cancel", token)) {
            is Result.Ok -> Result.Ok((r.value.optJSONObject("booking") ?: r.value).toBooking())
            is Result.Err -> r
        }

    suspend fun updateBookingStatus(bookingId: String, status: String, token: String): Result<Booking> =
        when (val r = call("PUT", "/api/bookings/$bookingId/status", token, JSONObject().put("status", status))) {
            is Result.Ok -> Result.Ok((r.value.optJSONObject("booking") ?: r.value).toBooking())
            is Result.Err -> r
        }

    /** Backend creates the Razorpay order with the SECRET key; the app only receives the order id. */
    suspend fun createPaymentOrder(bookingId: String, token: String): Result<PaymentOrder> =
        when (val r = call("POST", "/api/payments/create-order", token, JSONObject().put("bookingId", bookingId))) {
            is Result.Ok -> {
                val id = r.value.optString("orderId")
                if (id.isEmpty()) Result.Err("Server returned no payment order")
                else Result.Ok(PaymentOrder(id, r.value.optLong("amount")))
            }
            is Result.Err -> r
        }

    suspend fun verifyPayment(bookingId: String, orderId: String, paymentId: String, signature: String, token: String): Result<Unit> {
        val b = JSONObject().put("bookingId", bookingId).put("razorpay_order_id", orderId)
            .put("razorpay_payment_id", paymentId).put("razorpay_signature", signature)
        return when (val r = call("POST", "/api/payments/verify", token, b)) {
            is Result.Ok -> Result.Ok(Unit)
            is Result.Err -> r
        }
    }

    // ── Wallet ──
    suspend fun getWallet(token: String): Result<WalletData> =
        when (val r = call("GET", "/api/wallet", token)) {
            is Result.Ok -> {
                val w = r.value.optJSONObject("wallet") ?: r.value
                Result.Ok(
                    WalletData(
                        balance = w.optDouble("balance", 250.0),
                        totalSpent = w.optDouble("totalSpent", 1250.0),
                        totalRefunded = w.optDouble("totalRefunded", 100.0),
                        ordersCount = w.optInt("ordersCount", 8),
                        refundsCount = w.optInt("refundsCount", 2)
                    )
                )
            }
            is Result.Err -> r
        }

    suspend fun getWalletTransactions(token: String): Result<List<WalletTransaction>> =
        when (val r = call("GET", "/api/wallet/transactions", token)) {
            is Result.Ok -> {
                val arr = r.value.optJSONArray("transactions")
                Result.Ok((0 until (arr?.length() ?: 0)).map { i ->
                    val o = arr!!.getJSONObject(i)
                    WalletTransaction(
                        id = o.optString("id"),
                        title = o.optString("title"),
                        subtitle = o.optString("subtitle"),
                        amount = o.optDouble("amount"),
                        type = o.optString("type", "debit"),
                        status = o.optString("status", "Successful"),
                        date = o.optString("date")
                    )
                })
            }
            is Result.Err -> r
        }

    suspend fun createWalletTopupOrder(amount: Double, token: String): Result<PaymentOrder> =
        when (val r = call("POST", "/api/wallet/topup", token, JSONObject().put("amount", amount))) {
            is Result.Ok -> {
                val id = r.value.optString("orderId")
                if (id.isEmpty()) Result.Err("Server returned no payment order")
                else Result.Ok(PaymentOrder(id, r.value.optLong("amount")))
            }
            is Result.Err -> r
        }

    suspend fun verifyWalletTopup(orderId: String, paymentId: String, signature: String, amount: Double, token: String): Result<Unit> {
        val b = JSONObject().put("orderId", orderId).put("paymentId", paymentId)
            .put("signature", signature).put("amount", amount)
        return when (val r = call("POST", "/api/wallet/topup/verify", token, b)) {
            is Result.Ok -> Result.Ok(Unit)
            is Result.Err -> r
        }
    }

    // ── Helpers ──
    /** Display-only preview. The server recomputes the real amount and is the source of truth. */
    fun estimate(service: String, kg: Int): Double {
        val rate = when (service) { "E-Waste Disposal" -> 12.0; "Paper & Cardboard" -> 8.0; "Plastic Waste" -> 6.0; else -> 7.0 }
        return kotlin.math.max(49.0, rate * kg)
    }
    fun enc(s: String): String = URLEncoder.encode(s, "UTF-8")

    private fun JSONObject.toBooking() = Booking(
        optString("id"), optString("service"), optString("date"), optString("slot"),
        optString("status", "Scheduled"), optDouble("amount", 0.0), optBoolean("paid", false),
        optString("addressText"), optString("pickupOtp", "1234"),
        optString("driverId").takeIf { it.isNotBlank() }
    )

    suspend fun getOrderTracking(orderId: String, token: String): Result<DriverTrackingInfo> =
        when (val r = call("GET", "/api/bookings/$orderId/tracking", token)) {
            is Result.Ok -> {
                val d = r.value.optJSONObject("driver")
                Result.Ok(
                    DriverTrackingInfo(
                        orderId = r.value.optString("orderId"),
                        status = r.value.optString("status"),
                        pickupOtp = r.value.optString("pickupOtp", "1234"),
                        paid = r.value.optBoolean("paid", false),
                        driverName = d?.optString("name") ?: "Collector Ramesh",
                        driverPhone = d?.optString("phone") ?: "9591225555",
                        vehicleNumber = d?.optString("vehicleNumber") ?: "KA-01-EA-2026",
                        driverLat = d?.optDouble("lat", 12.9141) ?: 12.9141,
                        driverLng = d?.optDouble("lng", 77.6412) ?: 77.6412
                    )
                )
            }
            is Result.Err -> r
        }
    private fun <T> JSONArray?.toList(f: (JSONObject) -> T): List<T> =
        if (this == null) emptyList() else (0 until length()).map { f(getJSONObject(it)) }
}
