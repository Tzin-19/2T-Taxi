package com.example.taxibookingproject.model

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: Int = 3, // 1: Admin, 2: Driver, 3: Passenger
    val avatarUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)