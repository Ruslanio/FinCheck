package com.financetracker.ui.bottombar

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.financetracker.R
import com.financetracker.home.navigation.HomeRoute
import com.financetracker.profile.navigation.ProfileRoute
import com.financetracker.transactions.navigation.TransactionRoute

sealed class BottomBarDestination(
    val route: Any,
    val icon: ImageVector,
    @StringRes val labelRes: Int
) {
    data object Home : BottomBarDestination(
        route = HomeRoute,
        icon = Icons.Rounded.Home,
        labelRes = R.string.bottom_bar_home
    )

    data object Transactions : BottomBarDestination(
        route = TransactionRoute,
        icon = Icons.AutoMirrored.Rounded.List,
        labelRes = R.string.bottom_bar_transaction
    )

    data object Profile : BottomBarDestination(
        route = ProfileRoute,
        icon = Icons.Rounded.Person,
        labelRes = R.string.bottom_bar_profile
    )
}

fun getBottomBarDestinations() =
    listOf(
        BottomBarDestination.Home,
        BottomBarDestination.Transactions,
        BottomBarDestination.Profile
    )
