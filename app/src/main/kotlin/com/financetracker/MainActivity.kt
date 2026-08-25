package com.financetracker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financetracker.core.ui.theme.FinanceTrackerTheme
import com.financetracker.feature.applock.AppLockViewModel
import com.financetracker.navigation.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val appLockViewModel: AppLockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceTrackerTheme {
                val lockState by appLockViewModel.state.collectAsStateWithLifecycle()
                AppNavGraph(
                    lockState = lockState,
                    onRetryAuth = { showBiometricPrompt() },
                    onLockedNavigate = { appLockViewModel.resetToIdle() },
                )
            }
        }
        if (appLockViewModel.onAppForeground()) {
            showBiometricPrompt()
        }
    }

    override fun onResume() {
        super.onResume()
        if (appLockViewModel.onAppForeground()) {
            showBiometricPrompt()
        }
    }

    override fun onPause() {
        super.onPause()
        appLockViewModel.onAppBackground()
    }

    private fun showBiometricPrompt() {
        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult,
                ) {
                    appLockViewModel.onAuthSuccess()
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence,
                ) {
                    appLockViewModel.onAuthError(errorCode)
                }

                override fun onAuthenticationFailed() {
                    appLockViewModel.onAuthFailed()
                }
            },
        )
        prompt.authenticate(appLockViewModel.buildPromptInfo())
    }
}
