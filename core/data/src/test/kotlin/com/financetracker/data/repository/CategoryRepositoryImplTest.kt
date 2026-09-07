package com.financetracker.data.repository

import app.cash.turbine.test
import com.financetracker.data.storage.TokenStorage
import com.financetracker.database.dao.CategoryDao
import com.financetracker.database.entity.CategoryEntity
import com.financetracker.network.dto.CategoryResponseDto
import com.financetracker.network.service.CategoryApiService
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class CategoryRepositoryImplTest {

    private val dao = mockk<CategoryDao>()
    private val api = mockk<CategoryApiService>()
    private val tokenStorage = mockk<TokenStorage>()

    private lateinit var repo: CategoryRepositoryImpl

    @Before
    fun setup() {
        repo = CategoryRepositoryImpl(dao, api, tokenStorage)
    }

    @Test
    fun getCategories_emitsFromDao() = runTest {
        val entities = listOf(makeEntity("c1"), makeEntity("c2"))
        every { dao.getAll() } returns flowOf(entities)

        repo.getCategories().test {
            val categories = awaitItem()
            assertEquals(2, categories.size)
            assertEquals("c1", categories[0].id)
            awaitComplete()
        }
    }

    @Test
    fun syncCategories_returnsFailureWhenNoToken() = runTest {
        every { tokenStorage.getAccessToken() } returns null

        val result = repo.syncCategories()

        assertTrue(result is CategoryRepository.SyncResult.Failure)
    }

    @Test
    fun syncCategories_callsReconcileOnSuccess() = runTest {
        every { tokenStorage.getAccessToken() } returns "token"
        val dtos = listOf(makeDto("c1"), makeDto("c2"))
        val response = mockk<Response<List<CategoryResponseDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns dtos
        coEvery { api.getCategories(any()) } returns response
        val reconcileSlot = slot<List<CategoryEntity>>()
        coJustRun { dao.reconcile(capture(reconcileSlot)) }

        val result = repo.syncCategories()

        assertTrue(result is CategoryRepository.SyncResult.Success)
        coVerify(exactly = 1) { dao.reconcile(any()) }
        assertEquals(2, reconcileSlot.captured.size)
    }

    @Test
    fun syncCategories_returnsRetryOnApiFailure() = runTest {
        every { tokenStorage.getAccessToken() } returns "token"
        val response = mockk<Response<List<CategoryResponseDto>>>()
        every { response.isSuccessful } returns false
        every { response.code() } returns 500
        coEvery { api.getCategories(any()) } returns response

        val result = repo.syncCategories()

        assertTrue(result is CategoryRepository.SyncResult.Retry)
    }

    @Test
    fun syncCategories_returnsRetryOnNetworkException() = runTest {
        every { tokenStorage.getAccessToken() } returns "token"
        coEvery { api.getCategories(any()) } throws java.io.IOException("Network error")

        val result = repo.syncCategories()

        assertTrue(result is CategoryRepository.SyncResult.Retry)
    }

    @Test
    fun syncCategories_reconcileIncludesAddedUpdatedAndRemovesAbsent() = runTest {
        every { tokenStorage.getAccessToken() } returns "token"
        val serverCategories = listOf(
            makeDto("c1", name = "Renamed Food"),
            makeDto("c3", name = "New Category"),
        )
        val response = mockk<Response<List<CategoryResponseDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns serverCategories
        coEvery { api.getCategories(any()) } returns response
        val reconcileSlot = slot<List<CategoryEntity>>()
        coJustRun { dao.reconcile(capture(reconcileSlot)) }

        repo.syncCategories()

        val ids = reconcileSlot.captured.map { it.id }
        assertTrue(ids.contains("c1"))
        assertTrue(ids.contains("c3"))
        assertEquals("Renamed Food", reconcileSlot.captured.find { it.id == "c1" }?.name)
    }

    @Test
    fun syncCategories_sendsBearerToken() = runTest {
        every { tokenStorage.getAccessToken() } returns "mytoken"
        val tokenSlot = slot<String>()
        val response = mockk<Response<List<CategoryResponseDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns emptyList()
        coEvery { api.getCategories(capture(tokenSlot)) } returns response
        coJustRun { dao.reconcile(any()) }

        repo.syncCategories()

        assertEquals("Bearer mytoken", tokenSlot.captured)
    }

    private fun makeDto(
        id: String = "cat-1",
        name: String = "Food",
        type: String = "expense",
        isFallback: Boolean = false,
    ) = CategoryResponseDto(id = id, name = name, type = type, isFallback = isFallback)

    private fun makeEntity(
        id: String = "cat-1",
        name: String = "Food",
        type: String = "expense",
        isFallback: Boolean = false,
    ) = CategoryEntity(id = id, name = name, type = type, isFallback = isFallback)
}
