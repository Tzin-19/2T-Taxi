package com.example.taxibookingproject.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taxibookingproject.controller.AuthController
import com.example.taxibookingproject.ui.components.TaxiButton
import com.example.taxibookingproject.ui.components.TaxiTextField

@Composable
fun ForgotPasswordScreen(
    authController: AuthController,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Quên mật khẩu",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Nhập email của bạn để nhận liên kết đặt lại mật khẩu.",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        TaxiTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email đăng ký"
        )

        // Hiển thị thông báo phản hồi từ Firebase
        if (message.isNotEmpty()) {
            Text(
                text = message,
                color = if (isError) Color.Red else Color(0xFF4CAF50), // Đỏ nếu lỗi, Xanh nếu thành công
                modifier = Modifier.padding(vertical = 12.dp),
                fontSize = 14.sp
            )
        }

        TaxiButton(text = "GỬI YÊU CẦU") {
            authController.resetPassword(
                email = email,
                onSuccess = {
                    isError = false
                    message = "Thành công! Hãy kiểm tra hộp thư đến (hoặc Spam) của bạn."
                },
                onFailure = { error ->
                    isError = true
                    message = error
                }
            )
        }

        TextButton(onClick = onBackToLogin) {
            Text("Quay lại Đăng nhập", fontWeight = FontWeight.Medium)
        }
    }
}