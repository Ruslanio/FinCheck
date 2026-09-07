package com.financetracker.sync.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.financetracker.data.repository.CategoryRepository
import com.financetracker.data.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncTransactionsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val categoryResult = categoryRepository.syncCategories()
        val txResult = transactionRepository.syncTransactions()

        return when {
            txResult == TransactionRepository.SyncResult.Failure ||
                categoryResult == CategoryRepository.SyncResult.Failure -> Result.failure()
            txResult == TransactionRepository.SyncResult.Retry ||
                categoryResult == CategoryRepository.SyncResult.Retry -> Result.retry()
            else -> Result.success()
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
