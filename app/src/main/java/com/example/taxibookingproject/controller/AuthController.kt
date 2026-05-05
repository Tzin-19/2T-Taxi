package com.example.taxibookingproject.controller

import com.example.taxibookingproject.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AuthController {
    // Gọi các công cụ của Firebase
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    // 1. HÀM ĐĂNG KÝ TÀI KHOẢN
    fun registerUser(
        email: String,
        pass: String,
        fullName: String,
        phone: String,
        role: Int, // 1: Admin, 2: Driver, 3: Passenger
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Bước 1: Tạo tài khoản trên Firebase Authentication
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        // Bước 2: Gom dữ liệu vào Model User đã tạo hôm qua
                        val newUser = User(
                            uid = firebaseUser.uid,
                            fullName = fullName,
                            email = email,
                            phone = phone,
                            role = role
                        )

                        // Bước 3: Lưu chi tiết User vào Realtime Database nhánh "Users"
                        database.reference.child("Users").child(firebaseUser.uid)
                            .setValue(newUser)
                            .addOnSuccessListener {
                                onSuccess() // Báo về là thành công toàn tập
                            }
                            .addOnFailureListener { e ->
                                onFailure("Lỗi lưu dữ liệu: ${e.message}")
                            }
                    }
                } else {
                    onFailure(task.exception?.message ?: "Lỗi đăng ký tài khoản")
                }
            }
    }

    // 2. HÀM ĐĂNG NHẬP
    fun loginUser(
        email: String,
        pass: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess() // Đăng nhập đúng
                } else {
                    onFailure(task.exception?.message ?: "Sai email hoặc mật khẩu")
                }
            }
    }

    // 3. HÀM KIỂM TRA ĐÃ ĐĂNG NHẬP CHƯA (Giữ phiên đăng nhập)
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // 4. HÀM ĐĂNG XUẤT
    fun logout() {
        auth.signOut()
    }
}