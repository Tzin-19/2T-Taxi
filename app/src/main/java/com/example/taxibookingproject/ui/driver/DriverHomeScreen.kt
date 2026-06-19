package com.example.taxibookingproject.ui.driver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.taxibookingproject.controller.AuthController
import com.example.taxibookingproject.controller.BookingController
import com.example.taxibookingproject.controller.LocationManager
import com.example.taxibookingproject.model.Trip
import com.example.taxibookingproject.model.User
import com.example.taxibookingproject.ui.theme.DeepYellow
import com.example.taxibookingproject.ui.theme.SoftYellow
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen(
    authController: AuthController,
    locationManager: LocationManager,
    bookingController: BookingController,
    onLogout: () -> Unit,
    onNavigateToEarnings: () -> Unit,
    onNewRequest: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isWorking by remember { mutableStateOf(DriverState.isWorking) }
    var showTripList by remember { mutableStateOf(false) }
    var showQRCode by remember { mutableStateOf(false) }
    
    val uid = authController.getCurrentUserUid() ?: ""
    var driverData by remember { mutableStateOf<User?>(null) }
    var activeTrip by remember { mutableStateOf<Trip?>(null) }
    var passengerData by remember { mutableStateOf<User?>(null) }
    var currentLatLng by remember { mutableStateOf(LatLng(21.0285, 105.8542)) }
    var availableTrips by remember { mutableStateOf<List<Trip>>(emptyList()) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Tải thông tin tài xế và lắng nghe chuyến xe hiện tại
    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            authController.listenToUserData(uid) { driverData = it }
            bookingController.listenForActiveTrip(uid) { activeTrip = it }
        }
    }

    // Lắng nghe dữ liệu khách hàng khi có chuyến xe active
    LaunchedEffect(activeTrip?.passengerId) {
        activeTrip?.passengerId?.let { pid ->
            authController.getUserData(pid, { passengerData = it }, {})
        } ?: run { passengerData = null }
    }

    // Lắng nghe danh sách cuốc xe đang chờ khi đang làm việc
    LaunchedEffect(isWorking) {
        if (isWorking) {
            bookingController.listenForAvailableTrips { trips ->
                availableTrips = trips
            }
        } else {
            availableTrips = emptyList()
        }
    }

    val sortedTrips = remember(availableTrips, currentLatLng) {
        availableTrips.map { trip ->
            val results = FloatArray(1)
            Location.distanceBetween(currentLatLng.latitude, currentLatLng.longitude, trip.pickupLat, trip.pickupLng, results)
            trip to (results[0] / 1000.0)
        }.sortedBy { it.second }
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            isWorking = true
            DriverState.isWorking = true
        }
    }

    // Cập nhật vị trí tài xế liên tục
    LaunchedEffect(isWorking, hasLocationPermission) {
        if (isWorking && hasLocationPermission) {
            while(isWorking) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        currentLatLng = LatLng(it.latitude, it.longitude)
                        locationManager.updateDriverLocation(uid, it.latitude, it.longitude)
                    }
                }
                delay(5000)
            }
        } else if (!isWorking) {
            locationManager.goOffline(uid)
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLatLng, 15f)
    }

    // Tự động di chuyển camera theo vị trí tài xế (nếu không có chuyến)
    LaunchedEffect(currentLatLng) {
        if (activeTrip == null) {
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLng(currentLatLng)
            )
        }
    }

    BackHandler(showTripList) {
        showTripList = false
    }

    // Dialog hiển thị mã QR
    if (showQRCode) {
        AlertDialog(
            onDismissRequest = { showQRCode = false },
            title = { Text("Mã QR Thanh toán", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=PAYMENT_FOR_TRIP_${activeTrip?.tripId}",
                            contentDescription = "QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Số tiền: ${String.format("%,.0f", activeTrip?.price ?: 0.0)}đ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                    Text("Vui lòng quét mã để thanh toán", fontSize = 13.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showQRCode = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepYellow, contentColor = Color.Black)
                ) {
                    Text("ĐÃ HIỂU")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = Color.White
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepYellow)
                        .clickable { 
                            scope.launch { drawerState.close() }
                            onNavigateToProfile() 
                        }
                        .padding(top = 40.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (driverData?.profileImageUrl.isNullOrEmpty()) {
                                Icon(Icons.Default.Person, null, tint = Color.Black, modifier = Modifier.size(44.dp))
                            } else {
                                AsyncImage(
                                    model = driverData?.profileImageUrl,
                                    contentDescription = "Profile Image",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = driverData?.fullName ?: "Đối tác Tài xế",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "⭐ ${driverData?.rating ?: 5.0} • Xem đánh giá",
                            color = Color.Black.copy(0.6f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                scope.launch { drawerState.close() }
                                onNavigateToReviews()
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // NÚT GẠT TRỰC TUYẾN TRONG DRAWER
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isWorking) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            null,
                            tint = if (isWorking) Color(0xFF4CAF50) else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Text(
                            if (isWorking) "Đang trực tuyến" else "Đang ngoại tuyến",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                    Switch(
                        checked = isWorking,
                        onCheckedChange = { 
                            if (!hasLocationPermission && it) {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            } else {
                                isWorking = it 
                                DriverState.isWorking = it
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color.Black
                        )
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))

                DriverDrawerItem(Icons.Default.LibraryAdd, "Nhận đơn") {
                    if (isWorking) {
                        showTripList = true
                        scope.launch { drawerState.close() }
                    } else {
                        Toast.makeText(context, "Vui lòng bật trực tuyến để nhận đơn", Toast.LENGTH_SHORT).show()
                    }
                }
                DriverDrawerItem(Icons.Default.AccountBalanceWallet, "Thu Nhập Cá Nhân") { 
                    scope.launch { drawerState.close() }
                    onNavigateToEarnings() 
                }
                DriverDrawerItem(Icons.Default.History, "Lịch sử chuyến xe") {
                    scope.launch { drawerState.close() }
                    onNavigateToHistory()
                }
                DriverDrawerItem(Icons.Default.Notifications, "Thông báo") {
                    scope.launch { drawerState.close() }
                    onNavigateToNotifications()
                }
                DriverDrawerItem(Icons.Default.Star, "Lịch sử đánh giá") {
                    scope.launch { drawerState.close() }
                    onNavigateToReviews()
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                DriverDrawerItem(Icons.AutoMirrored.Filled.ExitToApp, "Đăng xuất", Color.Red) { 
                    DriverState.isWorking = false
                    onLogout() 
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    ) {
        Scaffold { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (showTripList) {
                    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { showTripList = false }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                            }
                            Text("Danh sách cuốc xe gần nhất", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        
                        if (sortedTrips.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Hiện không có cuốc xe nào quanh đây", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(sortedTrips) { (trip, dist) ->
                                    TripItemCard(trip, dist) {
                                        bookingController.acceptTrip(trip.tripId, uid, {
                                            showTripList = false
                                        }, { err ->
                                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                        })
                                    }
                                }
                            }
                        }
                    }
                } else {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = hasLocationPermission && isWorking),
                        uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = hasLocationPermission)
                    ) {
                        if (activeTrip != null) {
                            val trip = activeTrip!!
                            val targetLatLng = if (trip.status == "ACCEPTED" || trip.status == "ARRIVED") 
                                LatLng(trip.pickupLat, trip.pickupLng) else LatLng(trip.destLat, trip.destLng)
                            
                            Marker(
                                state = MarkerState(position = currentLatLng),
                                title = "Vị trí của tôi",
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                            )

                            Marker(
                                state = MarkerState(position = LatLng(trip.pickupLat, trip.pickupLng)),
                                title = "Điểm đón",
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                            )
                            Marker(
                                state = MarkerState(position = LatLng(trip.destLat, trip.destLng)),
                                title = "Điểm trả",
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                            )
                            
                            Polyline(
                                points = listOf(currentLatLng, targetLatLng),
                                color = DeepYellow,
                                width = 10f
                            )
                        }
                    }

                    // Nút Menu
                    FloatingActionButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        shape = CircleShape
                    ) { Icon(Icons.Default.Menu, "Menu") }

                    // Card thu nhập
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                            .clickable { onNavigateToEarnings() },
                        shape = RoundedCornerShape(30.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Payments, null, tint = DeepYellow, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Thu nhập hôm nay", fontSize = 11.sp, color = Color.Gray)
                                Text("${String.format("%,.0f", driverData?.totalEarnings ?: 0.0)}đ", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.Black)
                            }
                        }
                    }

                    // PANEL DƯỚI CÙNG
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it }),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(28.dp),
                            elevation = CardDefaults.cardElevation(15.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (!isWorking) {
                                    Text("BẠN ĐANG NGOẠI TUYẾN", fontWeight = FontWeight.Black, color = Color.Gray)
                                    Text("Mở menu để bắt đầu nhận cuốc", fontSize = 12.sp, color = Color.Gray)
                                } else if (activeTrip != null) {
                                    val trip = activeTrip!!
                                    val targetLatLng = if (trip.status == "ACCEPTED" || trip.status == "ARRIVED") 
                                        LatLng(trip.pickupLat, trip.pickupLng) else LatLng(trip.destLat, trip.destLng)
                                    
                                    val results = FloatArray(1)
                                    Location.distanceBetween(currentLatLng.latitude, currentLatLng.longitude, targetLatLng.latitude, targetLatLng.longitude, results)
                                    val distanceToTarget = results[0] / 1000.0

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.size(48.dp).background(SoftYellow, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                if (trip.status == "ACCEPTED" || trip.status == "ARRIVED") Icons.Default.PersonPinCircle else Icons.Default.LocationOn,
                                                null, tint = Color.Black
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                if (trip.status == "ACCEPTED" || trip.status == "ARRIVED") "Đến điểm đón khách" else "Đến điểm trả khách",
                                                fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                if (trip.status == "ACCEPTED" || trip.status == "ARRIVED") trip.pickupLocation else trip.destinationLocation,
                                                fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, maxLines = 1
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("${String.format("%.1f", distanceToTarget)} km", fontWeight = FontWeight.Black, fontSize = 20.sp, color = DeepYellow)
                                            Text("CÒN LẠI", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }
                                    
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                    // THÔNG TIN KHÁCH HÀNG
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = passengerData?.profileImageUrl,
                                            contentDescription = null,
                                            modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.LightGray),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(passengerData?.fullName ?: "Khách hàng", fontWeight = FontWeight.Bold)
                                            Text("SĐT: ${passengerData?.phone ?: "---"}", fontSize = 12.sp, color = Color.Gray)
                                        }
                                        Row {
                                            IconButton(onClick = {
                                                passengerData?.phone?.let { phone ->
                                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                                        data = Uri.parse("tel:$phone")
                                                    }
                                                    context.startActivity(intent)
                                                }
                                            }) {
                                                Icon(Icons.Default.Phone, null, tint = Color(0xFF4CAF50))
                                            }
                                            IconButton(onClick = {
                                                onNavigateToChat(trip.tripId)
                                            }) {
                                                Icon(Icons.Default.Chat, null, tint = DeepYellow)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        // NÚT THANH TOÁN
                                        OutlinedButton(
                                            onClick = { showQRCode = true },
                                            modifier = Modifier.weight(1f).height(50.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black)
                                        ) {
                                            Icon(Icons.Default.QrCode2, null, tint = Color.Black)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("THANH TOÁN", color = Color.Black, fontWeight = FontWeight.Bold)
                                        }

                                        // NÚT ĐÃ XONG
                                        Button(
                                            onClick = { 
                                                bookingController.completeTrip(trip, uid, {
                                                    Toast.makeText(context, "Chuyến đi hoàn thành!", Toast.LENGTH_SHORT).show()
                                                }, { err ->
                                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                                })
                                            },
                                            modifier = Modifier.weight(1f).height(50.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("ĐÃ XONG", fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                    
                                    // Nút điều hướng hỗ trợ (Small)
                                    TextButton(
                                        onClick = { 
                                            val nextStatus = when(trip.status) {
                                                "ACCEPTED" -> "ARRIVED"
                                                "ARRIVED" -> "PICKED_UP"
                                                else -> trip.status
                                            }
                                            bookingController.updateTripStatus(trip.tripId, nextStatus) {}
                                        },
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Text(
                                            when(trip.status) {
                                                "ACCEPTED" -> "Cập nhật: Đã đến điểm đón"
                                                "ARRIVED" -> "Cập nhật: Đã đón khách"
                                                else -> "Đang trong hành trình"
                                            },
                                            fontSize = 11.sp, color = DeepYellow
                                        )
                                    }
                                } else {
                                    // TRẠNG THÁI ĐANG QUÉT TÌM CUỐC
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Explore, null, tint = DeepYellow)
                                            Text("TRẠNG THÁI", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                            Text("Đang chờ", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                        }
                                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.LightGray))
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.History, null, tint = DeepYellow)
                                            Text("HÔM NAY", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                            Text("12 cuốc", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                        color = DeepYellow, trackColor = Color(0xFFF0F0F0)
                                    )
                                    Text("Đang tìm kiếm yêu cầu gần bạn...", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TripItemCard(trip: Trip, distance: Double, onAccept: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${String.format("%.1f", distance)} km", fontWeight = FontWeight.Bold, color = DeepYellow)
                Text("${String.format("%,.0f", trip.price)}đ", fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TripOrigin, null, tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(trip.pickupLocation, fontSize = 14.sp, maxLines = 1)
            }
            Box(modifier = Modifier.padding(start = 7.dp).height(10.dp).width(1.dp).background(Color.LightGray))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(trip.destinationLocation, fontSize = 14.sp, maxLines = 1)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("NHẬN ĐƠN NÀY", color = Color.White)
            }
        }
    }
}

@Composable
fun DriverDrawerItem(icon: ImageVector, label: String, color: Color = Color.Black, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(20.dp))
        Text(label, color = color, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

object DriverState {
    var isWorking: Boolean = false
}

