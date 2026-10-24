package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.PeacockPrimary
import com.example.ui.theme.PeacockPrimaryContainer

enum class AppTab(
    val labelBurmese: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTagId: String
) {
    HOME(
        labelBurmese = "ပင်မစာမျက်နှာ",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTagId = "tab_home"
    ),
    GROUPS(
        labelBurmese = "အုပ်စုများ",
        selectedIcon = Icons.Filled.Groups,
        unselectedIcon = Icons.Outlined.Groups,
        testTagId = "tab_groups"
    ),
    CHAT(
        labelBurmese = "Chat",
        selectedIcon = Icons.Filled.Chat,
        unselectedIcon = Icons.Outlined.Chat,
        testTagId = "tab_chat"
    ),
    NOTIFICATIONS(
        labelBurmese = "အသိပေးချက်",
        selectedIcon = Icons.Filled.Notifications,
        unselectedIcon = Icons.Outlined.Notifications,
        testTagId = "tab_notifications"
    ),
    PROFILE(
        labelBurmese = "ကိုယ်ရေးအချက်အလက်",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        testTagId = "tab_profile"
    )
}

/**
 * Modern Material 3 Bottom Navigation Bar with Peacock Teal & Golden Amber styling,
 * Burmese labels, and safe area navigation bar insets.
 */
@Composable
fun MyanmarConnectBottomBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    unreadNotificationsCount: Int = 0,
    unreadMessagesCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .shadow(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            AppTab.values().forEach { tab ->
                val isSelected = currentTab == tab

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) PeacockPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = spring(),
                    label = "iconColor"
                )

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        val iconVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon
                        val badgeCount = when (tab) {
                            AppTab.NOTIFICATIONS -> unreadNotificationsCount
                            AppTab.CHAT -> unreadMessagesCount
                            else -> 0
                        }

                        BadgedBox(
                            badge = {
                                if (badgeCount > 0) {
                                    Badge(
                                        containerColor = AmberSecondary, // Warm Golden Amber badge
                                        contentColor = Color.White,
                                        modifier = Modifier.offset(x = 2.dp, y = (-2).dp)
                                    ) {
                                        Text(
                                            text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = tab.labelBurmese,
                                tint = iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = tab.labelBurmese,
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            lineHeight = 14.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PeacockPrimary,
                        selectedTextColor = PeacockPrimary,
                        indicatorColor = PeacockPrimaryContainer.copy(alpha = 0.65f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag(tab.testTagId)
                )
            }
        }
    }
}
