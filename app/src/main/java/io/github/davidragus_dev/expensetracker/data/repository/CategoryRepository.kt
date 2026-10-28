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
