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
