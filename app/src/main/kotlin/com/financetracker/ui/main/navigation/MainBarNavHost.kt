package com.financetracker.ui.main.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.financetracker.home.navigation.HomeRoute
import com.financetracker.home.navigation.homeScreen
import com.financetracker.profile.navigation.profileScreen
import com.financetracker.transactions.navigation.transactionScreen

@Composable
fun MainBarNavHost(
    modifier: Modifier,
    navigateToAuth: () -> Unit,
    mainNavController: NavHostController
) {
    NavHost(
        modifier = modifier,
        navController = mainNavController,
        startDestination = HomeRoute,
    ) {
        homeScreen()
        transactionScreen()
        profileScreen(
            navigateToAuth = navigateToAuth
        )
    }
}
