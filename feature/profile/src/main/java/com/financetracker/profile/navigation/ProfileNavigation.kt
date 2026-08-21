package com.financetracker.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.financetracker.profile.ui.ProfileScreen
import kotlinx.serialization.Serializable

@Serializable
object ProfileRoute

fun NavController.navigateToProfile(builder: (NavOptionsBuilder.() -> Unit) = {}) {
    this.navigate(ProfileRoute, builder)
}

fun NavGraphBuilder.profileScreen(
    navigateToAuth: () -> Unit,
) {
    composable<ProfileRoute> {
        ProfileScreen(
            navigateToAuth = navigateToAuth
        )
    }
}
