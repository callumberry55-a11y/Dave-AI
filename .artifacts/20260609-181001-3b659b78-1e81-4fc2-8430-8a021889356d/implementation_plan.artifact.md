# Consolidate FCM Messaging Service

Incorporate the standard FCM notification handling logic into the existing `DaveMessagingService` while leveraging the specialized `DaveNotificationManager`.

## Proposed Changes

### [Messaging Service]

#### [DaveMessagingService.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/service/DaveMessagingService.kt)

- Refine `onMessageReceived` to ensure robust notification handling.
- It already uses `DaveNotificationManager.showGenericNotification`, which creates the necessary channel and builds the notification using `android.app.Notification.Builder`.

```kotlin
// DaveMessagingService.kt already has:
override fun onMessageReceived(remoteMessage: RemoteMessage) {
    // ...
    remoteMessage.notification?.let {
        notificationManager.showGenericNotification(
            it.title ?: "Dave AI Alert",
            it.body ?: ""
        )
    }
}
```

### [Notification Management]

#### [DaveNotificationManager.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/util/DaveNotificationManager.kt)

- Verify `showGenericNotification` uses the `default_notification_channel`. (Completed)
- Ensure the channel is created during initialization. (Completed)

## Verification Plan

### Manual Verification
- **Code Review**: Confirm `DaveMessagingService` is registered with the `com.google.firebase.MESSAGING_EVENT` intent filter in `AndroidManifest.xml`.
- **FCM Delivery**: Send a notification payload from the Firebase console.
- **Visual Check**: Verify the notification appears in the "Default Alerts" channel with the correct title and body.
