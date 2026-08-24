package com.financetracker.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financetracker.auth.R
import com.financetracker.core.ui.components.EmailInputField
import com.financetracker.core.ui.components.PasswordInputField
import com.financetracker.core.ui.components.PrimaryButton

@Composable
fun LoginScreen(
    registerSuccessMessage: String?,
    navigateToRegister: () -> Unit,
    navigateToHome: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState is AuthUiState.Success) {
        navigateToHome.invoke()
    }

    val emailError = (uiState as? AuthUiState.Error)
        ?.message
        ?.takeIf { it == LoginViewModel.ERROR_EMAIL_REQUIRED }
        ?.let { stringResource(R.string.error_email_required) }

    val passwordError = (uiState as? AuthUiState.Error)
        ?.message
        ?.takeIf { it == LoginViewModel.ERROR_PASSWORD_TOO_SHORT }
        ?.let { stringResource(R.string.error_password_too_short) }

    val screenError = (uiState as? AuthUiState.Error)
        ?.message
        ?.takeIf {
            it !in setOf(
                LoginViewModel.ERROR_EMAIL_REQUIRED,
                LoginViewModel.ERROR_PASSWORD_TOO_SHORT
            )
        }
        ?.let { code -> mapLoginErrorCode(code) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        registerSuccessMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        EmailInputField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = stringResource(R.string.label_email),
            modifier = Modifier.fillMaxWidth(),
            isError = emailError != null,
            supportingText = emailError,
        )
        Spacer(modifier = Modifier.height(8.dp))

        PasswordInputField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = stringResource(R.string.label_password),
            togglePasswordVisibilityContentDescription = stringResource(R.string.cd_toggle_password_visibility),
            modifier = Modifier.fillMaxWidth(),
            isError = passwordError != null,
            supportingText = passwordError,
        )
        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButton(
            text = stringResource(R.string.label_login),
            onClick = viewModel::onLoginClick,
            modifier = Modifier.fillMaxWidth(),
            isLoading = uiState is AuthUiState.Loading,
        )

        if (screenError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = screenError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = navigateToRegister) {
            Text(stringResource(R.string.label_register))
        }
    }
}

@Composable
private fun mapLoginErrorCode(code: String): String =
    when (code) {
        "error_invalid_credentials" -> stringResource(R.string.error_invalid_credentials)
        "error_network" -> stringResource(R.string.error_network)
        else -> code
    }
