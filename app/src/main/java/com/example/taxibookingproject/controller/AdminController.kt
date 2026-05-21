package com.example.taxibookingproject.controller

import com.google.firebase.database.FirebaseDatabase

class AdminController {
    // Cập nhật URL Database chính xác từ log hệ thống để tránh lỗi Region
    private val rootRef = FirebaseDatabase.getInstance("https://taxibookingapp-58cd8-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    // 1. Khóa/Mở khóa một người dùng (Khách hoặc Tài xế)
    fun toggleUserStatus(uid: String, isLocked: Boolean, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        rootRef.child("Users").child(uid).child("isLocked").setValue(isLocked)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess()
                else onFailure(task.exception?.message ?: "Lỗi thao tác khóa/mở khóa")
            }
    }

    // 2. Xóa một chuyến xe bị lỗi trong hệ thống
    fun deleteTrip(tripId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        rootRef.child("Trips").child(tripId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess()
                else onFailure(task.exception?.message ?: "Lỗi khi xóa chuyến xe")
            }
    }
}