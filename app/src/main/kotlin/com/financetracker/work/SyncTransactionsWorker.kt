package com.financetracker.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.financetracker.core.data.storage.TokenStorage
import com.financetracker.data.local.dao.TransactionDao
import com.financetracker.data.mapper.TransactionMapper
import com.financetracker.data.network.TransactionApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SyncTransactionsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val api: TransactionApiService,
    private val dao: TransactionDao,
    private val tokenStorage: TokenStorage,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val token = tokenStorage.getAccessToken()
            ?: return Result.failure()
        val userId = tokenStorage.getUserId()
            ?: return Result.failure()

        return withContext(Dispatchers.IO) {
            runCatching {
                val latestTimestamp = dao.getLatestTimestamp(userId)

                val response = api.getTransactions(
                    token = "Bearer $token",
                    page = 0,
                    size = 50,
                    category = null,
                )

                if (!response.isSuccessful) {
                    return@withContext Result.retry()
                }

                val body = response.body()!!
                val newEntities = with(TransactionMapper) {
                    body.data
                        .map { it.toEntity() }
                        .filter { entity ->
                            latestTimestamp == null || entity.occurredAt > latestTimestamp
                        }
                }

                if (newEntities.isNotEmpty()) {
                    dao.upsertAll(newEntities)
                }

                Result.success()
            }.getOrElse {
                Result.retry()
            }
        }
    }

    companion object {
        const val WORK_NAME = "SyncTransactionsWorker"

        fun buildRequest() =
            PeriodicWorkRequestBuilder<SyncTransactionsWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS,
                )
                .build()
    }
}
