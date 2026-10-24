package com.example.data.repository

import com.example.data.model.AppNotification
import com.example.data.model.ChatMessage
import com.example.data.model.Comment
import com.example.data.model.CommunityGroup
import com.example.data.model.NotificationType
import com.example.data.model.Post
import com.example.data.model.PostReport
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.util.UUID

class MyanmarConnectRepository private constructor() {

    // Current authenticated user session
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    // Registered user accounts map (phoneOrEmail -> Pair(UserProfile, hashedPasswordHex))
    private val userAccounts = mutableMapOf<String, Pair<UserProfile, String>>()

    // Posts stream
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    // Comments map: postId -> List<Comment>
    private val _comments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val comments: StateFlow<Map<String, List<Comment>>> = _comments.asStateFlow()

    // Groups stream
    private val _groups = MutableStateFlow<List<CommunityGroup>>(emptyList())
    val groups: StateFlow<List<CommunityGroup>> = _groups.asStateFlow()

    // Direct Messages: conversationId -> List<ChatMessage>
    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    // Notifications stream
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // Blocked user IDs by current user
    private val _blockedUserIds = MutableStateFlow<Set<String>>(emptySet())
    val blockedUserIds: StateFlow<Set<String>> = _blockedUserIds.asStateFlow()

    // Reports submitted
    private val _reports = MutableStateFlow<List<PostReport>>(emptyList())
    val reports: StateFlow<List<PostReport>> = _reports.asStateFlow()

    init {
        seedInitialData()
    }

    // --- Authentication ---

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun register(
        displayName: String,
        phoneOrEmail: String,
        passwordPlain: String,
        locationThai: String,
        occupation: String
    ): Result<UserProfile> {
        val key = phoneOrEmail.trim().lowercase()
        if (key.isBlank() || passwordPlain.length < 6) {
            return Result.failure(IllegalArgumentException("အချက်အလက်များ မပြည့်စုံပါ သို့မဟုတ် စကားဝှက် အနည်းဆုံး ၆ လုံး ရှိရပါမည်။"))
        }
        if (userAccounts.containsKey(key)) {
            return Result.failure(IllegalStateException("ဤဖုန်း သို့မဟုတ် အီးမေးလ်ဖြင့် အကောင့်ဖွင့်ထားပြီးဖြစ်ပါသည်။"))
        }

        val newUser = UserProfile(
            uid = "user_${UUID.randomUUID().toString().take(8)}",
            displayName = displayName.ifBlank { "မြန်မာလုပ်သား" },
            phoneOrEmail = key,
            bio = "$locationThai တွင် အလုပ်လုပ်ကိုင်နေပါသည်။",
            avatarUrl = "",
            locationThai = locationThai,
            occupation = occupation,
            joinedAt = System.currentTimeMillis()
        )
        val hashed = hashPassword(passwordPlain)
        userAccounts[key] = Pair(newUser, hashed)
        _currentUser.value = newUser
        return Result.success(newUser)
    }

    fun login(phoneOrEmail: String, passwordPlain: String): Result<UserProfile> {
        val key = phoneOrEmail.trim().lowercase()
        val account = userAccounts[key]
            ?: return Result.failure(IllegalArgumentException("အကောင့်မရှိသေးပါ သို့မဟုတ် အချက်အလက်မှားယွင်းနေပါသည်။"))

        val hashed = hashPassword(passwordPlain)
        if (account.second != hashed) {
            return Result.failure(IllegalArgumentException("စကားဝှက် မှားယွင်းနေပါသည်။ ကျေးဇူးပြု၍ ပြန်လည်စစ်ဆေးပါ။"))
        }

        _currentUser.value = account.first
        return Result.success(account.first)
    }

