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
    val distanceValue: Double = 0.0,
    val price: Double = 0.0,
    val vehicleTypeName: String = "", // Loại xe đã chọn (4, 8, 12, 16 chỗ)
    val status: String = "PENDING",
    val timestamp: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val rating: Float = 0f,
    val review: String = ""
)