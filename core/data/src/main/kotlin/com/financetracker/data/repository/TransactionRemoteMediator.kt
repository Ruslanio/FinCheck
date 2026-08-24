package com.financetracker.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.financetracker.data.mapper.TransactionMapper
import com.financetracker.data.storage.TokenStorage
import com.financetracker.database.dao.TransactionDao
import com.financetracker.database.entity.TransactionEntity
import com.financetracker.network.service.TransactionApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalPagingApi::class)
class TransactionRemoteMediator(
    private val userId: String,
    private val category: String?,
    private val dao: TransactionDao,
    private val api: TransactionApiService,
    private val tokenStorage: TokenStorage,
) : RemoteMediator<Int, TransactionEntity>() {

    private var currentPage = 0

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TransactionEntity>,
    ): MediatorResult {
        return withContext(Dispatchers.IO) {
            runCatching {
                val page = when (loadType) {
                    LoadType.REFRESH -> {
                        currentPage = 0
                        0
                    }
                    LoadType.PREPEND -> return@withContext MediatorResult.Success(
                        endOfPaginationReached = true,
                    )
                    LoadType.APPEND -> currentPage + 1
                }

                val token = tokenStorage.getAccessToken()
                    ?: return@withContext MediatorResult.Error(
                        IllegalStateException("No access token"),
                    )

                val response = api.getTransactions(
                    token = "Bearer $token",
                    page = page,
                    size = state.config.pageSize,
                    category = category,
                )

                if (!response.isSuccessful) {
                    return@withContext MediatorResult.Error(
                        Exception("API error: ${response.code()}"),
                    )
                }

                val body = response.body()!!
                val entities = with(TransactionMapper) { body.data.map { it.toEntity() } }

                withContext(Dispatchers.IO) {
                    if (loadType == LoadType.REFRESH) {
                        dao.clearAll(userId)
                    }
                    dao.upsertAll(entities)
                    currentPage = page
                }

                MediatorResult.Success(
                    endOfPaginationReached = entities.size < state.config.pageSize,
                )
            }.getOrElse { throwable ->
                MediatorResult.Error(throwable)
            }
        }
    }
}
