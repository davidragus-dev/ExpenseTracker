package io.github.davidragus_dev.expensetracker.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType.EXPENSE
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType.INCOME

/** Categorías que se crean la primera vez que se abre la app. */
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

/** Inserta [DefaultCategories] cuando Room crea la base de datos por primera vez. */
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
