package com.financetracker.core.security

import androidx.biometric.BiometricPrompt
import com.financetracker.data.storage.TokenStorage
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
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
    private val tokenStorage = mockk<TokenStorage>()
    private lateinit var viewModel: AppLockViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { tokenStorage.hasValidToken() } returns true
        every { biometricAuthManager.canAuthenticate() } returns true
        viewModel = AppLockViewModel(biometricAuthManager, tokenStorage)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onAppForeground no valid token returns false and state is Unavailable`() {
        every { tokenStorage.hasValidToken() } returns false

        val result = viewModel.onAppForeground()

        assertFalse(result)
        assertEquals(BiometricState.Unavailable, viewModel.state.value)
    }

    @Test
    fun `onAppForeground canAuthenticate false returns false and state is Unavailable`() {
        every { biometricAuthManager.canAuthenticate() } returns false

        val result = viewModel.onAppForeground()

        assertFalse(result)
        assertEquals(BiometricState.Unavailable, viewModel.state.value)
    }

    @Test
    fun `onAppForeground first call with default backgroundTimestamp returns true and state is Authenticating`() {
        // backgroundTimestamp = 0 by default → duration = currentTime >> 30s
        val result = viewModel.onAppForeground()

        assertTrue(result)
        assertEquals(BiometricState.Authenticating, viewModel.state.value)
    }

    @Test
    fun `onAppForeground within 30s of last auth returns false and state is Authenticated`() {
        // backgroundTimestamp = 0 → background duration >> 30s, so grace period check fails
        // but recentlyAuthed is true since we just called onAuthSuccess
        viewModel.onAuthSuccess()

        val result = viewModel.onAppForeground()

        assertFalse(result)
        assertEquals(BiometricState.Authenticated, viewModel.state.value)
    }

    @Test
    fun `onAppForeground background under 30s returns false and state is Authenticated`() {
        viewModel.onAppBackground() // sets backgroundTimestamp to now

        // Immediately call onAppForeground — elapsed ≈ 0ms < 30s grace period
        val result = viewModel.onAppForeground()

        assertFalse(result)
        assertEquals(BiometricState.Authenticated, viewModel.state.value)
    }

    @Test
    fun `onAppForeground background over 30s returns true and state is Authenticating`() {
        // backgroundTimestamp is 0 (never set via onAppBackground) → duration is very large
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
