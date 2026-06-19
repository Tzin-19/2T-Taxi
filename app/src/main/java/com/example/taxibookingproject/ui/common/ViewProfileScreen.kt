package com.example.taxibookingproject.ui.common

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import com.example.taxibookingproject.model.User
import com.example.taxibookingproject.ui.theme.DeepYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewProfileScreen(
    uid: String,
    authController: AuthController,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            authController.getUserData(uid, {
                user = it
                isLoading = false
            }, {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                isLoading = false
            })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông tin đối tác", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepYellow)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DeepYellow)
            }
        } else {
            user?.let { profile ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box {
                        if (profile.profileImageUrl.isNullOrEmpty()) {
                            Box(
                                modifier = Modifier.size(130.dp).clip(CircleShape).background(Color(0xFFEEEEEE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(90.dp), tint = Color.Gray)
                            }
                        } else {
                            AsyncImage(
                                model = profile.profileImageUrl,
                                contentDescription = "Profile Image",
                                modifier = Modifier.size(130.dp).clip(CircleShape).border(3.dp, DeepYellow, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(profile.fullName, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    
                    val roleText = when(profile.role) {
                        1 -> "Quản trị viên"
                        2 -> "Đối tác tài xế"
                        else -> "Khách hàng"
                    }
                    Text(roleText, color = DeepYellow, fontWeight = FontWeight.Bold)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    ProfileInfoDisplayRow("Số điện thoại", profile.phone)
                    ProfileInfoDisplayRow("Email", profile.email)
                    
                    if (profile.role == 2) {
                        ProfileInfoDisplayRow("Dòng xe", profile.carModel ?: "N/A")
                        ProfileInfoDisplayRow("Biển số xe", profile.plateNumber ?: "N/A")
                        ProfileInfoDisplayRow("Đánh giá", "${profile.rating} ⭐")
                    } else {
                        ProfileInfoDisplayRow("Hạng thành viên", "Thành viên Bạc")
                        ProfileInfoDisplayRow("Điểm tích lũy", "500 điểm")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoDisplayRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        HorizontalDivider(modifier = Modifier.padding(top = 10.dp), thickness = 0.8.dp, color = Color(0xFFF0F0F0))
    }
}
