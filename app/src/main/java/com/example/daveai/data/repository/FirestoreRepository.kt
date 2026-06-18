package com.example.daveai.data.repository

import android.util.Log
import com.example.daveai.data.db.ChatMessageEntity
import com.example.daveai.data.db.ChatSessionEntity
import com.example.daveai.data.db.RelationshipEntity
import com.example.daveai.data.db.SemanticMemory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun syncSession(uid: String, session: ChatSessionEntity) {
        try {
            db.collection("users").document(uid)
                .collection("sessions").document(session.sessionId)
                .set(session, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Failed to sync session", e)
        }
    }

    suspend fun syncMessage(uid: String, sessionId: String, message: ChatMessageEntity) {
        try {
            // We use a generated ID or the Room ID if it's stable
            val messageId = if (message.id != 0) message.id.toString() else null
            val ref = if (messageId != null) {
                db.collection("users").document(uid)
                    .collection("sessions").document(sessionId)
                    .collection("messages").document(messageId)
            } else {
                db.collection("users").document(uid)
                    .collection("sessions").document(sessionId)
                    .collection("messages").document()
            }
            ref.set(message, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Failed to sync message", e)
        }
    }

    suspend fun syncSemanticMemory(uid: String, memory: SemanticMemory) {
        try {
            db.collection("users").document(uid)
                .collection("semantic_memories").document(memory.id.toString())
                .set(memory, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Failed to sync memory", e)
        }
    }

    suspend fun syncRelationship(uid: String, relationship: RelationshipEntity) {
        try {
            db.collection("users").document(uid)
                .collection("relationship").document("ledger")
                .set(relationship, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Failed to sync relationship", e)
        }
    }

    suspend fun deleteSession(uid: String, sessionId: String) {
        try {
            // Note: Deleting a document does not delete its subcollections in Firestore.
            // We'd need to delete messages manually if we really want to clean up.
            db.collection("users").document(uid)
                .collection("sessions").document(sessionId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Failed to delete session from Firestore", e)
        }
    }
}
