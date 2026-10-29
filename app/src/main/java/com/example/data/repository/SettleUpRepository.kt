package com.example.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.data.AppDatabase
import com.example.data.entity.*
import com.example.data.model.*
import kotlinx.coroutines.flow.*
import java.util.UUID

class SettleUpRepository(private val db: AppDatabase) {

    private val personDao = db.personDao()
    private val debtDao = db.debtDao()
    private val transactionDao = db.transactionDao()
    private val reminderDao = db.reminderDao()
    private val notificationDao = db.notificationDao()
    private val userProfileDao = db.userProfileDao()

    suspend fun checkAndSeedInitialData() {
        val currentDebts = debtDao.getAllDebts().first()
        if (currentDebts.isEmpty()) {
            seedMockupData()
        }
    }

    suspend fun resetToDefaultData() {
        db.withTransaction {
            // Delete all and re-seed
            val debts = debtDao.getAllDebts().first()
            debts.forEach { debtDao.deleteDebt(it) }
            val people = personDao.getAllPeople().first()
            people.forEach { personDao.deletePerson(it) }
            val txs = transactionDao.getAllTransactions().first()
            txs.forEach { transactionDao.deleteTransaction(it) }
            val reminders = reminderDao.getAllReminders().first()
            reminders.forEach { reminderDao.deleteReminder(it) }
            notificationDao.clearAllNotifications()
            seedMockupData()
        }
    }

