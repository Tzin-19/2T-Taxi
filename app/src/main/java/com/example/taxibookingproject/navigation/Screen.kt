package com.example.taxibookingproject.navigation

sealed class Screen(val route: String) {
    // Auth screens
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")

    // Passenger screens
    object PassengerHome : Screen("passenger_home")
    object Booking : Screen("booking?type={type}") {
        fun createRoute(type: String?) = if (type != null) "booking?type=$type" else "booking"
    }
    object Tracking : Screen("tracking")
    object Rating : Screen("rating")
    object PassengerProfile : Screen("passenger_profile")
    object PaymentHistory : Screen("payment_history")
    object FavoritePlaces : Screen("favorite_places")
    object SearchHistory : Screen("search_history")
    object SpendingStatistics : Screen("spending_statistics")

    // Driver screens
    object DriverHome : Screen("driver_home")
    object IncomingRequest : Screen("incoming_request")
    object Navigation : Screen("navigation")
    object Earnings : Screen("earnings")
    object DriverProfile : Screen("driver_profile")
    object DriverHistory : Screen("driver_history")
    object DriverNotifications : Screen("driver_notifications")
    object DriverReviews : Screen("driver_reviews")

    // Common
    object ViewProfile : Screen("view_profile/{uid}") {
        fun createRoute(uid: String) = "view_profile/$uid"
    }

    // Chat & notifications
    object Chat : Screen("chat/{tripId}") {
        fun createRoute(tripId: String) = "chat/$tripId"
    }
    object PassengerNotifications : Screen("passenger_notifications")

    // Admin screens
    object AdminDashboard : Screen("admin_dashboard")
}