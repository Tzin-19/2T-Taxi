package com.example.taxibookingproject.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.taxibookingproject.controller.*
import com.example.taxibookingproject.ui.admin.AdminDashboardScreen
import com.example.taxibookingproject.ui.auth.ForgotPasswordScreen
import com.example.taxibookingproject.ui.auth.LoginScreen
import com.example.taxibookingproject.ui.auth.RegisterScreen
import com.example.taxibookingproject.ui.driver.*
import com.example.taxibookingproject.ui.passenger.*

@Composable
fun NavGraph(
    navController: NavHostController,
    authController: AuthController,
    adminController: AdminController,
    bookingController: BookingController,
    locationManager: LocationManager
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // --- AUTH ---
        composable(Screen.Login.route) {
            LoginScreen(
                authController = authController,
                onLoginSuccess = { role ->
                    val destination = when (role) {
                        1 -> Screen.AdminDashboard.route
                        2 -> Screen.DriverHome.route
                        else -> Screen.PassengerHome.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onGoToRegister = { navController.navigate(Screen.Register.route) },
                onGoToForgot = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                authController = authController,
                onRegisterSuccess = { navController.popBackStack() },
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                authController = authController,
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // --- PASSENGER ---
        composable(Screen.PassengerHome.route) {
            PassengerHomeScreen(
                authController = authController,
                onLogout = {
                    authController.logout()
                    navController.navigate(Screen.Login.route) { 
                        popUpTo(0) { inclusive = true } 
                    }
                },
                onNavigateToBooking = { navController.navigate(Screen.Booking.route) },
                onNavigateToProfile = { navController.navigate(Screen.PassengerProfile.route) }
            )
        }
        composable(Screen.Booking.route) {
            BookingScreen(
                onBack = { navController.popBackStack() },
                onConfirmBooking = { navController.navigate(Screen.Tracking.route) }
            )
        }
        composable(Screen.Tracking.route) {
            TrackingScreen(
                onCancelTrip = { navController.popBackStack() },
                onArrived = { navController.navigate(Screen.Rating.route) }
            )
        }
        composable(Screen.Rating.route) {
            RatingScreen(onFinish = { 
                navController.navigate(Screen.PassengerHome.route) {
                    popUpTo(Screen.PassengerHome.route) { inclusive = true }
                }
            })
        }
        composable(Screen.PassengerProfile.route) {
            PassengerProfileScreen(
                authController = authController,
                onBack = { navController.popBackStack() }
            )
        }

        // --- DRIVER ---
        composable(Screen.DriverHome.route) {
            DriverHomeScreen(
                authController = authController,
                onLogout = {
                    authController.logout()
                    navController.navigate(Screen.Login.route) { 
                        popUpTo(0) { inclusive = true } 
                    }
                },
                onNavigateToEarnings = { navController.navigate(Screen.Earnings.route) },
                onNewRequest = { navController.navigate(Screen.IncomingRequest.route) },
                onNavigateToProfile = { navController.navigate(Screen.DriverProfile.route) }
            )
        }
        composable(Screen.IncomingRequest.route) {
            IncomingRequestScreen(
                onAccept = { navController.navigate(Screen.Navigation.route) },
                onDecline = { navController.popBackStack() }
            )
        }
        composable(Screen.Navigation.route) {
            NavigationScreen(onFinishTrip = { 
                navController.navigate(Screen.DriverHome.route) {
                    popUpTo(Screen.DriverHome.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Earnings.route) {
            EarningsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.DriverProfile.route) {
            DriverProfileScreen(
                authController = authController,
                onBack = { navController.popBackStack() }
            )
        }

        // --- ADMIN ---
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                adminController = adminController,
                onLogout = {
                    authController.logout()
                    navController.navigate(Screen.Login.route) { 
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
