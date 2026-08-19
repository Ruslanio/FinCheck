package com.financetracker.feature.transactions.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.financetracker.feature.transactions.ui.TransactionListScreen

const val ROUTE_TRANSACTION = "ROUTE_TRANSACTION"

fun NavController.navigateToTransaction(navOptions: NavOptions? = null) {
    this.navigate(ROUTE_TRANSACTION, navOptions)
}

fun NavGraphBuilder.transactionScreen() {
    composable(route = ROUTE_TRANSACTION) {
        TransactionListScreen()
    }
}
