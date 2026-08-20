package com.financetracker.feature.transactions.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.financetracker.feature.transactions.ui.TransactionListScreen
import kotlinx.serialization.Serializable

@Serializable
object TransactionRoute

fun NavController.navigateToTransaction(navOptions: NavOptions? = null) {
    this.navigate(TransactionRoute, navOptions)
}

fun NavGraphBuilder.transactionScreen() {
    composable<TransactionRoute> {
        TransactionListScreen()
    }
}
