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

import io.github.davidragus_dev.expensetracker.data.local.TransactionWithCategory
import io.github.davidragus_dev.expensetracker.domain.model.MonthTotals
import java.time.LocalDate
import java.time.YearMonth

data class MonthlyUiState(
    val month: YearMonth,
    val totals: MonthTotals = MonthTotals(incomeCents = 0, expenseCents = 0),
    val transactions: List<TransactionWithCategory> = emptyList(),
    /** True until the first data for [month] has been loaded. */
    val isLoading: Boolean = true,
) {
    /** Date for a new transaction: today when viewing the current month, else the 1st of [month]. */
    fun defaultNewTransactionDate(today: LocalDate = LocalDate.now()): LocalDate =
        if (YearMonth.from(today) == month) today else month.atDay(1)
}
