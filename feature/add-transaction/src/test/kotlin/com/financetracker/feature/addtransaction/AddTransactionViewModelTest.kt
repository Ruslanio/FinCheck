package com.financetracker.feature.addtransaction

import app.cash.turbine.test
import com.financetracker.data.model.TransactionUiModel
import com.financetracker.data.repository.TransactionRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<TransactionRepository>()
    private lateinit var viewModel: AddTransactionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AddTransactionViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addTransaction Loading state emitted before repo call completes`() = runTest {
        coEvery { repository.createTransaction(any(), any(), any(), any()) } coAnswers {
            delay(1)
            TransactionRepository.CreateResult.NetworkError
        }

        viewModel.uiState.test {
            skipItems(1) // Idle
            viewModel.addTransaction(10.0, "Food", null)
            assertEquals(AddTransactionUiState.Loading, awaitItem())
            skipItems(1) // final Error state
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addTransaction Success emits Success uiState and Success event`() = runTest {
        val tx = fakeTransaction()
        coEvery { repository.createTransaction(any(), any(), any(), any()) } coAnswers {
            delay(1)
            TransactionRepository.CreateResult.Success(tx)
        }

        viewModel.events.test {
            viewModel.uiState.test {
                skipItems(1) // Idle
                viewModel.addTransaction(10.0, "Food", null)
                assertEquals(AddTransactionUiState.Loading, awaitItem())
                assertEquals(AddTransactionUiState.Success, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(AddTransactionEvent.Success(tx), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addTransaction Duplicate emits Success uiState and Duplicate event`() = runTest {
        val tx = fakeTransaction()
        coEvery { repository.createTransaction(any(), any(), any(), any()) } coAnswers {
            delay(1)
            TransactionRepository.CreateResult.Duplicate(tx)
        }

        viewModel.events.test {
            viewModel.uiState.test {
                skipItems(1) // Idle
                viewModel.addTransaction(10.0, "Food", null)
                assertEquals(AddTransactionUiState.Loading, awaitItem())
                assertEquals(AddTransactionUiState.Success, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(AddTransactionEvent.Duplicate(tx), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addTransaction Error emits Error uiState and Error event`() = runTest {
        coEvery { repository.createTransaction(any(), any(), any(), any()) } coAnswers {
            delay(1)
            TransactionRepository.CreateResult.Error("bad request")
        }

        viewModel.events.test {
            viewModel.uiState.test {
                skipItems(1) // Idle
                viewModel.addTransaction(10.0, "Food", null)
                assertEquals(AddTransactionUiState.Loading, awaitItem())
                assertTrue(awaitItem() is AddTransactionUiState.Error)
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(AddTransactionEvent.Error("bad request"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addTransaction NetworkError emits Error uiState and NetworkError event`() = runTest {
        coEvery { repository.createTransaction(any(), any(), any(), any()) } coAnswers {
            delay(1)
            TransactionRepository.CreateResult.NetworkError
        }

        viewModel.events.test {
            viewModel.uiState.test {
                skipItems(1) // Idle
                viewModel.addTransaction(10.0, "Food", null)
                assertEquals(AddTransactionUiState.Loading, awaitItem())
                assertTrue(awaitItem() is AddTransactionUiState.Error)
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(AddTransactionEvent.NetworkError, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addTransaction second call while Loading is ignored`() = runTest {
        coEvery { repository.createTransaction(any(), any(), any(), any()) } coAnswers {
            delay(1)
            TransactionRepository.CreateResult.NetworkError
        }

        viewModel.uiState.test {
            skipItems(1) // Idle
            viewModel.addTransaction(10.0, "Food", null)
            assertEquals(AddTransactionUiState.Loading, awaitItem())

            viewModel.addTransaction(20.0, "Transport", null) // guard sees Loading — ignored

            skipItems(1) // final Error state from first call
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { repository.createTransaction(any(), any(), any(), any()) }
    }

    private fun fakeTransaction() = TransactionUiModel(
        id = "t1",
        amount = 10.0,
        category = "Food",
        description = null,
        occurredAt = 1_000_000L,
        isExpense = false,
    )
}
