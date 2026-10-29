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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.TransactionEntity
import com.example.data.model.DebtDirection
import com.example.data.model.DebtStatus
import com.example.data.model.TransactionType
import com.example.data.repository.SettleUpRepository
import com.example.ui.components.SimpleTopBar
import com.example.ui.components.TransactionRowItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.SettleUpViewModel

@Composable
fun DebtDetailScreen(
    debtId: String,
    viewModel: SettleUpViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val context = LocalContext.current
    val repository = remember { SettleUpRepository.getInstance(context) }
    val debtWithTxs by repository.observeDebtWithTransactions(debtId).collectAsState(initial = null to emptyList())

    val debt = debtWithTxs.first
    val transactions = debtWithTxs.second

    var showAddMoreDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhiteBg),
        topBar = {
            SimpleTopBar(
                title = debt?.description?.ifBlank { "Debt Details" } ?: "Debt Details",
                onBack = { viewModel.navigateBack() }
            )
        }
    ) { innerPadding ->
        if (debt == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ForestGreen)
            }
        } else {
            val isOwedToMe = debt.direction == DebtDirection.LENT
            val isCleared = debt.status == DebtStatus.CLEARED

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(OffWhiteBg),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Main Balance Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .testTag("debt_detail_balance_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isCleared) Color(0xFF263238) else ForestGreen)
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
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (isCleared) "CLEARED" else if (isOwedToMe) "OWED TO YOU" else "YOU OWE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Text(
                                    text = debt.personName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Outstanding balance",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isCleared) "KSh 0" else viewModel.formatCurrency(debt.currentBalance),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Original Principal", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                    Text(
                                        viewModel.formatCurrency(debt.originalPrincipal),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                                Column {
                                    Text("Total Repaid", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                    Text(
                                        viewModel.formatCurrency(debt.totalRepaid),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MintGreen
                                    )
                                }
                                if (debt.dueDate != null) {
                                    Column {
                                        Text("Due Date", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                        Text(
                                            viewModel.formatDate(debt.dueDate),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!isCleared) {
                            Button(
                                onClick = { viewModel.navigateTo(Screen.RecordRepayment(debtId)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("debt_detail_record_repayment_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isOwedToMe) "Record Received" else "Record Repayment")
                            }
                        }

                        OutlinedButton(
                            onClick = { showAddMoreDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("debt_detail_add_more_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ForestGreen))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = ForestGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isOwedToMe) "Lend More" else "Borrow More", color = ForestGreen)
                        }

                        IconButton(
                            onClick = { viewModel.navigateTo(Screen.AddReminder(debtId)) },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                        ) {
                            Icon(Icons.Default.Alarm, contentDescription = "Add Reminder", tint = ForestGreen)
                        }
                    }
                }

                // Transaction Ledger Title
                item {
                    Text(
                        text = "Transaction History (${transactions.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
                    )
                }

                // Transaction Items
                items(transactions.reversed(), key = { it.id }) { tx ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { viewModel.navigateTo(Screen.TransactionDetail(tx.id)) }
                            .testTag("tx_item_${tx.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isRepayment = tx.type == TransactionType.REPAYMENT_MADE || tx.type == TransactionType.REPAYMENT_RECEIVED
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isRepayment) MintGreenLight else WarmAmberBadge),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isRepayment) Icons.Default.CheckCircle else Icons.Default.AddCircle,
                                    contentDescription = null,
                                    tint = if (isRepayment) MintGreenDark else WarmAmberIcon,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when (tx.type) {
                                        TransactionType.REPAYMENT_MADE -> "Repayment made"
                                        TransactionType.REPAYMENT_RECEIVED -> "Payment received"
                                        TransactionType.BORROWED -> "Borrowed"
                                        TransactionType.LENT -> "Lent"
                                        TransactionType.ADJUSTMENT -> "Adjustment"
                                        TransactionType.REVERSAL -> "Reversal"
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = viewModel.formatDate(tx.effectiveDate) + if (tx.note.isNotBlank()) " · ${tx.note}" else "",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }

                            Text(
                                text = (if (isRepayment) "- " else "+ ") + viewModel.formatCurrency(tx.amount),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRepayment) MintGreenDark else WarmAmberIcon
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddMoreDialog && debt != null) {
        var addAmount by remember { mutableStateOf("") }
        var addNote by remember { mutableStateOf("") }
        val isBorrowing = debt.direction == DebtDirection.BORROWED

        AlertDialog(
            onDismissRequest = { showAddMoreDialog = false },
            title = {
                Text(
                    text = if (isBorrowing) "Borrow Additional Amount" else "Lend Additional Amount",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "This will add a new transaction to this active debt record without altering the original principal history.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = addAmount,
                        onValueChange = { addAmount = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Amount (KSh) *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_more_amount_input")
                    )
                    OutlinedTextField(
                        value = addNote,
                        onValueChange = { addNote = it },
                        label = { Text("Reason / Note (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountVal = addAmount.toLongOrNull() ?: 0L
                        if (amountVal > 0) {
                            val txType = if (isBorrowing) TransactionType.BORROWED else TransactionType.LENT
                            viewModel.recordAdditionalBorrowingOrLending(debtId, txType, amountVal, addNote)
                            showAddMoreDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    modifier = Modifier.testTag("confirm_add_more_button")
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
