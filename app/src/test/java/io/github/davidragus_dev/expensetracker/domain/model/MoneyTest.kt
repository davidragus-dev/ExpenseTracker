package io.github.davidragus_dev.expensetracker.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class MoneyTest {

    private val spain = Locale.forLanguageTag("es-ES")

    // NumberFormat separa el importe y el símbolo con un espacio no separable
    private fun String.normalizeSpaces() = replace(' ', ' ').replace(' ', ' ')

    @Test
    fun format_euros_inSpanish() {
        assertEquals("12.345,67 €", Money.format(1_234_567, "EUR", spain).normalizeSpaces())
    }

    @Test
    fun format_alwaysShowsTwoDecimals() {
        assertEquals("5,00 €", Money.format(500, "EUR", spain).normalizeSpaces())
        assertEquals("0,05 €", Money.format(5, "EUR", spain).normalizeSpaces())
    }

    @Test
    fun format_negativeAmount() {
        assertEquals("-12,50 €", Money.format(-1_250, "EUR", spain).normalizeSpaces())
    }

    @Test
    fun monthTotals_balanceIsIncomeMinusExpense() {
        assertEquals(-300L, MonthTotals(incomeCents = 1_000, expenseCents = 1_300).balanceCents)
    }
}
