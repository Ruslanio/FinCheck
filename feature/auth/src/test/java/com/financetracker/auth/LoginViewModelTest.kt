package com.financetracker.auth

import app.cash.turbine.test
import com.financetracker.auth.ui.AuthUiState
import com.financetracker.auth.ui.LoginViewModel
import com.financetracker.data.repository.AuthRepository
import com.financetracker.data.repository.AuthResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val authRepository: AuthRepository = mockk()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `blank email emits Error and never calls repo`() = runTest {
        viewModel.email = ""
        viewModel.password = "password123"

        viewModel.uiState.test {
            skipItems(1) // Idle
            viewModel.onLoginClick()
            assertEquals(AuthUiState.Error(LoginViewModel.ERROR_EMAIL_REQUIRED), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    @Test
    fun `short password emits Error and never calls repo`() = runTest {
        viewModel.email = "user@example.com"
        viewModel.password = "short"

        viewModel.uiState.test {
            skipItems(1) // Idle
            viewModel.onLoginClick()
            assertEquals(AuthUiState.Error(LoginViewModel.ERROR_PASSWORD_TOO_SHORT), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    @Test
    fun `valid input emits Loading then Success`() = runTest {
        viewModel.email = "user@example.com"
        viewModel.password = "password123"
        // delay(1) creates a suspension point so Loading is observable before Success
        coEvery { authRepository.login(any(), any()) } coAnswers {
            delay(1)
            AuthResult.Success
        }

        viewModel.uiState.test {
            skipItems(1) // Idle
            viewModel.onLoginClick()
            assertEquals(AuthUiState.Loading, awaitItem())
            assertEquals(AuthUiState.Success, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `valid input and repo Error emits Error with correct message`() = runTest {
        viewModel.email = "user@example.com"
        viewModel.password = "password123"
        coEvery { authRepository.login(any(), any()) } coAnswers {
            delay(1)
            AuthResult.Error("error_invalid_credentials")
        }

        viewModel.uiState.test {
            skipItems(1) // Idle
            viewModel.onLoginClick()
            assertEquals(AuthUiState.Loading, awaitItem())
            assertEquals(AuthUiState.Error("error_invalid_credentials"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `valid input and NetworkError emits Error with network code`() = runTest {
        viewModel.email = "user@example.com"
        viewModel.password = "password123"
        coEvery { authRepository.login(any(), any()) } coAnswers {
            delay(1)
            AuthResult.NetworkError
        }

        viewModel.uiState.test {
            skipItems(1) // Idle
            viewModel.onLoginClick()
            assertEquals(AuthUiState.Loading, awaitItem())
            assertEquals(AuthUiState.Error(LoginViewModel.ERROR_NETWORK), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
