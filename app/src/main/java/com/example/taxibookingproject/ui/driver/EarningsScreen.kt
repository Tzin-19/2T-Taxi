package com.example.taxibookingproject.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taxibookingproject.controller.AuthController
import com.example.taxibookingproject.controller.BookingController
import com.example.taxibookingproject.model.Trip
import com.example.taxibookingproject.ui.theme.DeepYellow
import com.example.taxibookingproject.ui.theme.SoftYellow
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningsScreen(
    authController: AuthController,
    bookingController: BookingController,
    onBack: () -> Unit
) {
    val uid = authController.getCurrentUserUid() ?: ""
    var history by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Giờ, 1: Ngày, 2: Tháng

    // Lắng nghe dữ liệu lịch sử trực tiếp từ Firebase Realtime Database
    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            bookingController.listenForDriverHistory(uid) { trips ->
                history = trips
            }
        }
    }

    val totalEarnings = history.sumOf { it.price }
    val totalKm = history.sumOf { it.distanceValue }
    
    val chartData = remember(history, selectedTab) {
        processChartData(history, selectedTab)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thu Nhập Cá Nhân", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFAFAFA))
                .padding(horizontal = 20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Thẻ tổng quan - Dữ liệu từ Firebase
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepYellow)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Tổng thu nhập từ Firebase", color = Color.Black.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${String.format(Locale.getDefault(), "%,.0f", totalEarnings)}đ", color = Color.Black, fontSize = 34.sp, fontWeight = FontWeight.Black)
                        
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatItem("Chuyến đi", "${history.size}")
                            StatItem("Km tích lũy", String.format(Locale.getDefault(), "%.1f", totalKm))
                            StatItem("Đánh giá", "5.0")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color.Black,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color.Black
                        )
                    }
                ) {
                    val tabs = listOf("Hàng giờ", "Hàng ngày", "Hàng tháng")
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }) {
                            Text(title, modifier = Modifier.padding(vertical = 12.dp), fontWeight = if(selectedTab == index) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            // Biểu đồ dựa trên dữ liệu thật
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    if (chartData.isEmpty() || chartData.all { it == 0f }) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Chưa có dữ liệu từ Firebase", color = Color.Gray, fontSize = 12.sp)
                        }
                    } else {
                        Row(
                            modifier = Modifier.padding(24.dp).fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            chartData.forEach { heightFactor ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                        .fillMaxHeight(heightFactor.coerceIn(0.05f, 1f))
                                        .background(DeepYellow, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text("Lịch sử nhận đơn", fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (history.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("Dữ liệu trống", color = Color.Gray)
                    }
                }
            } else {
                items(history) { trip ->
                    EarningHistoryItem(trip)
                }
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

private fun processChartData(history: List<Trip>, tab: Int): List<Float> {
    if (history.isEmpty()) return emptyList()
    val calendar = Calendar.getInstance()
    val now = System.currentTimeMillis()
    
    val data = when (tab) {
        0 -> { // 24h qua
            val hourly = FloatArray(24)
            history.forEach { trip ->
                if (now - trip.timestamp < 24 * 60 * 60 * 1000) {
                    calendar.timeInMillis = trip.timestamp
                    hourly[calendar.get(Calendar.HOUR_OF_DAY)] += trip.price.toFloat()
                }
            }
            hourly.toList()
        }
        1 -> { // 7 ngày qua
            val daily = FloatArray(7)
            history.forEach { trip ->
                val diffDays = ((now - trip.timestamp) / (24 * 60 * 60 * 1000)).toInt()
                if (diffDays in 0..6) daily[6 - diffDays] += trip.price.toFloat()
            }
            daily.toList()
        }
        2 -> { // 12 tháng qua
            val monthly = FloatArray(12)
            history.forEach { trip ->
                calendar.timeInMillis = trip.timestamp
                if (calendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)) {
                    monthly[calendar.get(Calendar.MONTH)] += trip.price.toFloat()
                }
            }
            monthly.toList()
        }
        else -> emptyList()
    }
    
    val max = data.maxOrNull() ?: 1f
    return if (max == 0f) data else data.map { it / max }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.Black, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text(label, color = Color.Black.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EarningHistoryItem(trip: Trip) {
    val sdf = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault())
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(SoftYellow.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Color.Black, modifier = Modifier.size(20.dp)) }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(trip.destinationLocation, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${String.format(Locale.getDefault(), "%.1f", trip.distanceValue)} km • ${sdf.format(Date(trip.timestamp))}", color = Color.Gray, fontSize = 11.sp)
            }
            
            Text("${String.format(Locale.getDefault(), "%,.0f", trip.price)}đ", fontWeight = FontWeight.Black, color = Color.Black)
        }
    }
}
