package com.example.taxibookingproject.ui.driver

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
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
import com.example.taxibookingproject.model.User
import com.example.taxibookingproject.ui.theme.DeepYellow
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen(
    authController: AuthController,
    onLogout: () -> Unit,
    onNavigateToEarnings: () -> Unit,
    onNewRequest: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isWorking by remember { mutableStateOf(false) }
    
    val uid = authController.getCurrentUserUid() ?: ""
    var driverData by remember { mutableStateOf<User?>(null) }

    // Tải thông tin tài xế
    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            authController.getUserData(uid, {
                driverData = it
            }, {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            })
        }
    }

    // Kiểm tra và xin quyền vị trí
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    
    val hanoi = LatLng(21.0285, 105.8542)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(hanoi, 15f)
    }

    val statusColor by animateColorAsState(
        targetValue = if (isWorking) DeepYellow else Color.White,
        label = "statusColor"
    )
    val contentColor = Color.Black

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
                            text = "⭐ ${driverData?.rating ?: 5.0} • Đối tác 5 sao",
                            color = Color.Black.copy(0.6f),
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                DriverDrawerItem(Icons.Default.AccountBalanceWallet, "Ví thu nhập") { onNavigateToEarnings() }
                DriverDrawerItem(Icons.Default.History, "Lịch sử chuyến xe")
                DriverDrawerItem(Icons.Default.Notifications, "Thông báo")
                DriverDrawerItem(Icons.Default.Settings, "Cài đặt")
                
                Spacer(modifier = Modifier.weight(1f))
                
                DriverDrawerItem(Icons.AutoMirrored.Filled.ExitToApp, "Đăng xuất", Color.Red) { onLogout() }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    ) {
        Scaffold { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = hasLocationPermission && isWorking),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = hasLocationPermission)
                )

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
                            Text("1,250,000đ", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.Black)
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { scope.launch { drawerState.open() } },
                    modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    shape = CircleShape
                ) { Icon(Icons.Default.Menu, "Menu") }

                if (isWorking) {
                    Button(
                        onClick = onNewRequest,
                        modifier = Modifier.align(Alignment.Center).padding(bottom = 100.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepYellow, contentColor = Color.Black),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Demo: Cuốc xe mới 🔔", fontWeight = FontWeight.Bold)
                    }
                }

                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(10.dp),
                    colors = CardDefaults.cardColors(containerColor = statusColor)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isWorking) "BẠN ĐANG TRỰC TUYẾN" else "BẠN ĐANG NGOẠI TUYẾN",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = contentColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isWorking) "Sẵn sàng nhận cuốc xe mới" else "Gạt nút bên dưới để bắt đầu làm việc",
                            fontSize = 14.sp,
                            color = contentColor.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Switch(
                            checked = isWorking,
                            onCheckedChange = { 
                                if (!hasLocationPermission && it) {
                                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                } else {
                                    isWorking = it 
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color.Black,
                                uncheckedTrackColor = Color(0xFFEEEEEE),
                                uncheckedThumbColor = Color.White
                            )
                        )
                    }
                }
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
