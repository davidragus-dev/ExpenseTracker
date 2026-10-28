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