    fun signInWithGoogleMock(email: String, name: String, avatar: String): UserProfile {
        val key = email.trim().lowercase()
        val existing = userAccounts[key]?.first
        if (existing != null) {
            _currentUser.value = existing
            return existing
        }
        val newUser = UserProfile(
            uid = "guser_${UUID.randomUUID().toString().take(8)}",
            displayName = name.ifBlank { "Google User" },
            phoneOrEmail = key,
            bio = "ထိုင်းနိုင်ငံရောက် မြန်မာလုပ်သား အသိုက်အဝန်းဝင်",
            avatarUrl = avatar,
            locationThai = "ဘန်ကောက် (Bangkok)",
            occupation = "အထွေထွေလုပ်သား"
        )
        userAccounts[key] = Pair(newUser, hashPassword("google_oauth_token_placeholder"))
        _currentUser.value = newUser
        return newUser
    }

    fun logout() {
        _currentUser.value = null
    }

    fun updateProfile(displayName: String, bio: String, locationThai: String, occupation: String, avatarUrl: String) {
        val current = _currentUser.value ?: return
        val updated = current.copy(
            displayName = displayName.trim(),
            bio = bio.trim(),
            locationThai = locationThai.trim(),
            occupation = occupation.trim(),
            avatarUrl = avatarUrl
        )
        _currentUser.value = updated
        val key = updated.phoneOrEmail.lowercase()
        if (userAccounts.containsKey(key)) {
            val passHash = userAccounts[key]!!.second
            userAccounts[key] = Pair(updated, passHash)
        }
    }

    fun deleteAccount() {
        val current = _currentUser.value ?: return
        userAccounts.remove(current.phoneOrEmail.lowercase())
        // Remove user's posts
        _posts.value = _posts.value.filter { it.authorId != current.uid }
        _currentUser.value = null
    }

    // --- Posts & Feed ---

    fun createPost(content: String, imageUri: String? = null, groupId: String? = null) {
        val user = _currentUser.value ?: return
        val group = _groups.value.find { it.id == groupId }
        val newPost = Post(
            id = "post_${UUID.randomUUID().toString().take(8)}",
            authorId = user.uid,
            authorName = user.displayName,
            authorAvatar = user.avatarUrl,
            authorLocation = user.locationThai,
            content = content.trim(),
            imageUri = imageUri,
            groupId = groupId,
            groupName = group?.nameBurmese,
            timestamp = System.currentTimeMillis()
        )
        _posts.value = listOf(newPost) + _posts.value

        // If posted in a group, notify group
        if (group != null) {
            addNotification(
                recipientUid = "system_group",
                senderName = user.displayName,
                type = NotificationType.GROUP_ACTIVITY,
                messageBurmese = "${group.nameBurmese} အုပ်စုတွင် ဆွေးနွေးချက် အသစ်တင်ခဲ့ပါသည်",
                targetPostId = newPost.id
            )
        }
    }

    fun toggleLike(postId: String) {
        val user = _currentUser.value ?: return
        val list = _posts.value.toMutableList()
        val index = list.indexOfFirst { it.id == postId }
        if (index == -1) return

        val post = list[index]
        val isLiked = post.likedByUids.contains(user.uid)
        val newLikedBy = if (isLiked) {
            post.likedByUids - user.uid
        } else {
            post.likedByUids + user.uid
        }
        val newCount = newLikedBy.size
        list[index] = post.copy(
            likeCount = newCount,
            likedByUids = newLikedBy
        )
        _posts.value = list

        if (!isLiked && post.authorId != user.uid) {
            addNotification(
                recipientUid = post.authorId,
                senderName = user.displayName,
                type = NotificationType.LIKE,
                messageBurmese = "${user.displayName} က သင်၏ပို့စ်ကို သဘောကျပါသည်",
                targetPostId = postId
            )
        }
    }

