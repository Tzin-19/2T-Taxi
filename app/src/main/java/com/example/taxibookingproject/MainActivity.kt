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
import com.example.taxibookingproject.controller.*
import com.example.taxibookingproject.navigation.NavGraph
import com.example.taxibookingproject.ui.theme.TaxiBookingProjectTheme
import com.google.android.libraries.places.api.Places

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Sử dụng API Key từ manifest hoặc cấu hình
        val apiKey = "AIzaSyB9pjq8i1-2BmiWOdciM2TSQWycyOsqYBY"

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        setContent {
            TaxiBookingProjectTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    
                    val authController = remember { AuthController() }
                    val adminController = remember { AdminController() }
                    val bookingController = remember { BookingController() }
                    val locationManager = remember { LocationManager() }
                    val directionsController = remember { DirectionsController() }

                    NavGraph(
                        navController = navController,
                        authController = authController,
                        adminController = adminController,
                        bookingController = bookingController,
                        locationManager = locationManager,
                        directionsController = directionsController
                    )
                }
            }
        }
    }
}
