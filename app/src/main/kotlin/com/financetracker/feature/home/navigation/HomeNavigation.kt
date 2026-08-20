package com.financetracker.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.financetracker.feature.home.ui.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

fun NavController.navigateToHome() {
    this.navigate(HomeRoute) { popUpTo(id = 0) }
}

fun NavGraphBuilder.homeGraph(
    navigateToTransaction: () -> Unit,
    navigateToAuth: () -> Unit,
) {
    composable<HomeRoute> {
        HomeScreen(
            navigateToTransaction = navigateToTransaction,
            navigateToAuth = navigateToAuth,
        )
    }
}
