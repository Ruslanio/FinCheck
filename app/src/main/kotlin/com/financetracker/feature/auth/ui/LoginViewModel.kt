package com.financetracker.feature.auth.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onLoginClick() {
        when {
            email.isBlank() -> _uiState.value = AuthUiState.Error(ERROR_EMAIL_REQUIRED)
            password.length < MIN_PASSWORD_LENGTH -> _uiState.value = AuthUiState.Error(ERROR_PASSWORD_TOO_SHORT)
            else -> _uiState.value = AuthUiState.Loading
        }
        // Sprint 2: launch { login(email, password) }
    }

    companion object {
        const val ERROR_EMAIL_REQUIRED = "error_email_required"
        const val ERROR_PASSWORD_TOO_SHORT = "error_password_too_short"
        private const val MIN_PASSWORD_LENGTH = 8
    }
}
