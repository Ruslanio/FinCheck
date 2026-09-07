package com.financetracker.database.entity

import androidx.room.ColumnInfo

data class TransactionWithCategory(
    val id: String,
    val userId: String,
    val amount: Double,
    val categoryId: String,
    val description: String?,
    val idempotencyKey: String?,
    val occurredAt: Long,
    val createdAt: Long,
    @ColumnInfo(name = "categoryName") val categoryName: String?,
    @ColumnInfo(name = "categoryType") val categoryType: String?,
    @ColumnInfo(name = "categoryIsFallback") val categoryIsFallback: Boolean?,
)
