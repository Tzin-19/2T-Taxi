package com.example.taxibookingproject.model

import com.google.firebase.database.PropertyName

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: Int = 3, // 1: Admin, 2: Driver, 3: Passenger
    val carModel: String? = null,
    val plateNumber: String? = null,
    val rating: Double = 5.0,
    val profileImageUrl: String? = null,
    
    @get:PropertyName("isLocked")
    @set:PropertyName("isLocked")
    var isLocked: Boolean = false
)
