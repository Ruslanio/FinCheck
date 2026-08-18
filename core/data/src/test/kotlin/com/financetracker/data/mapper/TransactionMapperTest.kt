package com.financetracker.data.mapper

import com.financetracker.data.local.entity.TransactionEntity
import com.financetracker.data.network.dto.TransactionResponseDto
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

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
            category = "transport",
            idempotencyKey = "key-1",
        )
        val entity = with(TransactionMapper) { dto.toEntity() }
        assertEquals("abc", entity.id)
        assertEquals("user-42", entity.userId)
        assertEquals(99.9, entity.amount, 0.001)
        assertEquals("transport", entity.category)
        assertEquals("key-1", entity.idempotencyKey)
    }

    // endregion

    // region toUiModel

    @Test
    fun toUiModel_isExpenseTrueWhenAmountNegative() {
        val model = with(TransactionMapper) { makeEntity(amount = -50.0).toUiModel() }
        assertTrue(model.isExpense)
    }

    @Test
    fun toUiModel_isExpenseFalseWhenAmountPositive() {
        val model = with(TransactionMapper) { makeEntity(amount = 100.0).toUiModel() }
        assertFalse(model.isExpense)
    }

    @Test
    fun toUiModel_isExpenseFalseWhenAmountZero() {
        val model = with(TransactionMapper) { makeEntity(amount = 0.0).toUiModel() }
        assertFalse(model.isExpense)
    }

    // endregion

    private fun makeDto(
        id: String = "1",
        userId: String = "u1",
        amount: Double = 10.0,
        category: String = "food",
        description: String? = null,
        idempotencyKey: String? = null,
        occurredAt: String = "2024-01-15T10:30:00Z",
    ) = TransactionResponseDto(
        id = id,
        userId = userId,
        amount = amount,
        category = category,
        description = description,
        idempotencyKey = idempotencyKey,
        occurredAt = occurredAt,
    )

    private fun makeEntity(
        id: String = "1",
        userId: String = "u1",
        amount: Double = 10.0,
        category: String = "food",
        description: String? = null,
        idempotencyKey: String? = null,
        occurredAt: Long = 1_000_000L,
        createdAt: Long = System.currentTimeMillis(),
    ) = TransactionEntity(
        id = id,
        userId = userId,
        amount = amount,
        category = category,
        description = description,
        idempotencyKey = idempotencyKey,
        occurredAt = occurredAt,
        createdAt = createdAt,
    )
}
