package com.financetracker.transactions.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.financetracker.transactions.ui.TransactionListScreen
import kotlinx.serialization.Serializable

@Serializable
object TransactionRoute

fun NavController.navigateToTransaction(builder: (NavOptionsBuilder.() -> Unit) = {}) {
    this.navigate(TransactionRoute, builder)
}

fun NavGraphBuilder.transactionScreen() {
    composable<TransactionRoute> {
        TransactionListScreen()
    }
}
