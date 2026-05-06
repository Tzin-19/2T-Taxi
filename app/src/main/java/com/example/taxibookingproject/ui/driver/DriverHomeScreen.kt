package com.example.taxibookingproject.ui.driver

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taxibookingproject.ui.components.TaxiButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen(onLogout: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Giao diện Tài xế") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Chào mừng Tài xế!", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Hệ thống đang tìm kiếm chuyến xe cho bạn...")
            
            Spacer(modifier = Modifier.height(32.dp))
            
            TaxiButton("ĐĂNG XUẤT") {
                onLogout()
            }
        }
    }
}