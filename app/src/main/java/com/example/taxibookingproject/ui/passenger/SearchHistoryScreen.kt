package com.example.taxibookingproject.ui.passenger

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taxibookingproject.controller.AuthController
import com.example.taxibookingproject.controller.BookingController
import com.example.taxibookingproject.model.SavedPlace
import com.example.taxibookingproject.model.User
import com.example.taxibookingproject.ui.theme.DeepYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchHistoryScreen(
    authController: AuthController,
    bookingController: BookingController,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uid = authController.getCurrentUserUid() ?: ""
    var userData by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            authController.listenToUserData(uid) { data ->
                userData = data
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử tìm kiếm", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    val history = userData?.searchHistory?.values?.toList() ?: emptyList()
                    if (history.isNotEmpty()) {
                        TextButton(onClick = {
                            bookingController.clearSearchHistory(uid)
                            Toast.makeText(context, "Đã xóa toàn bộ lịch sử", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("XÓA TẤT CẢ", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
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
            val historyList = userData?.searchHistory?.values?.toList()?.reversed() ?: emptyList()

            if (historyList.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.History,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Lịch sử tìm kiếm trống", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(historyList) { item ->
                        SearchHistoryItem(
                            item = item,
                            onDelete = {
                                bookingController.deleteHistoryItem(uid, item.id)
                                Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchHistoryItem(
    item: SavedPlace,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF0F4C3), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.History, null, tint = Color.DarkGray, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.address,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Xóa", tint = Color.Red, modifier = Modifier.size(20.dp))
            }
        }
    }
}
