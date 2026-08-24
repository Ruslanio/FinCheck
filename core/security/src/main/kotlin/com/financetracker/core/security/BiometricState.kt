package com.financetracker.core.security

sealed interface BiometricState {
    data object Idle : BiometricState
    data object Authenticating : BiometricState
    data object Authenticated : BiometricState
    data object Failed : BiometricState
    data object Locked : BiometricState
    data object Unavailable : BiometricState
}
