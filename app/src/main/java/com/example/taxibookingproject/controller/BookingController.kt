package com.example.taxibookingproject.controller

import com.example.taxibookingproject.model.Trip
import com.google.firebase.database.FirebaseDatabase

class BookingController {
    // Trỏ tới nhánh "Trips" trong Database
    private val database = FirebaseDatabase.getInstance().reference.child("Trips")

    // 1. KHÁCH HÀNG: Tạo chuyến xe mới
    fun bookTrip(trip: Trip, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        // Tạo một ID ngẫu nhiên và duy nhất cho chuyến xe
        val tripId = database.push().key ?: return
        val newTrip = trip.copy(tripId = tripId) // Gán ID mới vào dữ liệu chuyến xe

        database.child(tripId).setValue(newTrip)
            .addOnSuccessListener {
                onSuccess(tripId) // Trả về ID chuyến xe để Khách theo dõi
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Lỗi khi đặt xe")
            }
    }

    // 2. TÀI XẾ: Bấm nút "Nhận cuốc"
    fun acceptTrip(tripId: String, driverId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        // Cập nhật 2 thông tin cùng lúc: ID tài xế và trạng thái chuyến đi
        val updates = mapOf(
            "driverId" to driverId,
            "status" to "ACCEPTED"
        )

        database.child(tripId).updateChildren(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Lỗi khi nhận cuốc") }
    }

    // 3. CHUNG: Cập nhật trạng thái chuyến (ON_GOING, COMPLETED, CANCELLED)
    fun updateTripStatus(tripId: String, status: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        database.child(tripId).child("status").setValue(status)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Lỗi cập nhật trạng thái") }
    }
}