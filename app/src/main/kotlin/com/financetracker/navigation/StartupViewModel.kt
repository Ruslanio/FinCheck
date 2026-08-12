package com.financetracker.navigation

import androidx.lifecycle.ViewModel
import com.financetracker.core.data.storage.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StartupViewModel @Inject constructor(
    private val tokenStorage: TokenStorage,
) : ViewModel() {

    fun hasValidToken(): Boolean = tokenStorage.hasValidToken()
}
