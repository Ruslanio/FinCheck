package com.financetracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.financetracker.feature.applock.BiometricState
import com.financetracker.feature.applock.navigation.AppLockRoute
import com.financetracker.feature.applock.navigation.appLockScreen
import com.financetracker.feature.applock.navigation.navigateToAppLock
import com.financetracker.ui.StartupViewModel
import com.financetracker.ui.main.MainScreen
import com.financetracker.ui.main.navigation.MainGraph
import com.financetracker.ui.main.navigation.navigateToMain

@Composable
fun AppNavGraph(
    lockState: BiometricState,
    onRetryAuth: () -> Unit,
    onLockedNavigate: () -> Unit,
) {
    val navController = rememberNavController()
    val startupViewModel: StartupViewModel = hiltViewModel()
    val startDestination =
        if (startupViewModel.isUserLoggedIn()) MainGraph else AuthGraph

    LaunchedEffect(lockState) {
        when (lockState) {
            BiometricState.Authenticating -> navController.navigateToAppLock()
            BiometricState.Authenticated -> navController.popBackStack(AppLockRoute, inclusive = true)
            BiometricState.Locked -> {
                navController.navigateToAuth()
                onLockedNavigate()
            }
            else -> {}
        }
    }

    FinanceCheckNavHost(
        navController = navController,
        startDestination = startDestination,
        lockState = lockState,
        onRetryAuth = onRetryAuth,
    )
}

@Composable
fun FinanceCheckNavHost(
    navController: NavHostController,
    startDestination: Any,
    lockState: BiometricState,
    onRetryAuth: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        authGraph(
            navigateToRegister = { navController.navigateToRegister() },
            navigateToLogin = { navController.navigateToLogin(it) },
            navigateToHome = { navController.navigateToMain() },
            onBackClick = { navController.popBackStack() },
        )
        composable<MainGraph> {
            MainScreen(
                navigateToAuth = { navController.navigateToAuth() },
            )
        }
        appLockScreen(
            lockState = lockState,
            onRetry = onRetryAuth,
        )
    }
}
