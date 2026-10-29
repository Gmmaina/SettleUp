package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DebtDirection
import com.example.ui.components.SimpleTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.SettleUpViewModel

@Composable
fun AddDebtScreen(
    initialDirection: DebtDirection,
    targetPersonId: String?,
    viewModel: SettleUpViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val allPeople by viewModel.allPeople.collectAsState()

    var direction by remember { mutableStateOf(initialDirection) }
    var selectedPersonId by remember { mutableStateOf(targetPersonId) }
    var newPersonName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var dueDateDays by remember { mutableIntStateOf(14) } // 7, 14, 30 days or none
    var hasDueDate by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhiteBg),
        topBar = {
            SimpleTopBar(
                title = if (direction == DebtDirection.BORROWED) "Record Borrowing" else "Record Lending",
                onBack = { viewModel.navigateBack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(OffWhiteBg)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Direction Toggle Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isBorrowed = direction == DebtDirection.BORROWED
                    Button(
                        onClick = { direction = DebtDirection.BORROWED },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("direction_borrowed_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBorrowed) WarmAmberIcon else Color.Transparent,
                            contentColor = if (isBorrowed) Color.White else TextSecondary
                        ),
                        elevation = if (isBorrowed) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("I Borrowed", fontWeight = FontWeight.Bold)
                    }

                    val isLent = direction == DebtDirection.LENT
                    Button(
                        onClick = { direction = DebtDirection.LENT },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("direction_lent_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLent) ForestGreen else Color.Transparent,
                            contentColor = if (isLent) Color.White else TextSecondary
                        ),
                        elevation = if (isLent) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("I Lent", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Person Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (direction == DebtDirection.BORROWED) "Who did you borrow from?" else "Who did you lend to?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (allPeople.isNotEmpty()) {
                        Text("Select existing contact:", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(allPeople) { person ->
                                val isSelected = selectedPersonId == person.id
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            selectedPersonId = if (isSelected) null else person.id
                                            if (!isSelected) newPersonName = ""
                                        }
                                        .testTag("select_person_${person.id}"),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) ForestGreen else Color(0xFFF1F3F5),
                                    border = if (isSelected) null else CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
                                ) {
                                    Text(
                                        text = person.name,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else TextPrimary,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = DividerColor)
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Text("Or enter new person's name:", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newPersonName,
                        onValueChange = {
                            newPersonName = it
                            if (it.isNotBlank()) selectedPersonId = null
                        },
                        placeholder = { Text("e.g. Mary Wanjiku") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_person_name"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // Amount Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Amount (KSh) *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                        placeholder = { Text("0") },
                        prefix = { Text("KSh ", fontWeight = FontWeight.Bold, color = ForestGreen) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_debt_amount"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Quick amount chips
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("500", "1000", "2000", "5000").forEach { quickAmount ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { amountText = quickAmount },
                                color = Color(0xFFF1F3F5),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "+$quickAmount",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Description & Notes Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Details & Notes",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    OutlinedTextField(
                        value = descriptionText,
                        onValueChange = { descriptionText = it },
                        label = { Text("Purpose / Description") },
                        placeholder = { Text("e.g. Car maintenance, Emergency, Rent share") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_debt_description"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Transaction message (optional)") },
                        placeholder = { Text("e.g. Sent via M-Pesa to phone number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Due Date Card
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
                            text = "Set Due Date & Reminder",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Switch(
                            checked = hasDueDate,
                            onCheckedChange = { hasDueDate = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = ForestGreen, checkedTrackColor = ForestGreenContainer)
                        )
                    }

                    if (hasDueDate) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(7 to "In 1 week", 14 to "In 2 weeks", 30 to "In 1 month").forEach { (days, label) ->
                                val isSelected = dueDateDays == days
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { dueDateDays = days },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) ForestGreenContainer else Color(0xFFF1F3F5),
                                    border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ForestGreen)) else null
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) ForestGreen else TextSecondary,
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Record Button
            Button(
                onClick = {
                    val amountVal = amountText.toLongOrNull() ?: 0L
                    val dueTimestamp = if (hasDueDate) System.currentTimeMillis() + (dueDateDays * 24 * 60 * 60 * 1000L) else null
                    viewModel.createDebt(
                        personId = selectedPersonId,
                        newPersonName = newPersonName,
                        direction = direction,
                        amount = amountVal,
                        description = descriptionText.ifBlank { "Personal Loan" },
                        dueDate = dueTimestamp,
                        initialNote = noteText
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_debt_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Debt Record", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
