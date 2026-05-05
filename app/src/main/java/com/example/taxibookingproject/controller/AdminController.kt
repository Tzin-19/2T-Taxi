package com.example.taxibookingproject.controller

import com.google.firebase.database.FirebaseDatabase

class AdminController {
    private val rootRef = FirebaseDatabase.getInstance().reference

    // 1. Khóa/Mở khóa một người dùng (Khách hoặc Tài xế)
    fun toggleUserStatus(uid: String, isLocked: Boolean, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        rootRef.child("Users").child(uid).child("isLocked").setValue(isLocked)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Lỗi thao tác") }
    }

    // 2. Xóa một chuyến xe bị lỗi trong hệ thống
    fun deleteTrip(tripId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        rootRef.child("Trips").child(tripId).removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Lỗi khi xóa chuyến xe") }
    }
}