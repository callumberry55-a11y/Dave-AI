# Update Dave's Security Protocols

This task involves rotating the core developer identity credentials and enhancing the underlying security architecture to provide more robust verification layers.

## Proposed Changes

### [Core Security]

#### [ChatRepository.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/data/repository/ChatRepository.kt)

- Rotate `MASTER_DEV_ID` from `AXON_77_SIGMA` to `AXON_88_VANGUARD_SIGMA`.
- Update the `systemPrompt` to reference the new protocol.
- Update `identifyCandidateTask` and `handleDevVerifyTask` to recognize the new ID.
- Add logic for a secondary "Protocol Bypass" emergency code (`VANGUARD_EXTREME_99`).

```kotlin
// Change in ChatRepository.kt
private val MASTER_DEV_ID = "AXON_88_VANGUARD_SIGMA"
private val EMERGENCY_BYPASS_CODE = "VANGUARD_EXTREME_99"
```

#### [LandingScreen.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/ui/landing/LandingScreen.kt)

- Update the visual security log mention to reflect the new protocol deployment.

```diff
- "New cybersecurity protocol AXON_77_SIGMA deployed.",
+ "Neural Guard v88: AXON_VANGUARD protocol active.",
```

### [Security Infrastructure]

#### [SecurityRepository.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/data/repository/SecurityRepository.kt)

- Add support for storing and verifying a secondary recovery key.
- Enhance security logging to include protocol versioning.

## Verification Plan

### Automated Tests
- No specific automated tests for this logic, will verify via manual interaction.

### Manual Verification
- **Handshake Verification**: Start a chat with Dave and attempt to verify as Callum using the old `AXON_77_SIGMA` (should fail) and the new `AXON_88_VANGUARD_SIGMA` (should succeed).
- **Emergency Bypass**: Verify that the emergency code also grants architect access.
- **System Prompt Integrity**: Check Dave's response when asked about his security protocols to ensure he mentions the new version.
- **Logging**: Verify in the vault (or via ADB logs) that `DEV_HANDSHAKE_SUCCESS` is logged with the new protocol details.
