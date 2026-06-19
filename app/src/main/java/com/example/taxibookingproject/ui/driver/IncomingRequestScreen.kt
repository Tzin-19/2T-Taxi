package com.example.taxibookingproject.ui.driver

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taxibookingproject.controller.AuthController
import com.example.taxibookingproject.controller.BookingController
import com.example.taxibookingproject.controller.DirectionsController
import com.example.taxibookingproject.model.Trip
import com.example.taxibookingproject.ui.theme.DeepYellow
import com.example.taxibookingproject.ui.theme.SoftYellow
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun IncomingRequestScreen(
    tripId: String,
    bookingController: BookingController,
    authController: AuthController,
    directionsController: DirectionsController,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onViewPassengerProfile: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope() // Khai báo scope để chạy coroutine trong Compose
    var timeLeft by remember { mutableIntStateOf(15) }
    var tripData by remember { mutableStateOf<Trip?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    
    val apiKey = "AIzaSyB9pjq8i1-2BmiWOdciM2TSQWycyOsqYBY"

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(21.0285, 105.8542), 15f)
    }

    LaunchedEffect(tripId) {
        bookingController.listenToTripStatus(tripId) { trip ->
            tripData = trip
            isLoading = false
            
            // Lấy lộ trình giữa điểm đón và điểm trả
            val origin = LatLng(trip.pickupLat, trip.pickupLng)
            val dest = LatLng(trip.destLat, trip.destLng)
            
            // Di chuyển camera bao quát cả 2 điểm
            val bounds = LatLngBounds.builder()
                .include(origin)
                .include(dest)
                .build()
            cameraPositionState.position = CameraPosition.fromLatLngZoom(bounds.center, 13f)

            // Gọi API lấy đường đi bằng scope của Compose
            scope.launch {
                val points = directionsController.getRoutePoints(origin, dest, apiKey)
                routePoints = points
            }

            if (trip.status != "PENDING") onDecline()
        }
    }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        if (timeLeft == 0) onDecline()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DeepYellow)
            }
        } else {
            tripData?.let { trip ->
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                ) {
                    Marker(
                        state = MarkerState(position = LatLng(trip.pickupLat, trip.pickupLng)),
                        title = "Điểm đón",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                    Marker(
                        state = MarkerState(position = LatLng(trip.destLat, trip.destLng)),
                        title = "Điểm đến",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                    
                    if (routePoints.isNotEmpty()) {
                        Polyline(
                            points = routePoints,
                            color = Color.Black,
                            width = 10f
                        )
                    }
                }

                // UI Overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDecline,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Bỏ qua", tint = Color.Black)
                    }

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Timer, "Timer", tint = DeepYellow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Tự hủy sau: ${timeLeft}s",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = Color.White,
                    shadowElevation = 20.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .navigationBarsPadding()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(Color.LightGray, CircleShape)
                                .align(Alignment.CenterHorizontally)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text("THU NHẬP DỰ KIẾN", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${String.format("%,.0f", trip.price)}đ",
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    color = SoftYellow.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        trip.distance,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF856404)
                                    )
                                }
                                Text("KHOẢNG CÁCH", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8F9FA), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            TripPointRow(Icons.Default.TripOrigin, Color(0xFF1976D2), trip.pickupLocation, "Điểm đón")
                            Box(modifier = Modifier.padding(start = 9.dp).height(20.dp).width(1.dp).background(Color.LightGray))
                            TripPointRow(Icons.Default.LocationOn, Color.Red, trip.destinationLocation, "Điểm đến")
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onViewPassengerProfile(trip.passengerId) },
                                color = Color(0xFFEEEEEE)
                            ) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.padding(16.dp), tint = Color.DarkGray)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Button(
                                onClick = {
                                    val driverId = authController.getCurrentUserUid() ?: ""
                                    bookingController.acceptTrip(tripId, driverId, { onAccept() }, { error ->
                                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                    })
                                },
                                modifier = Modifier.weight(1f).height(60.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepYellow, contentColor = Color.Black)
                            ) {
                                Text("NHẬN CUỐC", fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TripPointRow(icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, address: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(address, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
    }
}
