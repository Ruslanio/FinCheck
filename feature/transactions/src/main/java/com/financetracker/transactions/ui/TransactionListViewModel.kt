package com.financetracker.transactions.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.financetracker.data.model.TransactionUiModel
import com.financetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val repository: TransactionRepository,
) : ViewModel() {

    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: Flow<PagingData<TransactionUiModel>> =
        _categoryFilter
            .flatMapLatest { category -> repository.getTransactions(category = category) }
            .cachedIn(viewModelScope)

    private val _uiState = MutableStateFlow<TransactionListUiState>(TransactionListUiState.Idle)
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddTransactionEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<AddTransactionEvent> = _events.asSharedFlow()

    fun setCategory(category: String?) {
        _categoryFilter.value = category
    }

    fun clearCategory() {
        _categoryFilter.value = null
    }

    fun addTransaction(
        amount: Double,
        category: String,
        description: String?,
        idempotencyKey: String? = UUID.randomUUID().toString(),
    ) {
        if (_uiState.value is TransactionListUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = TransactionListUiState.Loading

            val result = repository.createTransaction(
                amount = amount,
                category = category,
                description = description,
                idempotencyKey = idempotencyKey,
            )

            _uiState.value = when (result) {
                is TransactionRepository.CreateResult.Success -> TransactionListUiState.Success
                is TransactionRepository.CreateResult.Duplicate -> TransactionListUiState.Success
                is TransactionRepository.CreateResult.Error -> TransactionListUiState.Error(result.message)
                TransactionRepository.CreateResult.NetworkError -> TransactionListUiState.Error("No connection")
            }

            val event = when (result) {
                is TransactionRepository.CreateResult.Success -> AddTransactionEvent.Success(result.transaction)
                is TransactionRepository.CreateResult.Duplicate -> AddTransactionEvent.Duplicate(result.transaction)
                is TransactionRepository.CreateResult.Error -> AddTransactionEvent.Error(result.message)
                TransactionRepository.CreateResult.NetworkError -> AddTransactionEvent.NetworkError
            }
            _events.emit(event)
        }
    }
}
