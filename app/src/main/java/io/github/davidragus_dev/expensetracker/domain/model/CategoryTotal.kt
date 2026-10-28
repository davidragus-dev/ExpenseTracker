package io.github.davidragus_dev.expensetracker.domain.model

/** Sum of a category's transactions in a period, in cents. */
data class CategoryTotal(
    val categoryId: Long,
    val name: String,
    val color: Long,
    val totalCents: Long,
)
