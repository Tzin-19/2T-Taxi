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

    fun loginUser(emailOrPhone: String, pass: String, onSuccess: (Int) -> Unit, onFailure: (String) -> Unit) {
        if (emailOrPhone.contains("@")) {
            // Đăng nhập bằng email trực tiếp
            auth.signInWithEmailAndPassword(emailOrPhone, pass).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    fetchUserRoleAndStatus(uid, onSuccess, onFailure)
                } else {
                    onFailure(task.exception?.message ?: "Sai email hoặc mật khẩu")
                }
            }
        } else {
            // Tìm kiếm email tương ứng với số điện thoại trong DB
            database.reference.child("Users").orderByChild("phone").equalTo(emailOrPhone)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userSnapshot = snapshot.children.firstOrNull()
                        val userEmail = userSnapshot?.child("email")?.getValue(String::class.java)
                        if (userEmail != null) {
                            auth.signInWithEmailAndPassword(userEmail, pass).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val uid = auth.currentUser?.uid ?: ""
                                    fetchUserRoleAndStatus(uid, onSuccess, onFailure)
                                } else {
                                    onFailure(task.exception?.message ?: "Sai mật khẩu")
                                }
                            }
                        } else {
                            onFailure("Không tìm thấy tài khoản với số điện thoại này.")
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        onFailure("Lỗi kết nối database: ${error.message}")
                    }
                })
        }
    }

    private fun fetchUserRoleAndStatus(uid: String, onSuccess: (Int) -> Unit, onFailure: (String) -> Unit) {
        database.reference.child("Users").child(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val isLocked = snapshot.child("isLocked").getValue(Boolean::class.java) ?: false
                    if (isLocked) {
                        auth.signOut()
                        onFailure("Tài khoản của bạn đã bị khóa.")
                    } else {
                        val role = snapshot.child("role").getValue(Int::class.java) ?: 3
                        onSuccess(role)
                    }
                } else {
                    auth.signOut()
                    onFailure("Không tìm thấy thông tin vai trò người dùng.")
                }
            }
            .addOnFailureListener {
                onFailure("Lỗi kết nối dữ liệu: ${it.message}")
            }
    }

    fun registerUser(email: String, pass: String, fullName: String, phone: String, role: Int, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        // Kiểm tra xem số điện thoại đã tồn tại trong DB chưa trước khi tạo tài khoản Auth
        database.reference.child("Users").orderByChild("phone").equalTo(phone)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        onFailure("Số điện thoại này đã được đăng ký cho tài khoản khác.")
                    } else {
                        // Bắt đầu đăng ký Firebase Auth
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
                }
                override fun onCancelled(error: DatabaseError) {
                    onFailure("Lỗi kết nối cơ sở dữ liệu: ${error.message}")
                }
            })
    }

    fun loginWithGoogleCredential(idToken: String, onSuccess: (Int) -> Unit, onFailure: (String) -> Unit) {
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val uid = user?.uid ?: ""
                
                database.reference.child("Users").child(uid).get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val isLocked = snapshot.child("isLocked").getValue(Boolean::class.java) ?: false
                        if (isLocked) {
                            auth.signOut()
                            onFailure("Tài khoản đã bị khóa.")
                        } else {
                            val role = snapshot.child("role").getValue(Int::class.java) ?: 3
                            onSuccess(role)
                        }
                    } else {
                        // Tài khoản mới từ Google, tự động tạo khách hàng (role = 3)
                        val newUser = User(
                            uid = uid,
                            fullName = user?.displayName ?: "Google User",
                            email = user?.email ?: "",
                            phone = user?.phoneNumber ?: "",
                            role = 3,
                            isLocked = false
                        )
                        database.reference.child("Users").child(uid).setValue(newUser)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    onSuccess(3)
                                } else {
                                    onFailure("Không thể lưu thông tin Google user")
                                }
                            }
                    }
                }.addOnFailureListener {
                    onFailure("Lỗi kiểm tra thông tin: ${it.message}")
                }
            } else {
                onFailure(task.exception?.message ?: "Đăng nhập Google thất bại")
            }
        }
    }

    fun loginWithFacebookCredential(tokenStr: String, onSuccess: (Int) -> Unit, onFailure: (String) -> Unit) {
        val credential = com.google.firebase.auth.FacebookAuthProvider.getCredential(tokenStr)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val uid = user?.uid ?: ""
                
                database.reference.child("Users").child(uid).get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val isLocked = snapshot.child("isLocked").getValue(Boolean::class.java) ?: false
                        if (isLocked) {
                            auth.signOut()
                            onFailure("Tài khoản đã bị khóa.")
                        } else {
                            val role = snapshot.child("role").getValue(Int::class.java) ?: 3
                            onSuccess(role)
                        }
                    } else {
                        // Tài khoản mới từ Facebook, tự động tạo khách hàng (role = 3)
                        val newUser = User(
                            uid = uid,
                            fullName = user?.displayName ?: "Facebook User",
                            email = user?.email ?: "",
                            phone = user?.phoneNumber ?: "",
                            role = 3,
                            isLocked = false
                        )
                        database.reference.child("Users").child(uid).setValue(newUser)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    onSuccess(3)
                                } else {
                                    onFailure("Không thể lưu thông tin Facebook user")
                                }
                            }
                    }
                }.addOnFailureListener {
                    onFailure("Lỗi kiểm tra thông tin: ${it.message}")
                }
            } else {
                onFailure(task.exception?.message ?: "Đăng nhập Facebook thất bại")
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
