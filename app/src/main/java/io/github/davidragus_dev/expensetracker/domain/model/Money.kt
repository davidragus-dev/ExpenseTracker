package io.github.davidragus_dev.expensetracker.domain.model

import java.math.BigDecimal
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
}
