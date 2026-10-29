package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.TransactionType
import com.example.data.repository.SettleUpRepository
import com.example.ui.components.SimpleTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.SettleUpViewModel

@Composable
fun TransactionDetailScreen(
    transactionId: String,
    viewModel: SettleUpViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val context = LocalContext.current
    val repository = remember { SettleUpRepository.getInstance(context) }
    var transaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var personName by remember { mutableStateOf("Unknown") }
    var showReversalDialog by remember { mutableStateOf(false) }

    LaunchedEffect(transactionId) {
        val allTxs = repository.observeRecentTransactions(100)
        allTxs.collect { list ->
            val match = list.find { it.first.id == transactionId }
            if (match != null) {
                transaction = match.first
                personName = match.second
            }
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhiteBg),
        topBar = {
            SimpleTopBar(
                title = "Transaction Receipt",
                onBack = { viewModel.navigateBack() }
            )
        }
    ) { innerPadding ->
        if (transaction == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ForestGreen)
            }
        } else {
            val tx = transaction!!
            val isRepayment = tx.type == TransactionType.REPAYMENT_MADE || tx.type == TransactionType.REPAYMENT_RECEIVED

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(OffWhiteBg)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Receipt Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isRepayment) MintGreenLight else WarmAmberBadge),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isRepayment) Icons.Default.CheckCircle else Icons.Default.ArrowOutward,
                                contentDescription = null,
                                tint = if (isRepayment) MintGreenDark else WarmAmberIcon,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = when (tx.type) {
                                TransactionType.REPAYMENT_RECEIVED -> "Repayment Received"
                                TransactionType.REPAYMENT_MADE -> "Repayment Made"
                                TransactionType.BORROWED -> "Money Borrowed"
                                TransactionType.LENT -> "Money Lent"
                                TransactionType.ADJUSTMENT -> "Manual Adjustment"
                                TransactionType.REVERSAL -> "Reversal Entry"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = viewModel.formatCurrency(tx.amount),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = DividerColor)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Details grid
                        ReceiptDetailRow(label = "Person", value = personName)
                        ReceiptDetailRow(label = "Date & Time", value = viewModel.formatDate(tx.effectiveDate))
                        ReceiptDetailRow(label = "Transaction Ref", value = tx.id.take(12).uppercase())
                        ReceiptDetailRow(label = "Verification", value = tx.verificationStatus)

                        if (tx.note.isNotBlank()) {
                            ReceiptDetailRow(label = "Message / Note", value = tx.note)
                        }
                    }
                }

                // Proof / Evidence Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Transaction Proof & Message", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF9FAFB),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (tx.note.isNotBlank()) "\"${tx.note}\"" else "No note attached to this entry.",
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MintGreenDark, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Logged by record owner", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }

                // Reversal Action (if not already a reversal)
                if (tx.type != TransactionType.REVERSAL) {
                    OutlinedButton(
                        onClick = { showReversalDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reverse_transaction_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                        border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AlertRed))
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reverse This Transaction", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showReversalDialog && transaction != null) {
        var reason by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showReversalDialog = false },
            title = { Text("Confirm Reversal", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Reversals create an immutable compensating transaction in the ledger rather than deleting history. A reason is required.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it; error = null },
                        label = { Text("Reason for Reversal *") },
                        placeholder = { Text("e.g. Sent wrong amount, duplicate entry") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reversal_reason_input"),
                        isError = error != null
                    )
                    if (error != null) {
                        Text(text = error!!, color = AlertRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reason.isBlank()) {
                            error = "Reason cannot be empty"
                        } else {
                            viewModel.recordReversal(transaction!!.id, reason)
                            showReversalDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                    modifier = Modifier.testTag("confirm_reversal_button")
                ) {
                    Text("Confirm Reversal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReversalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ReceiptDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
