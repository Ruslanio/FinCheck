package com.financetracker.feature.addtransaction

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financetracker.core.ui.util.LocalSnackbarHostState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionFab(
    viewModel: AddTransactionViewModel = hiltViewModel(),
) {
    var showSheet by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current

    val msgTransactionAdded = stringResource(R.string.msg_transaction_added)
    val msgAlreadyRecorded = stringResource(R.string.msg_already_recorded)
    val msgNoConnection = stringResource(R.string.error_network)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddTransactionEvent.Success -> {
                    showSheet = false
                    snackbarHostState?.showSnackbar(msgTransactionAdded)
                }
                is AddTransactionEvent.Duplicate -> {
                    showSheet = false
                    snackbarHostState?.showSnackbar(msgAlreadyRecorded)
                }
                is AddTransactionEvent.Error -> snackbarHostState?.showSnackbar(event.message)
                AddTransactionEvent.NetworkError -> snackbarHostState?.showSnackbar(msgNoConnection)
            }
        }
    }

    FloatingActionButton(onClick = { showSheet = true }) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.cd_add_transaction),
        )
    }

    if (showSheet) {
        AddTransactionSheet(
            onDismiss = { showSheet = false },
            onSubmit = { amount, category, description ->
                viewModel.addTransaction(
                    amount = amount,
                    category = category,
                    description = description,
                )
            },
            isLoading = uiState is AddTransactionUiState.Loading,
        )
    }
}
