package com.financetracker.ui.bottombar

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
object MainGraph

fun NavController.navigateToMain() {
    this.navigate(MainGraph) { popUpTo(id = 0) }
}
