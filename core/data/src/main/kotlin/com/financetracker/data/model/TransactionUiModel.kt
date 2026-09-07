package com.financetracker.data.model

data class TransactionUiModel(
    val id: String,
    val amount: Double,
    val categoryId: String,
    val categoryName: String?,
    val categoryType: CategoryType?,
    val description: String?,
    val occurredAt: Long,
)
