package com.financetracker.data.mapper

import com.financetracker.data.model.TransactionUiModel
import com.rumir.database.entity.TransactionEntity
import com.rumir.network.dto.TransactionResponseDto
import java.time.Instant

object TransactionMapper {

    fun TransactionResponseDto.toEntity(): TransactionEntity =
        TransactionEntity(
            id = id,
            userId = userId,
            amount = amount,
            category = category,
            description = description,
            idempotencyKey = idempotencyKey,
            occurredAt = Instant.parse(occurredAt).toEpochMilli(),
            createdAt = System.currentTimeMillis(),
        )

    fun TransactionEntity.toUiModel(): TransactionUiModel =
        TransactionUiModel(
            id = id,
            amount = amount,
            category = category,
            description = description,
            occurredAt = occurredAt,
            isExpense = amount < 0,
        )
}
