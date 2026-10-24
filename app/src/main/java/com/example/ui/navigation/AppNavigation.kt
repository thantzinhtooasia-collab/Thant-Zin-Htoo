package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.repository.MyanmarConnectRepository
import com.example.ui.components.AppTab
import com.example.ui.components.MyanmarConnectBottomBar
import com.example.ui.components.MyanmarConnectScaffold
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.GroupsScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileScreen

/**
 * Top-level route constants for Jetpack Navigation Compose
 */
object AppDestinations {
    const val AUTH_ROUTE = "auth_screen"
    const val MAIN_SHELL_ROUTE = "main_app_shell"
}

/**
 * Root Jetpack Navigation Host handling switching between Authentication
 * and the Main Application Shell with clean backstack management.
 */
@Composable
fun AppNavHost(
    repository: MyanmarConnectRepository,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val currentUser by repository.currentUser.collectAsState()
    val startRoute = if (currentUser == null) AppDestinations.AUTH_ROUTE else AppDestinations.MAIN_SHELL_ROUTE

    NavHost(
        navController = navController,
        startDestination = startRoute,
        modifier = modifier
    ) {
        // 1. Authentication Destination
        composable(AppDestinations.AUTH_ROUTE) {
            AuthScreen(
                repository = repository,
                onAuthSuccess = {
                    navController.navigate(AppDestinations.MAIN_SHELL_ROUTE) {
                        // Pop authentication screen from backstack so Back button does not return to login
                        popUpTo(AppDestinations.AUTH_ROUTE) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // 2. Main Application Shell Destination
        composable(AppDestinations.MAIN_SHELL_ROUTE) {
            MainAppShell(
                repository = repository,
                onLogoutOrDeleted = {
                    navController.navigate(AppDestinations.AUTH_ROUTE) {
                        // Clear main app shell from backstack on logout/account deletion
                        popUpTo(AppDestinations.MAIN_SHELL_ROUTE) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

/**
 * Main application container containing the Burmese Bottom Navigation Bar
 * and the 5 core tabs: Home, Groups, Chat, Notifications, and Profile.
 */
@Composable
fun MainAppShell(
    repository: MyanmarConnectRepository,
    onLogoutOrDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notifications by repository.notifications.collectAsState()
    var currentTab by remember { mutableStateOf(AppTab.HOME) }

    // Inter-tab deep navigation state
    var viewingTargetUserId by remember { mutableStateOf<String?>(null) }
    var chatTargetUserId by remember { mutableStateOf<String?>(null) }
    var chatTargetUserName by remember { mutableStateOf<String?>(null) }

    val unreadNotifications = notifications.count { !it.isRead }
    val unreadMessages = 0

    // Return to Home feed if Back is pressed on other tabs when no sub-view is active
    BackHandler(enabled = currentTab != AppTab.HOME && viewingTargetUserId == null && chatTargetUserId == null) {
        currentTab = AppTab.HOME
    }

    MyanmarConnectScaffold(
        currentTab = currentTab,
        onTabSelected = { tab ->
            if (tab != AppTab.PROFILE) viewingTargetUserId = null
            if (tab != AppTab.CHAT) {
                chatTargetUserId = null
                chatTargetUserName = null
            }
            currentTab = tab
        },
        unreadNotificationsCount = unreadNotifications,
        unreadMessagesCount = unreadMessages,
        modifier = modifier
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)

        when (currentTab) {
            AppTab.HOME -> {
                FeedScreen(
                    repository = repository,
                    onNavigateToUserProfile = { targetUid ->
                        viewingTargetUserId = targetUid
                        currentTab = AppTab.PROFILE
                    },
                    modifier = screenModifier
                )
            }

            AppTab.GROUPS -> {
                GroupsScreen(
                    repository = repository,
                    onNavigateToUserProfile = { targetUid ->
                        viewingTargetUserId = targetUid
                        currentTab = AppTab.PROFILE
                    },
                    modifier = screenModifier
                )
            }

            AppTab.CHAT -> {
                ChatScreen(
                    repository = repository,
                    initialTargetUserId = chatTargetUserId,
                    initialTargetUserName = chatTargetUserName,
                    modifier = screenModifier
                )
            }

            AppTab.NOTIFICATIONS -> {
                NotificationsScreen(
                    repository = repository,
                    onNavigateToChat = { targetUid, targetName ->
                        chatTargetUserId = targetUid
                        chatTargetUserName = targetName
                        currentTab = AppTab.CHAT
                    },
                    onNavigateToPost = { _ ->
                        currentTab = AppTab.HOME
                    },
                    modifier = screenModifier
                )
            }

            AppTab.PROFILE -> {
                ProfileScreen(
                    repository = repository,
                    viewingTargetUserId = viewingTargetUserId,
                    onNavigateBackFromOtherProfile = {
                        viewingTargetUserId = null
                    },
                    onStartChatWithUser = { targetUid, targetName ->
                        viewingTargetUserId = null
                        chatTargetUserId = targetUid
                        chatTargetUserName = targetName
                        currentTab = AppTab.CHAT
                    },
                    onLogoutOrDeleted = onLogoutOrDeleted,
                    modifier = screenModifier
                )
            }
        }
    }
}
