package com.example.data.model

import java.util.UUID

/**
 * Worker profile representation
 */
data class UserProfile(
    val uid: String = UUID.randomUUID().toString(),
    val displayName: String = "",
    val phoneOrEmail: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val locationThai: String = "ဘန်ကောက် (Bangkok)",
    val occupation: String = "အထည်ချုပ်စက်ရုံ (Garment Factory)",
    val joinedAt: Long = System.currentTimeMillis()
)

/**
 * Social media feed post
 */
data class Post(
    val id: String = UUID.randomUUID().toString(),
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatar: String = "",
    val authorLocation: String = "",
    val content: String = "",
    val imageUri: String? = null,
    val groupId: String? = null,
    val groupName: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val likedByUids: Set<String> = emptySet(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Post comment
 */
data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatar: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Community group for Myanmar workers
 */
data class CommunityGroup(
    val id: String = "",
    val nameBurmese: String = "",
    val nameEnglish: String = "",
    val descriptionBurmese: String = "",
    val category: String = "",
    val memberCount: Int = 0,
    val memberUids: Set<String> = emptySet(),
    val iconKey: String = "work",
    val isVerifiedBadge: Boolean = true
)

/**
 * Direct messaging models
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val recipientId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatConversation(
    val id: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val otherUserAvatar: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0
)

/**
 * In-app notification
 */
enum class NotificationType {
    LIKE, COMMENT, MESSAGE, GROUP_ACTIVITY, SYSTEM
}

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val recipientUid: String = "",
    val senderName: String = "",
    val senderAvatar: String = "",
    val type: NotificationType = NotificationType.SYSTEM,
    val messageBurmese: String = "",
    val targetPostId: String? = null,
    val targetChatUserId: String? = null,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Content moderation report
 */
data class PostReport(
    val id: String = UUID.randomUUID().toString(),
    val postId: String = "",
    val reporterUid: String = "",
    val reasonBurmese: String = "",
    val reportedAt: Long = System.currentTimeMillis()
)
