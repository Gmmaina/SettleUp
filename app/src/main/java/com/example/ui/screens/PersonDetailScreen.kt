package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DebtDirection
import com.example.data.model.DebtStatus
import com.example.ui.components.SimpleTopBar
import com.example.ui.components.TransactionRowItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.SettleUpViewModel

@Composable
fun PersonDetailScreen(
    personId: String,
    viewModel: SettleUpViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val peopleWithBalances by viewModel.peopleWithBalances.collectAsState()
    val allDebts by viewModel.allDebts.collectAsState()
    val recentTxs by viewModel.recentTransactions.collectAsState()

    val person = remember(peopleWithBalances, personId) {
        peopleWithBalances.find { it.id == personId }
    }

    val debtsForPerson = remember(allDebts, personId) {
        allDebts.filter { it.personId == personId }
    }

    val activeDebts = remember(debtsForPerson) {
        debtsForPerson.filter { it.status == DebtStatus.ACTIVE }
    }

    val clearedDebts = remember(debtsForPerson) {
        debtsForPerson.filter { it.status == DebtStatus.CLEARED }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhiteBg),
        topBar = {
            SimpleTopBar(
                title = person?.name ?: "Person Details",
                onBack = { viewModel.navigateBack() }
            )
        }
    ) { innerPadding ->
        if (person == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Person record not found", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(OffWhiteBg),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Profile & Balance Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(ForestGreenContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = person.name.take(1).uppercase(),
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = person.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            if (person.phone.isNotBlank()) {
                                Text(
                                    text = person.phone,
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = DividerColor)
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("You owe them", fontSize = 12.sp, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = viewModel.formatCurrency(person.amountIOwe),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (person.amountIOwe > 0) WarmAmberIcon else TextMuted
                                    )
                                }

                                VerticalDivider(
                                    modifier = Modifier.height(36.dp),
                                    color = DividerColor
                                )

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Owed to you", fontSize = 12.sp, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = viewModel.formatCurrency(person.amountOwedToMe),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (person.amountOwedToMe > 0) MintGreenDark else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                // Action Buttons for this Person
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.navigateTo(Screen.AddDebt(DebtDirection.BORROWED, person.id))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WarmAmberBadge, contentColor = WarmAmberIcon)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Borrow from ${person.name}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                viewModel.navigateTo(Screen.AddDebt(DebtDirection.LENT, person.id))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lend to ${person.name}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Active Debts Section
                item {
                    Text(
                        text = "Active Debts (${activeDebts.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
                    )
                }

                if (activeDebts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Text(
                                text = "No active debts with ${person.name}.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(activeDebts, key = { it.debtId }) { debt ->
                        DebtCardItem(
                            debt = debt,
                            formatCurrency = { viewModel.formatCurrency(it) },
                            onClick = { viewModel.navigateTo(Screen.DebtDetail(debt.debtId)) }
                        )
                    }
                }

                // Cleared Debts Section (History preservation)
                if (clearedDebts.isNotEmpty()) {
                    item {
                        Text(
                            text = "Cleared Debts (${clearedDebts.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)
                        )
                    }

                    items(clearedDebts, key = { it.debtId }) { debt ->
                        DebtCardItem(
                            debt = debt,
                            formatCurrency = { viewModel.formatCurrency(it) },
                            onClick = { viewModel.navigateTo(Screen.DebtDetail(debt.debtId)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DebtCardItem(
    debt: com.example.data.model.DebtWithPerson,
    formatCurrency: (Long) -> String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOwedToMe = debt.direction == DebtDirection.LENT
    val isCleared = debt.status == DebtStatus.CLEARED

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("debt_card_${debt.debtId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Direction Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCleared) Color(0xFFE5E7EB) else if (isOwedToMe) MintGreenLight else WarmAmberBadge
                ) {
                    Text(
                        text = if (isCleared) "CLEARED" else if (isOwedToMe) "OWED TO YOU" else "MONEY YOU OWE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCleared) TextSecondary else if (isOwedToMe) MintGreenDark else WarmAmberIcon,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = if (isCleared) "Settled" else formatCurrency(debt.currentBalance),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCleared) TextMuted else if (isOwedToMe) MintGreenDark else WarmAmberIcon
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = debt.description.ifBlank { "Loan record" },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Original: ${formatCurrency(debt.originalPrincipal)}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                if (debt.totalRepaid > 0) {
                    Text(
                        text = "Repaid: ${formatCurrency(debt.totalRepaid)}",
                        fontSize = 12.sp,
                        color = MintGreenDark
                    )
                }
            }
        }
    }
}
