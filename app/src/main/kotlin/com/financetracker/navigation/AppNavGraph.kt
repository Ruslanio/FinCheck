package com.financetracker.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.financetracker.feature.auth.ui.LoginScreen
import com.financetracker.feature.auth.ui.RegisterScreen
import com.financetracker.feature.home.ui.HomeScreen

sealed class Destination(val route: String) {
    data object Login : Destination("login")
    data object Register : Destination("register")
    data object Home : Destination("home")
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val startupViewModel: StartupViewModel = hiltViewModel()
    val startDestination =
        if (startupViewModel.hasValidToken()) Destination.Home.route else Destination.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Destination.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Destination.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(Destination.Home.route) {
            HomeScreen(navController = navController)
        }
    }
}
