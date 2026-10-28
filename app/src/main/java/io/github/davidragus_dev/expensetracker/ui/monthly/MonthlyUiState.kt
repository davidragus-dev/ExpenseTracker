package io.github.davidragus_dev.expensetracker.ui.monthly

import io.github.davidragus_dev.expensetracker.data.local.TransactionWithCategory
import io.github.davidragus_dev.expensetracker.domain.model.MonthTotals
import java.time.YearMonth

data class MonthlyUiState(
    val month: YearMonth,
    val totals: MonthTotals = MonthTotals(incomeCents = 0, expenseCents = 0),
    val transactions: List<TransactionWithCategory> = emptyList(),
    /** True until the first data for [month] has been loaded. */
    val isLoading: Boolean = true,
)
