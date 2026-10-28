package io.github.davidragus_dev.expensetracker.ui.transaction

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.davidragus_dev.expensetracker.data.local.AppDatabase
import io.github.davidragus_dev.expensetracker.data.local.CategoryEntity
import io.github.davidragus_dev.expensetracker.data.local.DefaultCategoriesCallback
import io.github.davidragus_dev.expensetracker.data.local.TransactionEntity
import io.github.davidragus_dev.expensetracker.data.repository.CategoryRepository
import io.github.davidragus_dev.expensetracker.data.repository.TransactionRepository
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType.EXPENSE
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType.INCOME
import io.github.davidragus_dev.expensetracker.ui.transaction.TransactionFormViewModel.Companion.NEW_TRANSACTION_ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class TransactionFormViewModelTest {

    private lateinit var db: AppDatabase
    private lateinit var transactions: TransactionRepository
    private lateinit var categories: CategoryRepository

    private val today = LocalDate.of(2026, 10, 28)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).addCallback(DefaultCategoriesCallback).allowMainThreadQueries().build()
        transactions = TransactionRepository(db.transactionDao())
        categories = CategoryRepository(db.categoryDao())
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    private fun newForm() = TransactionFormViewModel(NEW_TRANSACTION_ID, today, transactions, categories)

    private suspend fun category(name: String): CategoryEntity =
        categories.observeAll().first().first { it.name == name }

    @Test
    fun newTransaction_startsEmpty_withExpenseCategoriesOnly() = runTest {
        val state = newForm().uiState.first { !it.isLoading }

        assertTrue(state.isNew)
        assertEquals(today, state.date)
        assertEquals(EXPENSE, state.type)
        assertTrue(state.categories.isNotEmpty())
        assertTrue(state.categories.all { it.type == EXPENSE })
        assertFalse(state.canSave)
    }

    @Test
    fun canSave_requiresAPositiveAmountAndACategory() = runTest {
        val form = newForm()
        form.uiState.first { !it.isLoading }

        form.onAmountChange("0")
        form.onCategorySelect(category("Comida").id)
        assertFalse(form.uiState.value.canSave)
        assertTrue(form.uiState.value.showAmountError)

        form.onAmountChange("12,5")
        assertTrue(form.uiState.value.canSave)
        assertFalse(form.uiState.value.showAmountError)

        form.onAmountChange("abc")
        assertFalse(form.uiState.value.canSave)
    }

    @Test
    fun changingType_clearsACategoryOfTheOtherType() = runTest {
        val form = newForm()
        form.uiState.first { !it.isLoading }
        form.onCategorySelect(category("Comida").id)

        form.onTypeChange(INCOME)

        assertNull(form.uiState.value.categoryId)
        assertTrue(form.uiState.value.categories.all { it.type == INCOME })
    }

    @Test
    fun save_addsANewTransaction() = runTest {
        val form = newForm()
        form.uiState.first { !it.isLoading }
        val food = category("Comida")

        form.onAmountChange("45,30")
        form.onCategorySelect(food.id)
        form.onDateChange(LocalDate.of(2026, 10, 3))
        form.onNoteChange("  Supermercado  ")
        form.save()
        form.uiState.first { it.isSaved }

        val saved = transactions.observeMonth(YearMonth.of(2026, 10)).first().single().transaction
        assertEquals(4_530L, saved.amountCents)
        assertEquals(EXPENSE, saved.type)
        assertEquals(food.id, saved.categoryId)
        assertEquals(LocalDate.of(2026, 10, 3), saved.date)
        assertEquals("Supermercado", saved.note)
    }

    @Test
    fun save_withBlankNote_storesNull() = runTest {
        val form = newForm()
        form.uiState.first { !it.isLoading }
        form.onAmountChange("5")
        form.onCategorySelect(category("Ocio").id)
        form.onNoteChange("   ")
        form.save()
        form.uiState.first { it.isSaved }

        assertNull(transactions.observeMonth(YearMonth.of(2026, 10)).first().single().transaction.note)
    }

    @Test
    fun editing_loadsTheTransaction_andSaveUpdatesIt() = runTest {
        val salary = category("Nómina")
        val original = TransactionEntity(
            amountCents = 250_050,
            type = INCOME,
            categoryId = salary.id,
            date = LocalDate.of(2026, 10, 1),
            note = "Octubre",
            createdAt = 1_000,
        )
        val id = transactions.add(original)
        val form = TransactionFormViewModel(id, today, transactions, categories)

        val loaded = form.uiState.first { !it.isLoading }
        assertFalse(loaded.isNew)
        assertEquals(INCOME, loaded.type)
        assertEquals(250_050L, loaded.amountCents)
        assertEquals(salary.id, loaded.categoryId)
        assertEquals(LocalDate.of(2026, 10, 1), loaded.date)
        assertEquals("Octubre", loaded.note)
        assertTrue(loaded.canSave)

        form.onAmountChange("2600")
        form.save()
        form.uiState.first { it.isSaved }

        val updated = transactions.get(id)!!
        assertEquals(260_000L, updated.amountCents)
        assertEquals(original.copy(id = id, amountCents = 260_000), updated)
        assertEquals(1, transactions.observeMonth(YearMonth.of(2026, 10)).first().size)
    }
}
