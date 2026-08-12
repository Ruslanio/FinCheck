package com.financetracker.core.data.repository

sealed interface AuthResult {
    data object Success : AuthResult
    data class Error(val message: String) : AuthResult
    data object NetworkError : AuthResult
}

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult
    suspend fun register(email: String, password: String): AuthResult
    suspend fun logout()
}
