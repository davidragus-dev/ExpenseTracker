package io.github.davidragus_dev.expensetracker.data.repository

import io.github.davidragus_dev.expensetracker.data.local.CategoryDao
import io.github.davidragus_dev.expensetracker.data.local.CategoryEntity
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val dao: CategoryDao) {

    fun observeAll(): Flow<List<CategoryEntity>> = dao.observeAll()

    fun observeByType(type: TransactionType): Flow<List<CategoryEntity>> = dao.observeByType(type)

    suspend fun get(id: Long): CategoryEntity? = dao.getById(id)

    /** Devuelve el id de la categoría creada. */
    suspend fun add(category: CategoryEntity): Long = dao.insert(category)

    suspend fun update(category: CategoryEntity) = dao.update(category)

    /**
     * Lanza [android.database.sqlite.SQLiteConstraintException] si la categoría
     * tiene movimientos.
     */
    suspend fun delete(category: CategoryEntity) = dao.delete(category)
}
