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
import com.financetracker.data.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncTransactionsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val transactionRepository: TransactionRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = when (transactionRepository.syncTransactions()) {
        TransactionRepository.SyncResult.Success -> Result.success()
        TransactionRepository.SyncResult.Retry -> Result.retry()
        TransactionRepository.SyncResult.Failure -> Result.failure()
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
