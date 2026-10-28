package io.github.davidragus_dev.expensetracker.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.davidragus_dev.expensetracker.data.local.AppDatabase
import io.github.davidragus_dev.expensetracker.data.local.CategoryEntity
import io.github.davidragus_dev.expensetracker.data.local.DefaultCategories
import io.github.davidragus_dev.expensetracker.data.local.DefaultCategoriesCallback
import io.github.davidragus_dev.expensetracker.data.local.TransactionEntity
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType.EXPENSE
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType.INCOME
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class CategoryRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: CategoryRepository

    @Before
    fun setUp() {
        // Con el mismo callback que la app real, para comprobar las categorías iniciales
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).addCallback(DefaultCategoriesCallback).allowMainThreadQueries().build()
        repository = CategoryRepository(db.categoryDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun newDatabase_containsDefaultCategories() = runTest {
        val names = repository.observeAll().first().map { it.name }.toSet()

        assertEquals(DefaultCategories.map { it.name }.toSet(), names)
    }

    @Test
    fun observeByType_returnsOnlyThatType() = runTest {
        val incomes = repository.observeByType(INCOME).first()

        assertTrue(incomes.isNotEmpty())
        assertTrue(incomes.all { it.type == INCOME })
        assertTrue("Nómina" in incomes.map { it.name })
    }

    @Test
    fun add_update_delete() = runTest {
        val id = repository.add(CategoryEntity(name = "Mascotas", type = EXPENSE, color = 0xFF795548, icon = "pets"))

        repository.update(repository.get(id)!!.copy(name = "Mascota"))
        assertEquals("Mascota", repository.get(id)!!.name)

        repository.delete(repository.get(id)!!)
        assertNull(repository.get(id))
    }

    @Test
    fun delete_categoryWithTransactions_isRejected() = runTest {
        val food = repository.observeByType(EXPENSE).first().first { it.name == "Comida" }
        db.transactionDao().insert(
            TransactionEntity(amountCents = 1_000, type = EXPENSE, categoryId = food.id, date = LocalDate.of(2026, 10, 1)),
        )

        try {
            repository.delete(food)
            fail("Se esperaba SQLiteConstraintException")
        } catch (expected: SQLiteConstraintException) {
            // La categoría y su movimiento siguen ahí
        }
        assertEquals(food, repository.get(food.id))
    }
}
