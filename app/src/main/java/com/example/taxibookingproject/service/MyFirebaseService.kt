package com.example.taxibookingproject.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Token mới: $token")
        // Gửi token này lên Server/Database để sau này gửi thông báo đúng người
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Xử lý khi có thông báo gửi đến (Ví dụ: Có chuyến xe mới)
        Log.d("FCM", "Thông báo: ${message.notification?.body}")
    }
}