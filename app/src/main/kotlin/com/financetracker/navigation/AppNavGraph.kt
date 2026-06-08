package com.financetracker.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.financetracker.feature.auth.ui.LoginScreen

sealed class Destination(val route: String) {
    data object Login : Destination("login")
    data object Home : Destination("home")
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Destination.Home.route,
    ) {
        composable(Destination.Login.route) {
            LoginScreen()
        }
        composable(Destination.Home.route) {
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}
