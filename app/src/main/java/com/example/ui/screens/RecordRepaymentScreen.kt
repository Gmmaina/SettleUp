package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.SettleUpViewModel

@Composable
fun RecordRepaymentScreen(
    targetDebtId: String?,
    viewModel: SettleUpViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val allDebts by viewModel.allDebts.collectAsState()
    val activeDebts = remember(allDebts) {
        allDebts.filter { it.status == DebtStatus.ACTIVE }
    }

    var selectedDebtId by remember { mutableStateOf(targetDebtId ?: activeDebts.firstOrNull()?.debtId) }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var allowOverpayment by remember { mutableStateOf(false) }

    val selectedDebt = remember(allDebts, selectedDebtId) {
        allDebts.find { it.debtId == selectedDebtId }
    }

    val currentBalance = selectedDebt?.currentBalance ?: 0L
    val enteredAmount = amountText.toLongOrNull() ?: 0L
    val isOverpayment = enteredAmount > currentBalance && currentBalance > 0
    val newBalance = (currentBalance - enteredAmount).coerceAtLeast(0L)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhiteBg),
        topBar = {
            SimpleTopBar(
                title = if (selectedDebt?.direction == DebtDirection.LENT) "Record Received Payment" else "Record Repayment",
                onBack = { viewModel.navigateBack() }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(OffWhiteBg),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Select Debt Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Select active debt to settle:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (activeDebts.isEmpty()) {
                            Text(
                                text = "No active debts to repay.",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        } else {
                            activeDebts.forEach { debt ->
                                val isSelected = debt.debtId == selectedDebtId
                                val isOwedToMe = debt.direction == DebtDirection.LENT

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) ForestGreenContainer else Color(0xFFF9FAFB))
                                        .clickable { selectedDebtId = debt.debtId }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedDebtId = debt.debtId },
                                        colors = RadioButtonDefaults.colors(selectedColor = ForestGreen)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${debt.personName} · ${debt.description.ifBlank { "Loan" }}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = if (isOwedToMe) "Owed to you" else "You owe them",
                                            fontSize = 12.sp,
                                            color = if (isOwedToMe) MintGreenDark else WarmAmberIcon
                                        )
                                    }
                                    Text(
                                        text = viewModel.formatCurrency(debt.currentBalance),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Amount Input
            if (selectedDebt != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                Text(
                                    text = "Repayment Amount *",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                TextButton(onClick = { amountText = currentBalance.toString() }) {
                                    Text("Pay Full Balance", color = ForestGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                                placeholder = { Text("0") },
                                prefix = { Text("KSh ", fontWeight = FontWeight.Bold, color = ForestGreen) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_repayment_amount"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            // Real-time Balance Preview
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Remaining balance after:", fontSize = 13.sp, color = TextSecondary)
                                    Text(
                                        text = viewModel.formatCurrency(newBalance),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (newBalance == 0L) MintGreenDark else TextPrimary
                                    )
                                }
                            }

                            // Overpayment Warning
                            if (isOverpayment) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Warning: Entered amount (${viewModel.formatCurrency(enteredAmount)}) exceeds current outstanding balance (${viewModel.formatCurrency(currentBalance)}).",
                                            color = AlertRed,
                                            fontSize = 12.sp
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Checkbox(
                                                checked = allowOverpayment,
                                                onCheckedChange = { allowOverpayment = it },
                                                colors = CheckboxDefaults.colors(checkedColor = AlertRed)
                                            )
                                            Text(
                                                text = "Allow overpayment record",
                                                fontSize = 12.sp,
                                                color = AlertRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Optional Note / Reference
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Transaction Note / Receipt Ref",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                placeholder = { Text("e.g. M-Pesa ref: QK82HD73KS or cash") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_repayment_note"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // Confirm Button
                item {
                    val canSubmit = enteredAmount > 0 && (!isOverpayment || allowOverpayment)
                    Button(
                        onClick = {
                            if (canSubmit && selectedDebtId != null) {
                                viewModel.recordRepayment(selectedDebtId!!, enteredAmount, noteText)
                            }
                        },
                        enabled = canSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_repayment_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Record Repayment", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
