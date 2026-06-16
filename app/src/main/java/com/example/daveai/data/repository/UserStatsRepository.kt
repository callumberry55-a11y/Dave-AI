package com.example.daveai.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val displayName: String? = null,
    val role: String? = "Explorer",
    val preferences: Map<String, String> = emptyMap(),
    val devId: String? = null
)

class UserStatsRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun trackUserLogin(uid: String, email: String?, attribution: Map<String, String?>, isDeveloper: Boolean = false) {
        try {
            val userRef = db.collection("users").document(uid)
            val userDoc = userRef.get().await()

            if (!userDoc.exists()) {
                val userData = mutableMapOf(
                    "email" to email,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "lastLogin" to FieldValue.serverTimestamp(),
                    "role" to if (isDeveloper) "Master Developer" else (if (attribution["preferred_network"] == "Aura") "Vanguard User" else "Elite User"),
                    "displayName" to if (isDeveloper) "Callum" else (email?.split("@")?.get(0) ?: "Dave Fan"),
                )
                
                if (attribution["preferred_network"] == "Aura") {
                    userData["network"] = "Aura"
                    userData["clickId"] = attribution["click_id"]
                }
                
                attribution.forEach { (key, value) ->
                    value?.let { userData[key] = it }
                }

                db.runTransaction { transaction ->
                    val statsRef = db.collection("stats").document("global")
                    val statsDoc = transaction[statsRef]
                    transaction[userRef] = userData
                    if (!statsDoc.exists()) {
                        transaction[statsRef] = mapOf("totalUsers" to 1L)
                    } else {
                        transaction.update(statsRef, "totalUsers", FieldValue.increment(1))
                    }
                }.await()
            } else {
                userRef.update("lastLogin", FieldValue.serverTimestamp()).await()
            }
        } catch (e: Exception) {
            Log.e("UserStats", "Failed to track user login", e)
        }
    }

    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val doc = db.collection("users").document(uid).get().await()
            if (doc.exists()) {
                @Suppress("UNCHECKED_CAST")
                UserProfile(
                    displayName = doc.getString("displayName"),
                    role = doc.getString("role"),
                    preferences = (doc["preferences"] as? Map<String, String>) ?: emptyMap(),
                    devId = doc.getString("devId")
                )
            } else null
        } catch (_: Exception) { null }
    }

    suspend fun setDevId(uid: String, devId: String) {
        try {
            db.collection("users").document(uid).update("devId", devId).await()
            Log.d("UserStats", "Personalized Dev ID registered for $uid")
        } catch (e: Exception) {
            Log.e("UserStats", "Failed to set devId", e)
            throw e
        }
    }

    suspend fun updatePreference(uid: String, key: String, value: String) {
        try {
            val userRef = db.collection("users").document(uid)
            db.runTransaction { transaction ->
                val snapshot = transaction[userRef]
                @Suppress("UNCHECKED_CAST")
                val prefs = (snapshot["preferences"] as? MutableMap<String, String>) ?: mutableMapOf()
                prefs[key] = value
                transaction.update(userRef, "preferences", prefs)
            }.await()
        } catch (e: Exception) {
            Log.e("UserStats", "Failed to update preference", e)
        }
    }

    suspend fun elevateToMasterDeveloper(uid: String) {
        try {
            val userRef = db.collection("users").document(uid)
            userRef.update(
                mapOf(
                    "role" to "Master Developer",
                    "displayName" to "Callum"
                )
            ).await()
            Log.d("UserStats", "User $uid elevated to Master Developer: Callum")
        } catch (e: Exception) {
            Log.e("UserStats", "Failed to elevate user", e)
        }
    }

    fun observeTotalUserCount(): Flow<Long> = callbackFlow {
        val statsRef = db.collection("stats").document("global")
        val subscription = statsRef.addSnapshotListener { snapshot, _ ->
            val count = snapshot?.getLong("totalUsers") ?: 0L
            trySend(count)
        }
        awaitClose { subscription.remove() }
    }

    suspend fun deleteUserData(uid: String) {
        try {
            db.runTransaction { transaction ->
                val userRef = db.collection("users").document(uid)
                val statsRef = db.collection("stats").document("global")
                transaction.delete(userRef)
                transaction.update(statsRef, "totalUsers", FieldValue.increment(-1))
            }.await()
            Log.d("UserStats", "User data for $uid successfully deleted from Firestore")
        } catch (e: Exception) {
            Log.e("UserStats", "Failed to delete user data", e)
            throw e
        }
    }

    suspend fun saveFcmToken(uid: String, token: String) {
        try {
            val data = mapOf(
                "token" to token,
                "timestamp" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid)
                .collection("fcmTokens").document(token)
                .set(data).await()
            Log.d("UserStats", "FCM Token successfully registered for $uid")
        } catch (e: Exception) {
            Log.e("UserStats", "Failed to save FCM token", e)
        }
    }
}
