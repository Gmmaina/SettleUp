package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.SettleUpBottomNavigation
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OffWhiteBg
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.SettleUpViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SettleUpApp()
            }
        }
    }
}

@Composable
fun SettleUpApp(viewModel: SettleUpViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val isRootScreen = currentScreen is Screen.Home ||
            currentScreen is Screen.People ||
            currentScreen is Screen.Summary ||
            currentScreen is Screen.Activity

    BackHandler(enabled = !isRootScreen) {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (isRootScreen) {
                SettleUpBottomNavigation(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OffWhiteBg)
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is Screen.Home -> HomeScreen(viewModel = viewModel)
                is Screen.People -> PeopleScreen(viewModel = viewModel)
                is Screen.Summary -> SummaryScreen(viewModel = viewModel)
                is Screen.Activity -> ActivityScreen(viewModel = viewModel)
                is Screen.DebtDetail -> DebtDetailScreen(debtId = screen.debtId, viewModel = viewModel)
                is Screen.PersonDetail -> PersonDetailScreen(personId = screen.personId, viewModel = viewModel)
                is Screen.TransactionDetail -> TransactionDetailScreen(transactionId = screen.transactionId, viewModel = viewModel)
                is Screen.AddDebt -> AddDebtScreen(
                    initialDirection = screen.initialDirection,
                    targetPersonId = screen.targetPersonId,
                    viewModel = viewModel
                )
                is Screen.RecordRepayment -> RecordRepaymentScreen(
                    targetDebtId = screen.targetDebtId,
                    viewModel = viewModel
                )
                is Screen.AddReminder -> RemindersScreen(
                    targetDebtId = screen.targetDebtId,
                    viewModel = viewModel
                )
                is Screen.Notifications -> NotificationsScreen(viewModel = viewModel)
                is Screen.Settings -> SettingsScreen(viewModel = viewModel)
                is Screen.ExportReport -> ExportScreen(viewModel = viewModel)
            }
        }
    }
}
