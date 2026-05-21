package com.example.taxibookingproject.controller

import com.google.firebase.database.FirebaseDatabase

class LocationManager {
    // Cập nhật URL chính xác theo yêu cầu của Firebase Region (asia-southeast1)
    private val database = FirebaseDatabase.getInstance("https://taxibookingapp-58cd8-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child("DriverLocations")

    // 1. TÀI XẾ: Cập nhật vị trí liên tục lên bản đồ
    fun updateDriverLocation(driverId: String, lat: Double, lng: Double) {
        val locationData = mapOf(
            "lat" to lat,
            "lng" to lng,
            "timestamp" to System.currentTimeMillis()
        )

        database.child(driverId).setValue(locationData)
    }

    // 2. TÀI XẾ: Tắt app hoặc nghỉ chạy (Xóa vị trí khỏi bản đồ)
    fun goOffline(driverId: String) {
        database.child(driverId).removeValue()
    }
}