package com.financetracker

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.financetracker.sync.work.SyncTransactionsWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject lateinit var workManager: WorkManager

    override fun onCreate() {
        super.onCreate()
        enqueueSyncWorker()
    }

    private fun enqueueSyncWorker() {
        // TODO: cancel SyncTransactionsWorker on logout (Sprint 3 polish)
        workManager.enqueueUniquePeriodicWork(
            SyncTransactionsWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            SyncTransactionsWorker.buildRequest(),
        )
    }
}
