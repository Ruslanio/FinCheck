package com.financetracker.feature.auth.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.financetracker.feature.auth.ui.LoginScreen
import com.financetracker.feature.auth.ui.RegisterScreen
import kotlinx.serialization.Serializable

@Serializable
object AuthGraph

@Serializable
private data class LoginRoute(
    val registerSuccessMsg: String? = null
)

@Serializable
private object RegisterRoute

fun NavController.navigateToAuth() {
    this.navigate(AuthGraph) { popUpTo(id = 0) }
}

fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    navigateToHome: () -> Unit,
    onBackClick: () -> Unit
) {
    navigation<AuthGraph>(
        startDestination = LoginRoute::class,
    ) {
        loginScreen(
            navigateToRegister = {
                navController.navigateToRegister()
            },
            navigateToHome = navigateToHome,
        )
        registerScreen(
            navigateToLogin = { registerSuccessMsg ->
                navController.navigateToLogin(registerSuccessMsg)
            },
            onBackClick = onBackClick
        )
    }
}

private fun NavController.navigateToLogin(registerSuccessMsg: String? = null) {
    this.navigate(LoginRoute(registerSuccessMsg = registerSuccessMsg))
}

private fun NavGraphBuilder.loginScreen(
    navigateToRegister: () -> Unit,
    navigateToHome: () -> Unit,
) {
    composable<LoginRoute> { backStack ->
        LoginScreen(
            registerSuccessMessage = backStack.getRegisterSuccessMsg(),
            navigateToRegister = navigateToRegister,
            navigateToHome = navigateToHome,
        )
    }
}

private fun NavController.navigateToRegister() {
    this.navigate(RegisterRoute)
}

private fun NavGraphBuilder.registerScreen(
    navigateToLogin: (msg: String) -> Unit,
    onBackClick: () -> Unit
) {
    composable<RegisterRoute> {
        RegisterScreen(
            navigateToLogin = navigateToLogin,
            onBackClick = onBackClick,
        )
    }
}

private fun NavBackStackEntry.getRegisterSuccessMsg(): String? {
    return this.toRoute<LoginRoute>().registerSuccessMsg
}
