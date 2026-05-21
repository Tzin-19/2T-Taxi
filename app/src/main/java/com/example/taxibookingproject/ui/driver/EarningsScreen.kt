package com.example.taxibookingproject.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taxibookingproject.ui.theme.DeepYellow
import com.example.taxibookingproject.ui.theme.MilkyYellow
import com.example.taxibookingproject.ui.theme.SoftYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thu nhập của bạn", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
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

            // Tổng quan thu nhập - Thẻ chính màu Vàng
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
                        Text("Tổng thu nhập tuần này", color = Color.Black.copy(alpha = 0.6f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("5,450,000đ", color = Color.Black, fontSize = 34.sp, fontWeight = FontWeight.Black)
                        
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatItem("Chuyến đi", "42")
                            StatItem("Số giờ", "38h")
                            StatItem("Đánh giá", "5.0")
                        }
                    }
                }
            }

            // Biểu đồ giả lập
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text("Xu hướng thu nhập", fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp).fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        listOf(0.4f, 0.7f, 0.9f, 0.5f, 0.8f, 1f, 0.6f).forEach { height ->
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .fillMaxHeight(height)
                                    .background(DeepYellow, RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }

            // Lịch sử chuyến xe
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Lịch sử gần đây", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    TextButton(onClick = {}) { 
                        Text("Xem tất cả", color = Color.Gray, fontWeight = FontWeight.Bold) 
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(5) { index ->
                EarningHistoryItem()
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.Black, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text(label, color = Color.Black.copy(alpha = 0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EarningHistoryItem() {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SoftYellow, RoundedCornerShape(12.dp)), 
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.TrendingUp, null, tint = Color.Black)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Chuyến xe #TX1029", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Hôm nay, 14:30", color = Color.Gray, fontSize = 12.sp)
            }
            Text("+45,000đ", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 16.sp)
        }
    }
}