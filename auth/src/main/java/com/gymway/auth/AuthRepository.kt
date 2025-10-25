package com.gymway.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log
import com.google.firebase.Timestamp
import com.gymway.auth.model.User

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String,
        role: String
    ): Result<String> {
        return try {
            Log.d("AuthRepository", "🚀 Starting registration process...")

            // 1. Create user in Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("User creation failed")
            val uid = user.uid
            Log.d("AuthRepository", "✅ User created in Auth: $uid")

            // 2. Save user data to Firestore
            // در registerWithEmail این تغییر رو بده:

            val userData = hashMapOf<String, Any>(
                "uid" to uid,
                "email" to email,
                "displayName" to displayName,
                "role" to role,
                "age" to 0,  // مقدار پیش‌فرض
                "gender" to "",  // مقدار پیش‌فرض
                "emailVerified" to false,
                "createdAt" to Timestamp.now()
            )
            firestore.collection("users").document(uid).set(userData).await()
            Log.d("AuthRepository", "✅ User data saved to Firestore")

            // 3. Send email verification
            user.sendEmailVerification().await()
            Log.d("AuthRepository", "✅ Email verification sent")

            Log.d("AuthRepository", "🎉 Registration completed successfully")
            Result.success(uid)

        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Registration failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun loginWithEmail(email: String, password: String): Result<String> {
        return try {
            Log.d("AuthRepository", "🔐 Attempting login...")

            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("Authentication failed")

            if (!user.isEmailVerified) {
                throw Exception("لطفاً ابتدا ایمیل خود را تأیید کنید")
            }

            Log.d("AuthRepository", "✅ Login successful: ${user.uid}")
            Result.success(user.uid)

        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Login failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // در متد getUserData در AuthRepository.kt این تغییر رو بده:

    suspend fun getUserData(uid: String): Result<User> {
        return try {
            Log.d("AuthRepository", "📥 Fetching user data for: $uid")

            val document = firestore.collection("users").document(uid).get().await()

            if (document.exists()) {
                val user = User(
                    uid = document.getString("uid") ?: "",
                    email = document.getString("email") ?: "",
                    displayName = document.getString("displayName") ?: "",
                    role = document.getString("role") ?: "athlete",
                    age = document.getLong("age")?.toInt(),  // اضافه شد
                    gender = document.getString("gender"),  // اضافه شد
                    emailVerified = document.getBoolean("emailVerified") ?: false,
                    createdAt = document.getTimestamp("createdAt")
                )
                Log.d("AuthRepository", "✅ User data loaded: ${user.displayName} - ${user.role}")
                Result.success(user)
            } else {
                Log.w("AuthRepository", "⚠️ User document not found in Firestore")
                Result.failure(Exception("User data not found"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Failed to load user data", e)
            Result.failure(e)
        }
    }
    suspend fun getCurrentUserData(): Result<User> {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("No user logged in")
            getUserData(currentUser.uid)
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Failed to get current user data", e)
            Result.failure(e)
        }
    }

    suspend fun checkEmailVerification(): Result<Boolean> {
        return try {
            auth.currentUser?.reload()?.await()
            val isVerified = auth.currentUser?.isEmailVerified ?: false
            Log.d("AuthRepository", "📧 Email verification status: $isVerified")
            Result.success(isVerified)
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Email verification check failed", e)
            Result.failure(e)
        }
    }

    suspend fun resendVerificationEmail(): Result<Boolean> {
        return try {
            val user = auth.currentUser ?: throw Exception("No user found")
            user.sendEmailVerification().await()
            Log.d("AuthRepository", "✅ Verification email resent")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Failed to resend verification email", e)
            Result.failure(e)
        }
    }

    fun getCurrentUser() = auth.currentUser

    suspend fun signOut() {
        try {
            auth.signOut()
            Log.d("AuthRepository", "✅ User signed out")
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Sign out failed", e)
        }
    }
    // به AuthRepository.kt این متد رو اضافه کن:

    suspend fun updateUserProfile(userId: String, updatedData: Map<String, Any>): Result<Boolean> {
        return try {
            Log.d("AuthRepository", "📝 Updating user profile for: $userId")

            firestore.collection("users").document(userId).update(updatedData).await()

            Log.d("AuthRepository", "✅ User profile updated successfully")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Failed to update user profile", e)
            Result.failure(e)
        }
    }
}