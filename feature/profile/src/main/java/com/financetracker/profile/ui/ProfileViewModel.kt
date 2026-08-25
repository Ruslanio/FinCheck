package com.financetracker.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financetracker.core.security.BiometricAuthManager
import com.financetracker.data.repository.AppLockRepository
import com.financetracker.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val biometricLockEnabled: Boolean = false,
    val biometricAvailable: Boolean = false,
)

sealed interface ProfileUiEvent {
    data object NavigateToAuth : ProfileUiEvent
    data object RequestBiometricVerification : ProfileUiEvent
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val appLockRepository: AppLockRepository,
    private val biometricAuthManager: BiometricAuthManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileUiEvent>()
    val events: SharedFlow<ProfileUiEvent> = _events.asSharedFlow()

    init {
        _uiState.update { it.copy(biometricAvailable = biometricAuthManager.canAuthenticate()) }
        viewModelScope.launch {
            appLockRepository.isBiometricLockEnabled.collect { enabled ->
                _uiState.update { it.copy(biometricLockEnabled = enabled) }
            }
        }
    }

    fun onLogoutClick() {
        viewModelScope.launch {
            authRepository.logout()
            _events.emit(ProfileUiEvent.NavigateToAuth)
        }
    }

    fun onBiometricToggle(checked: Boolean) {
        if (!checked) {
            viewModelScope.launch {
                appLockRepository.setBiometricLockEnabled(false)
            }
        } else {
            viewModelScope.launch {
                _events.emit(ProfileUiEvent.RequestBiometricVerification)
            }
        }
    }

    fun onBiometricVerificationResult(success: Boolean) {
        if (success) {
            viewModelScope.launch {
                appLockRepository.setBiometricLockEnabled(true)
            }
        }
        // On failure/cancel, DataStore still holds false → UI reverts automatically
    }

    fun buildPromptInfo() = biometricAuthManager.buildPromptInfo()
}
