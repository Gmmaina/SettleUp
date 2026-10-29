package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DebtDirection
import com.example.data.model.DebtStatus
import com.example.data.model.ReminderRecurrence
import com.example.data.model.TransactionType

@Entity(tableName = "people")
data class PersonEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey val id: String,
    val personId: String,
    val direction: DebtDirection,
    val originalPrincipal: Long, // in shillings / currency integer units
    val description: String = "",
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val status: DebtStatus = DebtStatus.ACTIVE,
    val isShared: Boolean = false
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val debtId: String,
    val personId: String,
    val type: TransactionType,
    val amount: Long, // positive amount
    val effectiveDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val note: String = "",
    val proofUri: String? = null,
    val proofType: String? = null,
    val verificationStatus: String = "VERIFIED", // "VERIFIED", "AWAITING_CONFIRMATION", "DISPUTED"
    val reversalOfId: String? = null,
    val adjustmentReason: String? = null
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val debtId: String?,
    val title: String,
    val note: String = "",
    val scheduledTime: Long,
    val recurrence: ReminderRecurrence = ReminderRecurrence.ONCE,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val type: String, // "REMINDER", "TRANSACTION", "STATUS"
    val relatedDebtId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "primary_user",
    val displayName: String = "Gideon",
    val email: String = "gideonmaina19@gmail.com",
    val preferredCurrency: String = "KSh",
    val timezone: String = "Africa/Nairobi",
    val notificationsEnabled: Boolean = true
)
