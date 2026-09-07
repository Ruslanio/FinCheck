package com.financetracker.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.financetracker.database.entity.TransactionEntity
import com.financetracker.database.entity.TransactionWithCategory

@Dao
interface TransactionDao {

    @Query(
        """
        SELECT t.id, t.userId, t.amount, t.categoryId, t.description, t.idempotencyKey,
               t.occurredAt, t.createdAt,
               c.name AS categoryName, c.type AS categoryType, c.isFallback AS categoryIsFallback
        FROM transactions t
        LEFT JOIN categories c ON t.categoryId = c.id
        WHERE t.userId = :userId
        ORDER BY t.occurredAt DESC
        """,
    )
    fun getTransactions(userId: String): PagingSource<Int, TransactionWithCategory>

    @Query(
        """
        SELECT t.id, t.userId, t.amount, t.categoryId, t.description, t.idempotencyKey,
               t.occurredAt, t.createdAt,
               c.name AS categoryName, c.type AS categoryType, c.isFallback AS categoryIsFallback
        FROM transactions t
        LEFT JOIN categories c ON t.categoryId = c.id
        WHERE t.userId = :userId
          AND LOWER(c.name) = LOWER(:categoryName)
        ORDER BY t.occurredAt DESC
        """,
    )
    fun getTransactionsByCategory(
        userId: String,
        categoryName: String,
    ): PagingSource<Int, TransactionWithCategory>

    @Upsert
    suspend fun upsertAll(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun clearAll(userId: String)

    @Query("SELECT COUNT(*) FROM transactions WHERE userId = :userId")
    suspend fun countByUserId(userId: String): Int

    @Query(
        """
        SELECT MAX(occurredAt) FROM transactions
        WHERE userId = :userId
        """,
    )
    suspend fun getLatestTimestamp(userId: String): Long?
}
