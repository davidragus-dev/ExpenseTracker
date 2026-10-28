package io.github.davidragus_dev.expensetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
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
    }
}
