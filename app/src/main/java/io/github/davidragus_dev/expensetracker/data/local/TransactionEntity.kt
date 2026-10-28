package io.github.davidragus_dev.expensetracker.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType
import java.time.LocalDate

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            // A category that has transactions cannot be deleted
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("categoryId"), Index("date")],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Positive amount in cents; [type] gives the sign. */
    val amountCents: Long,
    /** Must match the type of the category. */
    val type: TransactionType,
    val categoryId: Long,
    val date: LocalDate,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

/** A transaction together with its category, for display in lists. */
data class TransactionWithCategory(
    @Embedded val transaction: TransactionEntity,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: CategoryEntity,
)
