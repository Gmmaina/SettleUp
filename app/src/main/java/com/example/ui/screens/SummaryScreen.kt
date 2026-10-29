package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DebtDirection
import com.example.data.model.DebtStatus
import com.example.data.model.TimePeriodFilter
import com.example.data.model.TransactionType
import com.example.ui.theme.*
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.SettleUpViewModel

@Composable
fun SummaryScreen(
    viewModel: SettleUpViewModel,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.dashboardSummary.collectAsState()
    val allDebts by viewModel.allDebts.collectAsState()
    val people by viewModel.peopleWithBalances.collectAsState()
    var selectedPeriod by remember { mutableStateOf(TimePeriodFilter.THIS_MONTH) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhiteBg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Financial Summary",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Ledger balances & cash flows",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = { viewModel.navigateTo(Screen.ExportReport) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    modifier = Modifier.testTag("summary_export_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export", fontSize = 13.sp)
                }
            }
        }

        // Period Filters
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    TimePeriodFilter.THIS_WEEK to "This Week",
                    TimePeriodFilter.THIS_MONTH to "This Month",
                    TimePeriodFilter.THIS_YEAR to "This Year",
                    TimePeriodFilter.ALL to "All Time"
                ).forEach { (period, label) ->
                    val isSelected = selectedPeriod == period
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPeriod = period },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreen,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) ForestGreen else CardBorder
                        )
                    )
                }
            }
        }

        // Outstanding Balances Breakdown Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Current Balances",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total You Owe", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = viewModel.formatCurrency(summary.totalIOwe),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarmAmberIcon
                            )
                            Text("${summary.peopleIOweCount} people", fontSize = 11.sp, color = TextMuted)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Owed to You", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = viewModel.formatCurrency(summary.totalOwedToMe),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MintGreenDark
                            )
                            Text("${summary.peopleOwedToMeCount} people", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ratio Bar
                    val total = (summary.totalIOwe + summary.totalOwedToMe).coerceAtLeast(1L)
                    val oweRatio = summary.totalIOwe.toFloat() / total
                    val owedRatio = summary.totalOwedToMe.toFloat() / total

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        val width = size.width
                        val oweWidth = width * oweRatio
                        drawRoundRect(
                            color = WarmAmberIcon,
                            topLeft = Offset(0f, 0f),
                            size = Size(oweWidth, size.height),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = MintGreenDark,
                            topLeft = Offset(oweWidth, 0f),
                            size = Size(width - oweWidth, size.height),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(WarmAmberIcon))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("You Owe (${(oweRatio * 100).toInt()}%)", fontSize = 11.sp, color = TextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(MintGreenDark))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Owed to You (${(owedRatio * 100).toInt()}%)", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }

        // Debt Status Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Debt Records Overview",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${summary.activeDebtsCount}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                            Text("Active Debts", fontSize = 12.sp, color = TextSecondary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${summary.clearedDebtsCount}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MintGreenDark
                            )
                            Text("Cleared Debts", fontSize = 12.sp, color = TextSecondary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${allDebts.size}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text("Total Records", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }

        // Top Balances by Person
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Top Balances by Contact",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (people.isEmpty()) {
                        Text("No contacts found", fontSize = 13.sp, color = TextSecondary)
                    } else {
                        people.take(5).forEach { person ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = person.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )

                                Column(horizontalAlignment = Alignment.End) {
                                    if (person.amountOwedToMe > 0) {
                                        Text(
                                            text = "+ " + viewModel.formatCurrency(person.amountOwedToMe),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MintGreenDark
                                        )
                                    }
                                    if (person.amountIOwe > 0) {
                                        Text(
                                            text = "- " + viewModel.formatCurrency(person.amountIOwe),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WarmAmberIcon
                                        )
                                    }
                                    if (person.amountIOwe == 0L && person.amountOwedToMe == 0L) {
                                        Text("Settled", fontSize = 12.sp, color = TextMuted)
                                    }
                                }
                            }
                            HorizontalDivider(color = DividerColor)
                        }
                    }
                }
            }
        }
    }
}
