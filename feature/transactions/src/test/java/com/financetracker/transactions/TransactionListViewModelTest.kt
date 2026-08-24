package com.financetracker.transactions

import androidx.paging.PagingData
import app.cash.turbine.test
import com.financetracker.data.model.TransactionUiModel
import com.financetracker.data.repository.TransactionRepository
import com.financetracker.transactions.ui.AddTransactionEvent
import com.financetracker.transactions.ui.TransactionListUiState
import com.financetracker.transactions.ui.TransactionListViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<TransactionRepository>()
    private lateinit var viewModel: TransactionListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getTransactions(any()) } returns flowOf(PagingData.empty())
        viewModel = TransactionListViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region transactions flow

    @Test
    fun `transactions calls repository getTransactions with null category initially`() = runTest {
        viewModel.transactions.test {
            awaitItem()
            verify { repository.getTransactions(category = null) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region categoryFilter

    @Test
    fun `categoryFilter initial value is null`() = runTest {
        assertNull(viewModel.categoryFilter.value)
    }

    @Test
    fun `setCategory emits new category value`() = runTest {
        viewModel.categoryFilter.test {
            skipItems(1) // initial null
            viewModel.setCategory("food")
            assertEquals("food", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearCategory emits null after being set`() = runTest {
        viewModel.setCategory("food")
        viewModel.categoryFilter.test {
            skipItems(1) // current value "food"
            viewModel.clearCategory()
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setCategory triggers getTransactions with correct category argument`() = runTest {
        every { repository.getTransactions(category = "food") } returns flowOf(PagingData.empty())
        viewModel.setCategory("food")
        viewModel.transactions.test {
            awaitItem()
            verify { repository.getTransactions(category = "food") }
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region addTransaction uiState

    @Test
    fun `addTransaction Loading state emitted before repo call completes`() = runTest {
        coEvery { repository.createTransaction(any(), any(), any(), any()) } coAnswers {
            delay(1)
            TransactionRepository.CreateResult.NetworkError
        }

        viewModel.uiState.test {
            skipItems(1) // Idle
            viewModel.addTransaction(10.0, "food", null)
            assertEquals(TransactionListUiState.Loading, awaitItem())
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
                viewModel.addTransaction(10.0, "food", null)
                assertEquals(TransactionListUiState.Loading, awaitItem())
                assertEquals(TransactionListUiState.Success, awaitItem())
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
                viewModel.addTransaction(10.0, "food", null)
                assertEquals(TransactionListUiState.Loading, awaitItem())
                assertEquals(TransactionListUiState.Success, awaitItem())
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
                viewModel.addTransaction(10.0, "food", null)
                assertEquals(TransactionListUiState.Loading, awaitItem())
                assertTrue(awaitItem() is TransactionListUiState.Error)
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
                viewModel.addTransaction(10.0, "food", null)
                assertEquals(TransactionListUiState.Loading, awaitItem())
                assertTrue(awaitItem() is TransactionListUiState.Error)
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
            viewModel.addTransaction(10.0, "food", null)
            assertEquals(TransactionListUiState.Loading, awaitItem()) // Loading is now set

            viewModel.addTransaction(20.0, "transport", null) // guard sees Loading — ignored

            skipItems(1) // final Error state from first call
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { repository.createTransaction(any(), any(), any(), any()) }
    }

    // endregion

    private fun fakeTransaction() = TransactionUiModel(
        id = "t1",
        amount = 10.0,
        category = "food",
        description = null,
        occurredAt = 1_000_000L,
        isExpense = false,
    )
}
