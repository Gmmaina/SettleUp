package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.*
import com.example.data.model.*
import com.example.data.repository.SettleUpRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

sealed class Screen {
    data object Home : Screen()
    data object People : Screen()
    data object Summary : Screen()
    data object Activity : Screen()
    data class DebtDetail(val debtId: String) : Screen()
    data class PersonDetail(val personId: String) : Screen()
    data class TransactionDetail(val transactionId: String) : Screen()
    data class AddDebt(val initialDirection: DebtDirection = DebtDirection.BORROWED, val targetPersonId: String? = null) : Screen()
    data class RecordRepayment(val targetDebtId: String? = null) : Screen()
    data class AddReminder(val targetDebtId: String? = null) : Screen()
    data object Notifications : Screen()
    data object Settings : Screen()
    data object ExportReport : Screen()
}

class SettleUpViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettleUpRepository.getInstance(application)

    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Home))
    val currentScreen: StateFlow<Screen> = _screenStack.map { it.last() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Screen.Home)

    // User Profile
    val userProfile: StateFlow<UserProfileEntity> = repository.observeProfile()
        .map { it ?: UserProfileEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfileEntity())

    // Dashboard
    val dashboardSummary: StateFlow<DashboardSummary> = repository.observeDashboard()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DashboardSummary(0, 0, 0, 0, 0, 0, 0)
        )

    val recentTransactions: StateFlow<List<Pair<TransactionEntity, String>>> =
        repository.observeRecentTransactions(10)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // People
    val peopleWithBalances: StateFlow<List<PersonWithBalance>> =
        repository.observePeopleWithBalances()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPeople: StateFlow<List<PersonEntity>> = repository.observeAllPeople()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Debts
    val allDebts: StateFlow<List<DebtWithPerson>> = repository.observeDebtsWithDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reminders
    val reminders: StateFlow<List<ReminderEntity>> = repository.observeReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications
    val notifications: StateFlow<List<NotificationEntity>> = repository.observeNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.observeUnreadNotificationsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // UI Feedback
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    // Navigation
    fun navigateTo(screen: Screen) {
        val current = _screenStack.value
        // If selecting a main bottom tab, reset root
        if (screen is Screen.Home || screen is Screen.People || screen is Screen.Summary || screen is Screen.Activity) {
            _screenStack.value = listOf(screen)
        } else {
            _screenStack.value = current + screen
        }
    }

    fun navigateBack(): Boolean {
        val current = _screenStack.value
        return if (current.size > 1) {
            _screenStack.value = current.dropLast(1)
            true
        } else {
            false
        }
    }

    // Helpers
    fun formatCurrency(amount: Long, currency: String = userProfile.value.preferredCurrency): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        return "$currency ${formatter.format(amount)}"
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
        return sdf.format(Date(timestamp))
    }

    fun formatRelativeDate(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val oneDay = 24 * 60 * 60 * 1000L
        return when {
            diff < 0 -> formatDate(timestamp)
            diff < oneDay -> "Today"
            diff < 2 * oneDay -> "Yesterday"
            diff < 7 * oneDay -> "${diff / oneDay}d ago"
            else -> formatDate(timestamp)
        }
    }

    // Actions
    fun createDebt(
        personId: String?,
        newPersonName: String?,
        direction: DebtDirection,
        amount: Long,
        description: String,
        dueDate: Long?,
        initialNote: String
    ) {
        viewModelScope.launch {
            if (amount <= 0) {
                _userMessage.emit("Amount must be greater than zero")
                return@launch
            }
            if (personId.isNullOrBlank() && newPersonName.isNullOrBlank()) {
                _userMessage.emit("Please select or enter a person's name")
                return@launch
            }
            val debtId = repository.createNewDebt(
                personId = personId,
                newPersonName = newPersonName,
                direction = direction,
                amount = amount,
                description = description,
                dueDate = dueDate,
                initialNote = initialNote
            )
            _userMessage.emit("Debt record saved successfully")
            navigateTo(Screen.DebtDetail(debtId))
        }
    }

    fun recordRepayment(
        debtId: String,
        amount: Long,
        note: String
    ) {
        viewModelScope.launch {
            if (amount <= 0) {
                _userMessage.emit("Repayment amount must be greater than zero")
                return@launch
            }
            val success = repository.recordRepayment(debtId, amount, note = note)
            if (success) {
                _userMessage.emit("Repayment recorded successfully")
                navigateBack()
            } else {
                _userMessage.emit("Failed to record repayment")
            }
        }
    }

    fun recordAdditionalBorrowingOrLending(
        debtId: String,
        type: TransactionType,
        amount: Long,
        note: String
    ) {
        viewModelScope.launch {
            if (amount <= 0) {
                _userMessage.emit("Amount must be greater than zero")
                return@launch
            }
            repository.recordAdditionalTransaction(debtId, type, amount, note)
            _userMessage.emit("Transaction added to debt")
        }
    }

    fun recordReversal(transactionId: String, reason: String) {
        viewModelScope.launch {
            if (reason.isBlank()) {
                _userMessage.emit("Please provide a reason for the reversal")
                return@launch
            }
            repository.recordReversal(transactionId, reason)
            _userMessage.emit("Transaction reversed")
            navigateBack()
        }
    }

    fun addPerson(name: String, phone: String, email: String, notes: String) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _userMessage.emit("Person name cannot be empty")
                return@launch
            }
            val id = repository.addPerson(name, phone, email, notes)
            _userMessage.emit("Added $name")
            navigateTo(Screen.PersonDetail(id))
        }
    }

    fun addReminder(debtId: String?, title: String, note: String, time: Long, recurrence: ReminderRecurrence) {
        viewModelScope.launch {
            if (title.isBlank()) {
                _userMessage.emit("Reminder title cannot be empty")
                return@launch
            }
            val reminder = ReminderEntity(
                id = "rem_" + UUID.randomUUID().toString().take(8),
                debtId = debtId,
                title = title,
                note = note,
                scheduledTime = time,
                recurrence = recurrence
            )
            repository.addReminder(reminder)
            _userMessage.emit("Reminder scheduled")
            navigateBack()
        }
    }

    fun toggleReminderCompleted(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.updateReminder(reminder.copy(isCompleted = !reminder.isCompleted))
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            _userMessage.emit("Reminder removed")
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun updateProfile(name: String, email: String, currency: String, timezone: String) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(
                displayName = name.ifBlank { "User" },
                email = email,
                preferredCurrency = currency.ifBlank { "KSh" },
                timezone = timezone
            )
            repository.updateProfile(updated)
            _userMessage.emit("Profile updated")
        }
    }

    fun resetToDemoData() {
        viewModelScope.launch {
            repository.resetToDefaultData()
            _userMessage.emit("Demo data restored")
            navigateTo(Screen.Home)
        }
    }
}
