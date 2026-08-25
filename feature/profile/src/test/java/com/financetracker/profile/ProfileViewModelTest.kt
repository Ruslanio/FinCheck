package com.financetracker.profile

import app.cash.turbine.test
import com.financetracker.core.security.BiometricAuthManager
import com.financetracker.data.repository.AppLockRepository
import com.financetracker.data.repository.AuthRepository
import com.financetracker.profile.ui.ProfileUiEvent
import com.financetracker.profile.ui.ProfileViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val authRepository = mockk<AuthRepository>()
    private val appLockRepository = mockk<AppLockRepository>()
    private val biometricAuthManager = mockk<BiometricAuthManager>()
    private val preferenceFlow = MutableStateFlow(false)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { appLockRepository.isBiometricLockEnabled } returns preferenceFlow
        every { biometricAuthManager.canAuthenticate() } returns true
        coEvery { authRepository.logout() } returns Unit
        coEvery { appLockRepository.setBiometricLockEnabled(any()) } answers {
            preferenceFlow.value = firstArg()
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        ProfileViewModel(authRepository, appLockRepository, biometricAuthManager)

    @Test
    fun `default state has biometric lock disabled`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.biometricLockEnabled)
    }

    @Test
    fun `default state reflects biometric availability`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.biometricAvailable)
    }

    @Test
    fun `unsupported device has biometricAvailable false`() = runTest {
        every { biometricAuthManager.canAuthenticate() } returns false
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.biometricAvailable)
    }

    @Test
    fun `onBiometricToggle true emits RequestBiometricVerification`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onBiometricToggle(true)
            assertEquals(ProfileUiEvent.RequestBiometricVerification, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onBiometricVerificationResult true persists enabled`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onBiometricVerificationResult(true)
        advanceUntilIdle()

        coVerify { appLockRepository.setBiometricLockEnabled(true) }
        assertTrue(viewModel.uiState.value.biometricLockEnabled)
    }

    @Test
    fun `onBiometricVerificationResult false does not persist enabled`() = runTest {
        preferenceFlow.value = true
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onBiometricVerificationResult(false)
        advanceUntilIdle()

        coVerify(exactly = 0) { appLockRepository.setBiometricLockEnabled(true) }
    }

    @Test
    fun `onBiometricToggle false persists disabled immediately`() = runTest {
        preferenceFlow.value = true
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onBiometricToggle(false)
        advanceUntilIdle()

        coVerify { appLockRepository.setBiometricLockEnabled(false) }
        assertFalse(viewModel.uiState.value.biometricLockEnabled)
    }

    @Test
    fun `onLogoutClick emits NavigateToAuth`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onLogoutClick()
            assertEquals(ProfileUiEvent.NavigateToAuth, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
