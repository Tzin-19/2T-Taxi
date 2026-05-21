package com.example.taxibookingproject.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taxibookingproject.ui.components.TaxiButton
import com.example.taxibookingproject.ui.theme.DeepYellow
import com.example.taxibookingproject.ui.theme.MilkyYellow
import com.example.taxibookingproject.ui.theme.SoftYellow
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun NavigationScreen(onFinishTrip: () -> Unit) {
    val currentLoc = LatLng(21.0300, 105.8560)
    val destLoc = LatLng(21.0333, 105.8500)
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLoc, 16f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Google Map Navigation
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            Marker(state = MarkerState(position = destLoc), title = "Điểm đến của khách")
        }

        // Bảng chỉ dẫn phía trên - Tông màu đen/vàng tương phản cao để tài xế dễ nhìn
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(DeepYellow, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.TurnRight, null, tint = Color.Black, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Rẽ phải vào Lê Lợi", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Text("Còn 300m • Đi thẳng tiếp", color = Color.White.copy(alpha = 0.7f), fontSize = 15.sp)
                }
            }
        }

        // Thông tin chuyến đi phía dưới
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            elevation = CardDefaults.cardElevation(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Đang chở: Trần Văn A", fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text("Đến: Sân bay Tân Sơn Nhất", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    IconButton(
                        onClick = { /* Gọi cho khách */ },
                        modifier = Modifier
                            .size(52.dp)
                            .background(SoftYellow, CircleShape)
                    ) { Icon(Icons.Default.Call, null, tint = Color.Black) }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = Color(0xFFF5F5F5))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("DỰ KIẾN", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("12 phút", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("KHOẢNG CÁCH", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("3.2 km", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("GIÁ TIỀN", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("85.000đ", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                TaxiButton(
                    text = "HOÀN THÀNH CHUYẾN ĐI",
                    containerColor = DeepYellow
                ) {
                    onFinishTrip()
                }
            }
        }
    }
}