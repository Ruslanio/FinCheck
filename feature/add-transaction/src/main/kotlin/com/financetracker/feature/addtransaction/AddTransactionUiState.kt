package com.financetracker.feature.addtransaction

import com.financetracker.data.model.TransactionUiModel

sealed interface AddTransactionUiState {
    data object Idle : AddTransactionUiState
    data object Loading : AddTransactionUiState
    data class Error(val message: String) : AddTransactionUiState
    data object Success : AddTransactionUiState
}

sealed interface AddTransactionEvent {
    data class Success(val transaction: TransactionUiModel) : AddTransactionEvent
    data class Duplicate(val transaction: TransactionUiModel) : AddTransactionEvent
    data class Error(val message: String) : AddTransactionEvent
    data object NetworkError : AddTransactionEvent
}
