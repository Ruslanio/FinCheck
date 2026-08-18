package com.financetracker.work

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class WorkManagerInitializer : Initializer<WorkManager> {

    override fun create(context: Context): WorkManager {
        val config = Configuration.Builder()
            .setWorkerFactory(getHiltWorkerFactory(context))
            .build()
        WorkManager.initialize(context, config)
        return WorkManager.getInstance(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    companion object {
        fun getHiltWorkerFactory(context: Context): HiltWorkerFactory {
            val entryPoint = EntryPointAccessors.fromApplication(
                context,
                HiltWorkerFactoryEntryPoint::class.java,
            )
            return entryPoint.hiltWorkerFactory()
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HiltWorkerFactoryEntryPoint {
    fun hiltWorkerFactory(): HiltWorkerFactory
}
