package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ReminderEntity
import com.example.data.model.DebtDirection
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.SettleUpViewModel

@Composable
fun HomeScreen(
    viewModel: SettleUpViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val summary by viewModel.dashboardSummary.collectAsState()
    val recentTxs by viewModel.recentTransactions.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()

    val pendingReminders = remember(reminders) {
        reminders.filter { !it.isCompleted }.take(2)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhiteBg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Top Header
        item {
            SettleUpHeader(
                userName = profile.displayName,
                unreadNotificationCount = unreadCount,
                onAvatarClick = { viewModel.navigateTo(Screen.Settings) },
                onNotificationClick = { viewModel.navigateTo(Screen.Notifications) }
            )
        }

        // Hero Financial Card
        item {
            HeroFinancialCard(
                totalOutstandingFormatted = viewModel.formatCurrency(summary.totalOutstanding),
                totalIOweFormatted = viewModel.formatCurrency(summary.totalIOwe),
                totalOwedToMeFormatted = viewModel.formatCurrency(summary.totalOwedToMe),
                onIOweClick = { viewModel.navigateTo(Screen.AddDebt(DebtDirection.BORROWED)) },
                onOwedToMeClick = { viewModel.navigateTo(Screen.AddDebt(DebtDirection.LENT)) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        // Two Category Cards: Money I owe & Owed to me
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DirectionSummaryCard(
                    title = "Money I owe",
                    amountFormatted = viewModel.formatCurrency(summary.totalIOwe),
                    peopleCount = summary.peopleIOweCount,
                    isOwedToMe = false,
                    onClick = { viewModel.navigateTo(Screen.AddDebt(DebtDirection.BORROWED)) },
                    modifier = Modifier.weight(1f)
                )

                DirectionSummaryCard(
                    title = "Owed to me",
                    amountFormatted = viewModel.formatCurrency(summary.totalOwedToMe),
                    peopleCount = summary.peopleOwedToMeCount,
                    isOwedToMe = true,
                    onClick = { viewModel.navigateTo(Screen.AddDebt(DebtDirection.LENT)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Actions Row
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Quick actions",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.Add,
                        label = "Borrow",
                        badgeColor = WarmAmberBadge,
                        iconTint = WarmAmberIcon,
                        onClick = { viewModel.navigateTo(Screen.AddDebt(DebtDirection.BORROWED)) },
                        modifier = Modifier.weight(1f),
                        testTag = "quick_action_borrow"
                    )
                    QuickActionButton(
                        icon = Icons.Default.Add,
                        label = "Lend",
                        badgeColor = MintGreenLight,
                        iconTint = MintGreenDark,
                        onClick = { viewModel.navigateTo(Screen.AddDebt(DebtDirection.LENT)) },
                        modifier = Modifier.weight(1f),
                        testTag = "quick_action_lend"
                    )
                    QuickActionButton(
                        icon = Icons.Default.Payments,
                        label = "Repay",
                        badgeColor = ForestGreenContainer,
                        iconTint = ForestGreen,
                        onClick = { viewModel.navigateTo(Screen.RecordRepayment()) },
                        modifier = Modifier.weight(1f),
                        testTag = "quick_action_repay"
                    )
                    QuickActionButton(
                        icon = Icons.Default.Alarm,
                        label = "Reminder",
                        badgeColor = Color(0xFFF1F3F5),
                        iconTint = TextPrimary,
                        onClick = { viewModel.navigateTo(Screen.AddReminder()) },
                        modifier = Modifier.weight(1f),
                        testTag = "quick_action_reminder"
                    )
                }
            }
        }

        // Upcoming Reminders (if any)
        if (pendingReminders.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFDE68A)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = WarmAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Upcoming Reminder",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            }
                            Text(
                                text = "View all",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WarmAmber,
                                modifier = Modifier.clickable { viewModel.navigateTo(Screen.AddReminder()) }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val firstRem = pendingReminders.first()
                        Text(
                            text = firstRem.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        if (firstRem.note.isNotBlank()) {
                            Text(
                                text = firstRem.note,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Recent Activity Card (Matches Mockup)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .testTag("recent_activity_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent activity",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = "View all",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestGreen,
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(Screen.Activity) }
                                .testTag("view_all_activity_link")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (recentTxs.isEmpty()) {
                        Text(
                            text = "No recent transactions yet. Add your first loan or borrowing above.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        recentTxs.take(4).forEachIndexed { index, (tx, personName) ->
                            TransactionRowItem(
                                transaction = tx,
                                personName = personName,
                                relativeDate = viewModel.formatRelativeDate(tx.effectiveDate),
                                amountFormatted = viewModel.formatCurrency(tx.amount),
                                onClick = { viewModel.navigateTo(Screen.TransactionDetail(tx.id)) }
                            )
                            if (index < recentTxs.take(4).size - 1) {
                                HorizontalDivider(
                                    color = DividerColor,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    badgeColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}
