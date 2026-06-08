# Fix Dave Spamming Messages

Dave was reportedly spamming friends with multiple messages in a short period. Research indicates this is likely caused by the `DaveNotificationService` auto-reply feature, which triggers a response for every notification received without a cooldown period. If a conversation is fast-paced, Dave might respond to every single line, leading to the observed behavior.

## Proposed Changes

### [Notification Service]

#### [DaveNotificationService.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/service/DaveNotificationService.kt)

- Implement a rate-limiting mechanism using a simple `HashMap` to track the last auto-reply timestamp for each package/sender.
- Set a cooldown period (e.g., 60 seconds) during which no new auto-replies will be sent to the same target.

```kotlin
// Example of the rate limiting logic to be added
private val lastReplyTimestamps = mutableMapOf<String, Long>()
private val COOLDOWN_MS = 60_000L // 1 minute

private fun canSendAutoReply(key: String): Boolean {
    val lastTime = lastReplyTimestamps[key] ?: 0L
    val now = System.currentTimeMillis()
    return (now - lastTime) > COOLDOWN_MS
}

// In attemptAutoReply:
val replyKey = "${sbn.packageName}_${title}"
if (!canSendAutoReply(replyKey)) {
    Log.d("DaveNotification", "Rate limiting auto-reply for $replyKey")
    return
}
// ... after sending:
lastReplyTimestamps[replyKey] = System.currentTimeMillis()
```

## Verification Plan

### Automated Tests
- Since this is a system-level service integration, automated unit tests for the rate-limiting logic itself will be added if a utility class is extracted, otherwise, it will be verified via manual simulation.

### Manual Verification
- Review the logic to ensure the `replyKey` (packageName + sender title) is specific enough to not block different conversations.
- Verify that the cooldown period is sufficient to prevent rapid-fire spamming while still allowing Dave to be helpful in subsequent interactions.
- Simulate (via code review/logic check) a burst of notifications and confirm only the first one triggers an auto-reply.
