package com.example.taxibookingproject.model

data class Payment(
    val paymentId: String = "",
    val tripId: String = "",
    val passengerId: String = "",
    val driverId: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "CASH", // Các loại: CASH (Tiền mặt), VNPAY, MOMO
    val status: String = "PENDING",     // Trạng thái: PENDING (Chờ), SUCCESS (Thành công)
    val timestamp: Long = System.currentTimeMillis()
)