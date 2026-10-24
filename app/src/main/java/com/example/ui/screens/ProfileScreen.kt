package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UserProfile
import com.example.data.repository.MyanmarConnectRepository
import com.example.ui.components.AccountDeletionDialog
import com.example.ui.components.CommunityGuidelinesDialog
import com.example.ui.components.PostItemCard
import com.example.ui.components.PrivacyPolicyDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    repository: MyanmarConnectRepository,
    viewingTargetUserId: String? = null,
    onNavigateBackFromOtherProfile: () -> Unit = {},
    onStartChatWithUser: (targetUid: String, targetName: String) -> Unit = { _, _ -> },
    onLogoutOrDeleted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentUser by repository.currentUser.collectAsState()
    val posts by repository.posts.collectAsState()
    val blockedUserIds by repository.blockedUserIds.collectAsState()

    val isViewingOtherUser = viewingTargetUserId != null && viewingTargetUserId != currentUser?.uid

    // Other user state lookup
    val otherUserPosts = if (isViewingOtherUser) {
        posts.filter { it.authorId == viewingTargetUserId }
    } else emptyList()
    val otherUserName = otherUserPosts.firstOrNull()?.authorName ?: "မြန်မာလုပ်သား"
    val otherUserLocation = otherUserPosts.firstOrNull()?.authorLocation ?: "ထိုင်းနိုင်ငံ"

    // Dialog flags
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showGuidelinesDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showBlockedUsersDialog by remember { mutableStateOf(false) }

    var selectedTabIndex by remember { mutableStateOf(0) }

    if (isViewingOtherUser) {
        UserProfileScreen(
            userId = viewingTargetUserId!!,
            onNavigateBack = onNavigateBackFromOtherProfile,
            onStartChat = onStartChatWithUser,
            modifier = modifier
        )
    } else {
        // Own User Profile Screen
        val user = currentUser
        val myPosts = if (user != null) posts.filter { it.authorId == user.uid } else emptyList()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "ကိုယ်ရေးအချက်အလက် (Profile)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "အကောင့်နှင့် ဆက်တင်များ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            modifier = modifier
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .testTag("my_profile_screen")
            ) {
                item {
                    // Profile Info Header Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (!user?.avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = user?.avatarUrl,
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user?.displayName?.take(1) ?: "M",
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = user?.displayName ?: "အမည်မရှိ",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )

                            Text(
                                text = user?.phoneOrEmail ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = user?.locationThai ?: "ထိုင်းနိုင်ငံ",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Work,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = user?.occupation ?: "အထွေထွေလုပ်သား",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }

                            if (!user?.bio.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = user?.bio ?: "",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Edit Profile Button
                            OutlinedButton(
                                onClick = { showEditProfileDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_profile_button")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ပရိုဖိုင် အချက်အလက်များ ပြင်ဆင်ရန်")
                            }
                        }
                    }

                    // Profile Tabs: My Posts vs Settings & Policies
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { Text("ကျွန်ုပ်၏ ပို့စ်များ (${myPosts.size})") }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("ဆက်တင်နှင့် မူဝါဒများ") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (selectedTabIndex == 0) {
                    // My Posts Tab
                    if (myPosts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "သင်တင်ထားသော ပို့စ် မရှိသေးပါ",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    } else {
                        items(myPosts, key = { it.id }) { post ->
                            PostItemCard(
                                post = post,
                                currentUserId = user?.uid.orEmpty(),
                                onLikeClick = { repository.toggleLike(post.id) },
                                onCommentClick = { },
                                onShareClick = { repository.sharePost(post.id) },
                                onAuthorClick = { },
                                onReportClick = { },
                                onBlockUserClick = { },
                                onDeletePostClick = { repository.deletePost(post.id) }
                            )
                        }
                    }
                } else {
                    // Settings & Legal Tab
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                // Blocked Users
                                SettingsRowItem(
                                    icon = Icons.Default.Block,
                                    title = "ပိတ်ဆို့ထားသော အသုံးပြုသူများ (Blocked Users)",
                                    subtitle = "${blockedUserIds.size} ဦး ပိတ်ဆို့ထားပါသည်",
                                    onClick = { showBlockedUsersDialog = true }
                                )

                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                                // Community Guidelines
                                SettingsRowItem(
                                    icon = Icons.Default.Security,
                                    title = "လူမှုစည်းကမ်းများ (Community Guidelines)",
                                    subtitle = "အသိုက်အဝန်း လိုက်နာရမည့် စည်းမျဉ်းများ",
                                    onClick = { showGuidelinesDialog = true }
                                )

                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                                // Privacy Policy
                                SettingsRowItem(
                                    icon = Icons.Default.Policy,
                                    title = "ကိုယ်ရေးအချက်အလက် လုံခြုံမှုမူဝါဒ (Privacy Policy)",
                                    subtitle = "အချက်အလက်ထိန်းသိမ်းမှုဆိုင်ရာ မူဝါဒ",
                                    onClick = { showPrivacyPolicyDialog = true }
                                )

                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                                // Logout
                                SettingsRowItem(
                                    icon = Icons.Default.ExitToApp,
                                    title = "အကောင့်မှ ထွက်မည် (Log Out)",
                                    subtitle = "အခြားအကောင့်ဖြင့် ပြန်လည်ဝင်ရောက်နိုင်ပါသည်",
                                    iconTint = MaterialTheme.colorScheme.error,
                                    onClick = {
                                        repository.logout()
                                        onLogoutOrDeleted()
                                    }
                                )

                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                                // Delete Account
                                SettingsRowItem(
                                    icon = Icons.Default.DeleteForever,
                                    title = "အကောင့်ကို အပြီးတိုင် ဖျက်သိမ်းရန် (Delete Account)",
                                    subtitle = "ပို့စ်များနှင့် အချက်အလက်များ အပြီးပျက်ပြယ်မည်",
                                    iconTint = MaterialTheme.colorScheme.error,
                                    onClick = { showDeleteAccountDialog = true }
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

    // Edit Profile Dialog
    if (showEditProfileDialog && currentUser != null) {
        val u = currentUser!!
        EditProfileDialog(
            initialUser = u,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, bio, location, occ, avatar ->
                repository.updateProfile(name, bio, location, occ, avatar)
                showEditProfileDialog = false
            }
        )
    }

    // Guidelines Dialog
    if (showGuidelinesDialog) {
        CommunityGuidelinesDialog(onDismiss = { showGuidelinesDialog = false })
    }

    // Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyPolicyDialog = false })
    }

    // Account Deletion Dialog
    if (showDeleteAccountDialog) {
        AccountDeletionDialog(
            onDismiss = { showDeleteAccountDialog = false },
            onConfirmDelete = {
                repository.deleteAccount()
                onLogoutOrDeleted()
            }
        )
    }

    // Blocked Users Management Dialog
    if (showBlockedUsersDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedUsersDialog = false },
            title = {
                Text(
                    text = "ပိတ်ဆို့ထားသော အသုံးပြုသူများ",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                if (blockedUserIds.isEmpty()) {
                    Text("မည်သည့် အသုံးပြုသူကိုမျှ ပိတ်ဆို့ထားခြင်း မရှိပါ။")
                } else {
                    Column {
                        blockedUserIds.forEach { uid ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "User ID: ${uid.take(12)}...")
                                TextButton(onClick = { repository.unblockUser(uid) }) {
                                    Text("ပြန်ဖွင့်မည် (Unblock)")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showBlockedUsersDialog = false }) {
                    Text("ပိတ်မည်")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun SettingsRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(iconTint.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    initialUser: UserProfile,
    onDismiss: () -> Unit,
    onSave: (displayName: String, bio: String, location: String, occupation: String, avatarUrl: String) -> Unit
) {
    var displayName by remember { mutableStateOf(initialUser.displayName) }
    var bio by remember { mutableStateOf(initialUser.bio) }
    var avatarUrl by remember { mutableStateOf(initialUser.avatarUrl) }

    val thaiLocations = listOf(
        "ဘန်ကောက် (Bangkok)",
        "မဟာချိုင် (Samut Sakhon)",
        "ချင်းမိုင် (Chiang Mai)",
        "ချွန်းဘူရီ (Chonburi)",
        "မဲဆောက် (Mae Sot)",
        "စူရတ်ဌာနီ (Surat Thani)",
        "ရနောင်း (Ranong)"
    )
    var selectedLocation by remember { mutableStateOf(initialUser.locationThai) }
    var locMenuExpanded by remember { mutableStateOf(false) }

    val occupations = listOf(
        "အထည်ချုပ်စက်ရုံ (Garment Factory)",
        "ပင်လယ်စာ အအေးခန်း (Seafood Processing)",
        "ဆောက်လုပ်ရေးလုပ်ငန်း (Construction)",
        "စားသောက်ဆိုင်နှင့် ဝန်ဆောင်မှု (Restaurant / Service)",
        "စိုက်ပျိုးရေးနှင့် ခြံလုပ်ငန်း (Agriculture)",
        "အထွေထွေလုပ်သား (General Worker)"
    )
    var selectedOccupation by remember { mutableStateOf(initialUser.occupation) }
    var occMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "ပရိုဖိုင် ပြင်ဆင်ရန်",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("အမည် (Display Name)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = locMenuExpanded,
                    onExpandedChange = { locMenuExpanded = !locMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedLocation,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("နေထိုင်ရာ ဒေသ") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = locMenuExpanded,
                        onDismissRequest = { locMenuExpanded = false }
                    ) {
                        thaiLocations.forEach { loc ->
                            DropdownMenuItem(
                                text = { Text(loc) },
                                onClick = {
                                    selectedLocation = loc
                                    locMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = occMenuExpanded,
                    onExpandedChange = { occMenuExpanded = !occMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedOccupation,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("အလုပ်အကိုင်") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = occMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = occMenuExpanded,
                        onDismissRequest = { occMenuExpanded = false }
                    ) {
                        occupations.forEach { occ ->
                            DropdownMenuItem(
                                text = { Text(occ) },
                                onClick = {
                                    selectedOccupation = occ
                                    occMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("ကိုယ်ရေးမှတ်ချက် (Bio)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(displayName, bio, selectedLocation, selectedOccupation, avatarUrl)
                },
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text("သိမ်းဆည်းမည်")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("မလုပ်တော့ပါ")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
