package com.financetracker.data.model

data class TransactionUiModel(
    val id: String,
    val amount: Double,
    val category: String,
    val description: String?,
    val occurredAt: Long,
    val isExpense: Boolean,
)
