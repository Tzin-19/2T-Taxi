package com.example.taxibookingproject.model

data class DriverInfo(
    val uid: String = "",
    val carModel: String = "",
    val plateNumber: String = "",
    val isOnline: Boolean = false,
    val currentLat: Double = 0.0,
    val currentLng: Double = 0.0,
    val rating: Float = 5.0f
)