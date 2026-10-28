package io.github.davidragus_dev.expensetracker.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class MoneyTest {

    private val spain = Locale.forLanguageTag("es-ES")

    // NumberFormat separates the amount and the symbol with a non-breaking space
    private fun String.normalizeSpaces() = replace('\u00A0', ' ').replace('\u202F', ' ')

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
    fun parseToCents_wholeAmounts() {
        assertEquals(1_200L, Money.parseToCents("12"))
        assertEquals(0L, Money.parseToCents("0"))
        assertEquals(700L, Money.parseToCents(" 7 € "))
    }

    @Test
    fun parseToCents_commaOrDotAsDecimalSeparator() {
        assertEquals(1_250L, Money.parseToCents("12,5"))
        assertEquals(1_250L, Money.parseToCents("12.50"))
        assertEquals(123_450L, Money.parseToCents("1234,5"))
        assertEquals(50L, Money.parseToCents(",5"))
        // While typing: "12," is still 12 €
        assertEquals(1_200L, Money.parseToCents("12,"))
    }

    @Test
    fun parseToCents_thousandsSeparators() {
        assertEquals(123_456L, Money.parseToCents("1.234,56"))
        assertEquals(123_456L, Money.parseToCents("1,234.56"))
        assertEquals(123_400L, Money.parseToCents("1.234"))
        assertEquals(123_456_700L, Money.parseToCents("1.234.567"))
        assertEquals(123_456_780L, Money.parseToCents("1,234,567.8"))
    }

    @Test
    fun parseToCents_invalidInput_returnsNull() {
        assertEquals(null, Money.parseToCents(""))
        assertEquals(null, Money.parseToCents("   "))
        assertEquals(null, Money.parseToCents("abc"))
        assertEquals(null, Money.parseToCents("-5"))
        assertEquals(null, Money.parseToCents("12,5,0"))
        assertEquals(null, Money.parseToCents("1,2345"))
        assertEquals(null, Money.parseToCents("."))
        assertEquals(null, Money.parseToCents("99999999999999"))
    }

    @Test
    fun toEditableText_usesTheLocaleDecimalSeparator_andParsesBack() {
        assertEquals("12,50", Money.toEditableText(1_250, spain))
        assertEquals("12.50", Money.toEditableText(1_250, Locale.US))
        assertEquals("1234,05", Money.toEditableText(123_405, spain))
        assertEquals("12", Money.toEditableText(1_200, spain))
        for (cents in listOf(5L, 1_250L, 123_405L, 100_000_000L)) {
            assertEquals(cents, Money.parseToCents(Money.toEditableText(cents, spain)))
        }
    }

    @Test
    fun monthTotals_balanceIsIncomeMinusExpense() {
        assertEquals(-300L, MonthTotals(incomeCents = 1_000, expenseCents = 1_300).balanceCents)
    }
}
