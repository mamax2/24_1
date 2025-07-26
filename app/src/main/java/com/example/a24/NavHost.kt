package com.example.a24

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.a24.ui.screens.ActivityDetailScreen
import com.example.a24.ui.screens.HomeScreen
import com.example.a24.ui.screens.LoginScreen
import com.example.a24.ui.screens.NotificationScreen
import com.example.a24.ui.screens.ProfileScreen
import com.example.a24.ui.screens.SignupScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController)
        }

        composable("home") {
            HomeScreen(navController)
        }

        composable("signup") {
            SignupScreen(navController)
        }

        composable("profile") {
            ProfileScreen(navController)
        }

        composable("notifications") {
            NotificationScreen(navController)
        }

        composable("activity_detail/{activityId}") { backStackEntry ->
            val activityId = backStackEntry.arguments?.getString("activityId") ?: ""
            ActivityDetailScreen(navController, activityId)
        }
    }
}