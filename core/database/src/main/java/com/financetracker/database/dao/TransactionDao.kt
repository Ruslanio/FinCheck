package com.financetracker.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.financetracker.database.entity.TransactionEntity

@Dao
interface TransactionDao {

    @Query(
        """
        SELECT * FROM transactions
        WHERE userId = :userId
        ORDER BY occurredAt DESC
        """,
    )
    fun getTransactions(userId: String): PagingSource<Int, TransactionEntity>

    @Query(
        """
        SELECT * FROM transactions
        WHERE userId = :userId
          AND LOWER(category) = LOWER(:category)
        ORDER BY occurredAt DESC
        """,
    )
    fun getTransactionsByCategory(
        userId: String,
        category: String,
    ): PagingSource<Int, TransactionEntity>

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
