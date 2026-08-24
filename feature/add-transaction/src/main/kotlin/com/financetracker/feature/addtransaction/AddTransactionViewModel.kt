package com.financetracker.feature.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddTransactionUiState>(AddTransactionUiState.Idle)
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddTransactionEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<AddTransactionEvent> = _events.asSharedFlow()

    fun addTransaction(
        amount: Double,
        category: String,
        description: String?,
        idempotencyKey: String? = UUID.randomUUID().toString(),
    ) {
        if (_uiState.value is AddTransactionUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = AddTransactionUiState.Loading

            val result = repository.createTransaction(
                amount = amount,
                category = category,
                description = description,
                idempotencyKey = idempotencyKey,
            )

            _uiState.value = when (result) {
                is TransactionRepository.CreateResult.Success -> AddTransactionUiState.Success
                is TransactionRepository.CreateResult.Duplicate -> AddTransactionUiState.Success
                is TransactionRepository.CreateResult.Error -> AddTransactionUiState.Error(result.message)
                TransactionRepository.CreateResult.NetworkError -> AddTransactionUiState.Error("No connection")
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
