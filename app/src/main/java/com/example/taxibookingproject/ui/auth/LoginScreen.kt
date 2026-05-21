package com.example.taxibookingproject.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.GTranslate
import androidx.compose.material.icons.filled.Lock
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
import com.example.taxibookingproject.ui.components.SocialLoginButton
import com.example.taxibookingproject.ui.components.TaxiButton
import com.example.taxibookingproject.ui.components.TaxiTextField
import com.example.taxibookingproject.ui.theme.DeepYellow
import com.example.taxibookingproject.ui.theme.SoftYellow

@Composable
fun LoginScreen(
    authController: AuthController,
    onLoginSuccess: (Int) -> Unit,
    onGoToRegister: () -> Unit,
    onGoToForgot: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Gradient Vàng sữa -> Trắng sang trọng
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(SoftYellow, Color.White, Color.White)
    )

    Box(modifier = Modifier.fillMaxSize().background(gradientBrush)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(70.dp))
            
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically()
            ) {
                Column {
                    Text(
                        text = "Chào mừng bạn! 👋",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                    Text(
                        text = "Đăng nhập để trải nghiệm dịch vụ Taxi 5 sao cùng chúng tôi",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            TaxiTextField(
                value = email,
                onValueChange = { email = it; errorMessage = "" },
                label = "Email hoặc Số điện thoại",
                leadingIcon = Icons.Default.Email
            )

            TaxiTextField(
                value = password,
                onValueChange = { password = it; errorMessage = "" },
                label = "Mật khẩu",
                leadingIcon = Icons.Default.Lock,
                isPassword = true
            )

            TextButton(
                onClick = onGoToForgot,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Quên mật khẩu?", fontWeight = FontWeight.Bold, color = Color.Black)
            }

            ErrorText(errorMessage)

            Spacer(modifier = Modifier.height(24.dp))

            TaxiButton(
                text = "ĐĂNG NHẬP",
                isLoading = isLoading,
                containerColor = DeepYellow
            ) {
                if (email.isEmpty() || password.isEmpty()) {
                    errorMessage = "📍 Bạn chưa nhập đầy đủ thông tin kìa!"
                    return@TaxiButton
                }

                isLoading = true
                authController.loginUser(email, password,
                    onSuccess = { role ->
                        isLoading = false
                        onLoginSuccess(role)
                    },
                    onFailure = { 
                        isLoading = false
                        errorMessage = "📍 Thông tin đăng nhập chưa đúng, thử lại nhé!"
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE))
                Text("hoặc", modifier = Modifier.padding(horizontal = 16.dp), fontSize = 12.sp, color = Color.Gray)
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    SocialLoginButton(text = "Google", icon = Icons.Default.GTranslate) { }
                }
                Box(modifier = Modifier.weight(1f)) {
                    SocialLoginButton(text = "Facebook", icon = Icons.Default.Facebook) { }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Chưa có tài khoản?", color = Color.Gray)
                TextButton(onClick = onGoToRegister) {
                    Text("Đăng ký ngay", fontWeight = FontWeight.Black, color = Color.Black)
                }
            }
        }
    }
}