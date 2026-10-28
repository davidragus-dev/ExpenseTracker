package io.github.davidragus_dev.expensetracker.domain.model

/** Totals for a period, in cents. */
data class MonthTotals(
    val incomeCents: Long,
    val expenseCents: Long,
) {
    val balanceCents: Long
        get() = incomeCents - expenseCents
}
