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

import io.github.davidragus_dev.expensetracker.data.local.TransactionDao
import io.github.davidragus_dev.expensetracker.data.local.TransactionEntity
import io.github.davidragus_dev.expensetracker.data.local.TransactionWithCategory
import io.github.davidragus_dev.expensetracker.domain.model.CategoryTotal
import io.github.davidragus_dev.expensetracker.domain.model.MonthTotals
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

class TransactionRepository(private val dao: TransactionDao) {

    fun observeMonth(month: YearMonth): Flow<List<TransactionWithCategory>> =
        dao.observeBetween(month.atDay(1), month.atEndOfMonth())

    fun observeMonthTotals(month: YearMonth): Flow<MonthTotals> =
        dao.observeTotalsBetween(month.atDay(1), month.atEndOfMonth())

    fun observeMonthTotalsByCategory(
        month: YearMonth,
        type: TransactionType,
    ): Flow<List<CategoryTotal>> =
        dao.observeTotalsByCategoryBetween(type, month.atDay(1), month.atEndOfMonth())

    suspend fun get(id: Long): TransactionEntity? = dao.getById(id)

    /** Returns the id of the new transaction. */
    suspend fun add(transaction: TransactionEntity): Long = dao.insert(transaction)

    suspend fun update(transaction: TransactionEntity) = dao.update(transaction)

    suspend fun delete(transaction: TransactionEntity) = dao.delete(transaction)
}
