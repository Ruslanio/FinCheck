package com.financetracker.data.mapper

import com.financetracker.data.model.Category
import com.financetracker.data.model.CategoryType
import com.financetracker.database.entity.CategoryEntity
import com.financetracker.network.dto.CategoryResponseDto

object CategoryMapper {

    fun CategoryResponseDto.toEntity(): CategoryEntity =
        CategoryEntity(
            id = id,
            name = name,
            type = type,
            isFallback = isFallback,
        )

    fun CategoryEntity.toDomain(): Category =
        Category(
            id = id,
            name = name,
            type = type.toCategoryType(),
            isFallback = isFallback,
        )

    fun String.toCategoryType(): CategoryType =
        when (lowercase()) {
            "income" -> CategoryType.INCOME
            else -> CategoryType.EXPENSE
        }
}
