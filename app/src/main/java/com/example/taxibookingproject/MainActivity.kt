package com.example.taxibookingproject

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.taxibookingproject.controller.AuthController
import com.example.taxibookingproject.ui.auth.ForgotPasswordScreen
import com.example.taxibookingproject.ui.auth.LoginScreen
import com.example.taxibookingproject.ui.auth.RegisterScreen
import com.example.taxibookingproject.ui.theme.TaxiBookingProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Bật tính năng hiển thị tràn viền cho đẹp
        enableEdgeToEdge()

        setContent {
            TaxiBookingProjectTheme {
                // Surface là nền tảng của giao diện
                Surface(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current

                    // Khởi tạo bộ não xử lý Auth
                    val authController = remember { AuthController() }

                    // Biến trạng thái để biết đang ở màn hình nào (Tạm thời thay cho Navigation)
                    var currentScreen by remember { mutableStateOf("LOGIN") }

                    when (currentScreen) {
                        "LOGIN" -> LoginScreen(
                            authController = authController,
                            onLoginSuccess = {
                                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                // Sau này Ngày 5 mình sẽ chuyển sang màn hình Map ở đây
                            },
                            onGoToRegister = { currentScreen = "REGISTER" },
                            onGoToForgotPassword = { currentScreen = "FORGOT" }
                        )

                        "REGISTER" -> RegisterScreen(
                            authController = authController,
                            onRegisterSuccess = {
                                Toast.makeText(context, "Đăng ký thành công! Mời bạn đăng nhập.", Toast.LENGTH_LONG).show()
                                currentScreen = "LOGIN"
                            },
                            onBackToLogin = { currentScreen = "LOGIN" }
                        )

                        "FORGOT" -> ForgotPasswordScreen(
                            authController = authController,
                            onBackToLogin = { currentScreen = "LOGIN" }
                        )
                    }
                }
            }
        }
    }
}