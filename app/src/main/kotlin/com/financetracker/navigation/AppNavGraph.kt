package com.financetracker.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
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
import com.financetracker.core.security.AppLockScreen
import com.financetracker.core.security.BiometricState
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
        if (lockState is BiometricState.Locked) {
            navController.navigateToAuth()
            onLockedNavigate()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FinanceCheckNavHost(
            navController = navController,
            startDestination = startDestination,
        )

        if (lockState is BiometricState.Authenticating ||
            lockState is BiometricState.Failed
        ) {
            AppLockScreen(
                state = lockState,
                onRetry = onRetryAuth,
            )
        }
    }
}

@Composable
fun FinanceCheckNavHost(
    navController: NavHostController,
    startDestination: Any,
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
    }
}
