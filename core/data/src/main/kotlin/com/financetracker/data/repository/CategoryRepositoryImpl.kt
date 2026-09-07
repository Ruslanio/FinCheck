package com.financetracker.data.repository

import com.financetracker.data.mapper.CategoryMapper
import com.financetracker.data.model.Category
import com.financetracker.data.storage.TokenStorage
import com.financetracker.database.dao.CategoryDao
import com.financetracker.network.service.CategoryApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao,
    private val api: CategoryApiService,
    private val tokenStorage: TokenStorage,
) : CategoryRepository {

    override fun getCategories(): Flow<List<Category>> =
        dao.getAll().map { entities ->
            with(CategoryMapper) { entities.map { it.toDomain() } }
        }

    override suspend fun syncCategories(): CategoryRepository.SyncResult {
        val token = tokenStorage.getAccessToken()
            ?: return CategoryRepository.SyncResult.Failure

        return withContext(Dispatchers.IO) {
            runCatching {
                val response = api.getCategories(token = "Bearer $token")

                if (!response.isSuccessful) {
                    return@withContext CategoryRepository.SyncResult.Retry
                }

                val entities = with(CategoryMapper) {
                    response.body()!!.map { it.toEntity() }
                }

                dao.reconcile(entities)

                CategoryRepository.SyncResult.Success
            }.getOrElse {
                CategoryRepository.SyncResult.Retry
            }
        }
    }
}
