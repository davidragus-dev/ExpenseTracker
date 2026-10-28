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

package io.github.davidragus_dev.expensetracker.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.davidragus_dev.expensetracker.data.local.AppDatabase
import io.github.davidragus_dev.expensetracker.data.local.CategoryEntity
import io.github.davidragus_dev.expensetracker.data.local.TransactionEntity
import io.github.davidragus_dev.expensetracker.domain.model.MonthTotals
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType.EXPENSE
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType.INCOME
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
class TransactionRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: TransactionRepository

    private val october = YearMonth.of(2026, 10)

    private var foodId = 0L
    private var transportId = 0L
    private var salaryId = 0L

    @Before
    fun setUp() = runTest {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = TransactionRepository(db.transactionDao())

        val categories = db.categoryDao()
        foodId = categories.insert(category("Comida", EXPENSE))
        transportId = categories.insert(category("Transporte", EXPENSE))
        salaryId = categories.insert(category("Nómina", INCOME))
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun observeMonth_includesFirstAndLastDay_andExcludesOtherMonths() = runTest {
        repository.add(expense(1_000, foodId, LocalDate.of(2026, 9, 30)))
        repository.add(expense(2_000, foodId, LocalDate.of(2026, 10, 1)))
        repository.add(expense(3_000, foodId, LocalDate.of(2026, 10, 31)))
        repository.add(expense(4_000, foodId, LocalDate.of(2026, 11, 1)))

        val amounts = repository.observeMonth(october).first().map { it.transaction.amountCents }

        // Most recent first
        assertEquals(listOf(3_000L, 2_000L), amounts)
    }

    @Test
    fun observeMonth_includesCategory() = runTest {
        repository.add(expense(1_000, transportId, LocalDate.of(2026, 10, 5)))

        val item = repository.observeMonth(october).first().single()

        assertEquals("Transporte", item.category.name)
    }

    @Test
    fun observeMonthTotals_sumsIncomeAndExpenses() = runTest {
        repository.add(income(250_000, salaryId, LocalDate.of(2026, 10, 1)))
        repository.add(expense(4_550, foodId, LocalDate.of(2026, 10, 3)))
        repository.add(expense(1_200, transportId, LocalDate.of(2026, 10, 15)))
        repository.add(expense(9_999, foodId, LocalDate.of(2026, 11, 2)))

        val totals = repository.observeMonthTotals(october).first()

        assertEquals(MonthTotals(incomeCents = 250_000, expenseCents = 5_750), totals)
        assertEquals(244_250L, totals.balanceCents)
    }

    @Test
    fun observeMonthTotals_emptyMonthIsZero() = runTest {
        assertEquals(MonthTotals(0, 0), repository.observeMonthTotals(october).first())
    }

    @Test
    fun observeMonthTotalsByCategory_groupsAndSortsByAmount() = runTest {
        repository.add(expense(1_000, transportId, LocalDate.of(2026, 10, 2)))
        repository.add(expense(2_000, foodId, LocalDate.of(2026, 10, 3)))
        repository.add(expense(1_500, foodId, LocalDate.of(2026, 10, 4)))
        repository.add(income(250_000, salaryId, LocalDate.of(2026, 10, 1)))

        val totals = repository.observeMonthTotalsByCategory(october, EXPENSE).first()

        assertEquals(listOf("Comida", "Transporte"), totals.map { it.name })
        assertEquals(listOf(3_500L, 1_000L), totals.map { it.totalCents })
    }

    @Test
    fun update_and_delete() = runTest {
        val id = repository.add(expense(1_000, foodId, LocalDate.of(2026, 10, 2)))

        repository.update(repository.get(id)!!.copy(amountCents = 1_500, note = "Cena"))
        val updated = repository.get(id)!!
        assertEquals(1_500L, updated.amountCents)
        assertEquals("Cena", updated.note)

        repository.delete(updated)
        assertNull(repository.get(id))
    }

    @Test
    fun observeMonthTotals_emitsAgainWhenDataChanges() = runTest {
        assertEquals(0L, repository.observeMonthTotals(october).first().expenseCents)

        repository.add(expense(700, foodId, LocalDate.of(2026, 10, 9)))

        assertEquals(700L, repository.observeMonthTotals(october).first().expenseCents)
    }

    private fun category(name: String, type: TransactionType) =
        CategoryEntity(name = name, type = type, color = 0xFF000000, icon = "test")

    private fun expense(cents: Long, categoryId: Long, date: LocalDate) =
        TransactionEntity(amountCents = cents, type = EXPENSE, categoryId = categoryId, date = date)

    private fun income(cents: Long, categoryId: Long, date: LocalDate) =
        TransactionEntity(amountCents = cents, type = INCOME, categoryId = categoryId, date = date)
}
