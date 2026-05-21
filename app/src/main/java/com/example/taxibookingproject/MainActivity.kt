package com.example.taxibookingproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.taxibookingproject.controller.AdminController
import com.example.taxibookingproject.controller.AuthController
import com.example.taxibookingproject.controller.BookingController
import com.example.taxibookingproject.controller.LocationManager
import com.example.taxibookingproject.navigation.NavGraph
import com.example.taxibookingproject.ui.theme.TaxiBookingProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TaxiBookingProjectTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    
                    // Khởi tạo các Controller cần thiết
                    val authController = remember { AuthController() }
                    val adminController = remember { AdminController() }
                    val bookingController = remember { BookingController() }
                    val locationManager = remember { LocationManager() }

                    NavGraph(
                        navController = navController,
                        authController = authController,
                        adminController = adminController,
                        bookingController = bookingController,
                        locationManager = locationManager
                    )
                }
            }
        }
    }
}