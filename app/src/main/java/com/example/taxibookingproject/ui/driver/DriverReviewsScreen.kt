package com.example.taxibookingproject.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taxibookingproject.controller.AuthController
import com.example.taxibookingproject.controller.BookingController
import com.example.taxibookingproject.model.Trip
import com.example.taxibookingproject.ui.theme.DeepYellow
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverReviewsScreen(
    authController: AuthController,
    bookingController: BookingController,
    onBack: () -> Unit
) {
    val uid = authController.getCurrentUserUid() ?: ""
    var reviews by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            bookingController.listenForDriverHistory(uid) { history ->
                // Lọc những chuyến đi có đánh giá sao > 0
                reviews = history.filter { it.rating > 0 }
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đánh giá khách hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
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
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = DeepYellow)
            } else if (reviews.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.StarBorder,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Bạn chưa nhận được đánh giá nào", color = Color.Gray)
                }
            } else {
                val totalRatingSum = reviews.sumOf { it.rating.toDouble() }
                val totalReviews = reviews.size
                val averageRating = totalRatingSum / totalReviews

                // Tính toán tỷ lệ sao
                val starBreakdown = remember(reviews) {
                    val breakdown = mutableMapOf<Int, Int>()
                    for (i in 1..5) breakdown[i] = 0
                    reviews.forEach {
                        val roundedStar = it.rating.toInt()
                        breakdown[roundedStar] = (breakdown[roundedStar] ?: 0) + 1
                    }
                    breakdown
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Summary Rating Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f", averageRating),
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DeepYellow
                                )
                                Row {
                                    repeat(5) { index ->
                                        Icon(
                                            imageVector = if (index < averageRating.toInt()) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = null,
                                            tint = DeepYellow,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$totalReviews đánh giá", fontSize = 12.sp, color = Color.Gray)
                            }

                            // Star breakdown columns
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f).padding(start = 24.dp)
                            ) {
                                for (star in 5 downTo 1) {
                                    val count = starBreakdown[star] ?: 0
                                    val percentage = if (totalReviews > 0) count.toFloat() / totalReviews else 0f
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("$star sao", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(36.dp))
                                        LinearProgressIndicator(
                                            progress = { percentage },
                                            color = DeepYellow,
                                            trackColor = Color(0xFFEEEEEE),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                        )
                                        Text("$count", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(start = 8.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Reviews List
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(reviews) { trip ->
                            ReviewItemCard(trip = trip)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewItemCard(trip: Trip) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(trip.completedAt ?: trip.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < trip.rating.toInt()) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = DeepYellow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(dateStr, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (trip.review.trim().isNotEmpty()) {
                Text(
                    text = "\"${trip.review}\"",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Text(
                    text = "Khách hàng không để lại bình luận",
                    fontSize = 13.sp,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Hành trình: ${trip.pickupLocation.split(",")[0]} ➔ ${trip.destinationLocation.split(",")[0]}",
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }
    }
}
