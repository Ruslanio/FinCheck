package com.financetracker.data.repository

import com.financetracker.data.storage.TokenStorage
import com.financetracker.network.dto.ErrorResponseDto
import com.financetracker.network.dto.LoginRequestDto
import com.financetracker.network.dto.LogoutRequestDto
import com.financetracker.network.dto.RegisterRequestDto
import com.financetracker.network.service.AuthApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import retrofit2.Response
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val tokenStorage: TokenStorage,
) : AuthRepository {

    override suspend fun login(email: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequestDto(email, password))
                when {
                    response.isSuccessful -> {
                        val body = checkNotNull(response.body())
                        tokenStorage.saveTokens(body.accessToken, body.refreshToken)
                        val userId = tokenStorage.getUserIdFromToken(body.accessToken)
                        if (userId != null) tokenStorage.saveUserId(userId)
                        AuthResult.Success
                    }
                    response.code() == 401 -> AuthResult.Error("error_invalid_credentials")
                    else -> AuthResult.Error("error_server")
                }
            } catch (_: Exception) {
                AuthResult.NetworkError
            }
        }

    override suspend fun register(email: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(RegisterRequestDto(email, password))
                when {
                    response.isSuccessful -> AuthResult.Success
                    response.code() == 409 -> AuthResult.Error("error_email_already_registered")
                    response.code() == 400 -> AuthResult.Error(parseErrorCode(response))
                    else -> AuthResult.Error("error_server")
                }
            } catch (_: Exception) {
                AuthResult.NetworkError
            }
        }

    override suspend fun logout() {
        val access = tokenStorage.getAccessToken().orEmpty()
        val refresh = tokenStorage.getRefreshToken().orEmpty()
        withContext(Dispatchers.IO) {
            try {
                apiService.logout(LogoutRequestDto(refresh, access))
            } catch (_: Exception) {
                // best-effort: always clear locally
            }
        }
        tokenStorage.clearTokens()
    }

    override fun isUserLoggedIn() = tokenStorage.hasValidToken()

    private fun parseErrorCode(response: Response<*>): String =
        try {
            val body = response.errorBody()?.string() ?: return "error_unknown"
            Json.decodeFromString<ErrorResponseDto>(body).error
        } catch (_: Exception) {
            "error_unknown"
        }
}
