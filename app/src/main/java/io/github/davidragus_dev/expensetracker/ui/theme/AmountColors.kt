package io.github.davidragus_dev.expensetracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// Material 3 has no "positive"/"negative" colors, so pick them based on the current theme
@Composable
private fun isDarkColorScheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

@Composable
fun incomeColor(): Color = if (isDarkColorScheme()) IncomeDark else IncomeLight

@Composable
fun expenseColor(): Color = if (isDarkColorScheme()) ExpenseDark else ExpenseLight
