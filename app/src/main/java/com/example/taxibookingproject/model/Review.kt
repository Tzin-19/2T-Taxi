package com.example.taxibookingproject.model

data class Review(
    val id: String = "",
    val tripId: String = "",
    val passengerId: String = "",
    val driverId: String = "",
    val rating: Float = 5f,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)