package com.financetracker.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.financetracker.feature.auth.navigation.AuthGraph
import com.financetracker.feature.auth.navigation.authGraph
import com.financetracker.feature.auth.navigation.navigateToAuth
import com.financetracker.feature.home.navigation.HomeGraph
import com.financetracker.feature.home.navigation.homeGraph
import com.financetracker.feature.home.navigation.navigateToHome
import com.financetracker.feature.transactions.navigation.navigateToTransaction
import com.financetracker.feature.transactions.navigation.transactionScreen


@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val startupViewModel: StartupViewModel = hiltViewModel()
    val startDestination =
        if (startupViewModel.isUserLoggedIn()) HomeGraph else AuthGraph

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
            navController = navController,
            navigateToHome = { navController.navigateToHome() },
            onBackClick = { navController.popBackStack() }
        )
        homeGraph(
            navigateToTransaction = { navController.navigateToTransaction() },
            navigateToAuth = { navController.navigateToAuth() }
        )
        transactionScreen()
    }
}
