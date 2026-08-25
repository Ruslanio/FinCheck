package com.financetracker.core.security.di

import android.content.Context
import com.financetracker.core.security.BiometricAuthManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideBiometricAuthManager(
        @ApplicationContext context: Context,
    ): BiometricAuthManager = BiometricAuthManager(context)
}
