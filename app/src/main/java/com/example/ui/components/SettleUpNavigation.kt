package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.Screen

@Composable
fun SettleUpBottomNavigation(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        containerColor = Color.White,
        tonalElevation = 6.dp
    ) {
        // Home Tab
        val isHome = currentScreen is Screen.Home
        NavigationBarItem(
            selected = isHome,
            onClick = { onNavigate(Screen.Home) },
            icon = {
                Icon(
                    imageVector = if (isHome) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = {
                Text(
                    text = "Home",
                    fontSize = 11.sp,
                    fontWeight = if (isHome) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ForestGreen,
                selectedTextColor = ForestGreen,
                indicatorColor = ForestGreen.copy(alpha = 0.12f),
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_item_home")
        )

        // People Tab
        val isPeople = currentScreen is Screen.People
        NavigationBarItem(
            selected = isPeople,
            onClick = { onNavigate(Screen.People) },
            icon = {
                Icon(
                    imageVector = if (isPeople) Icons.Filled.People else Icons.Outlined.People,
                    contentDescription = "People"
                )
            },
            label = {
                Text(
                    text = "People",
                    fontSize = 11.sp,
                    fontWeight = if (isPeople) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ForestGreen,
                selectedTextColor = ForestGreen,
                indicatorColor = ForestGreen.copy(alpha = 0.12f),
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_item_people")
        )

        // Summary Tab
        val isSummary = currentScreen is Screen.Summary
        NavigationBarItem(
            selected = isSummary,
            onClick = { onNavigate(Screen.Summary) },
            icon = {
                Icon(
                    imageVector = if (isSummary) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                    contentDescription = "Summary"
                )
            },
            label = {
                Text(
                    text = "Summary",
                    fontSize = 11.sp,
                    fontWeight = if (isSummary) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ForestGreen,
                selectedTextColor = ForestGreen,
                indicatorColor = ForestGreen.copy(alpha = 0.12f),
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_item_summary")
        )

        // Activity Tab
        val isActivity = currentScreen is Screen.Activity
        NavigationBarItem(
            selected = isActivity,
            onClick = { onNavigate(Screen.Activity) },
            icon = {
                Icon(
                    imageVector = if (isActivity) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                    contentDescription = "Activity"
                )
            },
            label = {
                Text(
                    text = "Activity",
                    fontSize = 11.sp,
                    fontWeight = if (isActivity) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ForestGreen,
                selectedTextColor = ForestGreen,
                indicatorColor = ForestGreen.copy(alpha = 0.12f),
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_item_activity")
        )
    }
}
