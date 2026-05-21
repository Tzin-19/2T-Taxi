package com.example.taxibookingproject.navigation

sealed class Screen(val route: String) {
    // Auth screens
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")

    // Passenger screens
    object PassengerHome : Screen("passenger_home")
    object Booking : Screen("booking")
    object Tracking : Screen("tracking")
    object Rating : Screen("rating")
    object PassengerProfile : Screen("passenger_profile")

    // Driver screens
    object DriverHome : Screen("driver_home")
    object IncomingRequest : Screen("incoming_request")
    object Navigation : Screen("navigation")
    object Earnings : Screen("earnings")
    object DriverProfile : Screen("driver_profile")

    // Admin screens
    object AdminDashboard : Screen("admin_dashboard")
}