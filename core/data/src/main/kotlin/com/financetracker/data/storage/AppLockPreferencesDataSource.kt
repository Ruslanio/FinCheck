package com.financetracker.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppLockPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val isBiometricLockEnabled: Flow<Boolean> = dataStore.data
        .map { it[BIOMETRIC_LOCK_ENABLED] ?: false }

    suspend fun setBiometricLockEnabled(enabled: Boolean) {
        dataStore.edit { it[BIOMETRIC_LOCK_ENABLED] = enabled }
    }

    private companion object {
        val BIOMETRIC_LOCK_ENABLED = booleanPreferencesKey("biometric_lock_enabled")
    }
}
