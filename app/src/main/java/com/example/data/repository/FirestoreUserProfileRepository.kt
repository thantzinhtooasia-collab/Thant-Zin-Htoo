package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.Post
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreUserRepo"

/**
 * Repository responsible for fetching and updating user profiles and their posts
 * using Firebase Firestore collections:
 * - /users/{userId}
 * - /posts (query where authorId == userId)
 *
 * Implements robust fallback to MyanmarConnectRepository when Firebase is not
 * initialized or in offline/test environments.
 */
class FirestoreUserProfileRepository(
    private val firestore: FirebaseFirestore? = null,
    private val fallbackRepo: MyanmarConnectRepository = MyanmarConnectRepository.getInstance()
) {

    companion object {
        fun create(context: Context): FirestoreUserProfileRepository {
            val db = try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    null
                } else {
                    val dbIdRes = context.resources.getIdentifier("firestore_database_id", "string", context.packageName)
                    if (dbIdRes != 0) {
                        val dbId = context.getString(dbIdRes)
                        FirebaseFirestore.getInstance(dbId)
                    } else {
                        Firebase.firestore
                    }
                }
            } catch (e: Exception) {
                null
            }
            return FirestoreUserProfileRepository(db)
        }
    }

    /**
     * Fetch user profile from Firestore /users/{userId}
     */
    suspend fun fetchUserProfile(userId: String): Result<UserProfile> {
        val db = firestore
        if (db == null) {
            val local = fallbackRepo.currentUser.value
            return if (local != null && local.uid == userId) {
                Result.success(local)
            } else {
                Result.success(UserProfile(uid = userId, displayName = "မြန်မာလုပ်သား", bio = "ထိုင်းနိုင်ငံရောက် အသိုက်အဝန်းဝင်"))
            }
        }

        return try {
            val doc = db.collection("users").document(userId).get().await()
            if (doc.exists()) {
                val profile = doc.toObject(UserProfile::class.java)
                    ?: UserProfile(uid = userId, displayName = doc.getString("displayName") ?: "မြန်မာလုပ်သား")
                Result.success(profile)
            } else {
                val local = fallbackRepo.currentUser.value
                if (local != null && local.uid == userId) {
                    Result.success(local)
                } else {
                    Result.success(UserProfile(uid = userId, displayName = "မြန်မာလုပ်သား", bio = "ထိုင်းနိုင်ငံရောက် အသိုက်အဝန်းဝင်"))
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed fetching user profile from Firestore: ${e.message}; using fallback")
            val local = fallbackRepo.currentUser.value
            if (local != null && local.uid == userId) {
                Result.success(local)
            } else {
                Result.success(UserProfile(uid = userId, displayName = "မြန်မာလုပ်သား", bio = "ထိုင်းနိုင်ငံရောက် အသိုက်အဝန်းဝင်"))
            }
        }
    }

    /**
     * Real-time listener for user profile changes
     */
    fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(fallbackRepo.currentUser.value)
            awaitClose { }
            return@callbackFlow
        }

        val docRef = db.collection("users").document(userId)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Profile snapshot error: ${error.message}")
                val local = fallbackRepo.currentUser.value
                if (local != null && local.uid == userId) {
                    trySend(local)
                }
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java)
                trySend(profile)
            } else {
                val local = fallbackRepo.currentUser.value
                if (local != null && local.uid == userId) {
                    trySend(local)
                }
            }
        }

        awaitClose { registration.remove() }
    }

    /**
     * Save or update user profile to Firestore /users/{userId}
     */
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        val db = firestore
        if (db != null) {
            try {
                db.collection("users").document(profile.uid).set(profile).await()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore set failed: ${e.message}; persisting in local repository")
            }
        }

        // Always sync with local memory cache
        fallbackRepo.updateProfile(
            displayName = profile.displayName,
            bio = profile.bio,
            locationThai = profile.locationThai,
            occupation = profile.occupation,
            avatarUrl = profile.avatarUrl
        )
        return Result.success(Unit)
    }

    /**
     * Fetch all posts created by a specific user from /posts
     */
    suspend fun fetchUserPosts(userId: String): Result<List<Post>> {
        val db = firestore
        if (db == null) {
            val localPosts = fallbackRepo.posts.value.filter { it.authorId == userId }
            return Result.success(localPosts)
        }

        return try {
            val snapshot = db.collection("posts")
                .whereEqualTo("authorId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val posts = snapshot.toObjects(Post::class.java)
            if (posts.isNotEmpty()) {
                Result.success(posts)
            } else {
                val localPosts = fallbackRepo.posts.value.filter { it.authorId == userId }
                Result.success(localPosts)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed query user posts from Firestore: ${e.message}; using fallback")
            val localPosts = fallbackRepo.posts.value.filter { it.authorId == userId }
            Result.success(localPosts)
        }
    }

    /**
     * Real-time listener for a user's published posts
     */
    fun observeUserPosts(userId: String): Flow<List<Post>> = callbackFlow {
        val db = firestore
        if (db == null) {
            val localPosts = fallbackRepo.posts.value.filter { it.authorId == userId }
            trySend(localPosts)
            awaitClose { }
            return@callbackFlow
        }

        val query = db.collection("posts")
            .whereEqualTo("authorId", userId)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "User posts snapshot error: ${error.message}")
                val localPosts = fallbackRepo.posts.value.filter { it.authorId == userId }
                trySend(localPosts)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val posts = snapshot.toObjects(Post::class.java)
                if (posts.isNotEmpty()) {
                    trySend(posts)
                } else {
                    val localPosts = fallbackRepo.posts.value.filter { it.authorId == userId }
                    trySend(localPosts)
                }
            } else {
                val localPosts = fallbackRepo.posts.value.filter { it.authorId == userId }
                trySend(localPosts)
            }
        }

        awaitClose { registration.remove() }
    }
}
