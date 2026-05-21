package com.example.taxibookingproject.ui.passenger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun TrackingScreen(onCancelTrip: () -> Unit, onArrived: () -> Unit) {
    val passengerLoc = LatLng(21.0285, 105.8542) 
    val driverLoc = LatLng(21.0300, 105.8560)    
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(passengerLoc, 15f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            Marker(state = MarkerState(position = passengerLoc), title = "Bạn đang ở đây")
            Marker(state = MarkerState(position = driverLoc), title = "Tài xế đang đến")
        }

        // Thông báo trạng thái phía trên - Tông vàng sữa nổi bật
        Card(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 56.dp, start = 16.dp, end = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DeepYellow),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Tài xế đang đến trong 3 phút",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        }

        // Nút giả lập cho Demo
        Button(
            onClick = onArrived,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 120.dp, end = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            shape = CircleShape,
            elevation = ButtonDefaults.buttonElevation(4.dp)
        ) { Text("Demo: Đã đến", fontSize = 10.sp) }

        // Panel thông tin tài xế - Style Trắng/Vàng sạch sẽ
        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            elevation = CardDefaults.cardElevation(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(64.dp).clip(CircleShape).background(MilkyYellow), 
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(36.dp), tint = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Trần Văn A", fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text(text = "VinFast VF8 • 29A-123.45", color = Color.Gray, fontSize = 14.sp)
                    }
                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(52.dp).background(SoftYellow, CircleShape)
                    ) { Icon(Icons.Default.Call, null, tint = Color.Black) }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f).height(60.dp),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Icon(Icons.Default.Chat, null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Nhắn tin", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onCancelTrip,
                        modifier = Modifier.weight(1f).height(60.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color(0xFFD32F2F))
                    ) {
                        Icon(Icons.Default.Close, null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Hủy chuyến", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}