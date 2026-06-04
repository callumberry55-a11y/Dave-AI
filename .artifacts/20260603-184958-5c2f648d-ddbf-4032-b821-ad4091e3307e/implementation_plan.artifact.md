# Add Playback Button for Dave's TTS

This plan outlines the changes to replace automatic TTS playback with a manual playback button in the Chat screen, while preserving autoplay for Live Voice mode.

## Proposed Changes

### [Data Layer]

#### [ChatRepository.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/data/repository/ChatRepository.kt)

- Expose `isSpeaking` state flow from `voiceManager`.
- Add `suspend fun speak(text: String)` to trigger TTS.
- Add `fun stopSpeaking()` to stop TTS.
- (Optional) Change `sendMessage` default `muteVoice` to `true` to encourage manual trigger, but I'll stick to explicit passing in ViewModel to be safe.

### [UI Layer]

#### [ChatViewModel.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/ui/chat/ChatViewModel.kt)

- Add `isSpeaking: Boolean` to `ChatUiState`.
- In `init`, observe `repository.isSpeaking` and update `ChatUiState`.
- Add `fun speak(text: String)` that launches a coroutine calling `repository.speak(text)`.
- Add `fun stopSpeaking()` calling `repository.stopSpeaking()`.
- Update `sendMessage()` signature to accept `muteVoice: Boolean = false`.

#### [ChatScreen.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/ui/chat/ChatScreen.kt)

- Update all calls to `viewModel.sendMessage()` in `ChatScreen` to pass `muteVoice = true`.
- In `MessageBubble`, add a playback `IconButton` for Dave's messages.
- The button will call `viewModel.speak(message.content)`.
- Use a "speaker" icon (e.g., `Icons.Rounded.VolumeUp`).

## Verification Plan

### Automated Tests
- I'll check if there are existing tests for `ChatViewModel` or `ChatRepository`.

### Manual Verification
- Deploy the app.
- Send a message in the chat and verify Dave does NOT autoplay the response.
- Click the new playback button on Dave's response and verify he speaks.
- Open Live Voice mode and verify he STILL autoplays responses there (as `muteVoice` will default to `false` in `ChatViewModel.sendMessage()`).
