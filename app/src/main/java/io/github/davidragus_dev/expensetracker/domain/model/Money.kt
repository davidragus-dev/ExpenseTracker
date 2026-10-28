package io.github.davidragus_dev.expensetracker.domain.model

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Los importes se guardan como céntimos en un [Long] para evitar los errores
 * de redondeo de Double. Se asume una moneda con 2 decimales.
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
