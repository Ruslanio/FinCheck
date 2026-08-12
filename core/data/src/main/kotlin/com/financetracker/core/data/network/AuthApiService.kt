package com.financetracker.core.data.network

import com.financetracker.core.data.network.dto.LoginRequestDto
import com.financetracker.core.data.network.dto.LoginResponseDto
import com.financetracker.core.data.network.dto.LogoutRequestDto
import com.financetracker.core.data.network.dto.RefreshRequestDto
import com.financetracker.core.data.network.dto.RegisterRequestDto
import com.financetracker.core.data.network.dto.RegisterResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestDto): Response<RegisterResponseDto>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): Response<LoginResponseDto>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequestDto): Response<LoginResponseDto>

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequestDto): Response<Unit>
}
