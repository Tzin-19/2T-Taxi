package com.example.taxibookingproject.controller

import com.example.taxibookingproject.model.SavedPlace
import com.example.taxibookingproject.model.Trip
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import com.example.taxibookingproject.model.ChatMessage

class BookingController {
    private val database = FirebaseDatabase.getInstance("https://taxibookingapp-58cd8-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    // Khách hàng đặt chuyến xe - Lưu vào Firebase
    fun createTrip(trip: Trip, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val tripId = database.child("Trips").push().key ?: return onFailure("Không thể tạo ID chuyến đi")
        val newTrip = trip.copy(tripId = tripId)
        
        database.child("Trips").child(tripId).setValue(newTrip)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Lưu vào lịch sử tìm kiếm khi đặt xe thành công
                    saveToHistory(trip.passengerId, SavedPlace(
                        id = System.currentTimeMillis().toString(),
                        name = trip.destinationLocation.split(",")[0],
                        address = trip.destinationLocation,
                        lat = trip.destLat,
                        lng = trip.destLng
                    ))
                    onSuccess(tripId) 
                }
                else onFailure(task.exception?.message ?: "Lỗi đặt xe")
            }
    }

    // Quản lý địa điểm đã lưu
    fun saveToHistory(uid: String, place: SavedPlace) {
        val historyRef = database.child("Users").child(uid).child("searchHistory")
        historyRef.child(place.id).setValue(place)
    }

    fun deleteHistoryItem(uid: String, itemId: String) {
        database.child("Users").child(uid).child("searchHistory").child(itemId).removeValue()
    }

    fun clearSearchHistory(uid: String) {
        database.child("Users").child(uid).child("searchHistory").removeValue()
    }

    fun saveFavorite(uid: String, place: SavedPlace) {
        val favRef = database.child("Users").child(uid).child("favoritePlaces")
        favRef.child(place.id).setValue(place)
    }

    fun deleteFavorite(uid: String, itemId: String) {
        database.child("Users").child(uid).child("favoritePlaces").child(itemId).removeValue()
    }

    fun updateSpecialPlace(uid: String, type: String, place: SavedPlace?) {
        database.child("Users").child(uid).child(type).setValue(place)
    }

    // Lấy lịch sử chuyến đi của hành khách
    fun listenForPassengerHistory(passengerId: String, onUpdate: (List<Trip>) -> Unit) {
        database.child("Trips").orderByChild("passengerId").equalTo(passengerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val history = snapshot.children.mapNotNull { it.getValue(Trip::class.java) }
                        .filter { it.status == "COMPLETED" }
                        .sortedByDescending { it.completedAt ?: it.timestamp }
                    onUpdate(history)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // Lắng nghe một chuyến xe cụ thể theo ID
    fun listenToTripStatus(tripId: String, onUpdate: (Trip) -> Unit) {
        database.child("Trips").child(tripId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val trip = snapshot.getValue(Trip::class.java)
                    if (trip != null) {
                        onUpdate(trip)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // Lắng nghe chuyến xe hiện tại của hành khách
    fun listenForPassengerActiveTrip(passengerId: String, onUpdate: (Trip?) -> Unit) {
        database.child("Trips").orderByChild("passengerId").equalTo(passengerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val activeTrip = snapshot.children.mapNotNull { it.getValue(Trip::class.java) }
                        .find { it.status != "CANCELLED" && (it.status != "COMPLETED" || it.rating == 0f) }
                    onUpdate(activeTrip)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // Gửi đánh giá cho chuyến đi và đồng bộ số sao trung bình của tài xế
    fun submitRating(tripId: String, driverId: String, rating: Float, review: String, onSuccess: () -> Unit) {
        val updates = mapOf(
            "rating" to rating,
            "review" to review
        )
        database.child("Trips").child(tripId).updateChildren(updates)
            .addOnSuccessListener {
                if (driverId.isNotEmpty()) {
                    database.child("Trips").orderByChild("driverId").equalTo(driverId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val ratings = snapshot.children.mapNotNull { child ->
                                    val t = child.getValue(Trip::class.java)
                                    if (child.key == tripId) {
                                        rating.toDouble()
                                    } else {
                                        t?.rating?.toDouble()?.takeIf { it > 0.0 }
                                    }
                                }
                                val average = if (ratings.isNotEmpty()) ratings.average() else rating.toDouble()
                                val roundedAverage = Math.round(average * 10.0) / 10.0
                                database.child("Users").child(driverId).child("rating").setValue(roundedAverage)
                                    .addOnCompleteListener { onSuccess() }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                onSuccess()
                            }
                        })
                } else {
                    onSuccess()
                }
            }
            .addOnFailureListener {
                onSuccess()
            }
    }

    // Gửi tin nhắn chat
    fun sendMessage(tripId: String, message: ChatMessage, onSuccess: () -> Unit = {}) {
        val chatRef = database.child("Chats").child(tripId).push()
        val newMessage = message.copy(messageId = chatRef.key ?: "")
        chatRef.setValue(newMessage)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess()
            }
    }

    // Lắng nghe tin nhắn chat
    fun listenForMessages(tripId: String, onUpdate: (List<ChatMessage>) -> Unit) {
        database.child("Chats").child(tripId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = snapshot.children.mapNotNull { it.getValue(ChatMessage::class.java) }
                    onUpdate(messages)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // Lắng nghe danh sách các cuốc xe đang chờ (Dành cho Tài xế)
    fun listenForAvailableTrips(onUpdate: (List<Trip>) -> Unit) {
        database.child("Trips").orderByChild("status").equalTo("PENDING")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val trips = snapshot.children.mapNotNull { it.getValue(Trip::class.java) }
                    onUpdate(trips)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // Lắng nghe chuyến xe hiện tại của tài xế
    fun listenForActiveTrip(driverId: String, onUpdate: (Trip?) -> Unit) {
        database.child("Trips").orderByChild("driverId").equalTo(driverId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val activeTrip = snapshot.children.mapNotNull { it.getValue(Trip::class.java) }
                        .find { it.status != "COMPLETED" && it.status != "CANCELLED" }
                    onUpdate(activeTrip)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // Lắng nghe lịch sử chuyến xe từ Firebase để tính toán thu nhập
    fun listenForDriverHistory(driverId: String, onUpdate: (List<Trip>) -> Unit) {
        database.child("Trips").orderByChild("driverId").equalTo(driverId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val history = snapshot.children.mapNotNull { it.getValue(Trip::class.java) }
                        .filter { it.status == "COMPLETED" }
                        .sortedByDescending { it.completedAt ?: it.timestamp }
                    onUpdate(history)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
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

    // Cập nhật trạng thái chuyến đi (ARRIVED, PICKED_UP)
    fun updateTripStatus(tripId: String, status: String, onSuccess: () -> Unit = {}) {
        database.child("Trips").child(tripId).child("status").setValue(status)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess()
            }
    }

    // Hoàn thành chuyến đi: Lưu mốc thời gian và cập nhật tổng thu nhập trên Firebase
    fun completeTrip(trip: Trip, driverId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val now = System.currentTimeMillis()
        val updates = mapOf(
            "Trips/${trip.tripId}/status" to "COMPLETED",
            "Trips/${trip.tripId}/completedAt" to now
        )
        
        database.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Đồng bộ tổng thu nhập vào dữ liệu User trên Firebase
                val driverRef = database.child("Users").child(driverId)
                driverRef.child("totalEarnings").get().addOnSuccessListener { snapshot ->
                    val currentEarnings = snapshot.getValue(Double::class.java) ?: 0.0
                    driverRef.child("totalEarnings").setValue(currentEarnings + trip.price)
                        .addOnCompleteListener { 
                            if (it.isSuccessful) onSuccess() 
                            else onFailure("Không thể cập nhật thu nhập trên Firebase")
                        }
                }.addOnFailureListener {
                    onFailure("Lỗi kết nối Firebase")
                }
            } else {
                onFailure(task.exception?.message ?: "Lỗi lưu dữ liệu chuyến đi")
            }
        }
    }
}
