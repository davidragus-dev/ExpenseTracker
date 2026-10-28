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
    /** Color en formato ARGB, p. ej. 0xFFEF6C00. */
    val color: Long,
    /** Clave del icono que mostrará la interfaz, p. ej. "restaurant". */
    val icon: String,
)
