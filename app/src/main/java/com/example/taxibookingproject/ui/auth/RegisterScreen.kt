package com.example.taxibookingproject.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taxibookingproject.controller.AuthController
import com.example.taxibookingproject.ui.components.ErrorText
import com.example.taxibookingproject.ui.components.TaxiButton
import com.example.taxibookingproject.ui.components.TaxiTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authController: AuthController,
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableIntStateOf(3) } // 2: Tài xế, 3: Khách
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var agreeTerms by remember { mutableStateOf(false) }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFE8F5E9), Color.White)
    )

    Box(modifier = Modifier.fillMaxSize().background(gradientBrush)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = "Gia nhập cộng đồng ✨",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = "Chỉ mất 1 phút để bắt đầu hành trình mới",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            TaxiTextField(
                value = fullName,
                onValueChange = { fullName = it; errorMessage = "" },
                label = "Họ và tên của bạn",
                leadingIcon = Icons.Default.Person
            )

            TaxiTextField(
                value = phone,
                onValueChange = { phone = it; errorMessage = "" },
                label = "Số điện thoại",
                leadingIcon = Icons.Default.Phone,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
            )

            TaxiTextField(
                value = email,
                onValueChange = { email = it; errorMessage = "" },
                label = "Email",
                leadingIcon = Icons.Default.Email,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
            )

            TaxiTextField(
                value = password,
                onValueChange = { password = it; errorMessage = "" },
                label = "Mật khẩu (6+ ký tự)",
                leadingIcon = Icons.Default.Lock,
                isPassword = true
            )

            TaxiTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = "" },
                label = "Xác nhận mật khẩu",
                leadingIcon = Icons.Default.LockReset,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Bạn đăng ký với vai trò:", fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                FilterChip(
                    selected = selectedRole == 3,
                    onClick = { selectedRole = 3 },
                    label = { Text("Khách hàng") },
                    leadingIcon = if (selectedRole == 3) { { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) } } else null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = selectedRole == 2,
                    onClick = { selectedRole = 2 },
                    label = { Text("Tài xế") },
                    leadingIcon = if (selectedRole == 2) { { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) } } else null
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = agreeTerms, onCheckedChange = { agreeTerms = it })
                Text(
                    text = "Tôi đồng ý với Điều khoản và Chính sách",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }

            ErrorText(errorMessage)

            Spacer(modifier = Modifier.height(24.dp))

            TaxiButton(
                text = "ĐĂNG KÝ NGAY",
                isLoading = isLoading,
                containerColor = Color(0xFF2E7D32)
            ) {
                when {
                    fullName.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() -> {
                        errorMessage = "📍 Bạn ơi, đừng bỏ trống ô nào nhé!"
                    }
                    password != confirmPassword -> {
                        errorMessage = "📍 Mật khẩu xác nhận không khớp rồi bạn."
                    }
                    password.length < 6 -> {
                        errorMessage = "📍 Mật khẩu phải từ 6 ký tự để bảo mật nhé."
                    }
                    !agreeTerms -> {
                        errorMessage = "📍 Bạn cần đồng ý với điều khoản để tiếp tục nè."
                    }
                    else -> {
                        isLoading = true
                        authController.registerUser(email, password, fullName, phone, selectedRole,
                            onSuccess = { 
                                isLoading = false
                                onRegisterSuccess() 
                            },
                            onFailure = { 
                                isLoading = false
                                errorMessage = "📍 Lỗi: $it"
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 40.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Đã có tài khoản?", color = Color.Gray)
                TextButton(onClick = onBackToLogin) {
                    Text("Đăng nhập", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}