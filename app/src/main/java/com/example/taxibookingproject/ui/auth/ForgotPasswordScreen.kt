package com.example.taxibookingproject.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
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
import com.example.taxibookingproject.ui.components.TaxiButton
import com.example.taxibookingproject.ui.components.TaxiTextField
import com.example.taxibookingproject.ui.theme.DeepYellow
import com.example.taxibookingproject.ui.theme.SoftYellow

@Composable
fun ForgotPasswordScreen(
    authController: AuthController,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(SoftYellow, Color.White, Color.White)
    )

    Box(modifier = Modifier.fillMaxSize().background(gradientBrush)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.Start
        ) {
            IconButton(
                onClick = onBackToLogin,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.Black)
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Quên mật khẩu? 🔑",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )
            Text(
                text = "Đừng lo lắng! Nhập email của bạn và tụi mình sẽ gửi hướng dẫn khôi phục ngay.",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            TaxiTextField(
                value = email,
                onValueChange = { email = it; statusMessage = "" },
                label = "Email đăng ký của bạn",
                leadingIcon = Icons.Default.Email
            )

            if (statusMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isError) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    ),
                    modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = statusMessage,
                        color = if (isError) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            TaxiButton(
                text = "GỬI YÊU CẦU KHÔI PHỤC",
                isLoading = isLoading,
                containerColor = DeepYellow
            ) {
                if (email.isEmpty()) {
                    isError = true
                    statusMessage = "📍 Úi, bạn quên nhập email kìa!"
                    return@TaxiButton
                }

                isLoading = true
                authController.resetPassword(
                    email = email,
                    onSuccess = {
                        isLoading = false
                        isError = false
                        statusMessage = "✨ Xong rồi! Bạn kiểm tra email để đặt lại mật khẩu nhé."
                    },
                    onFailure = { error ->
                        isLoading = false
                        isError = true
                        statusMessage = "📍 Lỗi rồi: $error"
                    }
                )
            }
        }
    }
}