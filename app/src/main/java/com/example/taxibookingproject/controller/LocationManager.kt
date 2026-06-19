package com.example.taxibookingproject.controller

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.android.gms.maps.model.LatLng

class LocationManager {
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

    // 2. KHÁCH HÀNG: Theo dõi vị trí 1 tài xế cụ thể
    fun trackDriverLocation(driverId: String, onUpdate: (LatLng) -> Unit) {
        database.child(driverId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lat = snapshot.child("lat").getValue(Double::class.java)
                val lng = snapshot.child("lng").getValue(Double::class.java)
                if (lat != null && lng != null) {
                    onUpdate(LatLng(lat, lng))
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // 3. KHÁCH HÀNG: Lấy tất cả tài xế đang online để hiển thị lên bản đồ trang chủ
    fun getActiveDrivers(onUpdate: (List<LatLng>) -> Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val locations = mutableListOf<LatLng>()
                for (child in snapshot.children) {
                    val lat = child.child("lat").getValue(Double::class.java)
                    val lng = child.child("lng").getValue(Double::class.java)
                    if (lat != null && lng != null) {
                        locations.add(LatLng(lat, lng))
                    }
                }
                onUpdate(locations)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // 4. TÀI XẾ: Tắt app hoặc nghỉ chạy (Xóa vị trí khỏi bản đồ)
    fun goOffline(driverId: String) {
        database.child(driverId).removeValue()
    }
}
