package io.github.davidragus_dev.expensetracker.data.local

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun localDate_isStoredAsDaysSinceEpoch() {
        assertEquals(0L, converters.localDateToEpochDay(LocalDate.of(1970, 1, 1)))
        assertEquals(1L, converters.localDateToEpochDay(LocalDate.of(1970, 1, 2)))
    }

    @Test
    fun localDate_roundTrips() {
        val date = LocalDate.of(2026, 10, 28)
        assertEquals(date, converters.epochDayToLocalDate(converters.localDateToEpochDay(date)))
    }
}
