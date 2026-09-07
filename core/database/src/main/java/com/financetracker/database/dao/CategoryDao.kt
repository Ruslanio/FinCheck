package com.financetracker.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.financetracker.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAll(): Flow<List<CategoryEntity>>

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE id NOT IN (:presentIds)")
    suspend fun deleteAbsent(presentIds: List<String>)

    @Transaction
    suspend fun reconcile(categories: List<CategoryEntity>) {
        if (categories.isEmpty()) return
        upsertAll(categories)
        deleteAbsent(categories.map { it.id })
    }

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}
