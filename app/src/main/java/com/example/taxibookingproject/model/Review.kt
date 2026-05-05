package com.example.taxibookingproject.model

data class Review(
    val reviewId: String = "",
    val tripId: String = "",
    val passengerId: String = "",
    val driverId: String = "",
    val rating: Float = 0f, // Số sao từ 1.0 đến 5.0
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)