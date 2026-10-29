package com.example.data.model

enum class DebtDirection {
    BORROWED, // Money I owe (user is debtor, another person is creditor)
    LENT      // Owed to me (user is creditor, another person is debtor)
}

enum class DebtStatus {
    ACTIVE,
    CLEARED
}

enum class TransactionType {
    BORROWED,           // Increases money user owes
    LENT,               // Increases money owed to user
    REPAYMENT_MADE,     // Reduces money user owes
    REPAYMENT_RECEIVED, // Reduces money owed to user
    ADJUSTMENT,         // Signed manual adjustment
    REVERSAL            // Reversal of an earlier transaction
}

enum class ReminderRecurrence {
    ONCE,
    WEEKLY,
    MONTHLY
}

enum class TimePeriodFilter {
    ALL,
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    THIS_YEAR
}

data class DashboardSummary(
    val totalOutstanding: Long, // Sum of owed + receivable
    val totalIOwe: Long,        // Money I owe
    val totalOwedToMe: Long,    // Owed to me
    val peopleIOweCount: Int,   // Number of people I owe
    val peopleOwedToMeCount: Int, // Number of people who owe me
    val activeDebtsCount: Int,
    val clearedDebtsCount: Int
)

data class PersonWithBalance(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val amountIOwe: Long,
    val amountOwedToMe: Long,
    val activeDebtsCount: Int,
    val totalDebtsCount: Int
) {
    val netBalance: Long get() = amountOwedToMe - amountIOwe
}

data class DebtWithPerson(
    val debtId: String,
    val personId: String,
    val personName: String,
    val direction: DebtDirection,
    val originalPrincipal: Long,
    val currentBalance: Long,
    val totalRepaid: Long,
    val description: String,
    val dueDate: Long?,
    val createdAt: Long,
    val status: DebtStatus,
    val isShared: Boolean
)
