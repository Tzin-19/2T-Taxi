package com.example.taxibookingproject.model

data class Trip(
    val tripId: String = "",
    val passengerId: String = "",
    val driverId: String? = null,
    val pickupLocation: String = "",
    val destinationLocation: String = "",
    val pickupLat: Double = 0.0,
    val pickupLng: Double = 0.0,
    val destLat: Double = 0.0,
    val destLng: Double = 0.0,
    val distance: String = "",
    val price: Double = 0.0,
    val status: String = "PENDING",
    val timestamp: Long = System.currentTimeMillis()
)