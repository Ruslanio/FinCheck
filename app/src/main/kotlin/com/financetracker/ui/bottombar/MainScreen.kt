package com.financetracker.ui.bottombar

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun MainScreen(
    navigateToAuth: () -> Unit
) {
    val mainNavController: NavHostController = rememberNavController()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            FinCheckBottomBar(
                destinations = getBottomBarDestinations(),
                currentDestination = currentDestination,
                onNavigateToDestination = { destination ->
                    val rootDestinationId = mainNavController.graph.findStartDestination().id
                    mainNavController.navigate(destination.route) {
                        bottomBarOptions(rootDestinationId)
                    }
                }
            )
        }
    ) { innerPadding ->
        MainBarNavHost(
            modifier = Modifier,
            mainNavController = mainNavController,
            navigateToAuth = navigateToAuth
        )
    }
}

private fun NavOptionsBuilder.bottomBarOptions(graphRootDestinationId: Int) {
    // pop back to the graph's start to avoid stacking tabs
    popUpTo(graphRootDestinationId) {
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}
