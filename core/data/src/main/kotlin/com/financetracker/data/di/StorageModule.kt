package com.financetracker.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.financetracker.data.storage.TokenStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.appLockDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_lock_prefs",
)

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideTokenStorage(@ApplicationContext context: Context): TokenStorage =
        TokenStorage(context)

    @Provides
    @Singleton
    fun provideAppLockDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.appLockDataStore
}
