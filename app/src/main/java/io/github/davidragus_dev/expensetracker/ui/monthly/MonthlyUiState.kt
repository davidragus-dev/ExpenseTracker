package io.github.davidragus_dev.expensetracker.ui.monthly

import io.github.davidragus_dev.expensetracker.data.local.TransactionWithCategory
import io.github.davidragus_dev.expensetracker.domain.model.MonthTotals
import java.time.LocalDate
import java.time.YearMonth

data class MonthlyUiState(
    val month: YearMonth,
    val totals: MonthTotals = MonthTotals(incomeCents = 0, expenseCents = 0),
    val transactions: List<TransactionWithCategory> = emptyList(),
    /** True until the first data for [month] has been loaded. */
    val isLoading: Boolean = true,
) {
    /** Date for a new transaction: today when viewing the current month, else the 1st of [month]. */
    fun defaultNewTransactionDate(today: LocalDate = LocalDate.now()): LocalDate =
        if (YearMonth.from(today) == month) today else month.atDay(1)
}
