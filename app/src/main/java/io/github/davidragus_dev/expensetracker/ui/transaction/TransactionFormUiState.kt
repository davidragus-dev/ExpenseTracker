package io.github.davidragus_dev.expensetracker.ui.transaction

import io.github.davidragus_dev.expensetracker.data.local.CategoryEntity
import io.github.davidragus_dev.expensetracker.domain.model.Money
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType
import java.time.LocalDate

data class TransactionFormUiState(
    /** False when editing an existing transaction. */
    val isNew: Boolean,
    val date: LocalDate,
    val type: TransactionType = TransactionType.EXPENSE,
    /** The amount exactly as typed, so the field never jumps while the user writes. */
    val amountText: String = "",
    val categoryId: Long? = null,
    val note: String = "",
    val allCategories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    /** Becomes true once saved, so the screen can close. */
    val isSaved: Boolean = false,
) {
    /** Only the categories of the selected type can be chosen. */
    val categories: List<CategoryEntity>
        get() = allCategories.filter { it.type == type }

    val amountCents: Long?
        get() = Money.parseToCents(amountText)

    private val isAmountValid: Boolean
        get() = (amountCents ?: 0) > 0

    /** Only complain about the amount once the user has typed something. */
    val showAmountError: Boolean
        get() = amountText.isNotBlank() && !isAmountValid

    val canSave: Boolean
        get() = !isLoading && !isSaving && isAmountValid && categories.any { it.id == categoryId }
}