    private suspend fun seedMockupData() {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L

        // Profile
        userProfileDao.insertOrUpdate(
            UserProfileEntity(
                id = "primary_user",
                displayName = "Gideon",
                email = "gideonmaina19@gmail.com",
                preferredCurrency = "KSh",
                timezone = "Africa/Nairobi",
                notificationsEnabled = true
            )
        )

        // People
        val maryId = "person_mary"
        val johnId = "person_john"
        val davidId = "person_david"
        val sarahId = "person_sarah"
        val kevinId = "person_kevin"

        personDao.insertPerson(PersonEntity(maryId, "Mary", "0712345678", "mary@example.com", "College friend"))
        personDao.insertPerson(PersonEntity(johnId, "John", "0723456789", "john@example.com", "Colleague at work"))
        personDao.insertPerson(PersonEntity(davidId, "David", "0734567890", "david@example.com", "Neighbor"))
        personDao.insertPerson(PersonEntity(sarahId, "Sarah", "0745678901", "sarah@example.com", "Cousin"))
        personDao.insertPerson(PersonEntity(kevinId, "Kevin", "0756789012", "kevin@example.com", "Mechanic"))

        // Debts:
        // MONEY I OWE: Total KSh 5,000 (2 people)
        // 1. John: Borrowed 4,000, borrowed additional 1,000 yesterday, repaid 2,000 earlier -> Net balance: 3,000
        val debtJohnId = "debt_john_1"
        debtDao.insertDebt(
            DebtEntity(
                id = debtJohnId,
                personId = johnId,
                direction = DebtDirection.BORROWED,
                originalPrincipal = 2000L,
                description = "Car maintenance loan",
                dueDate = now + 14 * oneDayMs,
                createdAt = now - 5 * oneDayMs,
                status = DebtStatus.ACTIVE
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                id = "tx_john_initial",
                debtId = debtJohnId,
                personId = johnId,
                type = TransactionType.BORROWED,
                amount = 2000L,
                effectiveDate = now - 5 * oneDayMs,
                note = "Initial loan for brake pads replacement"
            )
        )
        // Additional borrowing yesterday (+ KSh 1,000) matching mockup!
        transactionDao.insertTransaction(
            TransactionEntity(
                id = "tx_john_recent",
                debtId = debtJohnId,
                personId = johnId,
                type = TransactionType.BORROWED,
                amount = 1000L,
                effectiveDate = now - 1 * oneDayMs,
                note = "Additional emergency spare part purchase"
            )
        )

        // 2. Kevin: Borrowed 2,000
        val debtKevinId = "debt_kevin_1"
        debtDao.insertDebt(
            DebtEntity(
                id = debtKevinId,
                personId = kevinId,
                direction = DebtDirection.BORROWED,
                originalPrincipal = 2000L,
                description = "Hardware supplies credit",
                dueDate = now + 7 * oneDayMs,
                createdAt = now - 8 * oneDayMs,
                status = DebtStatus.ACTIVE
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                id = "tx_kevin_initial",
                debtId = debtKevinId,
                personId = kevinId,
                type = TransactionType.BORROWED,
                amount = 2000L,
                effectiveDate = now - 8 * oneDayMs,
                note = "Supplies for home painting"
            )
        )

        // OWED TO ME: Total KSh 3,500 (3 people)
        // 1. Mary: Lent 2,000 originally. Repaid 500 today! -> Net balance: 1,500
        val debtMaryId = "debt_mary_1"
        debtDao.insertDebt(
            DebtEntity(
                id = debtMaryId,
                personId = maryId,
                direction = DebtDirection.LENT,
                originalPrincipal = 2000L,
                description = "Emergency grocery support",
                dueDate = now + 10 * oneDayMs,
                createdAt = now - 3 * oneDayMs,
                status = DebtStatus.ACTIVE
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                id = "tx_mary_initial",
                debtId = debtMaryId,
                personId = maryId,
                type = TransactionType.LENT,
                amount = 2000L,
                effectiveDate = now - 3 * oneDayMs,
                note = "Sent via M-Pesa"
            )
        )
        // Payment received today (+ KSh 500) matching mockup!
        transactionDao.insertTransaction(
            TransactionEntity(
                id = "tx_mary_today",
                debtId = debtMaryId,
                personId = maryId,
                type = TransactionType.REPAYMENT_RECEIVED,
                amount = 500L,
                effectiveDate = now,
                note = "Partial repayment via M-Pesa",
                verificationStatus = "VERIFIED"
            )
        )

        // 2. David: Lent 1,200
        val debtDavidId = "debt_david_1"
        debtDao.insertDebt(
            DebtEntity(
                id = debtDavidId,
                personId = davidId,
                direction = DebtDirection.LENT,
                originalPrincipal = 1200L,
                description = "Dinner & transport split",
                dueDate = now + 4 * oneDayMs,
                createdAt = now - 6 * oneDayMs,
                status = DebtStatus.ACTIVE
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                id = "tx_david_initial",
                debtId = debtDavidId,
                personId = davidId,
                type = TransactionType.LENT,
                amount = 1200L,
                effectiveDate = now - 6 * oneDayMs,
                note = "Weekend outing bill share"
            )
        )

        // 3. Sarah: Lent 800
        val debtSarahId = "debt_sarah_1"
        debtDao.insertDebt(
            DebtEntity(
                id = debtSarahId,
                personId = sarahId,
                direction = DebtDirection.LENT,
                originalPrincipal = 800L,
                description = "Book purchase",
                dueDate = now + 20 * oneDayMs,
                createdAt = now - 12 * oneDayMs,
                status = DebtStatus.ACTIVE
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                id = "tx_sarah_initial",
                debtId = debtSarahId,
                personId = sarahId,
                type = TransactionType.LENT,
                amount = 800L,
                effectiveDate = now - 12 * oneDayMs,
                note = "Study guide book"
            )
        )

        // Add 1 Cleared debt to show history preservation:
        val clearedDebtId = "debt_mary_cleared"
        debtDao.insertDebt(
            DebtEntity(
                id = clearedDebtId,
                personId = maryId,
                direction = DebtDirection.LENT,
                originalPrincipal = 1500L,
                description = "Concert ticket advance",
                createdAt = now - 30 * oneDayMs,
                status = DebtStatus.CLEARED
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                id = "tx_cleared_1",
                debtId = clearedDebtId,
                personId = maryId,
                type = TransactionType.LENT,
                amount = 1500L,
                effectiveDate = now - 30 * oneDayMs
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                id = "tx_cleared_2",
                debtId = clearedDebtId,
                personId = maryId,
                type = TransactionType.REPAYMENT_RECEIVED,
                amount = 1500L,
                effectiveDate = now - 22 * oneDayMs,
                note = "Fully paid back"
            )
        )

        // Reminders
        reminderDao.insertReminder(
            ReminderEntity(
                id = "rem_1",
                debtId = debtJohnId,
                title = "Repay John car maintenance loan",
                note = "Agreed to send KSh 3,000 on mid-month salary",
                scheduledTime = now + 3 * oneDayMs,
                recurrence = ReminderRecurrence.ONCE
            )
        )
        reminderDao.insertReminder(
            ReminderEntity(
                id = "rem_2",
                debtId = debtMaryId,
                title = "Follow up with Mary for remaining balance",
                note = "Remaining KSh 1,500 due end of month",
                scheduledTime = now + 10 * oneDayMs,
                recurrence = ReminderRecurrence.ONCE
            )
        )

        // Notifications
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_1",
                title = "Payment recorded from Mary",
                message = "Mary made a partial repayment of KSh 500 via M-Pesa.",
                type = "TRANSACTION",
                relatedDebtId = debtMaryId,
                createdAt = now - 2 * 60 * 60 * 1000L,
                isRead = false
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_2",
                title = "Reminder: John's loan due soon",
                message = "KSh 3,000 balance owed to John is coming due in 3 days.",
                type = "REMINDER",
                relatedDebtId = debtJohnId,
                createdAt = now - 5 * 60 * 60 * 1000L,
                isRead = true
            )
        )
    }

    // Reactive Dashboard
    fun observeDashboard(): Flow<DashboardSummary> {
        return combine(
            debtDao.getAllDebts(),
            transactionDao.getAllTransactions()
        ) { debts, transactions ->
            calculateDashboardSummary(debts, transactions)
        }
    }

    private fun calculateDashboardSummary(
        debts: List<DebtEntity>,
        transactions: List<TransactionEntity>
    ): DashboardSummary {
        val txsByDebt = transactions.groupBy { it.debtId }

        var totalIOwe = 0L
        var totalOwedToMe = 0L
        val peopleIOwe = mutableSetOf<String>()
        val peopleOwedToMe = mutableSetOf<String>()
        var activeCount = 0
        var clearedCount = 0

        for (debt in debts) {
            val debtTxs = txsByDebt[debt.id] ?: emptyList()
            val balance = calculateBalanceForDebt(debt, debtTxs)

            if (balance > 0L) {
                activeCount++
                if (debt.direction == DebtDirection.BORROWED) {
                    totalIOwe += balance
                    peopleIOwe.add(debt.personId)
                } else {
                    totalOwedToMe += balance
                    peopleOwedToMe.add(debt.personId)
                }
            } else {
                clearedCount++
            }
        }

        return DashboardSummary(
            totalOutstanding = totalIOwe + totalOwedToMe,
            totalIOwe = totalIOwe,
            totalOwedToMe = totalOwedToMe,
            peopleIOweCount = peopleIOwe.size,
            peopleOwedToMeCount = peopleOwedToMe.size,
            activeDebtsCount = activeCount,
            clearedDebtsCount = clearedCount
        )
    }

    // Calculate individual debt balance
    fun calculateBalanceForDebt(debt: DebtEntity, txs: List<TransactionEntity>): Long {
        if (debt.direction == DebtDirection.BORROWED) {
            // Principal + extra borrowed - repayments + adjustments
            var balance = 0L
            val sortedTxs = txs.sortedBy { it.effectiveDate }
            for (tx in sortedTxs) {
                when (tx.type) {
                    TransactionType.BORROWED -> balance += tx.amount
                    TransactionType.REPAYMENT_MADE -> balance -= tx.amount
                    TransactionType.ADJUSTMENT -> balance += tx.amount // adjustment note may explain sign
                    TransactionType.REVERSAL -> {
                        // Reversal negates original transaction
                        val original = txs.find { it.id == tx.reversalOfId }
                        if (original != null) {
                            if (original.type == TransactionType.BORROWED) balance -= original.amount
                            else if (original.type == TransactionType.REPAYMENT_MADE) balance += original.amount
                        }
                    }
                    else -> {}
                }
            }
            return if (balance < 0L) 0L else balance
        } else {
            // LENT direction
            var balance = 0L
            val sortedTxs = txs.sortedBy { it.effectiveDate }
            for (tx in sortedTxs) {
                when (tx.type) {
                    TransactionType.LENT -> balance += tx.amount
                    TransactionType.REPAYMENT_RECEIVED -> balance -= tx.amount
                    TransactionType.ADJUSTMENT -> balance += tx.amount
                    TransactionType.REVERSAL -> {
                        val original = txs.find { it.id == tx.reversalOfId }
                        if (original != null) {
                            if (original.type == TransactionType.LENT) balance -= original.amount
                            else if (original.type == TransactionType.REPAYMENT_RECEIVED) balance += original.amount
                        }
                    }
                    else -> {}
                }
            }
            return if (balance < 0L) 0L else balance
        }
    }

    // Observe all debts with up-to-date balances and person names
    fun observeDebtsWithDetails(): Flow<List<DebtWithPerson>> {
        return combine(
            debtDao.getAllDebts(),
            personDao.getAllPeople(),
            transactionDao.getAllTransactions()
        ) { debts, people, txs ->
            val peopleMap = people.associateBy { it.id }
            val txsByDebt = txs.groupBy { it.debtId }

            debts.map { debt ->
                val debtTxs = txsByDebt[debt.id] ?: emptyList()
                val currentBalance = calculateBalanceForDebt(debt, debtTxs)
                val totalRepaid = debtTxs.filter {
                    it.type == TransactionType.REPAYMENT_MADE || it.type == TransactionType.REPAYMENT_RECEIVED
                }.sumOf { it.amount }

                DebtWithPerson(
                    debtId = debt.id,
                    personId = debt.personId,
                    personName = peopleMap[debt.personId]?.name ?: "Unknown",
                    direction = debt.direction,
                    originalPrincipal = debt.originalPrincipal,
                    currentBalance = currentBalance,
                    totalRepaid = totalRepaid,
                    description = debt.description,
                    dueDate = debt.dueDate,
                    createdAt = debt.createdAt,
                    status = if (currentBalance == 0L) DebtStatus.CLEARED else DebtStatus.ACTIVE,
                    isShared = debt.isShared
                )
            }
        }
    }

    // Observe single debt with transactions
    fun observeDebtWithTransactions(debtId: String): Flow<Pair<DebtWithPerson?, List<TransactionEntity>>> {
        return combine(
            debtDao.observeDebtById(debtId),
            personDao.getAllPeople(),
            transactionDao.getTransactionsForDebt(debtId)
        ) { debt, people, txs ->
            if (debt == null) {
                null to emptyList()
            } else {
                val person = people.find { it.id == debt.personId }
                val currentBalance = calculateBalanceForDebt(debt, txs)
                val totalRepaid = txs.filter {
                    it.type == TransactionType.REPAYMENT_MADE || it.type == TransactionType.REPAYMENT_RECEIVED
                }.sumOf { it.amount }

                val debtWithPerson = DebtWithPerson(
                    debtId = debt.id,
                    personId = debt.personId,
                    personName = person?.name ?: "Unknown",
                    direction = debt.direction,
                    originalPrincipal = debt.originalPrincipal,
                    currentBalance = currentBalance,
                    totalRepaid = totalRepaid,
                    description = debt.description,
                    dueDate = debt.dueDate,
                    createdAt = debt.createdAt,
                    status = if (currentBalance == 0L) DebtStatus.CLEARED else DebtStatus.ACTIVE,
                    isShared = debt.isShared
                )
                debtWithPerson to txs
            }
        }
    }

    // Observe people with balances
    fun observePeopleWithBalances(): Flow<List<PersonWithBalance>> {
        return combine(
            personDao.getAllPeople(),
            debtDao.getAllDebts(),
            transactionDao.getAllTransactions()
        ) { people, debts, txs ->
            val debtsByPerson = debts.groupBy { it.personId }
            val txsByDebt = txs.groupBy { it.debtId }

            people.map { person ->
                val personDebts = debtsByPerson[person.id] ?: emptyList()
                var oweThem = 0L
                var owedToMe = 0L
                var activeCount = 0

                for (debt in personDebts) {
                    val debtTxs = txsByDebt[debt.id] ?: emptyList()
                    val balance = calculateBalanceForDebt(debt, debtTxs)
                    if (balance > 0L) {
                        activeCount++
                        if (debt.direction == DebtDirection.BORROWED) {
                            oweThem += balance
                        } else {
                            owedToMe += balance
                        }
                    }
                }

                PersonWithBalance(
                    id = person.id,
                    name = person.name,
                    phone = person.phone,
                    email = person.email,
                    amountIOwe = oweThem,
                    amountOwedToMe = owedToMe,
                    activeDebtsCount = activeCount,
                    totalDebtsCount = personDebts.size
                )
            }.sortedByDescending { it.amountIOwe + it.amountOwedToMe }
        }
    }

    // Observe recent transactions for dashboard activity
    fun observeRecentTransactions(limit: Int = 10): Flow<List<Pair<TransactionEntity, String>>> {
        return combine(
            transactionDao.getAllTransactions(),
            personDao.getAllPeople()
        ) { txs, people ->
            val peopleMap = people.associateBy { it.id }
            txs.take(limit).map { tx ->
                tx to (peopleMap[tx.personId]?.name ?: "Unknown")
            }
        }
    }

    // Atomic actions
    suspend fun createNewDebt(
        personId: String?,
        newPersonName: String?,
        direction: DebtDirection,
        amount: Long,
        description: String,
        dueDate: Long?,
        initialNote: String
    ): String {
        return db.withTransaction {
            val resolvedPersonId = if (!personId.isNullOrBlank()) {
                personId
            } else {
                val newId = "person_" + UUID.randomUUID().toString().take(8)
                val newPerson = PersonEntity(
                    id = newId,
                    name = newPersonName?.trim() ?: "Unnamed Person"
                )
                personDao.insertPerson(newPerson)
                newId
            }

            val debtId = "debt_" + UUID.randomUUID().toString().take(8)
            val newDebt = DebtEntity(
                id = debtId,
                personId = resolvedPersonId,
                direction = direction,
                originalPrincipal = amount,
                description = description,
                dueDate = dueDate,
                createdAt = System.currentTimeMillis(),
                status = DebtStatus.ACTIVE
            )
            debtDao.insertDebt(newDebt)

            val txType = if (direction == DebtDirection.BORROWED) TransactionType.BORROWED else TransactionType.LENT
            val initialTx = TransactionEntity(
                id = "tx_" + UUID.randomUUID().toString().take(8),
                debtId = debtId,
                personId = resolvedPersonId,
                type = txType,
                amount = amount,
                effectiveDate = System.currentTimeMillis(),
                note = initialNote.ifBlank { "Initial amount" }
            )
            transactionDao.insertTransaction(initialTx)

            // If due date was set, optionally add reminder
            if (dueDate != null && dueDate > System.currentTimeMillis()) {
                val reminder = ReminderEntity(
                    id = "rem_" + UUID.randomUUID().toString().take(8),
                    debtId = debtId,
                    title = "Due date for ${newPersonName ?: "debt"}",
                    note = "Payment of $amount is due",
                    scheduledTime = dueDate
                )
                reminderDao.insertReminder(reminder)
            }

            debtId
        }
    }

    suspend fun recordRepayment(
        debtId: String,
        amount: Long,
        effectiveDate: Long = System.currentTimeMillis(),
        note: String,
        proofUri: String? = null
    ): Boolean {
        return db.withTransaction {
            val debt = debtDao.getDebtById(debtId) ?: return@withTransaction false
            val txs = transactionDao.getTransactionsForDebt(debtId).first()
            val currentBalance = calculateBalanceForDebt(debt, txs)

            val txType = if (debt.direction == DebtDirection.BORROWED) {
                TransactionType.REPAYMENT_MADE
            } else {
                TransactionType.REPAYMENT_RECEIVED
            }

            val newTx = TransactionEntity(
                id = "tx_" + UUID.randomUUID().toString().take(8),
                debtId = debtId,
                personId = debt.personId,
                type = txType,
                amount = amount,
                effectiveDate = effectiveDate,
                note = note,
                proofUri = proofUri
            )
            transactionDao.insertTransaction(newTx)

            // Check if fully cleared
            val newBalance = currentBalance - amount
            if (newBalance <= 0L) {
                debtDao.updateDebt(debt.copy(status = DebtStatus.CLEARED))
                notificationDao.insertNotification(
                    NotificationEntity(
                        id = "notif_" + UUID.randomUUID().toString().take(8),
                        title = "Debt Cleared!",
                        message = "The debt for ${debt.description.ifBlank { "loan" }} has been fully repaid.",
                        type = "STATUS",
                        relatedDebtId = debtId
                    )
                )
            }
            true
        }
    }

    suspend fun recordAdditionalTransaction(
        debtId: String,
        type: TransactionType,
        amount: Long,
        note: String
    ): Boolean {
        return db.withTransaction {
            val debt = debtDao.getDebtById(debtId) ?: return@withTransaction false
            val newTx = TransactionEntity(
                id = "tx_" + UUID.randomUUID().toString().take(8),
                debtId = debtId,
                personId = debt.personId,
                type = type,
                amount = amount,
                effectiveDate = System.currentTimeMillis(),
                note = note
            )
            transactionDao.insertTransaction(newTx)

            // If debt was CLEARED but more borrowing occurred, reactivate it
            val allTxs = transactionDao.getTransactionsForDebt(debtId).first()
            val newBalance = calculateBalanceForDebt(debt, allTxs)
            if (newBalance > 0L && debt.status == DebtStatus.CLEARED) {
                debtDao.updateDebt(debt.copy(status = DebtStatus.ACTIVE))
            }
            true
        }
    }

    suspend fun recordReversal(
        originalTransactionId: String,
        reason: String
    ): Boolean {
        return db.withTransaction {
            val original = transactionDao.getTransactionById(originalTransactionId) ?: return@withTransaction false
            val debt = debtDao.getDebtById(original.debtId) ?: return@withTransaction false

            val reversalTx = TransactionEntity(
                id = "tx_rev_" + UUID.randomUUID().toString().take(8),
                debtId = original.debtId,
                personId = original.personId,
                type = TransactionType.REVERSAL,
                amount = original.amount,
                effectiveDate = System.currentTimeMillis(),
                note = "Reversal: $reason",
                reversalOfId = originalTransactionId,
                adjustmentReason = reason
            )
            transactionDao.insertTransaction(reversalTx)

            // Recalculate debt status
            val allTxs = transactionDao.getTransactionsForDebt(debt.id).first()
            val newBalance = calculateBalanceForDebt(debt, allTxs)
            val newStatus = if (newBalance <= 0L) DebtStatus.CLEARED else DebtStatus.ACTIVE
            if (newStatus != debt.status) {
                debtDao.updateDebt(debt.copy(status = newStatus))
            }
            true
        }
    }

    // Reminders
    fun observeReminders(): Flow<List<ReminderEntity>> = reminderDao.getAllReminders()

    suspend fun addReminder(reminder: ReminderEntity) = reminderDao.insertReminder(reminder)

    suspend fun updateReminder(reminder: ReminderEntity) = reminderDao.updateReminder(reminder)

    suspend fun deleteReminder(reminder: ReminderEntity) = reminderDao.deleteReminder(reminder)

    // Notifications
    fun observeNotifications(): Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    fun observeUnreadNotificationsCount(): Flow<Int> = notificationDao.getUnreadCount()

    suspend fun markNotificationAsRead(id: String) = notificationDao.markAsRead(id)

    suspend fun markAllNotificationsAsRead() = notificationDao.markAllAsRead()

    // Profile
    fun observeProfile(): Flow<UserProfileEntity?> = userProfileDao.observeProfile()

    suspend fun updateProfile(profile: UserProfileEntity) = userProfileDao.insertOrUpdate(profile)

    // People
    fun observeAllPeople(): Flow<List<PersonEntity>> = personDao.getAllPeople()

    suspend fun addPerson(name: String, phone: String = "", email: String = "", notes: String = ""): String {
        val id = "person_" + UUID.randomUUID().toString().take(8)
        personDao.insertPerson(PersonEntity(id, name.trim(), phone.trim(), email.trim(), notes.trim()))
        return id
    }

    suspend fun deletePerson(person: PersonEntity) = personDao.deletePerson(person)

    companion object {
        @Volatile
        private var INSTANCE: SettleUpRepository? = null

        fun getInstance(context: Context): SettleUpRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getInstance(context)
                val repo = SettleUpRepository(db)
                INSTANCE = repo
                repo
            }
        }
    }
}
