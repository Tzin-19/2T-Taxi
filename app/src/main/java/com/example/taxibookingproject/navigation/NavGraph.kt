package com.example.taxibookingproject.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.taxibookingproject.controller.*
import com.example.taxibookingproject.ui.admin.AdminDashboardScreen
import com.example.taxibookingproject.ui.auth.ForgotPasswordScreen
import com.example.taxibookingproject.ui.auth.LoginScreen
import com.example.taxibookingproject.ui.auth.RegisterScreen
import com.example.taxibookingproject.ui.driver.*
import com.example.taxibookingproject.ui.passenger.*
import com.example.taxibookingproject.ui.common.ViewProfileScreen
import com.example.taxibookingproject.ui.common.ChatScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    authController: AuthController,
    adminController: AdminController,
    bookingController: BookingController,
    locationManager: LocationManager,
    directionsController: DirectionsController
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
                locationManager = locationManager,
                onLogout = {
                    authController.logout()
                    navController.navigate(Screen.Login.route) { 
                        popUpTo(0) { inclusive = true } 
                    }
                },
                onNavigateToBooking = { type -> 
                    navController.navigate(Screen.Booking.createRoute(type)) 
                },
                onNavigateToProfile = { navController.navigate(Screen.PassengerProfile.route) },
                onNavigateToPaymentHistory = { navController.navigate(Screen.PaymentHistory.route) },
                onNavigateToFavoritePlaces = { navController.navigate(Screen.FavoritePlaces.route) },
                onNavigateToSearchHistory = { navController.navigate(Screen.SearchHistory.route) },
                onNavigateToSpendingStatistics = { navController.navigate(Screen.SpendingStatistics.route) },
                onNavigateToNotifications = { navController.navigate(Screen.PassengerNotifications.route) }
            )
        }
        composable(
            route = Screen.Booking.route,
            arguments = listOf(navArgument("type") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type")
            BookingScreen(
                bookingController = bookingController,
                authController = authController,
                directionsController = directionsController,
                locationType = type,
                onBack = { navController.popBackStack() },
                onConfirmBooking = { tripId -> 
                    navController.navigate("tracking/$tripId") 
                },
                onNavigateToChat = { tripId ->
                    navController.navigate(Screen.Chat.createRoute(tripId))
                }
            )
        }
        composable(Screen.PaymentHistory.route) {
            PaymentHistoryScreen(
                authController = authController,
                bookingController = bookingController,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.FavoritePlaces.route) {
            FavoritePlacesScreen(
                authController = authController,
                bookingController = bookingController,
                onBack = { navController.popBackStack() },
                onQuickBook = { placeId ->
                    navController.navigate(Screen.Booking.createRoute("fav_$placeId"))
                }
            )
        }
        composable(Screen.SearchHistory.route) {
            SearchHistoryScreen(
                authController = authController,
                bookingController = bookingController,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.SpendingStatistics.route) {
            SpendingStatisticsScreen(
                authController = authController,
                bookingController = bookingController,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "tracking/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            TrackingScreen(
                tripId = tripId,
                bookingController = bookingController,
                locationManager = locationManager,
                authController = authController,
                directionsController = directionsController,
                onCancelTrip = { navController.popBackStack() },
                onArrived = { navController.navigate(Screen.Rating.route) },
                onViewDriverProfile = { driverId ->
                    navController.navigate(Screen.ViewProfile.createRoute(driverId))
                },
                onNavigateToChat = { tId ->
                    navController.navigate(Screen.Chat.createRoute(tId))
                }
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
        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            ChatScreen(
                tripId = tripId,
                bookingController = bookingController,
                authController = authController,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.PassengerNotifications.route) {
            PassengerNotificationsScreen(
                bookingController = bookingController,
                authController = authController,
                onBack = { navController.popBackStack() },
                onNavigateToChat = { tripId ->
                    navController.navigate(Screen.Chat.createRoute(tripId))
                }
            )
        }

        // --- DRIVER ---
        composable(Screen.DriverHome.route) {
            DriverHomeScreen(
                authController = authController,
                locationManager = locationManager,
                bookingController = bookingController,
                onLogout = {
                    authController.logout()
                    navController.navigate(Screen.Login.route) { 
                        popUpTo(0) { inclusive = true } 
                    }
                },
                onNavigateToEarnings = { navController.navigate(Screen.Earnings.route) },
                onNewRequest = { tripId -> 
                    navController.navigate("incoming_request/$tripId") 
                },
                onNavigateToProfile = { navController.navigate(Screen.DriverProfile.route) },
                onNavigateToHistory = { navController.navigate(Screen.DriverHistory.route) },
                onNavigateToNotifications = { navController.navigate(Screen.DriverNotifications.route) },
                onNavigateToReviews = { navController.navigate(Screen.DriverReviews.route) },
                onNavigateToChat = { tripId ->
                    navController.navigate(Screen.Chat.createRoute(tripId))
                }
            )
        }
        composable(Screen.DriverHistory.route) {
            DriverHistoryScreen(
                authController = authController,
                bookingController = bookingController,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.DriverNotifications.route) {
            DriverNotificationsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.DriverReviews.route) {
            DriverReviewsScreen(
                authController = authController,
                bookingController = bookingController,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "incoming_request/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            IncomingRequestScreen(
                tripId = tripId,
                bookingController = bookingController,
                authController = authController,
                directionsController = directionsController,
                onAccept = { navController.navigate("navigation/$tripId") },
                onDecline = { navController.popBackStack() },
                onViewPassengerProfile = { passengerId ->
                    navController.navigate(Screen.ViewProfile.createRoute(passengerId))
                }
            )
        }
        composable(
            route = "navigation/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            NavigationScreen(
                tripId = tripId,
                bookingController = bookingController,
                locationManager = locationManager,
                authController = authController,
                directionsController = directionsController,
                onFinishTrip = { 
                    navController.navigate(Screen.DriverHome.route) {
                        popUpTo(Screen.DriverHome.route) { inclusive = true }
                    }
                },
                onViewPassengerProfile = { passengerId ->
                    navController.navigate(Screen.ViewProfile.createRoute(passengerId))
                },
                onNavigateToChat = { tId ->
                    navController.navigate(Screen.Chat.createRoute(tId))
                }
            )
        }
        composable(Screen.Earnings.route) {
            EarningsScreen(
                authController = authController,
                bookingController = bookingController,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.DriverProfile.route) {
            DriverProfileScreen(
                authController = authController,
                onBack = { navController.popBackStack() }
            )
        }

        // --- COMMON VIEW PROFILE ---
        composable(
            route = Screen.ViewProfile.route,
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            ViewProfileScreen(
                uid = uid,
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
