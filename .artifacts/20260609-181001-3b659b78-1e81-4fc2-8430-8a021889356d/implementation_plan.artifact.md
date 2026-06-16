# Consolidate project to Single Package Identity

Unify the app under a single package name (`com.example.daveai`) to match the Firebase project and prevent resource waste.

## Proposed Changes

### [Build System]

#### [build.gradle.kts](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/build.gradle.kts)

- Remove the `developer` product flavor.
- Remove `flavorDimensions` if no longer needed.
- Keep a single `public` flavor or just move its configuration to `defaultConfig`.

### [Manifest]

#### [AndroidManifest.xml](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/AndroidManifest.xml)

- Remove the `<queries>` entry for `com.example.daveai.beta`.
- Use fixed strings for `intelligenceAuthority` if they are no longer dynamic.

### [Code Refactoring]

#### [ChatRepository.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/data/repository/ChatRepository.kt)

- Remove `importDeveloperIntelligence` logic that attempted to sync with a beta version of the app.
- Simplify package name checks.

#### [DaveNotificationService.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/service/DaveNotificationService.kt)

- Remove `checkInterIntelligence` logic as there is no longer a second app to communicate with.

## Verification Plan

### Automated Tests
- None.

### Manual Verification
- **Build**: Run `./gradlew assembleDebug` and ensure it builds without flavor errors.
- **Firebase Initialization**: Launch the app and verify that "Fetching FCM registration token" succeeds and no "invalid google_app_id" errors appear in Logcat.
- **Auth**: Verify that Google Login and Email Login still work correctly under the unified identity.
