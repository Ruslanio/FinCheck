package com.financetracker.feature.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.financetracker.R
import com.financetracker.feature.transactions.components.AddTransactionSheet
import com.financetracker.feature.transactions.components.CategoryChips
import com.financetracker.feature.transactions.components.TransactionRow
import com.financetracker.feature.transactions.components.TransactionSkeleton

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    navController: NavController,
    viewModel: TransactionListViewModel = hiltViewModel(),
) {
    val lazyPagingItems = viewModel.transactions.collectAsLazyPagingItems()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAddSheet by remember { mutableStateOf(false) }

    val msgTransactionAdded = stringResource(R.string.msg_transaction_added)
    val msgAlreadyRecorded = stringResource(R.string.msg_already_recorded)
    val msgNoConnection = stringResource(R.string.error_network)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddTransactionEvent.Success -> {
                    showAddSheet = false
                    snackbarHostState.showSnackbar(msgTransactionAdded)
                }
                is AddTransactionEvent.Duplicate -> {
                    showAddSheet = false
                    snackbarHostState.showSnackbar(msgAlreadyRecorded)
                }
                is AddTransactionEvent.Error -> snackbarHostState.showSnackbar(event.message)
                AddTransactionEvent.NetworkError -> snackbarHostState.showSnackbar(msgNoConnection)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_add_transaction),
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            CategoryChips(
                selectedCategory = categoryFilter,
                onCategorySelected = viewModel::setCategory,
                onClearFilter = viewModel::clearCategory,
            )

            val isRefreshing =
                lazyPagingItems.loadState.refresh is LoadState.Loading &&
                    lazyPagingItems.itemCount > 0

            val pullRefreshState = rememberPullRefreshState(
                refreshing = isRefreshing,
                onRefresh = { lazyPagingItems.refresh() },
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState),
            ) {
                when {
                    lazyPagingItems.loadState.refresh is LoadState.Loading &&
                        lazyPagingItems.itemCount == 0 ->
                        TransactionSkeleton()

                    lazyPagingItems.loadState.refresh is LoadState.NotLoading &&
                        lazyPagingItems.itemCount == 0 ->
                        EmptyTransactionsState()

                    lazyPagingItems.loadState.refresh is LoadState.Error ->
                        ErrorState(onRetry = { lazyPagingItems.refresh() })

                    else ->
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(
                                count = lazyPagingItems.itemCount,
                                key = lazyPagingItems.itemKey { it.id },
                            ) { index ->
                                val item = lazyPagingItems[index]
                                if (item != null) TransactionRow(transaction = item)
                            }

                            if (lazyPagingItems.loadState.append is LoadState.Loading) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                }

                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }

    if (showAddSheet) {
        AddTransactionSheet(
            onDismiss = { showAddSheet = false },
            onSubmit = { amount, category, description ->
                viewModel.addTransaction(
                    amount = amount,
                    category = category,
                    description = description,
                )
            },
            isLoading = uiState is TransactionListUiState.Loading,
        )
    }
}

@Composable
private fun EmptyTransactionsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.msg_no_transactions),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.msg_failed_to_load))
            Spacer(Modifier.height(8.dp))
            Button(onClick = onRetry) { Text(stringResource(R.string.label_retry)) }
        }
    }
}
