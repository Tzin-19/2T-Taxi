package com.example.taxibookingproject.ui.driver

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taxibookingproject.controller.AuthController
import com.example.taxibookingproject.controller.BookingController
import com.example.taxibookingproject.controller.DirectionsController
import com.example.taxibookingproject.controller.LocationManager
import com.example.taxibookingproject.model.Trip
import com.example.taxibookingproject.ui.theme.DeepYellow
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun NavigationScreen(
    tripId: String,
    bookingController: BookingController,
    locationManager: LocationManager,
    authController: AuthController,
    directionsController: DirectionsController,
    onFinishTrip: () -> Unit,
    onViewPassengerProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val context = LocalContext.current
    var tripData by remember { mutableStateOf<Trip?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    
    // Lấy API Key từ AndroidManifest hoặc BuildConfig (ở đây lấy từ meta-data nếu cần, hoặc truyền vào)
    // Để đơn giản, tôi sẽ lấy từ strings.xml nếu bạn đã định nghĩa, hoặc hardcode theo manifest bạn gửi
    val apiKey = "AIzaSyB9pjq8i1-2BmiWOdciM2TSQWycyOsqYBY" 

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(21.0285, 105.8542), 15f)
    }

    var passengerInfo by remember { mutableStateOf<com.example.taxibookingproject.model.User?>(null) }

    LaunchedEffect(tripData?.passengerId) {
        tripData?.passengerId?.let { passengerId ->
            authController.getUserData(passengerId, {
                passengerInfo = it
            }, {})
        }
    }

    LaunchedEffect(tripId) {
        bookingController.listenToTripStatus(tripId) { trip ->
            tripData = trip
            isLoading = false
            val targetPos = if (trip.status == "ACCEPTED" || trip.status == "ARRIVED") 
                LatLng(trip.pickupLat, trip.pickupLng) 
            else 
                LatLng(trip.destLat, trip.destLng)
            
            cameraPositionState.position = CameraPosition.fromLatLngZoom(targetPos, 15f)
        }
    }

    // Cập nhật đường đi khi tripData thay đổi
    LaunchedEffect(tripData?.status) {
        tripData?.let { trip ->
            val origin = LatLng(21.0285, 105.8542) // Vị trí giả định của tài xế
            val destination = if (trip.status == "ACCEPTED" || trip.status == "ARRIVED")
                LatLng(trip.pickupLat, trip.pickupLng)
            else
                LatLng(trip.destLat, trip.destLng)

            val points = directionsController.getRoutePoints(origin, destination, apiKey)
            if (points.isNotEmpty()) {
                routePoints = points
            }
        }
    }

    val driverId = authController.getCurrentUserUid() ?: ""
    LaunchedEffect(Unit) {
        while(true) {
            locationManager.updateDriverLocation(driverId, 21.0285, 105.8542)
            kotlinx.coroutines.delay(5000)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = DeepYellow)
        } else {
            tripData?.let { trip ->
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(myLocationButtonEnabled = true)
                ) {
                    Marker(
                        state = MarkerState(position = LatLng(trip.pickupLat, trip.pickupLng)),
                        title = "Điểm đón: ${trip.pickupLocation}",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                    Marker(
                        state = MarkerState(position = LatLng(trip.destLat, trip.destLng)),
                        title = "Điểm trả: ${trip.destinationLocation}",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                    
                    if (routePoints.isNotEmpty()) {
                        Polyline(
                            points = routePoints,
                            color = Color.Blue,
                            width = 12f
                        )
                    } else {
                        // Fallback sang đường thẳng nếu không lấy được directions
                        Polyline(
                            points = listOf(LatLng(trip.pickupLat, trip.pickupLng), LatLng(trip.destLat, trip.destLng)),
                            color = Color.Blue.copy(alpha = 0.5f),
                            width = 8f
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp).padding(top = 40.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Navigation, null, tint = Color.White, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                val msg = if (trip.status == "ACCEPTED" || trip.status == "ARRIVED") "Đang đến điểm đón..." else "Đang chở khách đến đích..."
                                Text(msg, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(text = if (trip.status == "ACCEPTED" || trip.status == "ARRIVED") trip.pickupLocation else trip.destinationLocation, color = Color.White.copy(0.7f), fontSize = 13.sp, maxLines = 1)
                            }
                        }
                        IconButton(onClick = {
                            val targetLat = if (trip.status == "ACCEPTED" || trip.status == "ARRIVED") trip.pickupLat else trip.destLat
                            val targetLng = if (trip.status == "ACCEPTED" || trip.status == "ARRIVED") trip.pickupLng else trip.destLng
                            val gmmIntentUri = Uri.parse("google.navigation:q=$targetLat,$targetLng")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            try { context.startActivity(mapIntent) } catch (e: Exception) { Toast.makeText(context, "Không tìm thấy ứng dụng Google Maps", Toast.LENGTH_SHORT).show() }
                        }, modifier = Modifier.background(DeepYellow, CircleShape)) {
                            Icon(Icons.Default.Directions, null, tint = Color.Black)
                        }
                    }
                }

                Card(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    elevation = CardDefaults.cardElevation(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.clickable { onViewPassengerProfile(trip.passengerId) }, verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Color.Black) }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(passengerInfo?.fullName ?: "Xem hồ sơ khách hàng", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("⭐ 5.0 • Khách hàng", color = Color.Gray, fontSize = 13.sp)
                            }
                            Row {
                                IconButton(onClick = {
                                    passengerInfo?.phone?.let { phone ->
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:$phone")
                                        }
                                        context.startActivity(intent)
                                    }
                                }) { Icon(Icons.Default.Call, null, tint = Color(0xFF4CAF50)) }
                                
                                IconButton(onClick = {
                                    onNavigateToChat(trip.tripId)
                                }) { Icon(Icons.Default.Chat, null, tint = DeepYellow) }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        val buttonText = when(trip.status) {
                            "ACCEPTED" -> "TÔI ĐÃ ĐẾN ĐIỂM ĐÓN"
                            "ARRIVED" -> "BẮT ĐẦU CHUYẾN XE"
                            else -> "HOÀN THÀNH CHUYẾN XE"
                        }

                        Button(
                            onClick = {
                                if (trip.status == "PICKED_UP") {
                                    isProcessing = true
                                    bookingController.completeTrip(trip, driverId, 
                                        onSuccess = { onFinishTrip() },
                                        onFailure = { 
                                            isProcessing = false
                                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                } else {
                                    val nextStatus = if (trip.status == "ACCEPTED") "ARRIVED" else "PICKED_UP"
                                    bookingController.updateTripStatus(tripId, nextStatus)
                                }
                            },
                            enabled = !isProcessing,
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepYellow, contentColor = Color.Black),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (isProcessing) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                            else Text(buttonText, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
