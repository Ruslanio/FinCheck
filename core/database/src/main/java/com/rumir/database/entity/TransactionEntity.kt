package com.rumir.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val amount: Double,
    val category: String,
    val description: String?,
    val idempotencyKey: String?,
    val occurredAt: Long,
    val createdAt: Long,
)
