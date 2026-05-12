package com.example.taxibookingproject.ui.passenger

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerHomeScreen(onLogout: () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Cấu hình vị trí mặc định (Ví dụ: Hà Nội)
    val hanoi = LatLng(21.0285, 105.8542)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(hanoi, 15f)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp)
            ) {
                // Header Profile
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2E3137)) // Màu tối sang trọng
                        .padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Khách hàng",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Thành viên thân thiết",
                        color = Color(0xFFFFD700), // Màu vàng Gold
                        fontSize = 12.sp
                    )
                }

                // Menu items
                Column(modifier = Modifier.padding(16.dp)) {
                    PassengerDrawerMenuItem(Icons.Default.History, "Lịch sử chuyến đi")
                    PassengerDrawerMenuItem(Icons.Default.Payment, "Thanh toán")
                    PassengerDrawerMenuItem(Icons.Default.CardGiftcard, "Ưu đãi / Khuyến mãi")
                    PassengerDrawerMenuItem(Icons.Default.SupportAgent, "Trung tâm hỗ trợ")
                    PassengerDrawerMenuItem(Icons.Default.Settings, "Cài đặt")
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    PassengerDrawerMenuItem(Icons.Default.ExitToApp, "Đăng xuất", color = Color.Red) {
                        onLogout()
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Taxi App") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color.Black
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Google Map làm nền
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = true)
                )

                // Panel tìm kiếm "Bạn muốn đi đâu?"
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Chào bạn, bạn muốn đi đâu?",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Thanh Search giả lập
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* Mở màn hình chọn điểm đến */ },
                                color = Color(0xFFF0F0F0),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Red)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Nhập điểm đến của bạn", color = Color.Gray)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Các địa điểm nhanh
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                QuickLocationItem(Icons.Default.Home, "Nhà riêng")
                                QuickLocationItem(Icons.Default.Work, "Công ty")
                                QuickLocationItem(Icons.Default.Add, "Thêm")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickLocationItem(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF2E7D32))
        }
        Text(text = label, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun PassengerDrawerMenuItem(icon: ImageVector, label: String, color: Color = Color.Black, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, fontSize = 16.sp, color = color)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}