package com.example.taxibookingproject.controller

import com.google.firebase.database.FirebaseDatabase

class LocationManager {
    // Trỏ tới nhánh "DriverLocations" trong Database
    private val database = FirebaseDatabase.getInstance().reference.child("DriverLocations")

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

    // (Sau này phần quét xe xung quanh cho Khách hàng sẽ dùng GeoFire để tính bán kính phức tạp hơn, tạm thời mình cấu trúc thế này trước).
}