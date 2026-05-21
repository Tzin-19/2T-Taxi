package com.example.taxibookingproject.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taxibookingproject.controller.AdminController
import com.example.taxibookingproject.ui.theme.DeepYellow
import com.example.taxibookingproject.ui.theme.MilkyYellow
import com.example.taxibookingproject.ui.theme.SoftYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminController: AdminController,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Dashboard, 1: Users

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản trị hệ thống", fontWeight = FontWeight.Black) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, null) },
                    label = { Text("Thống kê") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color.Black,
                        indicatorColor = DeepYellow
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.People, null) },
                    label = { Text("Người dùng") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color.Black,
                        indicatorColor = DeepYellow
                    )
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFFAFAFA))) {
            if (selectedTab == 0) {
                DashboardContent()
            } else {
                UserManagementContent(adminController)
            }
        }
    }
}

@Composable
fun DashboardContent() {
    LazyColumn(modifier = Modifier.padding(horizontal = 20.dp)) {
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text("Tổng quan hệ thống", fontSize = 22.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(20.dp))
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard("Chuyến đi", "1,250", Icons.Default.DirectionsCar, DeepYellow, Modifier.weight(1f))
                StatCard("Tài xế", "45", Icons.Default.Person, Color.Black, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard("Khách hàng", "850", Icons.Default.People, Color.Black, Modifier.weight(1f))
                StatCard("Doanh thu", "15M", Icons.Default.Payments, DeepYellow, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
        item {
            Text("Hoạt động gần đây", fontSize = 18.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(12.dp))
        }
        items(5) {
            RecentActivityItem()
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.size(40.dp).background(SoftYellow, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.Black)
                Text(title, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun RecentActivityItem() {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).background(MilkyYellow, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CheckCircle, null, tint = Color.Black, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Chuyến đi #1234 hoàn thành", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("2 phút trước • 45,000đ", color = Color.Gray, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun UserManagementContent(adminController: AdminController) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Spacer(modifier = Modifier.height(20.dp))
        Text("Quản lý người dùng", fontSize = 22.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(20.dp))
        LazyColumn {
            items(10) { index ->
                UserItem(
                    name = if (index % 2 == 0) "Đối tác Tài xế $index" else "Khách hàng $index",
                    role = if (index % 2 == 0) "Tài xế" else "Khách",
                    isLocked = index == 3
                )
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun UserItem(name: String, role: String, isLocked: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).background(SoftYellow, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = Color.Black)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = role, 
                        fontSize = 13.sp, 
                        fontWeight = FontWeight.SemiBold,
                        color = if (role == "Tài xế") Color(0xFF2E7D32) else DeepYellow
                    )
                }
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.background(if(isLocked) Color(0xFFFFEBEE) else Color.Transparent, CircleShape)
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = if (isLocked) Color.Red else Color.Gray
                )
            }
        }
    }
}