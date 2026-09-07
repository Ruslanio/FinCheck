package com.financetracker.network.service

import com.financetracker.network.dto.CategoryResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface CategoryApiService {

    @GET("categories")
    suspend fun getCategories(
        @Header("Authorization") token: String,
    ): Response<List<CategoryResponseDto>>
}
