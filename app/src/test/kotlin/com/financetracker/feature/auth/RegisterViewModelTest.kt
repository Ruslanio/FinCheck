package com.financetracker.feature.auth

import app.cash.turbine.test
import com.financetracker.data.repository.AuthRepository
import com.financetracker.data.repository.AuthResult
import com.financetracker.feature.auth.ui.AuthUiState
import com.financetracker.feature.auth.ui.RegisterViewModel
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
class RegisterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val authRepository: AuthRepository = mockk()
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RegisterViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `blank email emits Error and never calls repo`() = runTest {
        viewModel.email = ""
        viewModel.password = "password123"
        viewModel.confirmPassword = "password123"

        viewModel.uiState.test {
            skipItems(1) // Idle
            viewModel.onRegisterClick()
            assertEquals(AuthUiState.Error(RegisterViewModel.ERROR_EMAIL_REQUIRED), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { authRepository.register(any(), any()) }
    }

    @Test
    fun `short password emits Error and never calls repo`() = runTest {
        viewModel.email = "user@example.com"
        viewModel.password = "short"
        viewModel.confirmPassword = "short"

        viewModel.uiState.test {
            skipItems(1) // Idle
            viewModel.onRegisterClick()
            assertEquals(AuthUiState.Error(RegisterViewModel.ERROR_PASSWORD_TOO_SHORT), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { authRepository.register(any(), any()) }
    }

    @Test
    fun `valid input emits Loading then Success`() = runTest {
        viewModel.email = "user@example.com"
        viewModel.password = "password123"
        viewModel.confirmPassword = "password123"
        coEvery { authRepository.register(any(), any()) } coAnswers {
            delay(1)
            AuthResult.Success
        }

        viewModel.uiState.test {
            skipItems(1) // Idle
            viewModel.onRegisterClick()
            assertEquals(AuthUiState.Loading, awaitItem())
            assertEquals(AuthUiState.Success, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `repo returns email_already_registered Error emits Error state`() = runTest {
        viewModel.email = "user@example.com"
        viewModel.password = "password123"
        viewModel.confirmPassword = "password123"
        coEvery { authRepository.register(any(), any()) } coAnswers {
            delay(1)
            AuthResult.Error("error_email_already_registered")
        }

        viewModel.uiState.test {
            skipItems(1) // Idle
            viewModel.onRegisterClick()
            assertEquals(AuthUiState.Loading, awaitItem())
            assertEquals(AuthUiState.Error("error_email_already_registered"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `NetworkError emits Error with network code`() = runTest {
        viewModel.email = "user@example.com"
        viewModel.password = "password123"
        viewModel.confirmPassword = "password123"
        coEvery { authRepository.register(any(), any()) } coAnswers {
            delay(1)
            AuthResult.NetworkError
        }

        viewModel.uiState.test {
            skipItems(1) // Idle
            viewModel.onRegisterClick()
            assertEquals(AuthUiState.Loading, awaitItem())
            assertEquals(AuthUiState.Error(RegisterViewModel.ERROR_NETWORK), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
