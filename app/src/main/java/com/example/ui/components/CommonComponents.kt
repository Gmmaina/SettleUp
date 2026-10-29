package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.TransactionEntity
import com.example.data.model.DebtDirection
import com.example.data.model.TransactionType
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SettleUpHeader(
    userName: String,
    unreadNotificationCount: Int,
    onAvatarClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateString = SimpleDateFormat("EEEE, d MMMM", Locale.US).format(Date()).uppercase()
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dateString,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$greeting, $userName",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Here's where your money stands.",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Notification bell
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("notification_button")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadNotificationCount > 0) {
                            Badge(containerColor = AlertRed) {
                                Text("$unreadNotificationCount", color = Color.White)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // User profile avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE4EDE7))
                    .clickable { onAvatarClick() }
                    .testTag("profile_avatar_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = ForestGreen,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
fun HeroFinancialCard(
    totalOutstandingFormatted: String,
    totalIOweFormatted: String,
    totalOwedToMeFormatted: String,
    onIOweClick: () -> Unit,
    onOwedToMeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_financial_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ForestGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Total outstanding",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = totalOutstandingFormatted,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Divider line matching mockup
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.15f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onIOweClick() }
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "I owe",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = totalIOweFormatted,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOwedToMeClick() }
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Owed to me",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = totalOwedToMeFormatted,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun DirectionSummaryCard(
    title: String,
    amountFormatted: String,
    peopleCount: Int,
    isOwedToMe: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(if (isOwedToMe) "card_owed_to_me" else "card_money_i_owe"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Arrow Badge (Down-Left for I Owe, Up-Right for Owed to Me)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isOwedToMe) MintGreenLight else WarmAmberBadge),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isOwedToMe) Icons.Default.NorthEast else Icons.Default.SouthWest,
                    contentDescription = null,
                    tint = if (isOwedToMe) MintGreenDark else WarmAmberIcon,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = amountFormatted,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$peopleCount ${if (peopleCount == 1) "person" else "people"}",
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun TransactionRowItem(
    transaction: TransactionEntity,
    personName: String,
    relativeDate: String,
    amountFormatted: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncoming = transaction.type == TransactionType.REPAYMENT_RECEIVED ||
            transaction.type == TransactionType.BORROWED
    val isRepayment = transaction.type == TransactionType.REPAYMENT_MADE ||
            transaction.type == TransactionType.REPAYMENT_RECEIVED

    val title = when (transaction.type) {
        TransactionType.REPAYMENT_RECEIVED -> "Payment received"
        TransactionType.REPAYMENT_MADE -> "Payment made"
        TransactionType.BORROWED -> "Borrowed money"
        TransactionType.LENT -> "Lent money"
        TransactionType.ADJUSTMENT -> "Balance adjustment"
        TransactionType.REVERSAL -> "Transaction reversal"
    }

    val icon = when (transaction.type) {
        TransactionType.REPAYMENT_RECEIVED -> Icons.Default.NorthEast
        TransactionType.LENT -> Icons.Default.NorthEast
        TransactionType.BORROWED -> Icons.Default.SouthWest
        TransactionType.REPAYMENT_MADE -> Icons.Default.SouthWest
        TransactionType.ADJUSTMENT -> Icons.Default.Tune
        TransactionType.REVERSAL -> Icons.Default.Undo
    }

    val badgeBg = if (transaction.type == TransactionType.REPAYMENT_RECEIVED) MintGreenLight else WarmAmberBadge
    val iconTint = if (transaction.type == TransactionType.REPAYMENT_RECEIVED) MintGreenDark else WarmAmberIcon

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(badgeBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$personName · $relativeDate",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }

        Text(
            text = "+ $amountFormatted",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (transaction.type == TransactionType.REPAYMENT_RECEIVED) MintGreenDark else Color(0xFF8D5B4C)
        )
    }
}

@Composable
fun SimpleTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("top_bar_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary
            )
        }

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )

        actions()
    }
}
