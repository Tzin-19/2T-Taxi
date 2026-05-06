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
fun LoginScreen(
    authController: AuthController,
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit,
    onGoToForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Taxi App", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text = "Đăng nhập để bắt đầu hành trình", fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(40.dp))

        TaxiTextField(value = email, onValueChange = { email = it }, label = "Email")
        TaxiTextField(value = password, onValueChange = { password = it }, label = "Mật khẩu")

        // Nút Quên mật khẩu nằm bên phải
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = onGoToForgotPassword) {
                Text("Quên mật khẩu?", color = MaterialTheme.colorScheme.primary)
            }
        }

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red, modifier = Modifier.padding(vertical = 8.dp))
        }

        TaxiButton(text = "ĐĂNG NHẬP") {
            authController.loginUser(email, password,
                onSuccess = { onLoginSuccess() },
                onFailure = { errorMessage = it }
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Chưa có tài khoản?")
            TextButton(onClick = onGoToRegister) {
                Text("Đăng ký ngay", fontWeight = FontWeight.Bold)
            }
        }
    }
}