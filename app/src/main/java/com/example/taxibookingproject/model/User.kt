package com.example.taxibookingproject.model

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: Int = 3, // 1: Admin, 2: Driver, 3: Passenger
    val carModel: String? = null,
    val plateNumber: String? = null,
    val rating: Double = 5.0,
    val isLocked: Boolean = false // Dùng cho AdminController
)