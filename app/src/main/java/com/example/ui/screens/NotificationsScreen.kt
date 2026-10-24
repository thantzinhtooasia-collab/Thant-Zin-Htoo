package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNotification
import com.example.data.model.NotificationType
import com.example.data.repository.MyanmarConnectRepository
import com.example.ui.components.formatTimeAgo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    repository: MyanmarConnectRepository,
    onNavigateToChat: (targetUserId: String, targetUserName: String) -> Unit,
    onNavigateToPost: (postId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val notifications by repository.notifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "အသိပေးချက် (Notifications)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (unreadCount > 0) {
                            Text(
                                text = "မဖတ်ရသေးသော အသိပေးချက် ${unreadCount} ခု",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            )
                        }
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(
                            onClick = { repository.markAllNotificationsAsRead() },
                            modifier = Modifier.testTag("mark_all_read_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "အားလုံးဖတ်ပြီး",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (notifications.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "အသိပေးချက်များ မရှိသေးပါ",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "သင်၏ ပို့စ်များတွင် သဘောကျခြင်း၊ မှတ်ချက်ရေးခြင်း သို့မဟုတ် မက်ဆေ့ခ်ျ ရောက်ရှိလာပါက ဤနေရာတွင် ပြသပါမည်။",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("notifications_list")
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(notifications, key = { it.id }) { notif ->
                        val isUnread = !notif.isRead

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 5.dp)
                                .clickable {
                                    repository.markNotificationAsRead(notif.id)
                                    if (notif.targetChatUserId != null) {
                                        onNavigateToChat(notif.targetChatUserId, notif.senderName)
                                    } else if (notif.targetPostId != null) {
                                        onNavigateToPost(notif.targetPostId)
                                    }
                                }
                                .testTag("notification_item_${notif.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUnread) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isUnread) 2.dp else 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Type Icon
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(
                                            color = getNotificationBgColor(notif.type),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getNotificationIcon(notif.type),
                                        contentDescription = null,
                                        tint = getNotificationTint(notif.type),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = notif.messageBurmese,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                                            lineHeight = 20.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = formatTimeAgo(notif.timestamp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                if (isUnread) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

fun getNotificationIcon(type: NotificationType): ImageVector {
    return when (type) {
        NotificationType.LIKE -> Icons.Default.Favorite
        NotificationType.COMMENT -> Icons.Default.ChatBubble
        NotificationType.MESSAGE -> Icons.Default.ChatBubble
        NotificationType.GROUP_ACTIVITY -> Icons.Default.Groups
        NotificationType.SYSTEM -> Icons.Default.Info
    }
}

@Composable
fun getNotificationTint(type: NotificationType): androidx.compose.ui.graphics.Color {
    return when (type) {
        NotificationType.LIKE -> MaterialTheme.colorScheme.error
        NotificationType.COMMENT -> MaterialTheme.colorScheme.primary
        NotificationType.MESSAGE -> MaterialTheme.colorScheme.secondary
        NotificationType.GROUP_ACTIVITY -> MaterialTheme.colorScheme.primary
        NotificationType.SYSTEM -> MaterialTheme.colorScheme.tertiary
    }
}

@Composable
fun getNotificationBgColor(type: NotificationType): androidx.compose.ui.graphics.Color {
    return when (type) {
        NotificationType.LIKE -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
        NotificationType.COMMENT -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        NotificationType.MESSAGE -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        NotificationType.GROUP_ACTIVITY -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        NotificationType.SYSTEM -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
    }
}
