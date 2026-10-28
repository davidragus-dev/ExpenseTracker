package io.github.davidragus_dev.expensetracker.ui.monthly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.davidragus_dev.expensetracker.ExpenseTrackerApp
import io.github.davidragus_dev.expensetracker.data.local.TransactionEntity
import io.github.davidragus_dev.expensetracker.data.local.TransactionWithCategory
import io.github.davidragus_dev.expensetracker.data.repository.TransactionRepository
import io.github.davidragus_dev.expensetracker.domain.model.MonthTotals
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

class MonthlyViewModel(
    private val transactionRepository: TransactionRepository,
    initialMonth: YearMonth = YearMonth.now(),
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(initialMonth)

    /** The last deleted transaction, kept so the deletion can be undone. */
    private var lastDeleted: TransactionEntity? = null

    // Every time the month changes, stop observing the old month and observe the new one.
    // Totals are computed from the same list, so they always match what is shown.
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MonthlyUiState> = selectedMonth
        .flatMapLatest { month ->
            transactionRepository.observeMonth(month).map { transactions ->
                MonthlyUiState(
                    month = month,
                    totals = transactions.totals(),
                    transactions = transactions,
                    isLoading = false,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = MonthlyUiState(month = initialMonth),
        )

    fun showPreviousMonth() {
        selectedMonth.update { it.minusMonths(1) }
    }

    fun showNextMonth() {
        selectedMonth.update { it.plusMonths(1) }
    }

    fun delete(item: TransactionWithCategory) {
        lastDeleted = item.transaction
        viewModelScope.launch { transactionRepository.delete(item.transaction) }
    }

    /**
     * Restores the last deleted transaction with all its data but a new id. Reusing the old id
     * would make the list restore the old row's "swiped away" state and delete it again.
     */
    fun undoDelete() {
        val transaction = lastDeleted ?: return
        lastDeleted = null
        viewModelScope.launch { transactionRepository.add(transaction.copy(id = 0)) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExpenseTrackerApp
                MonthlyViewModel(app.container.transactionRepository)
            }
        }
    }
}

private fun List<TransactionWithCategory>.totals() = MonthTotals(
    incomeCents = filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amountCents },
    expenseCents = filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amountCents },
)
