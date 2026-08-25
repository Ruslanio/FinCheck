package com.financetracker.feature.applock.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.financetracker.feature.applock.AppLockScreen
import com.financetracker.feature.applock.BiometricState
import kotlinx.serialization.Serializable

@Serializable
object AppLockRoute

fun NavController.navigateToAppLock() {
    navigate(AppLockRoute) { launchSingleTop = true }
}

fun NavGraphBuilder.appLockScreen(
    lockState: BiometricState,
    onRetry: () -> Unit,
) {
    composable<AppLockRoute> {
        AppLockScreen(state = lockState, onRetry = onRetry)
    }
}
