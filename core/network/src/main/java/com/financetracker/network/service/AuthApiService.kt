package com.financetracker.network.service

import com.financetracker.network.dto.LoginRequestDto
import com.financetracker.network.dto.LoginResponseDto
import com.financetracker.network.dto.LogoutRequestDto
import com.financetracker.network.dto.RefreshRequestDto
import com.financetracker.network.dto.RegisterRequestDto
import com.financetracker.network.dto.RegisterResponseDto
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
