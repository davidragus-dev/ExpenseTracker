package io.github.davidragus_dev.expensetracker.ui.monthly

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.davidragus_dev.expensetracker.data.local.AppDatabase
import io.github.davidragus_dev.expensetracker.data.local.CategoryEntity
import io.github.davidragus_dev.expensetracker.data.local.TransactionEntity
import io.github.davidragus_dev.expensetracker.data.repository.TransactionRepository
import io.github.davidragus_dev.expensetracker.domain.model.MonthTotals
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType.EXPENSE
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType.INCOME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class MonthlyViewModelTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: TransactionRepository

    private val october = YearMonth.of(2026, 10)
    private var foodId = 0L
    private var salaryId = 0L

    @Before
    fun setUp() = runTest {
        // viewModelScope runs on Dispatchers.Main, which does not exist in unit tests
        Dispatchers.setMain(UnconfinedTestDispatcher())
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = TransactionRepository(db.transactionDao())
        foodId = db.categoryDao().insert(CategoryEntity(name = "Comida", type = EXPENSE, color = 0xFF000000, icon = "test"))
        salaryId = db.categoryDao().insert(CategoryEntity(name = "Nómina", type = INCOME, color = 0xFF000000, icon = "test"))
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isLoadingTheInitialMonth() {
        val viewModel = MonthlyViewModel(repository, initialMonth = october)

        val state = viewModel.uiState.value

        assertEquals(october, state.month)
        assertTrue(state.isLoading)
    }

    @Test
    fun loadedState_containsTheMonthTransactionsAndTotals() = runTest {
        repository.add(income(250_000, LocalDate.of(2026, 10, 1)))
        repository.add(expense(4_550, LocalDate.of(2026, 10, 3)))
        repository.add(expense(9_999, LocalDate.of(2026, 11, 2)))
        val viewModel = MonthlyViewModel(repository, initialMonth = october)

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(MonthTotals(incomeCents = 250_000, expenseCents = 4_550), state.totals)
        assertEquals(listOf(4_550L, 250_000L), state.transactions.map { it.transaction.amountCents })
    }

    @Test
    fun showNextAndPreviousMonth_changeTheMonthAndItsData() = runTest {
        repository.add(expense(4_550, LocalDate.of(2026, 10, 3)))
        repository.add(expense(9_999, LocalDate.of(2026, 11, 2)))
        val viewModel = MonthlyViewModel(repository, initialMonth = october)
        // Keep the state flow active, as the screen would
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.showNextMonth()
        val november = viewModel.uiState.first { it.month == october.plusMonths(1) && !it.isLoading }
        assertEquals(9_999L, november.totals.expenseCents)

        viewModel.showPreviousMonth()
        viewModel.showPreviousMonth()
        val september = viewModel.uiState.first { it.month == october.minusMonths(1) && !it.isLoading }
        assertTrue(september.transactions.isEmpty())
        assertEquals(MonthTotals(0, 0), september.totals)
    }

    @Test
    fun state_updatesWhenATransactionIsAdded() = runTest {
        val viewModel = MonthlyViewModel(repository, initialMonth = october)
        backgroundScope.launch { viewModel.uiState.collect {} }
        assertTrue(viewModel.uiState.first { !it.isLoading }.transactions.isEmpty())

        repository.add(expense(700, LocalDate.of(2026, 10, 9)))

        // The list and the totals come from two queries, so wait until both are updated
        val updated = viewModel.uiState.first { it.transactions.isNotEmpty() && it.totals.expenseCents > 0 }
        assertEquals(700L, updated.totals.expenseCents)
        assertEquals(1, updated.transactions.size)
    }

    private fun expense(cents: Long, date: LocalDate) =
        TransactionEntity(amountCents = cents, type = EXPENSE, categoryId = foodId, date = date)

    private fun income(cents: Long, date: LocalDate) =
        TransactionEntity(amountCents = cents, type = INCOME, categoryId = salaryId, date = date)
}
