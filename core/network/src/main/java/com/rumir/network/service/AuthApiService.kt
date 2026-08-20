package com.rumir.network.service

import com.rumir.network.dto.LoginRequestDto
import com.rumir.network.dto.LoginResponseDto
import com.rumir.network.dto.LogoutRequestDto
import com.rumir.network.dto.RefreshRequestDto
import com.rumir.network.dto.RegisterRequestDto
import com.rumir.network.dto.RegisterResponseDto
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
