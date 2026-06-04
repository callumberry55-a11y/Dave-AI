# Background Blur on Sidebar Open

Add a background blur effect to the main content when the navigation drawer (sidebar) is open. This will enhance the "glass" aesthetic of the application and improve focus on the sidebar items.

## Proposed Changes

### UI Components

#### [ChatScreen.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/ui/chat/ChatScreen.kt)

- Add necessary imports: `androidx.compose.ui.draw.blur`, `androidx.compose.animation.core.animateDpAsState`, `androidx.compose.animation.core.tween`.
- Define a `blurRadius` state that animates based on `drawerState.targetValue`.
- The maximum blur will be 16dp, scaled by `uiState.blurIntensity`.
- Apply `Modifier.blur(blurRadius)` to the `Scaffold` (main content).

```kotlin
val blurRadius by animateDpAsState(
    targetValue = if (drawerState.targetValue == DrawerValue.Open) (16 * uiState.blurIntensity).dp else 0.dp,
    animationSpec = tween(durationMillis = 300),
    label = "background_blur"
)

// ... inside ModalNavigationDrawer content:
Scaffold(
    modifier = Modifier
        .fillMaxSize()
        .blur(radius = blurRadius),
    // ...
)
```

#### [RiddleScreen.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/ui/riddle/RiddleScreen.kt)

- Apply the same logic as in `ChatScreen.kt`.
- Use `uiState.blurIntensity` for the max radius.

> [!NOTE]
> `Modifier.blur` only works on Android 12 (API 31) and above. On older devices, the content will remain sharp, which is the standard fallback behavior of Compose.

## Verification Plan

### Manual Verification
1. Open the sidebar in the Chat screen.
2. Observe if the background (conversation, input field, etc.) blurs smoothly.
3. Close the sidebar and ensure the blur disappears.
4. Repeat for the Riddle screen.
5. Adjust the "NEURAL BLUR" slider in the sidebar and verify it affects the background blur intensity.
