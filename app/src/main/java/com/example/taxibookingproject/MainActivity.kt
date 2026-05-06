package com.example.taxibookingproject

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.taxibookingproject.controller.AuthController
import com.example.taxibookingproject.ui.auth.ForgotPasswordScreen
import com.example.taxibookingproject.ui.auth.LoginScreen
import com.example.taxibookingproject.ui.auth.RegisterScreen
import com.example.taxibookingproject.ui.driver.DriverHomeScreen
import com.example.taxibookingproject.ui.passenger.PassengerHomeScreen
import com.example.taxibookingproject.ui.theme.TaxiBookingProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TaxiBookingProjectTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current
                    val authController = remember { AuthController() }
                    
                    // Quản lý màn hình hiện tại
                    var currentScreen by remember { mutableStateOf("LOGIN") }

                    when (currentScreen) {
                        "LOGIN" -> LoginScreen(
                            authController = authController,
                            onLoginSuccess = { role ->
                                when (role) {
                                    2 -> currentScreen = "DRIVER_HOME"
                                    3 -> currentScreen = "PASSENGER_HOME"
                                    1 -> Toast.makeText(context, "Admin chưa có giao diện!", Toast.LENGTH_SHORT).show()
                                    else -> Toast.makeText(context, "Lỗi phân quyền!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onGoToRegister = { currentScreen = "REGISTER" },
                            onGoToForgot = { currentScreen = "FORGOT" }
                        )

                        "REGISTER" -> RegisterScreen(
                            authController = authController,
                            onRegisterSuccess = {
                                Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_LONG).show()
                                currentScreen = "LOGIN"
                            },
                            onBackToLogin = { currentScreen = "LOGIN" }
                        )

                        "FORGOT" -> ForgotPasswordScreen(
                            authController = authController,
                            onBackToLogin = { currentScreen = "LOGIN" }
                        )

                        "DRIVER_HOME" -> DriverHomeScreen(
                            onLogout = {
                                authController.logout()
                                currentScreen = "LOGIN"
                            }
                        )

                        "PASSENGER_HOME" -> PassengerHomeScreen(
                            onLogout = {
                                authController.logout()
                                currentScreen = "LOGIN"
                            }
                        )
                    }
                }
            }
        }
    }
}