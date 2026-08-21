package com.financetracker.transactions.ui

import com.financetracker.data.model.TransactionUiModel

sealed interface TransactionListUiState {
    data object Idle : TransactionListUiState
    data object Loading : TransactionListUiState
    data class Error(val message: String) : TransactionListUiState
    data object Success : TransactionListUiState
}

sealed interface AddTransactionEvent {
    data class Success(val transaction: TransactionUiModel) : AddTransactionEvent
    data class Duplicate(val transaction: TransactionUiModel) : AddTransactionEvent
    data class Error(val message: String) : AddTransactionEvent
    data object NetworkError : AddTransactionEvent
}
