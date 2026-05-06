package com.example.taxibookingproject.controller

import com.example.taxibookingproject.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AuthController {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    // 1. HÀM ĐĂNG KÝ TÀI KHOẢN (Đã cập nhật Model mới)
    fun registerUser(
        email: String,
        pass: String,
        fullName: String,
        phone: String,
        role: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        // Khởi tạo User với các trường bổ sung cho Tài xế và Admin
                        val newUser = User(
                            uid = firebaseUser.uid,
                            fullName = fullName,
                            email = email,
                            phone = phone,
                            role = role,
                            // Nếu là tài xế (role=2) thì để trống thông tin xe để cập nhật sau
                            carModel = if (role == 2) "" else null,
                            plateNumber = if (role == 2) "" else null,
                            isLocked = false // Mặc định tài khoản không bị khóa
                        )

                        database.reference.child("Users").child(firebaseUser.uid)
                            .setValue(newUser)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { e -> onFailure("Lỗi lưu dữ liệu: ${e.message}") }
                    }
                } else {
                    onFailure(task.exception?.message ?: "Lỗi đăng ký tài khoản")
                }
            }
    }

    // 2. HÀM ĐĂNG NHẬP (Giữ nguyên)
    fun loginUser(email: String, pass: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess()
                else onFailure(task.exception?.message ?: "Sai email hoặc mật khẩu")
            }
    }

    // 3. KIỂM TRA TRẠNG THÁI (Giữ nguyên)
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    // 4. ĐĂNG XUẤT (Giữ nguyên)
    fun logout() = auth.signOut()

    // 5. QUÊN MẬT KHẨU (Gửi email reset)
    fun resetPassword(email: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (email.isEmpty()) {
            onFailure("Vui lòng nhập email")
            return
        }
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess()
                else onFailure(task.exception?.message ?: "Lỗi gửi email")
            }
    }
}