package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Post
import com.example.data.model.UserProfile
import com.example.data.repository.FirestoreUserProfileRepository
import com.example.data.repository.MyanmarConnectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UserProfileUiState {
    object Loading : UserProfileUiState
    data class Success(
        val profile: UserProfile,
        val posts: List<Post>,
        val isCurrentUser: Boolean
    ) : UserProfileUiState
    data class Error(val message: String) : UserProfileUiState
}

class UserProfileViewModel(
    private val firestoreRepo: FirestoreUserProfileRepository,
    private val appRepo: MyanmarConnectRepository = MyanmarConnectRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserProfileUiState>(UserProfileUiState.Loading)
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    private var currentLoadedUserId: String? = null

    fun loadProfile(userId: String) {
        currentLoadedUserId = userId
        _uiState.value = UserProfileUiState.Loading

        viewModelScope.launch {
            val isCurrent = appRepo.currentUser.value?.uid == userId

            // 1. Fetch user profile from Firestore
            val profileResult = firestoreRepo.fetchUserProfile(userId)
            val profile = profileResult.getOrElse {
                // If not found yet, default with safe profile
                val localUser = appRepo.currentUser.value
                if (localUser != null && localUser.uid == userId) {
                    localUser
                } else {
                    UserProfile(
                        uid = userId,
                        displayName = "မြန်မာလုပ်သား",
                        bio = "ထိုင်းနိုင်ငံရောက် အသိုက်အဝန်းဝင်",
                        locationThai = "ထိုင်းနိုင်ငံ"
                    )
                }
            }

            // 2. Fetch user's posts from Firestore /posts
            val postsResult = firestoreRepo.fetchUserPosts(userId)
            val posts = postsResult.getOrDefault(emptyList())

            _uiState.value = UserProfileUiState.Success(
                profile = profile,
                posts = posts,
                isCurrentUser = isCurrent
            )
        }
    }

    fun updateProfile(
        displayName: String,
        bio: String,
        locationThai: String,
        occupation: String,
        avatarUrl: String
    ) {
        val currentState = _uiState.value
        if (currentState !is UserProfileUiState.Success) return

        viewModelScope.launch {
            val updatedProfile = currentState.profile.copy(
                displayName = displayName.trim(),
                bio = bio.trim(),
                locationThai = locationThai.trim(),
                occupation = occupation.trim(),
                avatarUrl = avatarUrl.trim()
            )

            // Save to Firestore
            firestoreRepo.saveUserProfile(updatedProfile)

            // Update UI state immediately
            _uiState.value = currentState.copy(profile = updatedProfile)
        }
    }

    fun toggleLikePost(postId: String) {
        appRepo.toggleLike(postId)
        // Refresh local posts list in UI state
        val currentState = _uiState.value
        if (currentState is UserProfileUiState.Success) {
            val updatedPosts = currentState.posts.map { post ->
                if (post.id == postId) {
                    val currentUid = appRepo.currentUser.value?.uid.orEmpty()
                    val isLiked = post.likedByUids.contains(currentUid)
                    val newLikedBy = if (isLiked) post.likedByUids - currentUid else post.likedByUids + currentUid
                    post.copy(likeCount = newLikedBy.size, likedByUids = newLikedBy)
                } else post
            }
            _uiState.value = currentState.copy(posts = updatedPosts)
        }
    }

    fun deletePost(postId: String) {
        appRepo.deletePost(postId)
        val currentState = _uiState.value
        if (currentState is UserProfileUiState.Success) {
            _uiState.value = currentState.copy(posts = currentState.posts.filterNot { it.id == postId })
        }
    }

    class Factory(
        private val firestoreRepo: FirestoreUserProfileRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserProfileViewModel(firestoreRepo) as T
        }
    }
}
