/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Expense Tracker - keep track of monthly income and expenses
 * Copyright (C) 2026 David Ruiz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import org.junit.Assert.assertNotEquals
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

        // Totals are computed from the list, so the first non-empty state is already consistent
        val updated = viewModel.uiState.first { it.transactions.isNotEmpty() }
        assertEquals(700L, updated.totals.expenseCents)
        assertEquals(1, updated.transactions.size)
    }

    @Test
    fun delete_removesTheTransaction_andUndoRestoresIt() = runTest {
        val id = repository.add(expense(700, LocalDate.of(2026, 10, 9)))
        val viewModel = MonthlyViewModel(repository, initialMonth = october)
        backgroundScope.launch { viewModel.uiState.collect {} }
        val item = viewModel.uiState.first { it.transactions.isNotEmpty() }.transactions.single()

        viewModel.delete(item)
        val afterDelete = viewModel.uiState.first { it.transactions.isEmpty() }
        assertEquals(0L, afterDelete.totals.expenseCents)

        viewModel.undoDelete()
        val restored = viewModel.uiState.first { it.transactions.isNotEmpty() }.transactions.single()
        // Same data, new id (see MonthlyViewModel.undoDelete)
        assertNotEquals(id, restored.transaction.id)
        assertEquals(item.transaction.copy(id = restored.transaction.id), restored.transaction)
    }

    @Test
    fun undoDelete_keepsThePositionWithinTheSameDay() = runTest {
        val day = LocalDate.of(2026, 10, 9)
        repository.add(expense(100, day).copy(createdAt = 1_000))
        repository.add(expense(200, day).copy(createdAt = 2_000))
        repository.add(expense(300, day).copy(createdAt = 3_000))
        val viewModel = MonthlyViewModel(repository, initialMonth = october)
        backgroundScope.launch { viewModel.uiState.collect {} }
        val middle = viewModel.uiState.first { it.transactions.size == 3 }.transactions[1]

        viewModel.delete(middle)
        viewModel.uiState.first { it.transactions.size == 2 }
        viewModel.undoDelete()

        val amounts = viewModel.uiState.first { it.transactions.size == 3 }.transactions.map { it.transaction.amountCents }
        assertEquals(listOf(300L, 200L, 100L), amounts)
    }

    @Test
    fun defaultNewTransactionDate_isTodayInTheCurrentMonth_elseTheFirstDay() {
        val today = LocalDate.of(2026, 10, 28)

        assertEquals(today, MonthlyUiState(october).defaultNewTransactionDate(today))
        assertEquals(
            LocalDate.of(2026, 9, 1),
            MonthlyUiState(october.minusMonths(1)).defaultNewTransactionDate(today),
        )
    }

    private fun expense(cents: Long, date: LocalDate) =
        TransactionEntity(amountCents = cents, type = EXPENSE, categoryId = foodId, date = date)

    private fun income(cents: Long, date: LocalDate) =
        TransactionEntity(amountCents = cents, type = INCOME, categoryId = salaryId, date = date)
}
