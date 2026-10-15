package com.junkitout.app.data

data class Place(val area: String, val city: String, val pincode: String, val lat: Double, val lng: Double)

data class Address(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val house: String,
    val street: String,
    val landmark: String = "",
    val city: String = "Bengaluru",
    val state: String = "Karnataka",
    val type: String = "Home",
    val pincode: String = "",
    val isDefault: Boolean = false,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
)

data class Booking(
    val id: String, val service: String, val date: String, val slot: String,
    val status: String, val amount: Double, val paid: Boolean,
)

data class PaymentOrder(val orderId: String, val amountPaise: Long)

/** Result wrapper so screens can show real errors instead of crashing. */
sealed class Result<out T> {
    data class Ok<T>(val value: T) : Result<T>()
    data class Err(val message: String) : Result<Nothing>()
}
