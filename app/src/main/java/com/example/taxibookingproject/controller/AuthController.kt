package com.example.taxibookingproject.controller

import com.example.taxibookingproject.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AuthController {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://taxibookingapp-58cd8-default-rtdb.asia-southeast1.firebasedatabase.app")

    fun getCurrentUserUid(): String? = auth.currentUser?.uid

    fun getUserData(uid: String, onSuccess: (User) -> Unit, onFailure: (String) -> Unit) {
        database.reference.child("Users").child(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    onSuccess(user)
                } else {
                    onFailure("Không tìm thấy dữ liệu người dùng.")
                }
            }
            .addOnFailureListener {
                onFailure(it.message ?: "Lỗi tải dữ liệu")
            }
    }

    fun listenToUserData(uid: String, onDataChange: (User) -> Unit) {
        database.reference.child("Users").child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) onDataChange(user)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun updateProfileImage(uid: String, imageUrl: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        database.reference.child("Users").child(uid).child("profileImageUrl").setValue(imageUrl)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Lỗi cập nhật ảnh") }
    }

    fun updateUserDetails(uid: String, fullName: String, phone: String, carModel: String, plateNumber: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val updates = mapOf(
            "fullName" to fullName,
            "phone" to phone,
            "carModel" to carModel,
            "plateNumber" to plateNumber
        )
        database.reference.child("Users").child(uid).updateChildren(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Lỗi cập nhật thông tin") }
    }

    fun loginUser(email: String, pass: String, onSuccess: (Int) -> Unit, onFailure: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid = auth.currentUser?.uid ?: ""
                database.reference.child("Users").child(uid).child("role")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            val role = snapshot.getValue(Int::class.java) ?: 3
                            onSuccess(role)
                        } else {
                            auth.signOut()
                            onFailure("Không tìm thấy thông tin vai trò người dùng.")
                        }
                    }
                    .addOnFailureListener {
                        onFailure("Lỗi kết nối dữ liệu: ${it.message}")
                    }
            } else {
                onFailure(task.exception?.message ?: "Sai email hoặc mật khẩu")
            }
        }
    }

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
                    .addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            auth.signOut()
                            onSuccess()
                        } else {
                            onFailure("Lỗi lưu thông tin: ${dbTask.exception?.message}")
                        }
                    }
            } else {
                onFailure(task.exception?.message ?: "Lỗi đăng ký tài khoản")
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun resetPassword(email: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (email.isEmpty()) return onFailure("Vui lòng nhập email")
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) onSuccess() else onFailure("Email không tồn tại hoặc lỗi mạng")
        }
    }
}
