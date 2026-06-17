# Fix Vocal Synth Cancellation and Manifest Attribution Errors

This plan addresses the `JobCancellationException` occurring during Dave's vocal synthesis and the `attributionTag` error appearing in the system logs. It also adds a filter to prevent Dave from attempting to "speak" UI button tags.

## Proposed Changes

### Vocal Synthesis & UI Filtering

#### [DaveVoiceManager.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/util/DaveVoiceManager.kt)

- Update `speak` to handle `CancellationException` gracefully within the `speakJob`.
- Modify `speak` (or the input to it) to filter out `[BUTTON: ...]` tags, as these are UI elements and shouldn't be synthesized as speech.

```kotlin
// In speak()
val cleanText = text.replace(Regex("\\[BUTTON:.*?\\]"), "").trim()
if (cleanText.isEmpty()) return@withContext

// Inside speakJob launch
try {
    // ... fetching logic ...
} catch (e: CancellationException) {
    Log.d("DaveVoice", "Vocal sequence cancelled. Focus shift or new request detected.")
    throw e // Re-throw to ensure job state is correct
} catch (e: Exception) {
    Log.e("DaveVoice", "Vocal synth error", e)
}
```

### Android Manifest

#### [AndroidManifest.xml](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/AndroidManifest.xml)

- Update the `<attribution>` tag to match the `attributionTag` expected by the system (the package name or a specific tag declared in the manifest). The error `attributionTag not declared` suggests the current tag `"DaveAI"` is not recognized or needs to be associated with specific components. We will change it to `${appPackageName}` to match the manifest placeholder.

```xml
<attribution android:tag="com.example.daveai" android:label="@string/app_name" />
```

## Verification Plan

### Automated Tests
- No automated tests for this, as it involves hardware synthesis and system-level logging.

### Manual Verification
1. **Vocal Synthesis**:
    - Trigger Dave to speak a response that includes `[BUTTON:]` tags.
    - Verify that the buttons are **not** spoken.
    - Verify that no `Vocal synth error` or `JobCancellationException` is logged when Dave starts a new response while still speaking the previous one.
2. **Manifest Logs**:
    - Monitor `logcat` for `attributionTag not declared` errors.
    - Verify the error no longer appears after the manifest update.
