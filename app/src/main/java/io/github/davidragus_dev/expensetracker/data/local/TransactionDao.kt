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

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.github.davidragus_dev.expensetracker.domain.model.CategoryTotal
import io.github.davidragus_dev.expensetracker.domain.model.MonthTotals
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransactionDao {

    /**
     * Transactions between two dates (both inclusive), most recent first. Within the same day,
     * the last created goes first (by createdAt, so a restored transaction keeps its place).
     */
    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE date BETWEEN :from AND :to
        ORDER BY date DESC, createdAt DESC, id DESC
        """
    )
    fun observeBetween(from: LocalDate, to: LocalDate): Flow<List<TransactionWithCategory>>

    @Query(
        """
        SELECT
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amountCents END), 0) AS incomeCents,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amountCents END), 0) AS expenseCents
        FROM transactions
        WHERE date BETWEEN :from AND :to
        """
    )
    fun observeTotalsBetween(from: LocalDate, to: LocalDate): Flow<MonthTotals>

    /** Total per category for one transaction type, highest first. */
    @Query(
        """
        SELECT c.id AS categoryId, c.name AS name, c.color AS color,
               SUM(t.amountCents) AS totalCents
        FROM transactions t
        INNER JOIN categories c ON c.id = t.categoryId
        WHERE t.type = :type AND t.date BETWEEN :from AND :to
        GROUP BY c.id
        ORDER BY totalCents DESC
        """
    )
    fun observeTotalsByCategoryBetween(
        type: TransactionType,
        from: LocalDate,
        to: LocalDate,
    ): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)
}
