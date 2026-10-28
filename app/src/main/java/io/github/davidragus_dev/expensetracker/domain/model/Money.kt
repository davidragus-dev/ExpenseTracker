package io.github.davidragus_dev.expensetracker.domain.model

import java.math.BigDecimal
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Amounts are stored as cents in a [Long] to avoid the rounding errors of
 * Double. Currencies are assumed to have 2 decimal places.
 */
object Money {

    fun format(
        cents: Long,
        currencyCode: String = "EUR",
        locale: Locale = Locale.getDefault(),
    ): String {
        val formatter = NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance(currencyCode)
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return formatter.format(BigDecimal.valueOf(cents, 2))
    }

    /**
     * Amount as the user would type it, without symbol or thousands separators:
     * 1250 cents -> "12,50" (Spanish) or "12.50" (English); 1200 cents -> "12".
     * [parseToCents] reads it back.
     */
    fun toEditableText(cents: Long, locale: Locale = Locale.getDefault()): String {
        val euros = cents / 100
        val rest = cents % 100
        if (rest == 0L) return euros.toString()
        val separator = DecimalFormatSymbols.getInstance(locale).decimalSeparator
        return "$euros$separator${rest.toString().padStart(2, '0')}"
    }

    /**
     * Parses an amount typed by the user, such as "12,5", "12.50", "1.234,56" or "1,234.56",
     * into cents. Returns null if the text is not a valid positive amount with at most
     * 2 decimals.
     *
     * When both "," and "." appear, the last one is the decimal separator. When only one of
     * them appears, it is a thousands separator if it is repeated or followed by exactly
     * 3 digits ("1.234" = 1234), and a decimal separator otherwise.
     */
    fun parseToCents(input: String): Long? {
        val text = input.filterNot { it.isWhitespace() || it == '€' }
        if (text.isEmpty() || text.any { !it.isDigit() && it != ',' && it != '.' }) return null

        val lastSeparatorIndex = text.indexOfLast { it == ',' || it == '.' }
        val decimalSeparatorIndex = when {
            lastSeparatorIndex == -1 -> -1
            ',' in text && '.' in text -> lastSeparatorIndex
            text.count { it == text[lastSeparatorIndex] } > 1 -> -1
            text.length - lastSeparatorIndex - 1 == 3 -> -1
            else -> lastSeparatorIndex
        }

        val integerPart: String
        val decimalPart: String
        if (decimalSeparatorIndex == -1) {
            integerPart = text
            decimalPart = ""
        } else {
            integerPart = text.substring(0, decimalSeparatorIndex)
            decimalPart = text.substring(decimalSeparatorIndex + 1)
        }

        // Thousands groups must look like "1.234.567": 1-3 digits, then groups of exactly 3
        val groups = integerPart.split(',', '.')
        if (groups.size > 1 && (groups.first().length !in 1..3 || groups.drop(1).any { it.length != 3 })) {
            return null
        }

        val integerDigits = integerPart.filter { it.isDigit() }
        if (decimalPart.length > 2 || !decimalPart.all { it.isDigit() }) return null
        if (integerDigits.isEmpty() && decimalPart.isEmpty()) return null
        // Avoid overflowing Long with absurdly long inputs
        if (integerDigits.length > 13) return null

        val euros = integerDigits.ifEmpty { "0" }.toLong()
        val cents = decimalPart.padEnd(2, '0').toLong()
        return euros * 100 + cents
    }
}
