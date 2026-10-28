package io.github.davidragus_dev.expensetracker

import android.content.Context
import io.github.davidragus_dev.expensetracker.data.local.AppDatabase
import io.github.davidragus_dev.expensetracker.data.repository.CategoryRepository
import io.github.davidragus_dev.expensetracker.data.repository.TransactionRepository

/** Crea y guarda las dependencias compartidas de la app (inyección manual). */
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
