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

package io.github.davidragus_dev.expensetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [CategoryEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao

    abstract fun transactionDao(): TransactionDao

    companion object {
        private const val DATABASE_NAME = "expense_tracker.db"

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addCallback(DefaultCategoriesCallback)
                .build()
    }
}
