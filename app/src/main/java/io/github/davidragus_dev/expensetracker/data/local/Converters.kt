package io.github.davidragus_dev.expensetracker.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

/** Room cannot store LocalDate, so it is saved as the number of days since 1970-01-01. */
class Converters {

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate): Long = date.toEpochDay()

    @TypeConverter
    fun epochDayToLocalDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)
}
