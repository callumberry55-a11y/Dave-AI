# Personalized Dev ID Registration Flow

This update allows users to create their own unique "Axon ID" (Developer ID) which Dave will recognize for ARCHITECT MODE. This provides a more personal and secure way for users to manage their system credentials.

## Proposed Changes

### [User Data Infrastructure]

#### [UserStatsRepository.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/data/repository/UserStatsRepository.kt)

- Add `setDevId(uid: String, devId: String)` to persist the custom ID to Firestore.
- Update `trackUserLogin` to ensure `devId` is retrieved during profile fetch.

```kotlin
suspend fun setDevId(uid: String, devId: String) {
    try {
        db.collection("users").document(uid).update("devId", devId).await()
    } catch (e: Exception) {
        Log.e("UserStats", "Failed to set devId", e)
    }
}
```

### [Agentic Logic]

#### [ChatRepository.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/data/repository/ChatRepository.kt)

- **New Task**: Add `DaveTask.CREATE_DEV_ID`.
- **Trigger**: Update `identifyCandidateTask` to catch "axon id", "create my dev id", or "register my id".
- **Handler**: Implement `handleCreateDevIdTask(sessionId, uid)`:
    - Generate a random 13-character string with prefix `AXON_` and suffix `_SIGMA`.
    - Store it in Firestore via `userStatsRepository`.
    - Respond to the user with the new ID and instructions to save it.
- **Verification Update**: Modify `handleDevVerifyTask` to also check the `userProfile?.devId` if the hardcoded ones fail.

```kotlin
// Example handler logic:
private suspend fun handleCreateDevIdTask(sessionId: String, uid: String?): String {
    val uidValue = uid ?: return "You need to be logged in to register a Dev ID, boss. 🛡️"
    val randomId = "AXON_" + (1000..9999).random() + "_VANGUARD_" + (10..99).random() + "_SIGMA"
    userStatsRepository.setDevId(uidValue, randomId)
    val msg = "NEW CREDENTIAL GENERATED: Your personalized Axon ID is now **$randomId**. Save this signature. Use 'Verify identity: $randomId' to enter ARCHITECT MODE anytime. 🛠️⚡️"
    chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
    return msg
}
```

## Verification Plan

### Automated Tests
- None, verified via manual interaction.

### Manual Verification
- **Creation Flow**: Say "axon id" to Dave. Verify he generates a new ID and confirms storage.
- **Verification Flow**: Log out/reset and then type "Verify identity: [NEW_ID]". Dave should recognize it and grant access.
- **Profile Integrity**: Verify in Firestore (or via logs) that the `devId` field is correctly updated for the user.
