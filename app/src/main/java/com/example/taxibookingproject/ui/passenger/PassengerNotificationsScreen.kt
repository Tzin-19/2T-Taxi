package com.example.taxibookingproject.ui.passenger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.taxibookingproject.controller.AuthController
import com.example.taxibookingproject.controller.BookingController
import com.example.taxibookingproject.model.ChatMessage
import com.example.taxibookingproject.model.Trip
import com.example.taxibookingproject.model.User
import com.example.taxibookingproject.ui.theme.DeepYellow
import com.example.taxibookingproject.ui.theme.SoftYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerNotificationsScreen(
    bookingController: BookingController,
    authController: AuthController,
    onBack: () -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val currentUid = authController.getCurrentUserUid() ?: ""
    var activeTrip by remember { mutableStateOf<Trip?>(null) }
    var driverInfo by remember { mutableStateOf<User?>(null) }
    var chatMessages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }

    // 1. Listen for active trip
    LaunchedEffect(currentUid) {
        if (currentUid.isNotEmpty()) {
            bookingController.listenForPassengerActiveTrip(currentUid) { trip ->
                activeTrip = trip
            }
        }
    }

    // 2. Listen to driver info and messages if active trip exists
    LaunchedEffect(activeTrip) {
        val trip = activeTrip
        if (trip != null && trip.driverId != null) {
            authController.getUserData(trip.driverId, { driverInfo = it }, {})
            bookingController.listenForMessages(trip.tripId) { list ->
                chatMessages = list.sortedByDescending { it.timestamp }
            }
        } else {
            driverInfo = null
            chatMessages = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông báo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF7F7F7))
        ) {
            val latestMessage = chatMessages.firstOrNull()
            
            if (activeTrip == null && latestMessage == null) {
                // Empty State
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Không có thông báo mới nào", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (activeTrip != null) {
                        item {
                            Text(
                                "Chuyến đi hiện tại",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }

                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        activeTrip?.let { onNavigateToChat(it.tripId) }
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(SoftYellow)
                                    ) {
                                        if (driverInfo?.profileImageUrl.isNullOrEmpty()) {
                                            Icon(
                                                Icons.Default.Person,
                                                null,
                                                modifier = Modifier.align(Alignment.Center).size(28.dp),
                                                tint = Color.Black
                                            )
                                        } else {
                                            AsyncImage(
                                                model = driverInfo?.profileImageUrl,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            driverInfo?.fullName ?: "Tài xế của bạn",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        
                                        val subText = if (latestMessage != null) {
                                            val prefix = if (latestMessage.senderId == currentUid) "Bạn: " else "Tài xế: "
                                            "$prefix${latestMessage.text}"
                                        } else {
                                            "Kết nối chat với tài xế ngay..."
                                        }
                                        
                                        Text(
                                            subText,
                                            fontSize = 13.sp,
                                            color = if (latestMessage?.senderId != currentUid && latestMessage != null) Color.Black else Color.Gray,
                                            fontWeight = if (latestMessage?.senderId != currentUid && latestMessage != null) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }

                                    if (latestMessage != null) {
                                        val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
                                        Text(
                                            timeFormat.format(Date(latestMessage.timestamp)),
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Chat,
                                            null,
                                            tint = DeepYellow,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
