package com.financetracker.ui

import androidx.lifecycle.ViewModel
import com.financetracker.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StartupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    fun isUserLoggedIn(): Boolean = authRepository.isUserLoggedIn()
}
