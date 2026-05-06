package com.example.taxibookingproject.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.taxibookingproject.controller.AuthController
import com.example.taxibookingproject.ui.components.TaxiButton
import com.example.taxibookingproject.ui.components.TaxiTextField

@Composable
fun LoginScreen(
    authController: AuthController,
    onLoginSuccess: (Int) -> Unit, // Nhận role (2: Tài xế, 3: Khách hàng)
    onGoToRegister: () -> Unit,
    onGoToForgot: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Đăng Nhập", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        TaxiTextField(email, { email = it }, "Email")
        TaxiTextField(password, { password = it }, "Mật khẩu")

        TextButton(
            onClick = onGoToForgot,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Quên mật khẩu?")
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (isLoading) {
            // Hiển thị vòng xoay khi đang xử lý đăng nhập
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            TaxiButton("ĐĂNG NHẬP") {
                if (email.isEmpty() || password.isEmpty()) {
                    errorMessage = "Vui lòng nhập đầy đủ thông tin"
                    return@TaxiButton
                }

                isLoading = true
                errorMessage = ""
                
                authController.loginUser(
                    email,
                    password,
                    onSuccess = { role ->
                        isLoading = false
                        onLoginSuccess(role)
                    },
                    onFailure = { error ->
                        isLoading = false
                        errorMessage = error
                    }
                )
            }
        }

        TextButton(onClick = onGoToRegister) {
            Text("Chưa có tài khoản? Đăng ký ngay")
        }
    }
}