package com.financetracker.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.financetracker.feature.home.ui.HomeScreen
import kotlinx.serialization.Serializable

//TODO convert to graph flow when bottombar is introduced
@Serializable
object HomeGraph

fun NavController.navigateToHome() {
    this.navigate(HomeGraph) { popUpTo(id = 0) }
}

fun NavGraphBuilder.homeGraph(
    navigateToTransaction: () -> Unit,
    navigateToAuth: () -> Unit,
) {
    composable<HomeGraph> {
        HomeScreen(
            navigateToTransaction = navigateToTransaction,
            navigateToAuth = navigateToAuth,
        )
    }
}
