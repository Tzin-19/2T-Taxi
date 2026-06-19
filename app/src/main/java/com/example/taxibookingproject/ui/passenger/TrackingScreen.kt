package com.example.taxibookingproject.ui.passenger

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.taxibookingproject.controller.AuthController
import com.example.taxibookingproject.controller.BookingController
import com.example.taxibookingproject.controller.DirectionsController
import com.example.taxibookingproject.controller.LocationManager
import com.example.taxibookingproject.model.Trip
import com.example.taxibookingproject.model.User
import com.example.taxibookingproject.ui.theme.DeepYellow
import com.example.taxibookingproject.ui.theme.MilkyYellow
import com.example.taxibookingproject.ui.theme.SoftYellow
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun TrackingScreen(
    tripId: String,
    bookingController: BookingController,
    locationManager: LocationManager,
    authController: AuthController,
    directionsController: DirectionsController,
    onCancelTrip: () -> Unit,
    onArrived: () -> Unit,
    onViewDriverProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val context = LocalContext.current
    var tripData by remember { mutableStateOf<Trip?>(null) }
    var driverPos by remember { mutableStateOf<LatLng?>(null) }
    var driverInfo by remember { mutableStateOf<User?>(null) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    
    val apiKey = "AIzaSyB9pjq8i1-2BmiWOdciM2TSQWycyOsqYBY"

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(21.0285, 105.8542), 15f)
    }

    // 1. Lắng nghe trạng thái chuyến đi
    LaunchedEffect(tripId) {
        bookingController.listenToTripStatus(tripId) { trip ->
            tripData = trip
            // Di chuyển camera đến điểm đón khi bắt đầu
            if (tripData == null) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(trip.pickupLat, trip.pickupLng), 15f)
            }
            
            if (trip.status == "COMPLETED") {
                onArrived()
            }
        }
    }

    // 2. Lắng nghe vị trí và thông tin tài xế
    LaunchedEffect(tripData?.driverId) {
        tripData?.driverId?.let { driverId ->
            // Lấy vị trí xe
            locationManager.trackDriverLocation(driverId) { location ->
                driverPos = location
            }
            // Lấy thông tin tài xế (tên, ảnh...)
            authController.getUserData(driverId, { 
                driverInfo = it 
            }, {})
        }
    }

    // 3. Cập nhật đường đi khi vị trí tài xế hoặc trạng thái thay đổi
    LaunchedEffect(driverPos, tripData?.status) {
        val currentDriverPos = driverPos
        val currentTrip = tripData
        if (currentDriverPos != null && currentTrip != null) {
            val destination = if (currentTrip.status == "PICKED_UP")
                LatLng(currentTrip.destLat, currentTrip.destLng)
            else
                LatLng(currentTrip.pickupLat, currentTrip.pickupLng)

            val points = directionsController.getRoutePoints(currentDriverPos, destination, apiKey)
            if (points.isNotEmpty()) {
                routePoints = points
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            tripData?.let {
                Marker(
                    state = MarkerState(position = LatLng(it.pickupLat, it.pickupLng)),
                    title = "Điểm đón của bạn",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )

                Marker(
                    state = MarkerState(position = LatLng(it.destLat, it.destLng)),
                    title = "Điểm đến",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
                
                driverPos?.let { pos ->
                    Marker(
                        state = MarkerState(position = pos),
                        title = "Tài xế: ${driverInfo?.fullName ?: "Đang đến"}",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                    )
                }

                if (routePoints.isNotEmpty()) {
                    Polyline(
                        points = routePoints,
                        color = Color(0xFF2196F3),
                        width = 10f
                    )
                }
            }
        }

        // Trạng thái chuyến đi
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
                val statusText = when(tripData?.status) {
                    "PENDING" -> "Đang tìm tài xế..."
                    "ACCEPTED" -> "Đang đến"
                    "ARRIVED" -> "Đã đến"
                    "PICKED_UP" -> "Đang di chuyển"
                    else -> "Vui lòng đợi..."
                }
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = statusText, color = Color.Black, fontWeight = FontWeight.Black, fontSize = 15.sp)
            }
        }

        // Panel thông tin tài xế
        if (tripData?.driverId != null) {
            Card(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                elevation = CardDefaults.cardElevation(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.clickable { tripData?.driverId?.let { onViewDriverProfile(it) } },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(MilkyYellow)) {
                            if (driverInfo?.profileImageUrl.isNullOrEmpty()) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.align(Alignment.Center).size(36.dp), tint = Color.Black)
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
                            Text(text = driverInfo?.fullName ?: "Đang tải...", fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Text(text = "${driverInfo?.carModel ?: "Xe taxi"} • ${driverInfo?.plateNumber ?: ""}", color = Color.Gray, fontSize = 13.sp)
                        }
                        IconButton(
                            onClick = {
                                driverInfo?.phone?.let { phone ->
                                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                        data = android.net.Uri.parse("tel:$phone")
                                    }
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier.size(52.dp).background(SoftYellow, CircleShape)
                        ) { Icon(Icons.Default.Call, null, tint = Color.Black) }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.DirectionsCar, null, tint = DeepYellow, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Loại xe", fontSize = 11.sp, color = Color.Gray)
                            Text(tripData?.vehicleTypeName ?: "", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Payment, null, tint = DeepYellow, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Số tiền", fontSize = 11.sp, color = Color.Gray)
                            Text("${String.format("%,.0f", tripData?.price ?: 0.0)}đ", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Route, null, tint = DeepYellow, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Quãng đường", fontSize = 11.sp, color = Color.Gray)
                            Text(tripData?.distance ?: "", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(
                            onClick = { onNavigateToChat(tripId) },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Chat, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Nhắn tin")
                        }

                        Button(
                            onClick = {
                                bookingController.updateTripStatus(tripId, "CANCELLED") { onCancelTrip() }
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color.Red)
                        ) {
                            Icon(Icons.Default.Close, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hủy chuyến")
                        }
                    }
                }
            }
        }
    }
}
