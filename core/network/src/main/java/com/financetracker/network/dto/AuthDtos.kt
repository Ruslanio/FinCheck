package com.financetracker.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(val email: String, val password: String)

@Serializable
data class RegisterRequestDto(val email: String, val password: String)

@Serializable
data class RefreshRequestDto(val refreshToken: String)

@Serializable
data class LogoutRequestDto(val refreshToken: String, val accessToken: String)

@Serializable
data class LoginResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
)

@Serializable
data class RegisterResponseDto(val userId: String, val email: String)

@Serializable
data class ErrorResponseDto(val error: String)
