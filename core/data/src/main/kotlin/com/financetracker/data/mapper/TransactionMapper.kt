package com.financetracker.data.mapper

import com.financetracker.data.mapper.CategoryMapper.toCategoryType
import com.financetracker.data.model.TransactionUiModel
import com.financetracker.database.entity.TransactionEntity
import com.financetracker.database.entity.TransactionWithCategory
import com.financetracker.network.dto.TransactionResponseDto
import java.time.Instant

object TransactionMapper {

    fun TransactionResponseDto.toEntity(): TransactionEntity =
        TransactionEntity(
            id = id,
            userId = userId,
            amount = amount,
            categoryId = categoryId,
            description = description,
            idempotencyKey = idempotencyKey,
            occurredAt = Instant.parse(occurredAt).toEpochMilli(),
            createdAt = System.currentTimeMillis(),
        )

    fun TransactionWithCategory.toUiModel(): TransactionUiModel =
        TransactionUiModel(
            id = id,
            amount = amount,
            categoryId = categoryId,
            categoryName = categoryName,
            categoryType = categoryType?.toCategoryType(),
            description = description,
            occurredAt = occurredAt,
        )

    // Holding seam for createTransaction — category data resolved on next paging refresh.
    fun TransactionEntity.toUiModelStub(): TransactionUiModel =
        TransactionUiModel(
            id = id,
            amount = amount,
            categoryId = categoryId,
            categoryName = null,
            categoryType = null,
            description = description,
            occurredAt = occurredAt,
        )
}
