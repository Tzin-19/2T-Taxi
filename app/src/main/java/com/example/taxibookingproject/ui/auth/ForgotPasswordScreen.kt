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
import com.example.taxibookingproject.ui.components.ErrorText
import com.example.taxibookingproject.ui.components.TaxiButton
import com.example.taxibookingproject.ui.components.TaxiTextField

@OptIn(ExperimentalMaterial3Api::class)
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
        colors = listOf(Color(0xFFE3F2FD), Color.White)
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
                modifier = Modifier.padding(top = 16.dp, start = 0.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Quên mật khẩu? 🔑",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.tertiary
            )
            Text(
                text = "Đừng lo, chỉ cần nhập email đăng ký, tụi mình sẽ gửi mã khôi phục cho bạn ngay!",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            TaxiTextField(
                value = email,
                onValueChange = { email = it; statusMessage = "" },
                label = "Email của bạn",
                leadingIcon = Icons.Default.Email,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
            )

            if (statusMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isError) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    ),
                    modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusMessage,
                        color = if (isError) Color.Red else Color(0xFF2E7D32),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TaxiButton(
                text = "GỬI YÊU CẦU KHÔI PHỤC",
                isLoading = isLoading,
                containerColor = MaterialTheme.colorScheme.tertiary
            ) {
                if (email.isEmpty()) {
                    isError = true
                    statusMessage = "📍 Bạn nhập thiếu email mất rồi!"
                    return@TaxiButton
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    isError = true
                    statusMessage = "📍 Email có vẻ sai sai, bạn kiểm tra lại nhé."
                    return@TaxiButton
                }

                isLoading = true
                authController.resetPassword(
                    email = email,
                    onSuccess = {
                        isLoading = false
                        isError = false
                        statusMessage = "✨ Tuyệt vời! Bạn kiểm tra email để đặt lại mật khẩu nhé. (Đừng quên check cả mục Spam nha!)"
                    },
                    onFailure = { error ->
                        isLoading = false
                        isError = true
                        statusMessage = when {
                            error.contains("user-not-found") -> "📍 Email này chưa có trên hệ thống tụi mình."
                            else -> "📍 Có lỗi nhỏ xảy ra: $error"
                        }
                    }
                )
            }
        }
    }
}