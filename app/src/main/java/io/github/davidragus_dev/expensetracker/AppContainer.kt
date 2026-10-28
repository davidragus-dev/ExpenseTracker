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

package io.github.davidragus_dev.expensetracker

import android.content.Context
import io.github.davidragus_dev.expensetracker.data.local.AppDatabase
import io.github.davidragus_dev.expensetracker.data.repository.CategoryRepository
import io.github.davidragus_dev.expensetracker.data.repository.TransactionRepository

/** Creates and holds the app-wide dependencies (manual dependency injection). */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: AppDatabase by lazy { AppDatabase.build(appContext) }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(database.categoryDao())
    }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepository(database.transactionDao())
    }
}
