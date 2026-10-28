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

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.davidragus_dev.expensetracker.data.local.AppDatabase
import io.github.davidragus_dev.expensetracker.data.local.CategoryEntity
import io.github.davidragus_dev.expensetracker.data.local.DefaultCategories
import io.github.davidragus_dev.expensetracker.data.local.DefaultCategoriesCallback
import io.github.davidragus_dev.expensetracker.data.local.TransactionEntity
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType.EXPENSE
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType.INCOME
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class CategoryRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: CategoryRepository

    @Before
    fun setUp() {
        // Same callback as the real app, to check the default categories
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).addCallback(DefaultCategoriesCallback).allowMainThreadQueries().build()
        repository = CategoryRepository(db.categoryDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun newDatabase_containsDefaultCategories() = runTest {
        val names = repository.observeAll().first().map { it.name }.toSet()

        assertEquals(DefaultCategories.map { it.name }.toSet(), names)
    }

    @Test
    fun observeByType_returnsOnlyThatType() = runTest {
        val incomes = repository.observeByType(INCOME).first()

        assertTrue(incomes.isNotEmpty())
        assertTrue(incomes.all { it.type == INCOME })
        assertTrue("Nómina" in incomes.map { it.name })
    }

    @Test
    fun add_update_delete() = runTest {
        val id = repository.add(CategoryEntity(name = "Mascotas", type = EXPENSE, color = 0xFF795548, icon = "pets"))

        repository.update(repository.get(id)!!.copy(name = "Mascota"))
        assertEquals("Mascota", repository.get(id)!!.name)

        repository.delete(repository.get(id)!!)
        assertNull(repository.get(id))
    }

    @Test
    fun delete_categoryWithTransactions_isRejected() = runTest {
        val food = repository.observeByType(EXPENSE).first().first { it.name == "Comida" }
        db.transactionDao().insert(
            TransactionEntity(amountCents = 1_000, type = EXPENSE, categoryId = food.id, date = LocalDate.of(2026, 10, 1)),
        )

        try {
            repository.delete(food)
            fail("Expected SQLiteConstraintException")
        } catch (expected: SQLiteConstraintException) {
            // The category and its transaction are still there
        }
        assertEquals(food, repository.get(food.id))
    }
}
