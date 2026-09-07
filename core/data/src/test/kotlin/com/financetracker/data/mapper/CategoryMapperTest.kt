package com.financetracker.data.mapper

import com.financetracker.data.model.CategoryType
import com.financetracker.database.entity.CategoryEntity
import com.financetracker.network.dto.CategoryResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryMapperTest {

    @Test
    fun toEntity_mapsAllFields() {
        val dto = CategoryResponseDto(
            id = "cat-1",
            name = "Food",
            type = "expense",
            isFallback = false,
        )
        val entity = with(CategoryMapper) { dto.toEntity() }
        assertEquals("cat-1", entity.id)
        assertEquals("Food", entity.name)
        assertEquals("expense", entity.type)
        assertFalse(entity.isFallback)
    }

    @Test
    fun toEntity_isFallbackPreserved() {
        val dto = CategoryResponseDto(id = "fallback", name = "Other", type = "expense", isFallback = true)
        val entity = with(CategoryMapper) { dto.toEntity() }
        assertTrue(entity.isFallback)
    }

    @Test
    fun toDomain_expenseTypeMapsToCategoryTypeExpense() {
        val entity = CategoryEntity(id = "c1", name = "Food", type = "expense", isFallback = false)
        val domain = with(CategoryMapper) { entity.toDomain() }
        assertEquals(CategoryType.EXPENSE, domain.type)
    }

    @Test
    fun toDomain_incomeTypeMapsToCategoryTypeIncome() {
        val entity = CategoryEntity(id = "c1", name = "Salary", type = "income", isFallback = false)
        val domain = with(CategoryMapper) { entity.toDomain() }
        assertEquals(CategoryType.INCOME, domain.type)
    }

    @Test
    fun toDomain_unknownTypeFallsBackToExpense() {
        val entity = CategoryEntity(id = "c1", name = "Unknown", type = "other", isFallback = false)
        val domain = with(CategoryMapper) { entity.toDomain() }
        assertEquals(CategoryType.EXPENSE, domain.type)
    }

    @Test
    fun toCategoryType_caseInsensitive() {
        assertEquals(CategoryType.INCOME, with(CategoryMapper) { "INCOME".toCategoryType() })
        assertEquals(CategoryType.EXPENSE, with(CategoryMapper) { "EXPENSE".toCategoryType() })
    }

    @Test
    fun toDomain_preservesOtherFields() {
        val entity = CategoryEntity(id = "c42", name = "Transport", type = "expense", isFallback = true)
        val domain = with(CategoryMapper) { entity.toDomain() }
        assertEquals("c42", domain.id)
        assertEquals("Transport", domain.name)
        assertTrue(domain.isFallback)
    }
}