    fun addComment(postId: String, text: String) {
        val user = _currentUser.value ?: return
        if (text.isBlank()) return

        val comment = Comment(
            id = "cmt_${UUID.randomUUID().toString().take(8)}",
            postId = postId,
            authorId = user.uid,
            authorName = user.displayName,
            authorAvatar = user.avatarUrl,
            text = text.trim(),
            timestamp = System.currentTimeMillis()
        )
        val currentComments = _comments.value[postId].orEmpty()
        val updatedMap = _comments.value.toMutableMap()
        updatedMap[postId] = currentComments + comment
        _comments.value = updatedMap

        // Update post comment count
        val postList = _posts.value.toMutableList()
        val postIndex = postList.indexOfFirst { it.id == postId }
        if (postIndex != -1) {
            val p = postList[postIndex]
            postList[postIndex] = p.copy(commentCount = p.commentCount + 1)
            _posts.value = postList

            if (p.authorId != user.uid) {
                addNotification(
                    recipientUid = p.authorId,
                    senderName = user.displayName,
                    type = NotificationType.COMMENT,
                    messageBurmese = "${user.displayName} က သင်၏ပို့စ်တွင် မှတ်ချက်ရေးသားခဲ့သည်: \"${text.take(20)}\"",
                    targetPostId = postId
                )
            }
        }
    }

    fun sharePost(postId: String) {
        val list = _posts.value.toMutableList()
        val index = list.indexOfFirst { it.id == postId }
        if (index != -1) {
            val p = list[index]
            list[index] = p.copy(shareCount = p.shareCount + 1)
            _posts.value = list
        }
    }

    fun deletePost(postId: String) {
        _posts.value = _posts.value.filterNot { it.id == postId }
    }

    // --- Groups ---

    fun toggleJoinGroup(groupId: String) {
        val user = _currentUser.value ?: return
        val currentList = _groups.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == groupId }
        if (index == -1) return

