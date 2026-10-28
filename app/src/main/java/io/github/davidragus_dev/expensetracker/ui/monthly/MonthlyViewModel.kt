package io.github.davidragus_dev.expensetracker.ui.monthly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.davidragus_dev.expensetracker.ExpenseTrackerApp
import io.github.davidragus_dev.expensetracker.data.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.YearMonth

class MonthlyViewModel(
    private val transactionRepository: TransactionRepository,
    initialMonth: YearMonth = YearMonth.now(),
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(initialMonth)

    // Every time the month changes, stop observing the old month and observe the new one
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MonthlyUiState> = selectedMonth
        .flatMapLatest { month ->
            combine(
                transactionRepository.observeMonth(month),
                transactionRepository.observeMonthTotals(month),
            ) { transactions, totals ->
                MonthlyUiState(
                    month = month,
                    totals = totals,
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

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExpenseTrackerApp
                MonthlyViewModel(app.container.transactionRepository)
            }
        }
    }
}
