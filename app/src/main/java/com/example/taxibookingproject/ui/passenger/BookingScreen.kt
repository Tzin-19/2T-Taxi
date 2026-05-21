package com.example.taxibookingproject.ui.passenger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

data class VehicleOption(val id: Int, val name: String, val price: Double, val desc: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(onBack: () -> Unit, onConfirmBooking: () -> Unit) {
    val hanoi = LatLng(21.0285, 105.8542)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(hanoi, 14f)
    }

    val vehicleOptions = listOf(
        VehicleOption(1, "Taxi 4 Chỗ", 45000.0, "Tiết kiệm, nhanh chóng"),
        VehicleOption(2, "Xanh SM Luxury", 75000.0, "Xe điện VinFast VF8 sang trọng"),
        VehicleOption(3, "Taxi 7 Chỗ", 60000.0, "Phù hợp đi gia đình")
    )
    var selectedVehicle by remember { mutableStateOf(vehicleOptions[0]) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Bản đồ lộ trình
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            Marker(state = MarkerState(position = hanoi), title = "Điểm đón của bạn")
        }

        // Nút quay lại
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .background(Color.White, RoundedCornerShape(12.dp))
        ) { Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.Black) }

        // Panel thông tin đặt xe phía dưới
        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            elevation = CardDefaults.cardElevation(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = "Chọn phương tiện 🚕", fontSize = 22.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(20.dp))

                // Danh sách các loại xe
                vehicleOptions.forEach { vehicle ->
                    VehicleItem(
                        vehicle = vehicle,
                        isSelected = selectedVehicle.id == vehicle.id,
                        onClick = { selectedVehicle = vehicle }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = Color(0xFFF5F5F5))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payments, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
                        Text(text = " Tiền mặt", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp), fontSize = 16.sp)
                    }
                    Text(
                        text = "${selectedVehicle.price.toInt()}đ",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                TaxiButton(
                    text = "XÁC NHẬN ĐẶT XE",
                    containerColor = DeepYellow
                ) {
                    onConfirmBooking()
                }
            }
        }
    }
}

@Composable
fun VehicleItem(vehicle: VehicleOption, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) SoftYellow else Color.Transparent
    val borderColor = if (isSelected) DeepYellow else Color.Transparent

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, DeepYellow) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(if (isSelected) MilkyYellow else Color(0xFFF9F9F9), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.DirectionsCar, 
                    null, 
                    tint = if (isSelected) Color.Black else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = vehicle.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(text = vehicle.desc, fontSize = 13.sp, color = Color.Gray)
            }
            if (isSelected) {
                Icon(Icons.Default.LocationOn, null, tint = DeepYellow)
            }
        }
    }
}