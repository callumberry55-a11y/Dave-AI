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
)

class UserStatsRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun trackUserLogin(uid: String, email: String?, attribution: Map<String, String?>) {
        try {
            val userRef = db.collection("users").document(uid)
            val userDoc = userRef.get().await()

            if (!userDoc.exists()) {
                val userData = mutableMapOf(
                    "email" to email,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "lastLogin" to FieldValue.serverTimestamp(),
                    "role" to "Elite User",
                    "displayName" to (email?.split("@")?.get(0) ?: "Dave Fan"),
                )
                
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
                )
            } else null
        } catch (_: Exception) { null }
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

    fun observeTotalUserCount(): Flow<Long> = callbackFlow {
        val statsRef = db.collection("stats").document("global")
        val subscription = statsRef.addSnapshotListener { snapshot, _ ->
            val count = snapshot?.getLong("totalUsers") ?: 0L
            trySend(count)
        }
        awaitClose { subscription.remove() }
    }
}
