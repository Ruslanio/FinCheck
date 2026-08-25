package com.financetracker.feature.applock

import androidx.biometric.BiometricPrompt
import com.financetracker.core.security.BiometricAuthManager
import com.financetracker.data.repository.AppLockRepository
import com.financetracker.data.repository.AuthRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class AppLockViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val biometricAuthManager = mockk<BiometricAuthManager>()
    private val authRepository = mockk<AuthRepository>()
    private val appLockRepository = mockk<AppLockRepository>()
    private lateinit var viewModel: AppLockViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { authRepository.isUserLoggedIn() } returns true
        every { biometricAuthManager.canAuthenticate() } returns true
        every { appLockRepository.isBiometricLockEnabled } returns flowOf(true)
        viewModel = AppLockViewModel(biometricAuthManager, authRepository, appLockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onAppForeground not logged in returns false and state is Unavailable`() = runTest {
        every { authRepository.isUserLoggedIn() } returns false
        advanceUntilIdle()

        val result = viewModel.onAppForeground()

        assertFalse(result)
        assertEquals(BiometricState.Unavailable, viewModel.state.value)
    }

    @Test
    fun `onAppForeground biometric preference disabled returns false without changing state`() = runTest {
        val disabledRepo = mockk<AppLockRepository>()
        every { disabledRepo.isBiometricLockEnabled } returns flowOf(false)
        val vm = AppLockViewModel(biometricAuthManager, authRepository, disabledRepo)
        advanceUntilIdle()

        val result = vm.onAppForeground()

        assertFalse(result)
        assertEquals(BiometricState.Idle, vm.state.value)
    }

    @Test
    fun `onAppForeground canAuthenticate false returns false and state is Unavailable`() = runTest {
        every { biometricAuthManager.canAuthenticate() } returns false
        advanceUntilIdle()

        val result = viewModel.onAppForeground()

        assertFalse(result)
        assertEquals(BiometricState.Unavailable, viewModel.state.value)
    }

    @Test
    fun `onAppForeground first call with default backgroundTimestamp returns true and state is Authenticating`() = runTest {
        advanceUntilIdle()

        val result = viewModel.onAppForeground()

        assertTrue(result)
        assertEquals(BiometricState.Authenticating, viewModel.state.value)
    }

    @Test
    fun `onAppForeground within 30s of last auth returns false and state is Authenticated`() = runTest {
        advanceUntilIdle()
        viewModel.onAuthSuccess()

        val result = viewModel.onAppForeground()

        assertFalse(result)
        assertEquals(BiometricState.Authenticated, viewModel.state.value)
    }

    @Test
    fun `onAppForeground background under 30s returns false and state is Authenticated`() = runTest {
        advanceUntilIdle()
        viewModel.onAppBackground()

        val result = viewModel.onAppForeground()

        assertFalse(result)
        assertEquals(BiometricState.Authenticated, viewModel.state.value)
    }

    @Test
    fun `onAppForeground background over 30s not recently authed returns true and state is Authenticating`() = runTest {
        advanceUntilIdle()

        // backgroundTimestamp = 0 by default → duration >> 30s
        val result = viewModel.onAppForeground()

        assertTrue(result)
        assertEquals(BiometricState.Authenticating, viewModel.state.value)
    }

    @Test
    fun `onAuthSuccess sets state to Authenticated`() {
        viewModel.onAuthSuccess()

        assertEquals(BiometricState.Authenticated, viewModel.state.value)
    }

    @Test
    fun `onAuthError USER_CANCELED sets state to Locked`() {
        viewModel.onAuthError(BiometricPrompt.ERROR_USER_CANCELED)

        assertEquals(BiometricState.Locked, viewModel.state.value)
    }

    @Test
    fun `onAuthError ERROR_NEGATIVE_BUTTON sets state to Locked`() {
        viewModel.onAuthError(BiometricPrompt.ERROR_NEGATIVE_BUTTON)

        assertEquals(BiometricState.Locked, viewModel.state.value)
    }

    @Test
    fun `onAuthError ERROR_CANCELED sets state to Locked`() {
        viewModel.onAuthError(BiometricPrompt.ERROR_CANCELED)

        assertEquals(BiometricState.Locked, viewModel.state.value)
    }

    @Test
    fun `onAuthError other code sets state to Failed`() {
        viewModel.onAuthError(BiometricPrompt.ERROR_LOCKOUT)

        assertEquals(BiometricState.Failed, viewModel.state.value)
    }

    @Test
    fun `onAuthFailed sets state to Failed not Locked`() {
        viewModel.onAuthFailed()

        assertEquals(BiometricState.Failed, viewModel.state.value)
    }

    @Test
    fun `resetToIdle sets state to Idle`() {
        viewModel.onAuthSuccess()
        viewModel.resetToIdle()

        assertEquals(BiometricState.Idle, viewModel.state.value)
    }
}
