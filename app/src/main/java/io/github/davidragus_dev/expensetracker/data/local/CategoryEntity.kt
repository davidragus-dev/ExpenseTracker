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

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name", "type"], unique = true)],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: TransactionType,
    /** ARGB color, e.g. 0xFFEF6C00. */
    val color: Long,
    /** Key of the icon shown by the UI, e.g. "restaurant". */
    val icon: String,
)
