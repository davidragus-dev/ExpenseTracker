package io.github.davidragus_dev.expensetracker.domain.model

/** Suma de los movimientos de una categoría en un periodo, en céntimos. */
data class CategoryTotal(
    val categoryId: Long,
    val name: String,
    val color: Long,
    val totalCents: Long,
)
