package com.example.taxibookingproject.controller

import com.example.taxibookingproject.model.Trip
import com.google.firebase.database.FirebaseDatabase

class BookingController {
    // Cập nhật URL Database chính xác để tránh bị ngắt kết nối do sai Region
    private val database = FirebaseDatabase.getInstance("https://taxibookingapp-58cd8-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    // Khách hàng đặt chuyến xe
    fun createTrip(trip: Trip, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val tripId = database.child("Trips").push().key ?: return onFailure("Không thể tạo ID chuyến đi")
        val newTrip = trip.copy(tripId = tripId)
        
        database.child("Trips").child(tripId).setValue(newTrip)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess() 
                else onFailure(task.exception?.message ?: "Lỗi đặt xe")
            }
    }

    // Tài xế chấp nhận chuyến xe
    fun acceptTrip(tripId: String, driverId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val updates = mapOf(
            "driverId" to driverId,
            "status" to "ACCEPTED"
        )
        database.child("Trips").child(tripId).updateChildren(updates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess()
                else onFailure(task.exception?.message ?: "Lỗi chấp nhận chuyến")
            }
    }

    // Cập nhật trạng thái chuyến đi
    fun updateTripStatus(tripId: String, status: String, onSuccess: () -> Unit) {
        database.child("Trips").child(tripId).child("status").setValue(status)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess()
            }
    }
}