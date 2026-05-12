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
    var rememberMe by remember { mutableStateOf(true) }

    // Màu gradient cam vàng năng động cho GenZ
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFE082), Color.White, Color.White)
    )

    Box(modifier = Modifier.fillMaxSize().background(gradientBrush)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically()
            ) {
                Column {
                    Text(
                        text = "Mừng bạn trở lại! ✨",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "Đăng nhập để bắt đầu hành trình của bạn ngay nào.",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            TaxiTextField(
                value = email,
                onValueChange = { email = it; errorMessage = "" },
                label = "Email của bạn",
                leadingIcon = Icons.Default.Email
            )

            TaxiTextField(
                value = password,
                onValueChange = { password = it; errorMessage = "" },
                label = "Mật khẩu",
                leadingIcon = Icons.Default.Lock,
                isPassword = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text("Ghi nhớ nhé", fontSize = 14.sp, color = Color.DarkGray)
                }
                TextButton(onClick = onGoToForgot) {
                    Text("Quên mật khẩu?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            ErrorText(errorMessage)

            Spacer(modifier = Modifier.height(16.dp))

            TaxiButton(
                text = "ĐĂNG NHẬP",
                isLoading = isLoading
            ) {
                if (email.isEmpty() || password.isEmpty()) {
                    errorMessage = "📍 Úi, bạn chưa nhập email hoặc mật khẩu kìa!"
                    return@TaxiButton
                }
                
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    errorMessage = "📍 Email này trông hơi lạ, bạn kiểm tra lại nhé."
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
                        errorMessage = when {
                            it.contains("user-not-found") -> "📍 Tài khoản này chưa đăng ký bạn ơi."
                            it.contains("wrong-password") -> "📍 Mật khẩu chưa đúng rồi, thử xem lại nhé!"
                            else -> "📍 Có lỗi nhỏ nè: $it"
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Social Login - Phổ biến ở các app hiện đại
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE))
                Text("hoặc đăng nhập bằng", modifier = Modifier.padding(horizontal = 16.dp), fontSize = 12.sp, color = Color.Gray)
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    SocialLoginButton(text = "Google", icon = Icons.Default.GTranslate) { /* Xử lý sau */ }
                }
                Box(modifier = Modifier.weight(1f)) {
                    SocialLoginButton(text = "Facebook", icon = Icons.Default.Facebook) { /* Xử lý sau */ }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bạn là người mới?", color = Color.Gray)
                TextButton(onClick = onGoToRegister) {
                    Text("Đăng ký ngay thôi", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}