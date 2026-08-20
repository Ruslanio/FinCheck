package com.financetracker.feature.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.financetracker.feature.profile.ui.ProfileScreen
import kotlinx.serialization.Serializable

@Serializable
object ProfileRoute

fun NavController.navigateToProfile(navOptions: NavOptions? = null) {
    this.navigate(ProfileRoute, navOptions)
}

fun NavGraphBuilder.profileScreen() {
    composable<ProfileRoute> {
        ProfileScreen()
    }
}
