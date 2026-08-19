package com.rumir.network.service

import com.rumir.network.dto.CreateTransactionRequestDto
import com.rumir.network.dto.PagedTransactionsResponseDto
import com.rumir.network.dto.TransactionResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface TransactionApiService {

    @GET("transactions")
    suspend fun getTransactions(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("category") category: String? = null,
    ): Response<PagedTransactionsResponseDto>

    @POST("transactions")
    suspend fun createTransaction(
        @Header("Authorization") token: String,
        @Body body: CreateTransactionRequestDto,
    ): Response<TransactionResponseDto>
}
