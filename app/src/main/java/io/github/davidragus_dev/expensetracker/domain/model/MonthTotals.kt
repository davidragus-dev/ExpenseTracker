package io.github.davidragus_dev.expensetracker.domain.model

/** Totales de un periodo, en céntimos. */
data class MonthTotals(
    val incomeCents: Long,
    val expenseCents: Long,
) {
    val balanceCents: Long
        get() = incomeCents - expenseCents
}
