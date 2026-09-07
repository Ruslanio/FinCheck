package com.financetracker.data.mapper

import com.financetracker.data.model.CategoryType
import com.financetracker.database.entity.TransactionEntity
import com.financetracker.database.entity.TransactionWithCategory
import com.financetracker.network.dto.TransactionResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class TransactionMapperTest {

    // region toEntity

    @Test
    fun toEntity_occurredAtIso8601ConvertsToEpochMillis() {
        val iso = "2024-01-15T10:30:00Z"
        val expected = Instant.parse(iso).toEpochMilli()
        val entity = with(TransactionMapper) { makeDto(occurredAt = iso).toEntity() }
        assertEquals(expected, entity.occurredAt)
    }

    @Test
    fun toEntity_nullDescriptionMapsToNull() {
        val entity = with(TransactionMapper) { makeDto(description = null).toEntity() }
        assertNull(entity.description)
    }

    @Test
    fun toEntity_createdAtIsRecentTimestamp() {
        val before = System.currentTimeMillis()
        val entity = with(TransactionMapper) { makeDto().toEntity() }
        val after = System.currentTimeMillis()
        assertTrue(entity.createdAt in before..after)
    }

    @Test
    fun toEntity_otherFieldsMapDirectly() {
        val dto = makeDto(
            id = "abc",
            userId = "user-42",
            amount = 99.9,
            categoryId = "cat-transport",
            idempotencyKey = "key-1",
        )
        val entity = with(TransactionMapper) { dto.toEntity() }
        assertEquals("abc", entity.id)
        assertEquals("user-42", entity.userId)
        assertEquals(99.9, entity.amount, 0.001)
        assertEquals("cat-transport", entity.categoryId)
        assertEquals("key-1", entity.idempotencyKey)
    }

    // endregion

    // region TransactionWithCategory.toUiModel

    @Test
    fun toUiModel_expenseCategoryTypeResolvedFromJoin() {
        val row = makeJoinRow(categoryType = "expense")
        val model = with(TransactionMapper) { row.toUiModel() }
        assertEquals(CategoryType.EXPENSE, model.categoryType)
    }

    @Test
    fun toUiModel_incomeCategoryTypeResolvedFromJoin() {
        val row = makeJoinRow(categoryType = "income")
        val model = with(TransactionMapper) { row.toUiModel() }
        assertEquals(CategoryType.INCOME, model.categoryType)
    }

    @Test
    fun toUiModel_nullCategoryTypeWhenCategoryNotSynced() {
        val row = makeJoinRow(categoryType = null, categoryName = null)
        val model = with(TransactionMapper) { row.toUiModel() }
        assertNull(model.categoryType)
        assertNull(model.categoryName)
    }

    @Test
    fun toUiModel_categoryNamePassedThrough() {
        val row = makeJoinRow(categoryName = "Food")
        val model = with(TransactionMapper) { row.toUiModel() }
        assertEquals("Food", model.categoryName)
    }

    @Test
    fun toUiModel_categoryIdPreserved() {
        val row = makeJoinRow(categoryId = "cat-123")
        val model = with(TransactionMapper) { row.toUiModel() }
        assertEquals("cat-123", model.categoryId)
    }

    // endregion

    // region toUiModelStub (holding seam for create path)

    @Test
    fun toUiModelStub_categoryNameIsNull() {
        val entity = makeEntity()
        val model = with(TransactionMapper) { entity.toUiModelStub() }
        assertNull(model.categoryName)
        assertNull(model.categoryType)
    }

    @Test
    fun toUiModelStub_categoryIdPreserved() {
        val entity = makeEntity(categoryId = "cat-abc")
        val model = with(TransactionMapper) { entity.toUiModelStub() }
        assertEquals("cat-abc", model.categoryId)
    }

    // endregion

    private fun makeDto(
        id: String = "1",
        userId: String = "u1",
        amount: Double = 10.0,
        categoryId: String = "cat-food",
        description: String? = null,
        idempotencyKey: String? = null,
        occurredAt: String = "2024-01-15T10:30:00Z",
    ) = TransactionResponseDto(
        id = id,
        userId = userId,
        amount = amount,
        categoryId = categoryId,
        description = description,
        idempotencyKey = idempotencyKey,
        occurredAt = occurredAt,
    )

    private fun makeEntity(
        id: String = "1",
        userId: String = "u1",
        amount: Double = 10.0,
        categoryId: String = "cat-food",
        description: String? = null,
        idempotencyKey: String? = null,
        occurredAt: Long = 1_000_000L,
        createdAt: Long = System.currentTimeMillis(),
    ) = TransactionEntity(
        id = id,
        userId = userId,
        amount = amount,
        categoryId = categoryId,
        description = description,
        idempotencyKey = idempotencyKey,
        occurredAt = occurredAt,
        createdAt = createdAt,
    )

    private fun makeJoinRow(
        id: String = "1",
        userId: String = "u1",
        amount: Double = 10.0,
        categoryId: String = "cat-food",
        description: String? = null,
        idempotencyKey: String? = null,
        occurredAt: Long = 1_000_000L,
        createdAt: Long = System.currentTimeMillis(),
        categoryName: String? = "Food",
        categoryType: String? = "expense",
        categoryIsFallback: Boolean? = false,
    ) = TransactionWithCategory(
        id = id,
        userId = userId,
        amount = amount,
        categoryId = categoryId,
        description = description,
        idempotencyKey = idempotencyKey,
        occurredAt = occurredAt,
        createdAt = createdAt,
        categoryName = categoryName,
        categoryType = categoryType,
        categoryIsFallback = categoryIsFallback,
    )
}
