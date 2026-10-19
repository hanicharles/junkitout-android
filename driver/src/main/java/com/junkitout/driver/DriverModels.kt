package com.junkitout.driver

data class DriverInfo(
    val id: String,
    val phone: String,
    val name: String,
    val vehicleNumber: String,
    val status: String,
    val serviceArea: String = "Bengaluru"
)

data class PickupOrder(
    val id: String,
    val service: String,
    val date: String,
    val slot: String,
    val estimatedKg: Int,
    val amount: Double,
    val status: String,
    val paid: Boolean,
    val addressText: String,
    val customerName: String,
    val customerPhone: String,
    val isAssignedToMe: Boolean,
    val pickupOtp: String,
)

sealed class DriverResult<out T> {
    data class Ok<T>(val value: T) : DriverResult<T>()
    data class Err(val message: String) : DriverResult<Nothing>()
}