        val group = currentList[index]
        val isMember = group.memberUids.contains(user.uid)
        val newMembers = if (isMember) {
            group.memberUids - user.uid
        } else {
            group.memberUids + user.uid
        }
        currentList[index] = group.copy(
            memberUids = newMembers,
            memberCount = newMembers.size
        )
        _groups.value = currentList
    }

    // --- Direct Messages ---

    fun getConversationId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_$uid2" else "${uid2}_$uid1"
    }

    fun sendMessage(recipientId: String, text: String) {
        val user = _currentUser.value ?: return
        if (text.isBlank()) return

        val convId = getConversationId(user.uid, recipientId)
        val msg = ChatMessage(
            id = "msg_${UUID.randomUUID().toString().take(8)}",
            conversationId = convId,
            senderId = user.uid,
            senderName = user.displayName,
            recipientId = recipientId,
            text = text.trim(),
            timestamp = System.currentTimeMillis()
        )
        val map = _messages.value.toMutableMap()
        val currentList = map[convId].orEmpty()
        map[convId] = currentList + msg
        _messages.value = map

        addNotification(
            recipientUid = recipientId,
            senderName = user.displayName,
            type = NotificationType.MESSAGE,
            messageBurmese = "${user.displayName} က သင့်ထံသို့ မက်ဆေ့ခ်ျ ပေးပို့ခဲ့သည်",
            targetChatUserId = user.uid
        )
    }

    // --- Notifications ---

    private fun addNotification(
        recipientUid: String,
        senderName: String,
        type: NotificationType,
        messageBurmese: String,
        targetPostId: String? = null,
        targetChatUserId: String? = null
    ) {
        val notification = AppNotification(
            recipientUid = recipientUid,
            senderName = senderName,
            type = type,
            messageBurmese = messageBurmese,
            targetPostId = targetPostId,
            targetChatUserId = targetChatUserId,
            isRead = false,
            timestamp = System.currentTimeMillis()
        )
        _notifications.value = listOf(notification) + _notifications.value
    }

    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    // --- Safety & Moderation ---

    fun reportPost(postId: String, reason: String) {
        val user = _currentUser.value
        val report = PostReport(
            postId = postId,
            reporterUid = user?.uid ?: "anonymous",
            reasonBurmese = reason,
            reportedAt = System.currentTimeMillis()
        )
        _reports.value = _reports.value + report
    }

    fun blockUser(targetUid: String) {
        _blockedUserIds.value = _blockedUserIds.value + targetUid
    }

    fun unblockUser(targetUid: String) {
        _blockedUserIds.value = _blockedUserIds.value - targetUid
    }

    // --- Initial Seed Data for Instant Community Experience ---

    private fun seedInitialData() {
        // Preload default demo user
        val demoUser = UserProfile(
            uid = "user_worker_01",
            displayName = "ကိုမင်းသူ (Ko Min Thu)",
            phoneOrEmail = "0987654321",
            bio = "မဟာချိုင် ပင်လယ်စာစက်ရုံလုပ်သား။ ဗီဇာ၊ MOU သတင်းများနှင့် အတွေ့အကြုံများ မျှဝေပါသည်။",
            locationThai = "မဟာချိုင် (Samut Sakhon)",
            occupation = "ပင်လယ်စာ အအေးခန်းစက်ရုံ"
        )
        userAccounts[demoUser.phoneOrEmail] = Pair(demoUser, hashPassword("worker123"))
        _currentUser.value = demoUser

        // Seed initial 4 community groups
        _groups.value = listOf(
            CommunityGroup(
                id = "grp_work_permits",
                nameBurmese = "အလုပ်ပါမစ်နှင့် ဗီဇာအထောက်အထား",
                nameEnglish = "Work Permits & Visa Documentation",
                descriptionBurmese = "MOU စနစ်၊ ပန်းရောင်ကတ်၊ CI သက်တမ်းတိုးခြင်းနှင့် ရက် ၉၀ သတင်းပို့ခြင်းဆိုင်ရာ အကြံပြုချက်များ",
                category = "ဥပဒေရေးရာ (Legal)",
                memberCount = 1420,
                memberUids = setOf(demoUser.uid, "u_2", "u_3"),
                iconKey = "description"
            ),
            CommunityGroup(
                id = "grp_factory_jobs",
                nameBurmese = "စက်ရုံနှင့် အလုပ်အကိုင် အခွင့်အလမ်းများ",
                nameEnglish = "Factory & Job Opportunities",
                descriptionBurmese = "ဘန်ကောက်၊ မဟာချိုင်၊ ချွန်းဘူရီ နှင့် နီးစပ်ရာ စက်ရုံအလုပ်ခေါ်စာများနှင့် အိုတီရရှိမှု အချက်အလက်များ",
                category = "အလုပ်အကိုင် (Jobs)",
                memberCount = 2850,
                memberUids = setOf(demoUser.uid, "u_4"),
                iconKey = "work"
            ),
            CommunityGroup(
                id = "grp_mahachai_bangkok",
                nameBurmese = "မဟာချိုင်နှင့် ဘန်ကောက် အသိုက်အဝန်း",
                nameEnglish = "Mahachai & Bangkok Community Hub",
                descriptionBurmese = "အခန်းငှားရန်၊ မြန်မာအစားအစာဆိုင်များ၊ ဈေးဝယ်စရာများနှင့် အချင်းချင်း အကူအညီတောင်းခံရာ နေရာ",
                category = "လူမှုဘဝ (Community)",
                memberCount = 3120,
                memberUids = setOf(demoUser.uid, "u_5", "u_6"),
                iconKey = "groups"
            ),
            CommunityGroup(
                id = "grp_legal_aid",
                nameBurmese = "ဥပဒေအကူအညီနှင့် အလုပ်သမားအခွင့်အရေး",
                nameEnglish = "Legal Aid & Labor Rights",
                descriptionBurmese = "လုပ်ခလစာ မရရှိခြင်း၊ ထိခိုက်ဒဏ်ရာရမှု လျော်ကြေးနှင့် အခမဲ့ ဥပဒေအကြံပေးအဖွဲ့များ ဆက်သွယ်ရန် လမ်းညွှန်",
                category = "အကူအညီ (Hotline)",
                memberCount = 980,
                memberUids = setOf("u_admin"),
                iconKey = "gavel"
            )
        )

        // Seed initial helpful posts
        _posts.value = listOf(
            Post(
                id = "post_001",
                authorId = "user_lawyer_myanmar",
                authorName = "ဦးကျော်ဇင် (အလုပ်သမားကူညီရေးအဖွဲ့)",
                authorAvatar = "",
                authorLocation = "ဘန်ကောက် (Bangkok)",
                content = "📢 အရေးကြီးသတိပေးချက် - ၂၀၂၆ ခုနှစ်အတွက် CI စာအုပ်နှင့် အလုပ်ပါမစ် သက်တမ်းတိုးရက်များ စတင်နေပြီ ဖြစ်ပါသည်။ ပွဲစားများ၏ လိမ်လည်မှုမခံရစေရန် သက်ဆိုင်ရာ စခန်းများသို့ ကိုယ်တိုင် သို့မဟုတ် တရားဝင် ကိုယ်စားလှယ်များမှသာ ဆောင်ရွက်ကြပါရန် အသိပေးအပ်ပါသည်။ မရှင်းလင်းပါက ကွန်မန့်တွင် မေးမြန်းနိုင်ပါသည်။",
                groupId = "grp_work_permits",
                groupName = "အလုပ်ပါမစ်နှင့် ဗီဇာအထောက်အထား",
                likeCount = 89,
                commentCount = 14,
                shareCount = 35,
                likedByUids = setOf(demoUser.uid),
                timestamp = System.currentTimeMillis() - 3600000 * 4
            ),
            Post(
                id = "post_002",
                authorId = "user_recruiter_thai",
                authorName = "မသီတာ (စက်ရုံအလုပ်အကိုင်ရှာဖွေရေး)",
                authorAvatar = "",
                authorLocation = "မဟာချိုင် (Samut Sakhon)",
                content = "မဟာချိုင် ပင်လယ်စာအအေးခန်းစက်ရုံကြီးတွင် မြန်မာလုပ်သား အမြန်အလိုရှိသည်။ အခြေခံလစာ ၃၆၃ ဘတ် + နေ့တွက် အိုတီ (OT) ၂ နာရီမှ ၃ နာရီ ပုံမှန်ရှိသည်။ အလုပ်ပါမစ် တရားဝင် အထောက်အထားပြည့်စုံသူများ ဦးစားပေးပါမည်။ အခန်းအခမဲ့ စီစဉ်ပေးပါသည်။",
                groupId = "grp_factory_jobs",
                groupName = "စက်ရုံနှင့် အလုပ်အကိုင် အခွင့်အလမ်းများ",
                likeCount = 124,
                commentCount = 28,
                shareCount = 42,
                likedByUids = emptySet(),
                timestamp = System.currentTimeMillis() - 3600000 * 12
            ),
            Post(
                id = "post_003",
                authorId = "user_peer_aung",
                authorName = "အောင်အောင် (Aung Aung)",
                authorAvatar = "",
                authorLocation = "ချင်းမိုင် (Chiang Mai)",
                content = "ချင်းမိုင်ရောက် ညီအစ်ကို မောင်နှမများခင်ဗျာ၊ ယနေ့ ထိုင်းဘတ်ငွေမှ မြန်မာကျပ်ငွေသို့ လွှဲရာတွင် တရားဝင် ဘဏ်လမ်းကြောင်းနှင့် ယုံကြည်စိတ်ချရသော တရားဝင် ငွေလွှဲကိုယ်စားလှယ်များမှသာ လွှဲကြရန် တိုက်တွန်းလိုပါသည်။ လမ်းဘေးငွေလဲဆိုင် အချို့တွင် အကောင့်ပိတ်ခံရမှုများ ကြုံတွေ့နေရပါသည်။",
                groupId = "grp_mahachai_bangkok",
                groupName = "မဟာချိုင်နှင့် ဘန်ကောက် အသိုက်အဝန်း",
                likeCount = 65,
                commentCount = 9,
                shareCount = 18,
                likedByUids = setOf(demoUser.uid),
                timestamp = System.currentTimeMillis() - 3600000 * 24
            )
        )

        // Seed initial comments
        _comments.value = mapOf(
            "post_001" to listOf(
                Comment(
                    id = "c1",
                    postId = "post_001",
                    authorId = demoUser.uid,
                    authorName = demoUser.displayName,
                    authorAvatar = "",
                    text = "ဆရာခင်ဗျာ၊ ပန်းရောင်ကတ် သက်တမ်းကုန်ခါနီးသူများ ရက် ၉၀ တိုင်ကြားခြင်း အွန်လိုင်းမှ ပြုလုပ်၍ ရပါသလားခင်ဗျာ။",
                    timestamp = System.currentTimeMillis() - 3600000 * 3
                ),
                Comment(
                    id = "c2",
                    postId = "post_001",
                    authorId = "user_lawyer_myanmar",
                    authorName = "ဦးကျော်ဇင် (အလုပ်သမားကူညီရေးအဖွဲ့)",
                    authorAvatar = "",
                    text = "ရရှိပါသည် ကိုမင်းသူ။ Thai Immigration e-Service ဝဘ်ဆိုက်တွင် တိုက်ရိုက် လျှောက်ထားနိုင်ပါသည်ခင်ဗျာ။",
                    timestamp = System.currentTimeMillis() - 3600000 * 2
                )
            )
        )

        // Seed initial direct conversation
        val welcomeConvId = getConversationId(demoUser.uid, "user_lawyer_myanmar")
        _messages.value = mapOf(
            welcomeConvId to listOf(
                ChatMessage(
                    id = "m1",
                    conversationId = welcomeConvId,
                    senderId = "user_lawyer_myanmar",
                    senderName = "ဦးကျော်ဇင် (အလုပ်သမားကူညီရေးအဖွဲ့)",
                    recipientId = demoUser.uid,
                    text = "မင်္ဂလာပါ ကိုမင်းသူ၊ MyanmarConnect သို့ ကြိုဆိုပါသည်။ အလုပ်သမားအခွင့်အရေးနှင့် စာရွက်စာတမ်းဆိုင်ရာ အခက်အခဲရှိပါက မည်သည့်အချိန်မဆို မေးမြန်းနိုင်ပါသည်။",
                    timestamp = System.currentTimeMillis() - 3600000 * 6
                )
            )
        )

        // Seed initial notifications
        _notifications.value = listOf(
            AppNotification(
                id = "notif_01",
                recipientUid = demoUser.uid,
                senderName = "ဦးကျော်ဇင် (အလုပ်သမားကူညီရေးအဖွဲ့)",
                type = NotificationType.COMMENT,
                messageBurmese = "ဦးကျော်ဇင် က သင်၏မေးခွန်းကို ပြန်လည်ဖြေကြားခဲ့သည်",
                targetPostId = "post_001",
                isRead = false,
                timestamp = System.currentTimeMillis() - 3600000 * 2
            ),
            AppNotification(
                id = "notif_02",
                recipientUid = demoUser.uid,
                senderName = "MyanmarConnect အဖွဲ့",
                type = NotificationType.SYSTEM,
                messageBurmese = "မြန်မာလုပ်သားများအတွက် အသိုက်အဝန်း MyanmarConnect သို့ ကြိုဆိုပါသည်!",
                isRead = true,
                timestamp = System.currentTimeMillis() - 3600000 * 24
            )
        )
    }

    companion object {
        @Volatile
        private var instance: MyanmarConnectRepository? = null

        fun getInstance(): MyanmarConnectRepository {
            return instance ?: synchronized(this) {
                instance ?: MyanmarConnectRepository().also { instance = it }
            }
        }
    }
}
