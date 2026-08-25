package com.financetracker.feature.applock

import androidx.biometric.BiometricPrompt
import androidx.lifecycle.ViewModel
import com.financetracker.core.security.BiometricAuthManager
import com.financetracker.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val biometricAuthManager: BiometricAuthManager,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<BiometricState>(BiometricState.Idle)
    val state: StateFlow<BiometricState> = _state.asStateFlow()

    private var lastAuthTimestamp = 0L
    private var backgroundTimestamp = 0L

    fun onAppBackground() {
        backgroundTimestamp = System.currentTimeMillis()
    }

    fun onAppForeground(): Boolean {
        if (!authRepository.isUserLoggedIn()) {
            _state.value = BiometricState.Unavailable
            return false
        }
        if (!biometricAuthManager.canAuthenticate()) {
            _state.value = BiometricState.Unavailable
            return false
        }
        val backgroundDuration = System.currentTimeMillis() - backgroundTimestamp
        val recentlyAuthed =
            lastAuthTimestamp != 0L &&
                System.currentTimeMillis() - lastAuthTimestamp < GRACE_PERIOD_MS
        if (backgroundDuration < GRACE_PERIOD_MS || recentlyAuthed) {
            if (_state.value !is BiometricState.Authenticated) {
                _state.value = BiometricState.Authenticated
            }
            return false
        }
        _state.value = BiometricState.Authenticating
        return true
    }

    fun onAuthSuccess() {
        lastAuthTimestamp = System.currentTimeMillis()
        _state.value = BiometricState.Authenticated
    }

    fun onAuthError(errorCode: Int) {
        _state.value = when (errorCode) {
            BiometricPrompt.ERROR_USER_CANCELED,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
            BiometricPrompt.ERROR_CANCELED -> BiometricState.Locked
            else -> BiometricState.Failed
        }
    }

    fun onAuthFailed() {
        _state.value = BiometricState.Failed
    }

    fun resetToIdle() {
        _state.value = BiometricState.Idle
    }

    fun buildPromptInfo(): BiometricPrompt.PromptInfo =
        biometricAuthManager.buildPromptInfo()

    private companion object {
        const val GRACE_PERIOD_MS = 30_000L
    }
}
