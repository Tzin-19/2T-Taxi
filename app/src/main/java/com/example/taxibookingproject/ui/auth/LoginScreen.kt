package com.example.taxibookingproject.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
    var showTestAccountDialog by remember { mutableStateOf(false) }
    var selectedSocialType by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Gradient Vàng sữa -> Trắng sang trọng
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(SoftYellow, Color.White, Color.White)
    )

    if (showTestAccountDialog) {
        AlertDialog(
            onDismissRequest = { showTestAccountDialog = false },
            title = { Text("Đăng nhập bằng $selectedSocialType", fontWeight = FontWeight.Bold, color = Color.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Bạn muốn sử dụng tài khoản thử nghiệm nào để kiểm tra tính năng?", fontSize = 14.sp, color = Color.DarkGray)
                    Button(
                        onClick = {
                            showTestAccountDialog = false
                            isLoading = true
                            val testEmail = "${selectedSocialType.lowercase()}_passenger@2t-taxi.com"
                            val testPass = "123456"
                            authController.loginUser(testEmail, testPass,
                                onSuccess = { role ->
                                    isLoading = false
                                    onLoginSuccess(role)
                                },
                                onFailure = {
                                    authController.registerUser(testEmail, testPass, "$selectedSocialType Passenger", "0900000001", 3,
                                        onSuccess = {
                                            authController.loginUser(testEmail, testPass, { r -> isLoading = false; onLoginSuccess(r) }, { e -> isLoading = false; errorMessage = e })
                                        },
                                        onFailure = { e -> isLoading = false; errorMessage = e }
                                    )
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepYellow, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tài khoản Khách hàng", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            showTestAccountDialog = false
                            isLoading = true
                            val testEmail = "${selectedSocialType.lowercase()}_driver@2t-taxi.com"
                            val testPass = "123456"
                            authController.loginUser(testEmail, testPass,
                                onSuccess = { role ->
                                    isLoading = false
                                    onLoginSuccess(role)
                                },
                                onFailure = {
                                    authController.registerUser(testEmail, testPass, "$selectedSocialType Driver", "0900000002", 2,
                                        onSuccess = {
                                            authController.loginUser(testEmail, testPass, { r -> isLoading = false; onLoginSuccess(r) }, { e -> isLoading = false; errorMessage = e })
                                        },
                                        onFailure = { e -> isLoading = false; errorMessage = e }
                                    )
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tài khoản Đối tác Tài xế", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Lưu ý: Để sử dụng đăng nhập thực tế của $selectedSocialType, nhà phát triển cần cấu hình SHA-1 fingerprint và Client ID trên Firebase Console.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTestAccountDialog = false }) {
                    Text("ĐÓNG", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(gradientBrush)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))
            
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically()
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = DeepYellow, fontWeight = FontWeight.Black)) {
                            append("2T")
                        }
                        withStyle(style = SpanStyle(color = Color.Black, fontWeight = FontWeight.Black)) {
                            append("-Taxi")
                        }
                    },
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

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
                val input = email.trim()
                if (input.isEmpty() || password.isEmpty()) {
                    errorMessage = "📍 Bạn chưa nhập đầy đủ thông tin kìa!"
                    return@TaxiButton
                }

                if (input.contains("@")) {
                    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
                    if (!input.matches(emailPattern.toRegex())) {
                        errorMessage = "📍 Định dạng email chưa chính xác nhé."
                        return@TaxiButton
                    }
                } else {
                    val isNumeric = input.all { it.isDigit() }
                    if (!isNumeric || input.length < 9 || input.length > 11) {
                        errorMessage = "📍 Số điện thoại phải là dãy số từ 9 - 11 chữ số."
                        return@TaxiButton
                    }
                }

                isLoading = true
                authController.loginUser(input, password,
                    onSuccess = { role ->
                        isLoading = false
                        onLoginSuccess(role)
                    },
                    onFailure = { 
                        isLoading = false
                        errorMessage = "📍 Lỗi đăng nhập: $it"
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
                    SocialLoginButton(text = "Google", icon = Icons.Default.GTranslate) {
                        selectedSocialType = "Google"
                        showTestAccountDialog = true
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    SocialLoginButton(text = "Facebook", icon = Icons.Default.Facebook) {
                        selectedSocialType = "Facebook"
                        showTestAccountDialog = true
                    }
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