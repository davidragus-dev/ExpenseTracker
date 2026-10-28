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

package io.github.davidragus_dev.expensetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.davidragus_dev.expensetracker.ui.about.AboutScreen
import io.github.davidragus_dev.expensetracker.ui.monthly.MonthlyScreen
import io.github.davidragus_dev.expensetracker.ui.transaction.TransactionFormScreen
import io.github.davidragus_dev.expensetracker.ui.transaction.TransactionFormViewModel
import io.github.davidragus_dev.expensetracker.ui.transaction.TransactionFormViewModel.Companion.NEW_TRANSACTION_ID
import kotlinx.serialization.Serializable
import java.time.LocalDate

// Screens of the app. Navigation turns them into routes, so their fields are the screen arguments.

@Serializable
object MonthlyRoute

@Serializable
object AboutRoute

@Serializable
data class TransactionFormRoute(
    /** [NEW_TRANSACTION_ID] to add a new transaction. */
    val transactionId: Long = NEW_TRANSACTION_ID,
    /** Date for a new transaction, as days since 1970-01-01 (LocalDate is not serializable). */
    val defaultDateEpochDay: Long = LocalDate.now().toEpochDay(),
)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = MonthlyRoute) {
        composable<MonthlyRoute> {
            MonthlyScreen(
                onAddTransaction = { date ->
                    navController.navigate(TransactionFormRoute(defaultDateEpochDay = date.toEpochDay()))
                },
                onEditTransaction = { id ->
                    navController.navigate(TransactionFormRoute(transactionId = id))
                },
                onOpenAbout = { navController.navigate(AboutRoute) },
            )
        }
        composable<TransactionFormRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<TransactionFormRoute>()
            TransactionFormScreen(
                viewModel = viewModel(
                    factory = TransactionFormViewModel.factory(
                        transactionId = route.transactionId,
                        defaultDate = LocalDate.ofEpochDay(route.defaultDateEpochDay),
                    ),
                ),
                onClose = { navController.popBackStack() },
            )
        }
        composable<AboutRoute> {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
