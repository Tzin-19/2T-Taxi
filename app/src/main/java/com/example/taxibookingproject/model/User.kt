package com.example.taxibookingproject.model

import com.google.firebase.database.PropertyName

data class SavedPlace(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

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
    val totalEarnings: Double = 0.0,
    
    val homePlace: SavedPlace? = null,
    val workPlace: SavedPlace? = null,
    val favoritePlaces: Map<String, SavedPlace> = emptyMap(),
    val searchHistory: Map<String, SavedPlace> = emptyMap(),

    @get:PropertyName("isLocked")
    @set:PropertyName("isLocked")
    var isLocked: Boolean = false
)
