package com.financetracker.ui.main

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.financetracker.ui.main.bottombar.FinCheckBottomBar
import com.financetracker.ui.main.bottombar.getBottomBarDestinations
import com.financetracker.ui.main.navigation.MainBarNavHost

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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)            // lifts tab content above the bottom bar → FAB now visible
                .consumeWindowInsets(innerPadding),
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
