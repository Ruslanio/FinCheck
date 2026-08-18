package com.financetracker.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.financetracker.core.data.storage.TokenStorage
import com.financetracker.data.local.dao.TransactionDao
import com.financetracker.data.mapper.TransactionMapper
import com.financetracker.data.model.TransactionUiModel
import com.financetracker.data.network.TransactionApiService
import com.financetracker.data.network.dto.CreateTransactionRequestDto
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao,
    private val api: TransactionApiService,
    private val tokenStorage: TokenStorage,
) : TransactionRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getTransactions(category: String?): Flow<PagingData<TransactionUiModel>> {
        val userId = tokenStorage.getUserId() ?: return emptyFlow()

        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 5,
                enablePlaceholders = false,
            ),
            remoteMediator = TransactionRemoteMediator(
                userId = userId,
                category = category,
                dao = dao,
                api = api,
                tokenStorage = tokenStorage,
            ),
            pagingSourceFactory = {
                if (category != null) {
                    dao.getTransactionsByCategory(userId, category)
                } else {
                    dao.getTransactions(userId)
                }
            },
        ).flow.map { pagingData ->
            with(TransactionMapper) { pagingData.map { it.toUiModel() } }
        }
    }

    override suspend fun createTransaction(
        amount: Double,
        category: String,
        description: String?,
        idempotencyKey: String?,
    ): TransactionRepository.CreateResult {
        val token = tokenStorage.getAccessToken()
            ?: return TransactionRepository.CreateResult.NetworkError

        return withContext(Dispatchers.IO) {
            runCatching {
                val response = api.createTransaction(
                    token = "Bearer $token",
                    body = CreateTransactionRequestDto(
                        amount = amount,
                        category = category,
                        description = description,
                        idempotencyKey = idempotencyKey,
                    ),
                )
                when {
                    response.code() == 201 -> {
                        val body = response.body()!!
                        with(TransactionMapper) {
                            val entity = body.toEntity()
                            dao.upsertAll(listOf(entity))
                            TransactionRepository.CreateResult.Success(entity.toUiModel())
                        }
                    }
                    response.code() == 200 -> {
                        val body = response.body()!!
                        with(TransactionMapper) {
                            TransactionRepository.CreateResult.Duplicate(body.toEntity().toUiModel())
                        }
                    }
                    else -> TransactionRepository.CreateResult.Error(
                        "Server error: ${response.code()}",
                    )
                }
            }.getOrElse {
                TransactionRepository.CreateResult.NetworkError
            }
        }
    }
}
