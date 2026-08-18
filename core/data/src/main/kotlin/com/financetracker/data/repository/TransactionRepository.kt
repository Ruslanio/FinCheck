package com.financetracker.data.repository

import androidx.paging.PagingData
import com.financetracker.data.model.TransactionUiModel
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    fun getTransactions(category: String? = null): Flow<PagingData<TransactionUiModel>>

    suspend fun createTransaction(
        amount: Double,
        category: String,
        description: String?,
        idempotencyKey: String?,
    ): CreateResult

    sealed interface CreateResult {
        data class Success(val transaction: TransactionUiModel) : CreateResult
        data class Duplicate(val transaction: TransactionUiModel) : CreateResult
        data class Error(val message: String) : CreateResult
        data object NetworkError : CreateResult
    }
}
