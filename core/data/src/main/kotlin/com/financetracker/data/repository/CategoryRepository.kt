package com.financetracker.data.repository

import com.financetracker.data.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getCategories(): Flow<List<Category>>

    suspend fun syncCategories(): SyncResult

    sealed interface SyncResult {
        data object Success : SyncResult
        data object Retry : SyncResult
        data object Failure : SyncResult
    }
}
