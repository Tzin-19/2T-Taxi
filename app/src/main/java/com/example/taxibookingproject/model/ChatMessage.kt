package com.example.taxibookingproject.model

data class ChatMessage(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val messageType: String = "text" // "text", "image", "file"
)
