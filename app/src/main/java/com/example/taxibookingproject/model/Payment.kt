package com.example.taxibookingproject.model

data class Payment(
    val id: String = "",
    val tripId: String = "",
    val passengerId: String = "",
    val driverId: String = "",
    val amount: Double = 0.0,
    val method: String = "CASH", // CASH, WALLET, CARD
    val status: String = "PENDING",
    val timestamp: Long = System.currentTimeMillis()
)