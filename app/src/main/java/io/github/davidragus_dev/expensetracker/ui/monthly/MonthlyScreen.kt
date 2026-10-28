package io.github.davidragus_dev.expensetracker.ui.monthly

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.davidragus_dev.expensetracker.R
import io.github.davidragus_dev.expensetracker.data.local.CategoryEntity
import io.github.davidragus_dev.expensetracker.data.local.TransactionEntity
import io.github.davidragus_dev.expensetracker.data.local.TransactionWithCategory
import io.github.davidragus_dev.expensetracker.domain.model.MonthTotals
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType.EXPENSE
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType.INCOME
import io.github.davidragus_dev.expensetracker.ui.theme.ExpenseTrackerTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

/** Main screen: the selected month, its summary and its transactions. */
@Composable
fun MonthlyScreen(
    onAddTransaction: (defaultDate: LocalDate) -> Unit,
    onEditTransaction: (transactionId: Long) -> Unit,
    viewModel: MonthlyViewModel = viewModel(factory = MonthlyViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val deletedMessage = stringResource(R.string.transaction_deleted)
    val undoLabel = stringResource(R.string.undo)

    MonthlyContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onPreviousMonth = viewModel::showPreviousMonth,
        onNextMonth = viewModel::showNextMonth,
        onAddTransaction = { onAddTransaction(state.defaultNewTransactionDate()) },
        onTransactionClick = { onEditTransaction(it.transaction.id) },
        onTransactionDelete = { item ->
            viewModel.delete(item)
            scope.launch {
                // Only the last deletion can be undone, so replace any previous message
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState.showSnackbar(
                    message = deletedMessage,
                    actionLabel = undoLabel,
                    duration = SnackbarDuration.Short,
                )
                if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete()
            }
        },
    )
}

// Stateless version of the screen, so it can be previewed and tested without a ViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyContent(
    state: MonthlyUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onAddTransaction: () -> Unit,
    onTransactionClick: (TransactionWithCategory) -> Unit,
    onTransactionDelete: (TransactionWithCategory) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(title = { Text(stringResource(R.string.app_name)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = stringResource(R.string.add_transaction))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                // Extra space so the last row is not hidden behind the "+" button
                bottom = innerPadding.calculateBottomPadding() + 88.dp,
            ),
        ) {
            item {
                MonthSelector(
                    month = state.month,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
            item {
                MonthSummary(
                    totals = state.totals,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            item {
                Text(
                    text = stringResource(R.string.transactions),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                )
            }
            if (state.transactions.isEmpty() && !state.isLoading) {
                item {
                    Text(
                        text = stringResource(R.string.no_transactions),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    )
                }
            }
            items(state.transactions, key = { it.transaction.id }) { item ->
                SwipeToDeleteTransactionRow(
                    item = item,
                    onClick = { onTransactionClick(item) },
                    onDelete = { onTransactionDelete(item) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MonthlyContentPreview() {
    val food = CategoryEntity(id = 1, name = "Comida", type = EXPENSE, color = 0xFFEF6C00, icon = "restaurant")
    val salary = CategoryEntity(id = 2, name = "Nómina", type = INCOME, color = 0xFF43A047, icon = "payments")
    val today = LocalDate.now()
    ExpenseTrackerTheme {
        MonthlyContent(
            state = MonthlyUiState(
                month = YearMonth.from(today),
                totals = MonthTotals(incomeCents = 250_000, expenseCents = 6_230),
                transactions = listOf(
                    TransactionWithCategory(
                        TransactionEntity(id = 2, amountCents = 6_230, type = EXPENSE, categoryId = 1, date = today, note = "Supermercado"),
                        food,
                    ),
                    TransactionWithCategory(
                        TransactionEntity(id = 1, amountCents = 250_000, type = INCOME, categoryId = 2, date = today.withDayOfMonth(1)),
                        salary,
                    ),
                ),
                isLoading = false,
            ),
            onPreviousMonth = {},
            onNextMonth = {},
            onAddTransaction = {},
            onTransactionClick = {},
            onTransactionDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MonthlyContentEmptyPreview() {
    ExpenseTrackerTheme {
        MonthlyContent(
            state = MonthlyUiState(month = YearMonth.now(), isLoading = false),
            onPreviousMonth = {},
            onNextMonth = {},
            onAddTransaction = {},
            onTransactionClick = {},
            onTransactionDelete = {},
        )
    }
}
