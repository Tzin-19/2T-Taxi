package com.example.taxibookingproject.ui.passenger

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.taxibookingproject.controller.AuthController
import com.example.taxibookingproject.controller.BookingController
import com.example.taxibookingproject.controller.DirectionsController
import com.example.taxibookingproject.model.SavedPlace
import com.example.taxibookingproject.model.Trip
import com.example.taxibookingproject.model.User
import com.example.taxibookingproject.ui.theme.DeepYellow
import com.example.taxibookingproject.ui.theme.SoftYellow
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.*

data class VehicleType(
    val id: Int,
    val name: String,
    val baseFare: Double,
    val pricePerKm: Double,
    val icon: ImageVector,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    authController: AuthController,
    bookingController: BookingController,
    directionsController: DirectionsController,
    locationType: String? = null,
    onBack: () -> Unit,
    onConfirmBooking: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val context = LocalContext.current
    val uid = authController.getCurrentUserUid() ?: return
    
    var activeTrip by remember { mutableStateOf<Trip?>(null) }
    var driverData by remember { mutableStateOf<User?>(null) }
    var userData by remember { mutableStateOf<User?>(null) }
    
    LaunchedEffect(uid) {
        authController.listenToUserData(uid) { userData = it }
        bookingController.listenForPassengerActiveTrip(uid) { trip ->
            activeTrip = trip
            if (trip?.driverId != null) {
                authController.getUserData(trip.driverId, { driverData = it }, {})
            } else {
                driverData = null
            }
        }
    }

    if (activeTrip != null && activeTrip?.status != "PENDING") {
        ActiveTripOverlay(
            trip = activeTrip!!,
            driver = driverData,
            onRatingSubmit = { rating, review ->
                val dId = activeTrip?.driverId ?: ""
                bookingController.submitRating(activeTrip!!.tripId, dId, rating, review) {
                    activeTrip = null
                    Toast.makeText(context, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show()
                }
            },
            onCompleteTrip = {
                bookingController.updateTripStatus(activeTrip!!.tripId, "COMPLETED")
            },
            onNavigateToChat = {
                onNavigateToChat(activeTrip!!.tripId)
            }
        )
    } else {
        BookingForm(authController, bookingController, directionsController, userData, locationType, onBack, onConfirmBooking, activeTrip != null)
    }
}

@Composable
fun ActiveTripOverlay(
    trip: Trip,
    driver: User?,
    onRatingSubmit: (Float, String) -> Unit,
    onCompleteTrip: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    val isCompleted = trip.status == "COMPLETED"
    var rating by remember { mutableFloatStateOf(0f) }
    var review by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F7F7)).padding(16.dp)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = if (isCompleted) "Chuyến đi đã hoàn thành" else "Thông tin tài xế", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.Black, modifier = Modifier.padding(vertical = 24.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(model = driver?.profileImageUrl ?: "https://cdn-icons-png.flaticon.com/512/3135/3135715.png", contentDescription = null, modifier = Modifier.size(100.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(driver?.fullName ?: "Đang tải...", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Star, contentDescription = null, tint = DeepYellow, modifier = Modifier.size(16.dp)); Text(" ${driver?.rating ?: 5.0}", fontSize = 14.sp, color = Color.Gray) }
                    
                    if (!isCompleted) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val statusText = when(trip.status) {
                            "ACCEPTED" -> "Đang đến"
                            "ARRIVED" -> "Đã đến"
                            "PICKED_UP" -> "Đang di chuyển"
                            else -> "Đang xử lý"
                        }
                        Surface(
                            color = when(trip.status) {
                                "ARRIVED" -> Color(0xFFE8F5E9)
                                "PICKED_UP" -> Color(0xFFE3F2FD)
                                else -> Color(0xFFFFFDE7)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = statusText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = when(trip.status) {
                                    "ARRIVED" -> Color(0xFF2E7D32)
                                    "PICKED_UP" -> Color(0xFF1565C0)
                                    else -> Color(0xFFF57F17)
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        DetailItem("Biển số xe", driver?.plateNumber ?: "Đang cập nhật")
                        DetailItem("Loại xe", driver?.carModel?.takeIf { it.isNotEmpty() } ?: trip.vehicleTypeName)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TripOrigin, null, tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Điểm đón: ", fontSize = 12.sp, color = Color.Gray)
                        }
                        Text(text = trip.pickupLocation, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(start = 24.dp))
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Điểm đến: ", fontSize = 12.sp, color = Color.Gray)
                        }
                        Text(text = trip.destinationLocation, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(start = 24.dp))
                    }

                    if (!isCompleted) {
                        val callContext = LocalContext.current
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    driver?.phone?.let { phone ->
                                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                            data = android.net.Uri.parse("tel:$phone")
                                        }
                                        callContext.startActivity(intent)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("GỌI ĐIỆN")
                            }

                            Button(
                                onClick = onNavigateToChat,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepYellow, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("NHẮN TIN")
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            if (isCompleted) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Đánh giá của bạn", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row { repeat(5) { index -> Icon(imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = null, tint = DeepYellow, modifier = Modifier.size(40.dp).clickable { rating = index + 1f }) } }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(value = review, onValueChange = { review = it }, placeholder = { Text("Để lại lời bình luận...") }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp))
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { onRatingSubmit(rating, review) }, enabled = rating > 0, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = DeepYellow, contentColor = Color.Black)) { Text("GỬI ĐÁNH GIÁ", fontWeight = FontWeight.Black) }
                    }
                }
            } else {
                Button(onClick = onCompleteTrip, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)) { Text("XÁC NHẬN ĐÃ ĐẾN NƠI", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column { Text(label, fontSize = 12.sp, color = Color.Gray); Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingForm(
    authController: AuthController,
    bookingController: BookingController,
    directionsController: DirectionsController,
    userData: User?,
    locationType: String?,
    onBack: () -> Unit,
    onConfirmBooking: (String) -> Unit,
    isWaiting: Boolean
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val placesClient = remember { Places.createClient(context) }
    val sessionToken = remember { AutocompleteSessionToken.newInstance() }
    val apiKey = "AIzaSyB9pjq8i1-2BmiWOdciM2TSQWycyOsqYBY"

    var pickupAddress by remember { mutableStateOf("Đang xác định vị trí...") }
    var destAddress by remember { mutableStateOf("") }
    var pickupLatLng by remember { mutableStateOf(LatLng(16.0471, 108.2068)) } // Default to Da Nang
    var destLatLng by remember { mutableStateOf<LatLng?>(null) }
    
    var predictions by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isSearchingPickup by remember { mutableStateOf(false) }
    var isSearchingDest by remember { mutableStateOf(false) }
    var isSearchingPlaces by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var distanceKm by remember { mutableDoubleStateOf(0.0) }
    var durationText by remember { mutableStateOf("") }

    val performSearch = { query: String, isPickup: Boolean ->
        if (query.length >= 1 && query != "Đang xác định vị trí...") {
            isSearchingPlaces = true
            searchPlaces(placesClient, sessionToken, query, pickupLatLng) { result ->
                predictions = result
                isSearchingPlaces = false
            }
        } else {
            predictions = emptyList()
            isSearchingPlaces = false
        }
    }

    // Xử lý tự động điền địa điểm từ PassengerHome hoặc các trang khác
    LaunchedEffect(userData, locationType) {
        userData?.let { user ->
            if (locationType != null) {
                if (locationType == "home") {
                    user.homePlace?.let { 
                        destAddress = it.address
                        destLatLng = LatLng(it.lat, it.lng)
                        isSearchingDest = false
                    }
                } else if (locationType.startsWith("fav_")) {
                    val favId = locationType.removePrefix("fav_")
                    user.favoritePlaces[favId]?.let {
                        destAddress = it.address
                        destLatLng = LatLng(it.lat, it.lng)
                        isSearchingDest = false
                    }
                } else if (locationType == "history" || locationType == "favorite") {
                    isSearchingDest = true
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    pickupLatLng = currentLatLng
                    val geocoder = Geocoder(context, Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                        pickupAddress = addresses?.getOrNull(0)?.getAddressLine(0) ?: "Vị trí của bạn"
                    } catch (e: Exception) { pickupAddress = "Vị trí hiện tại" }
                }
            }
        }
    }

    val vehicleTypes = listOf(
        VehicleType(1, "Xe 4 Chỗ", 20000.0, 12000.0, Icons.Default.DirectionsCar, "Tiết kiệm, nhanh chóng"),
        VehicleType(2, "Xe 8 Chỗ", 30000.0, 15000.0, Icons.Default.AirportShuttle, "Rộng rãi cho nhóm nhỏ"),
        VehicleType(3, "Xe 12 Chỗ", 45000.0, 18000.0, Icons.Default.AirportShuttle, "Phù hợp cho đoàn khách"),
        VehicleType(4, "Xe 16 Chỗ", 60000.0, 22000.0, Icons.Default.DirectionsBus, "Dành cho chuyến đi tập thể")
    )
    var selectedType by remember { mutableStateOf(vehicleTypes[0]) }

    LaunchedEffect(pickupLatLng, destLatLng) {
        destLatLng?.let { dest ->
            val routeData = directionsController.getRouteData(pickupLatLng, dest, apiKey)
            if (routeData != null) {
                distanceKm = routeData.distanceKm
                durationText = routeData.durationText
            } else {
                val results = FloatArray(1)
                Location.distanceBetween(pickupLatLng.latitude, pickupLatLng.longitude, dest.latitude, dest.longitude, results)
                distanceKm = results[0].toDouble() / 1000.0
                durationText = ""
            }
        }
    }

    val handleManualSearch = { query: String, isPickup: Boolean ->
        if (query.isNotEmpty() && query != "Đang xác định vị trí...") {
            isSearchingPlaces = true
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                val addresses = geocoder.getFromLocationName(query, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val latLng = LatLng(addr.latitude, addr.longitude)
                    if (isPickup) {
                        pickupLatLng = latLng
                        pickupAddress = addr.getAddressLine(0) ?: query
                        isSearchingPickup = false
                    } else {
                        destLatLng = latLng
                        destAddress = addr.getAddressLine(0) ?: query
                        isSearchingDest = false
                    }
                    predictions = emptyList()
                    isSearchingPlaces = false
                    focusManager.clearFocus()
                } else {
                    searchPlaces(placesClient, sessionToken, query, pickupLatLng) { result ->
                        predictions = result
                        isSearchingPlaces = false
                        if (result.isEmpty()) Toast.makeText(context, "Không tìm thấy địa điểm chính xác. Vui lòng chọn từ gợi ý.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                isSearchingPlaces = false
                Toast.makeText(context, "Lỗi kết nối tìm kiếm", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isWaiting) "Đang tìm tài xế..." else "Đặt Xe", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color(0xFFF7F7F7)).padding(16.dp)) {
            if (isWaiting) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = DeepYellow)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Hệ thống đang tìm tài xế gần nhất cho bạn...", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(pickupAddress, textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.Gray)
                        Icon(Icons.Default.ArrowDownward, null, tint = Color.LightGray, modifier = Modifier.padding(vertical = 4.dp))
                        Text(destAddress, textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            } else {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        AddressSearchField(
                            value = pickupAddress,
                            onValueChange = { 
                                pickupAddress = it
                                isSearchingPickup = true
                                isSearchingDest = false
                                performSearch(it, true)
                            },
                            placeholder = "Nhập điểm đón",
                            icon = Icons.Default.TripOrigin,
                            iconColor = Color(0xFF1976D2),
                            onClear = { pickupAddress = ""; predictions = emptyList() },
                            onFocusChanged = { focused ->
                                if (focused) {
                                    isSearchingPickup = true
                                    isSearchingDest = false
                                    if (pickupAddress.isNotEmpty() && pickupAddress != "Đang xác định vị trí...") performSearch(pickupAddress, true)
                                }
                            },
                            onSearch = { handleManualSearch(pickupAddress, true) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 40.dp, vertical = 4.dp), thickness = 0.5.dp, color = Color.LightGray)

                        AddressSearchField(
                            value = destAddress,
                            onValueChange = { 
                                destAddress = it
                                isSearchingDest = true
                                isSearchingPickup = false
                                performSearch(it, false)
                            },
                            placeholder = "Bạn muốn đi đâu?",
                            icon = Icons.Default.LocationOn,
                            iconColor = Color.Red,
                            onClear = { destAddress = ""; destLatLng = null; distanceKm = 0.0; predictions = emptyList() },
                            onFocusChanged = { focused ->
                                if (focused) {
                                    isSearchingDest = true
                                    isSearchingPickup = false
                                    if (destAddress.isNotEmpty()) performSearch(destAddress, false)
                                }
                            },
                            onSearch = { handleManualSearch(destAddress, false) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isSearchingDest || isSearchingPickup) {
                    Card(modifier = Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(if (isSearchingPickup) "Chọn điểm đón" else "Chọn điểm đến", fontWeight = FontWeight.Bold, color = Color.Gray)
                                IconButton(onClick = { isSearchingDest = false; isSearchingPickup = false; focusManager.clearFocus() }) { Icon(Icons.Default.Close, null) }
                            }
                            if (isSearchingPlaces) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = DeepYellow)
                            }
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                val currentInput = if (isSearchingPickup) pickupAddress else destAddress
                                
                                if (predictions.isEmpty() && !isSearchingPlaces) {
                                    if (currentInput.isNotEmpty() && currentInput != "Đang xác định vị trí...") {
                                        item {
                                            ListItem(
                                                headlineContent = { Text("Sử dụng địa chỉ: \"$currentInput\"", color = Color(0xFF1976D2), fontWeight = FontWeight.Bold) },
                                                leadingContent = { Icon(Icons.Default.Map, null, tint = Color(0xFF1976D2)) },
                                                modifier = Modifier.clickable { handleManualSearch(currentInput, isSearchingPickup) }
                                            )
                                        }
                                    }
                                    
                                    // Hiển thị Yêu thích
                                    if (userData?.favoritePlaces?.isNotEmpty() == true) {
                                        item { Text("Địa điểm yêu thích", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, color = DeepYellow) }
                                        items(userData.favoritePlaces.values.toList()) { place ->
                                            LocationItem(place.name, place.address, Icons.Default.Favorite, Color.Red, 
                                                onSelect = {
                                                    if (isSearchingPickup) { pickupAddress = place.address; pickupLatLng = LatLng(place.lat, place.lng); isSearchingPickup = false }
                                                    else { destAddress = place.address; destLatLng = LatLng(place.lat, place.lng); isSearchingDest = false }
                                                    focusManager.clearFocus()
                                                },
                                                onDelete = { bookingController.deleteFavorite(userData.uid, place.id) },
                                                showBookButton = true,
                                                isFavorite = true
                                            )
                                        }
                                    }
                                    // Hiển thị Lịch sử tìm kiếm
                                    if (userData?.searchHistory?.isNotEmpty() == true) {
                                        item { Text("Lịch sử tìm kiếm", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, color = Color.Gray) }
                                        items(userData.searchHistory.values.toList().reversed()) { place ->
                                            LocationItem(place.name, place.address, Icons.Default.History, Color.Gray, 
                                                onSelect = {
                                                    if (isSearchingPickup) { pickupAddress = place.address; pickupLatLng = LatLng(place.lat, place.lng); isSearchingPickup = false }
                                                    else { destAddress = place.address; destLatLng = LatLng(place.lat, place.lng); isSearchingDest = false }
                                                    focusManager.clearFocus()
                                                },
                                                onDelete = { bookingController.deleteHistoryItem(userData.uid, place.id) },
                                                showBookButton = true,
                                                isFavorite = false
                                            )
                                        }
                                    }
                                    if (userData?.favoritePlaces.isNullOrEmpty() && userData?.searchHistory.isNullOrEmpty() && currentInput.isEmpty()) {
                                        item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("Hãy nhập địa điểm để tìm kiếm", color = Color.Gray) } }
                                    }
                                } else {
                                    item { Text("Kết quả gợi ý", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, color = Color.Gray) }
                                    items(predictions) { (address, placeId) ->
                                        ListItem(
                                            headlineContent = { Text(address, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                            leadingContent = { Icon(Icons.Default.LocationSearching, null, tint = Color.LightGray) },
                                            modifier = Modifier.clickable {
                                                val wasPickup = isSearchingPickup
                                                isSearchingPlaces = true
                                                fetchLatLng(placesClient, placeId) { latLng -> 
                                                    if (wasPickup) { pickupLatLng = latLng; pickupAddress = address; isSearchingPickup = false } 
                                                    else { destLatLng = latLng; destAddress = address; isSearchingDest = false }
                                                    isSearchingPlaces = false
                                                    predictions = emptyList()
                                                    focusManager.clearFocus()
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (destLatLng != null && !isSearchingDest && !isSearchingPickup) {
                    Text("CHỌN LOẠI XE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(vehicleTypes) { type ->
                            val currentPrice = type.baseFare + (type.pricePerKm * distanceKm)
                            VehicleItemRowModern(type = type, calculatedPrice = currentPrice, isSelected = selectedType.id == type.id, onClick = { selectedType = type })
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { 
                                Text("Khoảng cách", fontSize = 12.sp, color = Color.Gray)
                                Text("${String.format(Locale.getDefault(), "%.1f", distanceKm)} km${if (durationText.isNotEmpty()) " (~$durationText)" else ""}", fontSize = 16.sp, fontWeight = FontWeight.Bold) 
                            }
                            Column(horizontalAlignment = Alignment.End) { 
                                Text("Tổng cộng", fontSize = 12.sp, color = Color.Gray)
                                val finalPrice = selectedType.baseFare + (selectedType.pricePerKm * distanceKm)
                                Text("${String.format(Locale.getDefault(), "%,.0f", finalPrice)}đ", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.Black) 
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        if (pickupAddress == "Đang xác định vị trí...") {
                            Toast.makeText(context, "Vui lòng đợi hoặc chọn địa điểm đón cụ thể", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val currentUid = authController.getCurrentUserUid() ?: return@Button
                        isProcessing = true
                        val finalPrice = selectedType.baseFare + (selectedType.pricePerKm * distanceKm)
                        val trip = Trip(
                            passengerId = currentUid,
                            pickupLocation = pickupAddress,
                            destinationLocation = destAddress,
                            pickupLat = pickupLatLng.latitude, pickupLng = pickupLatLng.longitude,
                            destLat = destLatLng?.latitude ?: 0.0, destLng = destLatLng?.longitude ?: 0.0,
                            distance = String.format(Locale.getDefault(), "%.1f km", distanceKm),
                            distanceValue = distanceKm,
                            price = finalPrice,
                            vehicleTypeName = selectedType.name,
                            status = "PENDING"
                        )
                        bookingController.createTrip(trip, { onConfirmBooking(it) }, { isProcessing = false; Toast.makeText(context, "Lỗi đặt xe", Toast.LENGTH_SHORT).show() })
                    }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = DeepYellow, contentColor = Color.Black)) {
                        if (isProcessing) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp)) else Text("XÁC NHẬN ĐẶT XE", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                } else if (destLatLng == null && !isSearchingPickup && !isSearchingDest) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Vui lòng nhập điểm đến để chọn xe", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocationItem(
    name: String, 
    address: String, 
    icon: ImageVector, 
    iconColor: Color, 
    onSelect: () -> Unit, 
    onDelete: () -> Unit, 
    showBookButton: Boolean = false, 
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null
) {
    ListItem(
        modifier = Modifier.clickable { onSelect() },
        headlineContent = { Text(name, fontWeight = FontWeight.Bold) },
        supportingContent = { Text(address, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingContent = { Icon(icon, null, tint = iconColor) },
        trailingContent = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showBookButton) IconButton(onClick = onSelect) { Icon(Icons.Default.DirectionsCar, "Đặt chuyến đi", tint = DeepYellow) }
                if (onToggleFavorite != null) {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if (isFavorite) Color.Red else Color.LightGray)
                    }
                } else if (isFavorite) IconButton(onClick = onDelete) { Icon(Icons.Default.Favorite, null, tint = Color.Red) }
                if (!isFavorite) IconButton(onClick = onDelete) { Icon(Icons.Default.Close, null, tint = Color.LightGray, modifier = Modifier.size(18.dp)) } 
            }
        }
    )
}

@Composable
fun AddressSearchField(
    value: String, 
    onValueChange: (String) -> Unit, 
    placeholder: String, 
    icon: ImageVector, 
    iconColor: Color, 
    onClear: () -> Unit, 
    onSearch: () -> Unit = {},
    onFocusChanged: (Boolean) -> Unit = {}
) {
    TextField(
        value = value, 
        onValueChange = onValueChange, 
        placeholder = { Text(placeholder, fontSize = 14.sp) }, 
        leadingIcon = { Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp)) }, 
        trailingIcon = { 
            Row {
                if (value.isNotEmpty()) IconButton(onClick = onClear) { Icon(Icons.Default.Clear, null, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onSearch) { Icon(Icons.Default.Search, null, tint = Color.Gray) }
            }
        }, 
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), 
        modifier = Modifier.fillMaxWidth().onFocusChanged { onFocusChanged(it.isFocused) }, 
        singleLine = true
    )
}

@Composable
fun VehicleItemRowModern(type: VehicleType, calculatedPrice: Double, isSelected: Boolean, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(16.dp), color = if (isSelected) SoftYellow.copy(alpha = 0.3f) else Color.White, border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, DeepYellow) else null, shadowElevation = if (isSelected) 4.dp else 1.dp) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).background(Color(0xFFF0F0F0), CircleShape), contentAlignment = Alignment.Center) { Icon(type.icon, null, modifier = Modifier.size(30.dp), tint = Color.DarkGray) }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) { Text(type.name, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text(type.description, fontSize = 11.sp, color = Color.Gray) }
            Text("${String.format(Locale.getDefault(), "%,.0f", calculatedPrice)}đ", fontWeight = FontWeight.Black, fontSize = 18.sp, color = if (isSelected) Color.Black else Color.Gray)
        }
    }
}

private fun searchPlaces(
    placesClient: PlacesClient, 
    token: AutocompleteSessionToken, 
    query: String, 
    biasLatLng: LatLng? = null,
    onResult: (List<Pair<String, String>>) -> Unit
) {
    if (query.trim().isEmpty()) {
        onResult(emptyList())
        return
    }
    val builder = FindAutocompletePredictionsRequest.builder()
        .setSessionToken(token)
        .setQuery(query)
        .setCountries("VN")
    
    biasLatLng?.let {
        val biasRange = 0.8 // Tăng phạm vi lên khoảng 80km để bao phủ Đà Nẵng tốt hơn
        val bounds = RectangularBounds.newInstance(
            LatLng(it.latitude - biasRange, it.longitude - biasRange),
            LatLng(it.latitude + biasRange, it.longitude + biasRange)
        )
        builder.setLocationBias(bounds)
    }

    placesClient.findAutocompletePredictions(builder.build()).addOnSuccessListener { response -> 
        onResult(response.autocompletePredictions.map { it.getFullText(null).toString() to it.placeId }) 
    }.addOnFailureListener {
        onResult(emptyList())
    }
}

private fun fetchLatLng(placesClient: PlacesClient, placeId: String, onResult: (LatLng) -> Unit) {
    val request = FetchPlaceRequest.builder(placeId, listOf(com.google.android.libraries.places.api.model.Place.Field.LAT_LNG)).build()
    placesClient.fetchPlace(request).addOnSuccessListener { response -> response.place.latLng?.let { onResult(it) } }
}
