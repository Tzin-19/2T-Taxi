package com.example.taxibookingproject.ui.passenger

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.taxibookingproject.controller.LocationManager
import com.example.taxibookingproject.model.User
import com.example.taxibookingproject.ui.theme.DeepYellow
import com.example.taxibookingproject.ui.theme.MilkyYellow
import com.example.taxibookingproject.ui.theme.SoftYellow
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerHomeScreen(
    authController: AuthController,
    locationManager: LocationManager,
    onLogout: () -> Unit,
    onNavigateToBooking: (String?) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPaymentHistory: () -> Unit,
    onNavigateToFavoritePlaces: () -> Unit,
    onNavigateToSearchHistory: () -> Unit,
    onNavigateToSpendingStatistics: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val uid = authController.getCurrentUserUid() ?: ""
    var passengerData by remember { mutableStateOf<User?>(null) }
    var activeDrivers by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var showSupportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            authController.listenToUserData(uid) { data ->
                passengerData = data
            }
        }
    }

    LaunchedEffect(Unit) {
        locationManager.getActiveDrivers { drivers -> activeDrivers = drivers }
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        hasLocationPermission = permissions.values.any { it }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            title = { Text("Thông tin hỗ trợ", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, null, tint = DeepYellow, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("htchannelas@gmail.com")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, null, tint = DeepYellow, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("0369809629")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Link, null, tint = DeepYellow, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("fb.com/tzin.19.01", color = Color.Blue)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSupportDialog = false }) {
                    Text("Đóng", color = DeepYellow, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    val hanoi = LatLng(21.0285, 105.8542)
    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(hanoi, 14f) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp), drawerContainerColor = Color.White) {
                Box(modifier = Modifier.fillMaxWidth().background(DeepYellow).clickable { scope.launch { drawerState.close() }; onNavigateToProfile() }.padding(top = 40.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)) {
                    Column {
                        Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                            if (passengerData?.profileImageUrl.isNullOrEmpty()) {
                                Icon(Icons.Default.Person, null, tint = Color.Black, modifier = Modifier.size(44.dp))
                            } else {
                                AsyncImage(model = passengerData?.profileImageUrl, contentDescription = "Profile Image", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = if (passengerData?.fullName.isNullOrEmpty()) "Khách hàng" else "Chào bạn, ${passengerData?.fullName}", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                PassengerDrawerMenuItem(Icons.Default.Favorite, "Danh sách yêu thích") {
                    scope.launch { drawerState.close() }
                    onNavigateToFavoritePlaces()
                }
                PassengerDrawerMenuItem(Icons.Default.History, "Lịch sử tìm kiếm") {
                    scope.launch { drawerState.close() }
                    onNavigateToSearchHistory()
                }
                PassengerDrawerMenuItem(Icons.Default.BarChart, "Thống kê chi tiêu") {
                    scope.launch { drawerState.close() }
                    onNavigateToSpendingStatistics()
                }
                PassengerDrawerMenuItem(Icons.Default.Payment, "Lịch sử chuyến đi") {
                    scope.launch { drawerState.close() }
                    onNavigateToPaymentHistory()
                }
                PassengerDrawerMenuItem(Icons.Default.Notifications, "Thông báo") {
                    scope.launch { drawerState.close() }
                    onNavigateToNotifications()
                }
                PassengerDrawerMenuItem(Icons.Default.SupportAgent, "Hỗ trợ") {
                    scope.launch { drawerState.close() }
                    showSupportDialog = true
                }
                Spacer(modifier = Modifier.weight(1f))
                PassengerDrawerMenuItem(Icons.AutoMirrored.Filled.ExitToApp, "Đăng xuất", Color.Red) { onLogout() }
            }
        }
    ) {
        Scaffold { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = hasLocationPermission)
                ) {
                    activeDrivers.forEach { driverLoc ->
                        Marker(state = MarkerState(position = driverLoc), title = "Tài xế đang online", icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    }
                }

                FloatingActionButton(onClick = { scope.launch { drawerState.open() } }, modifier = Modifier.align(Alignment.TopStart).padding(16.dp), containerColor = Color.White, contentColor = Color.Black, shape = CircleShape) { Icon(Icons.Default.Menu, "Menu") }

                Card(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(28.dp), elevation = CardDefaults.cardElevation(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Bạn muốn đi đâu hôm nay?", fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Surface(modifier = Modifier.fillMaxWidth().height(60.dp).clickable { onNavigateToBooking(null) }, color = SoftYellow.copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MilkyYellow)) {
                            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Search, null, tint = DeepYellow, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Nhập điểm đến của bạn...", color = Color.Gray, fontSize = 16.sp)
                            }
                        }
                        

                    }
                }
            }
        }
    }
}

@Composable
fun QuickDestItem(icon: ImageVector, label: String, bgColor: Color, iconColor: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(bgColor), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
        }
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun PassengerDrawerMenuItem(icon: ImageVector, label: String, color: Color = Color.Black, onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 14.dp, horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(20.dp))
        Text(label, color = color, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}
