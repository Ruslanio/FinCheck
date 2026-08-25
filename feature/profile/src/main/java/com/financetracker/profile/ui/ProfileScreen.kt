package com.financetracker.profile.ui

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financetracker.profile.R

@Composable
fun ProfileScreen(
    navigateToAuth: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileUiEvent.NavigateToAuth -> navigateToAuth()
                ProfileUiEvent.RequestBiometricVerification -> {
                    val activity = context as FragmentActivity
                    val executor = ContextCompat.getMainExecutor(context)
                    val prompt = BiometricPrompt(
                        activity,
                        executor,
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationSucceeded(
                                result: BiometricPrompt.AuthenticationResult,
                            ) {
                                viewModel.onBiometricVerificationResult(true)
                            }

                            override fun onAuthenticationError(
                                errorCode: Int,
                                errString: CharSequence,
                            ) {
                                viewModel.onBiometricVerificationResult(false)
                            }

                            override fun onAuthenticationFailed() {
                                // Single attempt failed; dialog stays open for retry
                            }
                        },
                    )
                    prompt.authenticate(viewModel.buildPromptInfo())
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.label_app_lock),
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (!uiState.biometricAvailable) {
                    Text(
                        text = stringResource(R.string.label_app_lock_unavailable),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }
            Switch(
                checked = uiState.biometricAvailable && uiState.biometricLockEnabled,
                onCheckedChange = { viewModel.onBiometricToggle(it) },
                enabled = uiState.biometricAvailable,
            )
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = viewModel::onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.label_logout))
        }
    }
}
