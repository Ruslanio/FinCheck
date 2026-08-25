package com.financetracker.data.repository

import com.financetracker.data.storage.AppLockPreferencesDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppLockRepositoryImpl @Inject constructor(
    private val dataSource: AppLockPreferencesDataSource,
) : AppLockRepository {
    override val isBiometricLockEnabled: Flow<Boolean> = dataSource.isBiometricLockEnabled

    override suspend fun setBiometricLockEnabled(enabled: Boolean) =
        dataSource.setBiometricLockEnabled(enabled)
}
