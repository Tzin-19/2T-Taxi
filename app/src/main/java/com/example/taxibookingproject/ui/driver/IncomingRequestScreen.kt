package com.example.taxibookingproject.ui.driver

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taxibookingproject.ui.theme.DeepYellow
import com.example.taxibookingproject.ui.theme.MilkyYellow
import com.example.taxibookingproject.ui.theme.SoftYellow
import kotlinx.coroutines.delay

@Composable
fun IncomingRequestScreen(onAccept: () -> Unit, onDecline: () -> Unit) {
    var timeLeft by remember { mutableIntStateOf(15) }
    
    // Đếm ngược thời gian nhận cuốc
    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        if (timeLeft == 0) onDecline()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftYellow) // Chuyển sang tông vàng sữa nhạt
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Vòng tròn đếm ngược
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { timeLeft / 15f },
                    modifier = Modifier.size(130.dp),
                    color = DeepYellow,
                    strokeWidth = 10.dp,
                    trackColor = Color.White
                )
                Text(
                    text = "$timeLeft",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "CÓ CHUYẾN XE MỚI! 🚕",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Thẻ thông tin chuyến đi
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Điểm đón
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Navigation, null, tint = Color(0xFF1976D2), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("ĐIỂM ĐÓN", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("88 Lê Lợi, Quận 1, TP.HCM", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Điểm đến
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("ĐIỂM ĐẾN", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("Sân bay Tân Sơn Nhất", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = Color(0xFFF5F5F5))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("QUÃNG ĐƯỜNG", fontSize = 11.sp, color = Color.Gray)
                            Text("7.5 km", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("THU NHẬP", fontSize = 11.sp, color = Color.Gray)
                            Text("115.000đ", fontWeight = FontWeight.Black, fontSize = 26.sp, color = Color.Black)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Bộ nút điều khiển
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nút Bỏ qua
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f).height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(0.1f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                ) {
                    Text("BỎ QUA", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                // Nút Chấp nhận
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1.5f).height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepYellow, contentColor = Color.Black)
                ) {
                    Text("CHẤP NHẬN", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            }
        }
    }
}