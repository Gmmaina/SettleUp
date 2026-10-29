package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SettleUpRepository
import com.example.ui.components.SimpleTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.SettleUpViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun ExportScreen(
    viewModel: SettleUpViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val repository = remember { SettleUpRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    var exportType by remember { mutableIntStateOf(0) } // 0: CSV Ledger, 1: Text Summary Report
    var generatedContent by remember { mutableStateOf("Generating export...") }
    var copiedToClipboard by remember { mutableStateOf(false) }

    val debts by viewModel.allDebts.collectAsState()
    val summary by viewModel.dashboardSummary.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    LaunchedEffect(exportType, debts, summary) {
        if (exportType == 0) {
            // CSV Ledger
            val txsWithPeople = repository.observeRecentTransactions(500).first()
            val sb = StringBuilder()
            sb.append("Transaction ID,Contact Name,Type,Amount (KSh),Date,Notes,Status\n")
            txsWithPeople.forEach { (tx, personName) ->
                val safeNote = tx.note.replace(",", " ").replace("\n", " ")
                sb.append("${tx.id.take(8)},${personName},${tx.type},${tx.amount},${viewModel.formatDate(tx.effectiveDate)},${safeNote},${tx.verificationStatus}\n")
            }
            generatedContent = sb.toString()
        } else {
            // Text Summary Report
            val sb = StringBuilder()
            sb.append("=========================================\n")
            sb.append("SETTLEUP FINANCIAL SUMMARY REPORT\n")
            sb.append("Generated for: ${profile.displayName} (${profile.email})\n")
            sb.append("Date: ${viewModel.formatDate(System.currentTimeMillis())}\n")
            sb.append("Currency: ${profile.preferredCurrency}\n")
            sb.append("=========================================\n\n")
            sb.append("1. OUTSTANDING BALANCES:\n")
            sb.append("   - Money I Owe: ${viewModel.formatCurrency(summary.totalIOwe)} (${summary.peopleIOweCount} people)\n")
            sb.append("   - Owed to Me:  ${viewModel.formatCurrency(summary.totalOwedToMe)} (${summary.peopleOwedToMeCount} people)\n")
            sb.append("   - Total Outstanding: ${viewModel.formatCurrency(summary.totalOutstanding)}\n\n")
            sb.append("2. DEBT RECORDS:\n")
            sb.append("   - Active Debts: ${summary.activeDebtsCount}\n")
            sb.append("   - Cleared Debts: ${summary.clearedDebtsCount}\n\n")
            sb.append("3. DETAILED DEBTS BREAKDOWN:\n")
            debts.forEach { debt ->
                sb.append("   * ${debt.personName} [${debt.direction}]: ${viewModel.formatCurrency(debt.currentBalance)} (Original: ${viewModel.formatCurrency(debt.originalPrincipal)}, Status: ${debt.status})\n")
            }
            sb.append("\nEvery debt. Every payment. Clear records.\n")
            generatedContent = sb.toString()
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhiteBg),
        topBar = {
            SimpleTopBar(
                title = "Export & Reports",
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Format Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Export Format", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = exportType == 0,
                            onClick = { exportType = 0; copiedToClipboard = false },
                            label = { Text("CSV Ledger (.csv)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = exportType == 1,
                            onClick = { exportType = 1; copiedToClipboard = false },
                            label = { Text("Summary Report (.txt)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Preview Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (exportType == 0) "CSV Preview" else "Report Preview",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        TextButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(generatedContent))
                                copiedToClipboard = true
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MintGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (copiedToClipboard) "Copied!" else "Copy", color = MintGreen, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = generatedContent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 16.sp,
                        maxLines = 15
                    )
                }
            }

            // Export / Share Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, generatedContent)
                            putExtra(Intent.EXTRA_SUBJECT, "SettleUp Report - ${profile.displayName}")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share SettleUp Report")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("share_export_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share / Save Report", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
