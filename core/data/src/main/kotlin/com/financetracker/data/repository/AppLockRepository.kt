package com.financetracker.data.repository

import kotlinx.coroutines.flow.Flow

interface AppLockRepository {
    val isBiometricLockEnabled: Flow<Boolean>
    suspend fun setBiometricLockEnabled(enabled: Boolean)
}
