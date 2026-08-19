package com.financetracker.feature.auth.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financetracker.data.repository.AuthRepository
import com.financetracker.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onRegisterClick() {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error(ERROR_EMAIL_REQUIRED)
            return
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            _uiState.value = AuthUiState.Error(ERROR_PASSWORD_TOO_SHORT)
            return
        }
        if (password != confirmPassword) {
            _uiState.value = AuthUiState.Error(ERROR_PASSWORDS_DO_NOT_MATCH)
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _uiState.value = when (val result = authRepository.register(email, password)) {
                AuthResult.Success -> AuthUiState.Success
                is AuthResult.Error -> AuthUiState.Error(result.message)
                AuthResult.NetworkError -> AuthUiState.Error(ERROR_NETWORK)
            }
        }
    }

    companion object {
        const val ERROR_EMAIL_REQUIRED = "error_email_required"
        const val ERROR_PASSWORD_TOO_SHORT = "error_password_too_short"
        const val ERROR_PASSWORDS_DO_NOT_MATCH = "error_passwords_do_not_match"
        const val ERROR_NETWORK = "error_network"
        private const val MIN_PASSWORD_LENGTH = 8
    }
}
