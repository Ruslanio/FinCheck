package com.financetracker.data.repository

import app.cash.turbine.test
import com.financetracker.data.local.dao.TransactionDao
import com.financetracker.data.network.TransactionApiService
import com.financetracker.data.network.dto.TransactionResponseDto
import com.financetracker.data.storage.TokenStorage
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class TransactionRepositoryImplTest {

    private val dao = mockk<TransactionDao>()
    private val api = mockk<TransactionApiService>()
    private val tokenStorage = mockk<TokenStorage>()

    private lateinit var repo: TransactionRepositoryImpl

    @Before
    fun setup() {
        repo = TransactionRepositoryImpl(dao, api, tokenStorage)
    }

    // region getTransactions

    @Test
    fun getTransactions_returnsEmptyFlowWhenUserIdIsNull() = runTest {
        every { tokenStorage.getUserId() } returns null
        repo.getTransactions().test {
            awaitComplete()
        }
    }

    // endregion

    // region createTransaction

    @Test
    fun createTransaction_returnsNetworkErrorWhenNoToken() = runTest {
        every { tokenStorage.getAccessToken() } returns null
        val result = repo.createTransaction(10.0, "food", null, null)
        assertEquals(TransactionRepository.CreateResult.NetworkError, result)
    }

    @Test
    fun createTransaction_201_returnsSuccessAndCallsUpsert() = runTest {
        val token = "test-token"
        every { tokenStorage.getAccessToken() } returns token
        val mockResponse = mockk<Response<TransactionResponseDto>>()
        every { mockResponse.code() } returns 201
        every { mockResponse.body() } returns makeDto()
        coEvery { api.createTransaction(any(), any()) } returns mockResponse
        coJustRun { dao.upsertAll(any()) }

        val result = repo.createTransaction(10.0, "food", null, null)

        assertTrue(result is TransactionRepository.CreateResult.Success)
        coVerify(exactly = 1) { dao.upsertAll(any()) }
    }

    @Test
    fun createTransaction_200_returnsDuplicate() = runTest {
        val token = "test-token"
        every { tokenStorage.getAccessToken() } returns token
        val mockResponse = mockk<Response<TransactionResponseDto>>()
        every { mockResponse.code() } returns 200
        every { mockResponse.body() } returns makeDto()
        coEvery { api.createTransaction(any(), any()) } returns mockResponse

        val result = repo.createTransaction(10.0, "food", null, null)

        assertTrue(result is TransactionRepository.CreateResult.Duplicate)
    }

    @Test
    fun createTransaction_networkException_returnsNetworkError() = runTest {
        every { tokenStorage.getAccessToken() } returns "token"
        coEvery { api.createTransaction(any(), any()) } throws IOException("Network failure")

        val result = repo.createTransaction(10.0, "food", null, null)

        assertEquals(TransactionRepository.CreateResult.NetworkError, result)
    }

    @Test
    fun createTransaction_passesBearerTokenAsAuthorizationHeader() = runTest {
        val token = "mytoken"
        every { tokenStorage.getAccessToken() } returns token
        val capturedToken = slot<String>()
        val mockResponse = mockk<Response<TransactionResponseDto>>()
        every { mockResponse.code() } returns 201
        every { mockResponse.body() } returns makeDto()
        coEvery { api.createTransaction(capture(capturedToken), any()) } returns mockResponse
        coJustRun { dao.upsertAll(any()) }

        repo.createTransaction(10.0, "food", null, null)

        assertEquals("Bearer mytoken", capturedToken.captured)
    }

    // endregion

    private fun makeDto() = TransactionResponseDto(
        id = "1",
        userId = "u1",
        amount = 10.0,
        category = "food",
        description = null,
        idempotencyKey = null,
        occurredAt = "2024-01-15T10:30:00Z",
    )
}
