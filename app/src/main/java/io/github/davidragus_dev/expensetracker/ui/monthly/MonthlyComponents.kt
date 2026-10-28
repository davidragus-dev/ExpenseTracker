/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Expense Tracker - keep track of monthly income and expenses
 * Copyright (C) 2026 David Ruiz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.davidragus_dev.expensetracker.ui.monthly

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.davidragus_dev.expensetracker.R
import io.github.davidragus_dev.expensetracker.data.local.TransactionWithCategory
import io.github.davidragus_dev.expensetracker.domain.model.Money
import io.github.davidragus_dev.expensetracker.domain.model.MonthTotals
import io.github.davidragus_dev.expensetracker.domain.model.TransactionType
import io.github.davidragus_dev.expensetracker.ui.theme.expenseColor
import io.github.davidragus_dev.expensetracker.ui.theme.incomeColor
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
private fun currentLocale(): Locale = LocalConfiguration.current.locales[0]

/** "‹  October 2026  ›" */
@Composable
fun MonthSelector(
    month: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = currentLocale()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_left),
                contentDescription = stringResource(R.string.previous_month),
            )
        }
        Text(
            text = month.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
                .replaceFirstChar { it.titlecase(locale) },
            style = MaterialTheme.typography.titleLarge,
        )
        IconButton(onClick = onNextMonth) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = stringResource(R.string.next_month),
            )
        }
    }
}

/** Card with the month balance and, below it, total income and expenses. */
@Composable
fun MonthSummary(totals: MonthTotals, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.balance),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = Money.format(totals.balanceCents),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    totals.balanceCents > 0 -> incomeColor()
                    totals.balanceCents < 0 -> expenseColor()
                    else -> MaterialTheme.colorScheme.onSurface
                },
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                SummaryAmount(
                    label = stringResource(R.string.income),
                    text = Money.format(totals.incomeCents),
                    color = incomeColor(),
                    modifier = Modifier.weight(1f),
                )
                SummaryAmount(
                    label = stringResource(R.string.expenses),
                    text = Money.format(totals.expenseCents),
                    color = expenseColor(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SummaryAmount(label: String, text: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = text, style = MaterialTheme.typography.titleMedium, color = color)
    }
}

/**
 * One transaction: category badge, note (or category name), date and signed amount.
 */
@Composable
fun TransactionRow(item: TransactionWithCategory, modifier: Modifier = Modifier) {
    val transaction = item.transaction
    val category = item.category
    val locale = currentLocale()
    val date = transaction.date.format(dateFormatter(transaction.date, locale))
    val isIncome = transaction.type == TransactionType.INCOME

    ListItem(
        modifier = modifier,
        leadingContent = { CategoryBadge(name = category.name, color = Color(category.color)) },
        headlineContent = { Text(transaction.note ?: category.name) },
        supportingContent = {
            Text(if (transaction.note != null) "${category.name} · $date" else date)
        },
        trailingContent = {
            Text(
                text = if (isIncome) {
                    "+" + Money.format(transaction.amountCents)
                } else {
                    Money.format(-transaction.amountCents)
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (isIncome) incomeColor() else expenseColor(),
            )
        },
    )
}

/** A [TransactionRow] that opens on tap and is deleted by swiping it to the left. */
@Composable
fun SwipeToDeleteTransactionRow(
    item: TransactionWithCategory,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        onDismiss = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onDelete()
        },
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        TransactionRow(item = item, modifier = Modifier.clickable(onClick = onClick))
    }
}

// Placeholder until categories have real icons: a colored circle with the first letter
@Composable
private fun CategoryBadge(name: String, color: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.take(1).uppercase(),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

// The year is only shown for dates outside the current year
private fun dateFormatter(date: LocalDate, locale: Locale): DateTimeFormatter =
    if (date.year == LocalDate.now().year) {
        DateTimeFormatter.ofPattern("d MMM", locale)
    } else {
        DateTimeFormatter.ofPattern("d MMM yyyy", locale)
    }
