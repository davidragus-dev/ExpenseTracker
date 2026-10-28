package io.github.davidragus_dev.expensetracker.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.davidragus_dev.expensetracker.ExpenseTrackerApp
import io.github.davidragus_dev.expensetracker.data.local.TransactionEntity
import io.github.davidragus_dev.expensetracker.data.repository.CategoryRepository
import io.github.davidragus_dev.expensetracker.data.repository.TransactionRepository
import io.github.davidragus_dev.expensetracker.domain.model.Money
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Form to add a new transaction or edit an existing one. */
class TransactionFormViewModel(
    transactionId: Long,
    defaultDate: LocalDate,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TransactionFormUiState(isNew = transactionId == NEW_TRANSACTION_ID, date = defaultDate),
    )
    val uiState: StateFlow<TransactionFormUiState> = _uiState.asStateFlow()

    /** The transaction being edited, or null for a new one. */
    private var existing: TransactionEntity? = null

    init {
        viewModelScope.launch {
            if (transactionId != NEW_TRANSACTION_ID) {
                loadTransaction(transactionId)
            }
            categoryRepository.observeAll().collect { categories ->
                _uiState.update { it.copy(allCategories = categories, isLoading = false) }
            }
        }
    }

    private suspend fun loadTransaction(id: Long) {
        val transaction = transactionRepository.get(id)
        if (transaction == null) {
            // It was deleted in the meantime: fall back to an empty form
            _uiState.update { it.copy(isNew = true) }
            return
        }
        existing = transaction
        _uiState.update {
            it.copy(
                type = transaction.type,
                amountText = Money.toEditableText(transaction.amountCents),
                categoryId = transaction.categoryId,
                date = transaction.date,
                note = transaction.note.orEmpty(),
            )
        }
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.update { state ->
            // A category of the other type is no longer valid
            val keepCategory = state.allCategories.any { it.id == state.categoryId && it.type == type }
            state.copy(type = type, categoryId = state.categoryId.takeIf { keepCategory })
        }
    }

    fun onAmountChange(text: String) {
        _uiState.update { it.copy(amountText = text) }
    }

    fun onCategorySelect(categoryId: Long) {
        _uiState.update { it.copy(categoryId = categoryId) }
    }

    fun onDateChange(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        val amountCents = state.amountCents ?: return
        val categoryId = state.categoryId ?: return
        val note = state.note.trim().ifEmpty { null }

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val current = existing
            if (current == null) {
                transactionRepository.add(
                    TransactionEntity(
                        amountCents = amountCents,
                        type = state.type,
                        categoryId = categoryId,
                        date = state.date,
                        note = note,
                    ),
                )
            } else {
                // copy() keeps the id and createdAt of the original transaction
                transactionRepository.update(
                    current.copy(
                        amountCents = amountCents,
                        type = state.type,
                        categoryId = categoryId,
                        date = state.date,
                        note = note,
                    ),
                )
            }
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    companion object {
        /** Room ids start at 1, so 0 means "new transaction". */
        const val NEW_TRANSACTION_ID = 0L

        fun factory(transactionId: Long, defaultDate: LocalDate): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val container = (this[APPLICATION_KEY] as ExpenseTrackerApp).container
                    TransactionFormViewModel(
                        transactionId = transactionId,
                        defaultDate = defaultDate,
                        transactionRepository = container.transactionRepository,
                        categoryRepository = container.categoryRepository,
                    )
                }
            }
    }
}
