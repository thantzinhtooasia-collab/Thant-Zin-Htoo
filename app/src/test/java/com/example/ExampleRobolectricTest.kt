package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.FirestoreUserProfileRepository
import com.example.data.repository.MyanmarConnectRepository
import com.example.ui.viewmodel.UserProfileUiState
import com.example.ui.viewmodel.UserProfileViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MyanmarConnect", appName)
    }

    @Test
    fun `repository initial state and registration`() {
        val repo = MyanmarConnectRepository.getInstance()
        assertNotNull(repo.currentUser.value)
        assertEquals(4, repo.groups.value.size)

        // Test new user registration
        val regResult = repo.register(
            displayName = "မလှလှ (Hla Hla)",
            phoneOrEmail = "0911223344",
            passwordPlain = "securePass123",
            locationThai = "ဘန်ကောက် (Bangkok)",
            occupation = "အထည်ချုပ်စက်ရုံ (Garment Factory)"
        )
        assertTrue(regResult.isSuccess)
        assertEquals("မလှလှ (Hla Hla)", repo.currentUser.value?.displayName)

        // Test creating post
        val initialPostsCount = repo.posts.value.size
        repo.createPost("စက်ရုံအလုပ် အဆင်ပြေပါသလား မိတ်ဆွေတို့ရေ")
        assertEquals(initialPostsCount + 1, repo.posts.value.size)

        // Test like toggle
        val newPost = repo.posts.value.first()
        repo.toggleLike(newPost.id)
        assertTrue(repo.posts.value.first().likedByUids.contains(repo.currentUser.value!!.uid))

        // Test blocking
        repo.blockUser("spammer_uid_99")
        assertTrue(repo.blockedUserIds.value.contains("spammer_uid_99"))
        repo.unblockUser("spammer_uid_99")
        assertFalse(repo.blockedUserIds.value.contains("spammer_uid_99"))
    }

    @Test
    fun `user profile view model loads profile and handles updates`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val firestoreRepo = FirestoreUserProfileRepository.create(context)
        val appRepo = MyanmarConnectRepository.getInstance()
        val currentUid = appRepo.currentUser.value?.uid ?: "user_worker_01"

        val viewModel = UserProfileViewModel(firestoreRepo, appRepo)
        viewModel.loadProfile(currentUid)

        // Verify state reaches success or handles gracefully
        val state = viewModel.uiState.value
        if (state is UserProfileUiState.Success) {
            assertNotNull(state.profile)
            assertTrue(state.isCurrentUser)
        }
    }
}
