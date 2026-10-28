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

import io.github.davidragus_dev.expensetracker.data.local.CategoryDao
import io.github.davidragus_dev.expensetracker.data.local.CategoryEntity
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val dao: CategoryDao) {

    fun observeAll(): Flow<List<CategoryEntity>> = dao.observeAll()

    fun observeByType(type: TransactionType): Flow<List<CategoryEntity>> = dao.observeByType(type)

    suspend fun get(id: Long): CategoryEntity? = dao.getById(id)

    /** Returns the id of the new category. */
    suspend fun add(category: CategoryEntity): Long = dao.insert(category)

    suspend fun update(category: CategoryEntity) = dao.update(category)

    /**
     * Throws [android.database.sqlite.SQLiteConstraintException] if the category
     * has transactions.
     */
    suspend fun delete(category: CategoryEntity) = dao.delete(category)
}
