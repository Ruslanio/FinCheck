package com.financetracker.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.financetracker.auth.navigation.AuthGraph
import com.financetracker.auth.navigation.authGraph
import com.financetracker.auth.navigation.navigateToAuth
import com.financetracker.auth.navigation.navigateToLogin
import com.financetracker.auth.navigation.navigateToRegister
import com.financetracker.ui.StartupViewModel
import com.financetracker.ui.main.MainScreen
import com.financetracker.ui.main.navigation.MainGraph
import com.financetracker.ui.main.navigation.navigateToMain


@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val startupViewModel: StartupViewModel = hiltViewModel()
    val startDestination =
        if (startupViewModel.isUserLoggedIn()) MainGraph else AuthGraph

    FinanceCheckNavHost(
        navController = navController,
        startDestination = startDestination
    )
}

@Composable
fun FinanceCheckNavHost(
    navController: NavHostController,
    startDestination: Any
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        authGraph(
            navigateToRegister = { navController.navigateToRegister() },
            navigateToLogin = { navController.navigateToLogin(it) },
            navigateToHome = { navController.navigateToMain() },
            onBackClick = { navController.popBackStack() }
        )
        composable<MainGraph> {
            MainScreen(
                navigateToAuth = { navController.navigateToAuth() }
            )
        }
    }
}
