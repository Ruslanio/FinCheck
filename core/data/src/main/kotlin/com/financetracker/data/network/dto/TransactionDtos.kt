package com.financetracker.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransactionResponseDto(
    val id: String,
    val userId: String,
    val amount: Double,
    val category: String,
    val description: String?,
    val idempotencyKey: String?,
    val occurredAt: String,
)

@Serializable
data class PagedTransactionsResponseDto(
    val data: List<TransactionResponseDto>,
    val page: Int,
    val size: Int,
    val total: Long,
)

@Serializable
data class CreateTransactionRequestDto(
    val amount: Double,
    val category: String,
    val description: String? = null,
    val idempotencyKey: String? = null,
    val occurredAt: String? = null,
)
