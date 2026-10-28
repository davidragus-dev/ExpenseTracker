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

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType.EXPENSE
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType.INCOME

/** Categories created the first time the app is opened. */
val DefaultCategories = listOf(
    CategoryEntity(name = "Comida", type = EXPENSE, color = 0xFFEF6C00, icon = "restaurant"),
    CategoryEntity(name = "Hogar", type = EXPENSE, color = 0xFF6D4C41, icon = "home"),
    CategoryEntity(name = "Transporte", type = EXPENSE, color = 0xFF1E88E5, icon = "directions_car"),
    CategoryEntity(name = "Ocio", type = EXPENSE, color = 0xFF8E24AA, icon = "movie"),
    CategoryEntity(name = "Salud", type = EXPENSE, color = 0xFFE53935, icon = "local_hospital"),
    CategoryEntity(name = "Compras", type = EXPENSE, color = 0xFFD81B60, icon = "shopping_bag"),
    CategoryEntity(name = "Facturas", type = EXPENSE, color = 0xFF546E7A, icon = "receipt"),
    CategoryEntity(name = "Otros gastos", type = EXPENSE, color = 0xFF757575, icon = "more_horiz"),
    CategoryEntity(name = "Nómina", type = INCOME, color = 0xFF43A047, icon = "payments"),
    CategoryEntity(name = "Extras", type = INCOME, color = 0xFF00897B, icon = "work"),
    CategoryEntity(name = "Regalos", type = INCOME, color = 0xFF7CB342, icon = "card_giftcard"),
    CategoryEntity(name = "Otros ingresos", type = INCOME, color = 0xFF9E9D24, icon = "more_horiz"),
)

/** Inserts [DefaultCategories] when Room creates the database for the first time. */
object DefaultCategoriesCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        DefaultCategories.forEach { category ->
            db.execSQL(
                "INSERT INTO categories (name, type, color, icon) VALUES (?, ?, ?, ?)",
                arrayOf<Any>(category.name, category.type.name, category.color, category.icon),
            )
        }
    }
}
