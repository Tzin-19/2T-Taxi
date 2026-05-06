package com.example.taxibookingproject.controller

import com.example.taxibookingproject.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AuthController {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    fun registerUser(email: String, pass: String, fullName: String, phone: String, role: Int, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid = auth.currentUser?.uid ?: ""
                val newUser = User(
                    uid = uid, fullName = fullName, email = email, phone = phone, role = role,
                    carModel = if (role == 2) "" else null,
                    plateNumber = if (role == 2) "" else null,
                    isLocked = false
                )
                database.reference.child("Users").child(uid).setValue(newUser)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure("Lỗi lưu database: ${it.message}") }
            } else {
                onFailure(task.exception?.message ?: "Lỗi đăng ký")
            }
        }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (email.isEmpty()) return onFailure("Vui lòng nhập email")
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) onSuccess() else onFailure("Email không tồn tại hoặc lỗi mạng")
        }
    }

    fun loginUser(email: String, pass: String, onSuccess: (Int) -> Unit, onFailure: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid = auth.currentUser?.uid ?: ""
                
                // Sử dụng addListenerForSingleValueEvent thay vì get() để ổn định hơn trên một số thiết bị
                database.reference.child("Users").child(uid).child("role")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            val role = snapshot.getValue(Int::class.java) ?: 3
                            onSuccess(role)
                        } else {
                            // Nếu không tìm thấy role trong DB, ép buộc đăng xuất và báo lỗi
                            auth.signOut()
                            onFailure("Tài khoản không tồn tại trên hệ thống dữ liệu. Vui lòng đăng ký lại.")
                        }
                    }
                    .addOnFailureListener {
                        onFailure("Lỗi kết nối Database: ${it.message}")
                    }
            } else {
                onFailure(task.exception?.message ?: "Sai email hoặc mật khẩu")
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}