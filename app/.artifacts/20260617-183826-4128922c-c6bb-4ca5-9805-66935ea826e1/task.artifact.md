# Task: Fix Vocal Synth and Manifest Errors

- [x] Research Chat Implementation
	- [x] Analyze `ChatViewModel.kt` and `ChatRepository.kt`
	- [x] Investigate notification and bubble logic in `DaveNotificationManager.kt`
	- [x] Trace navigation in `MainActivity.kt` and `ChatRoomActivity.kt`
- [x] Implement Fixes
	- [x] Update `ChatViewModel.kt` to observe messages for new sessions
	- [x] Disable auto-expanding bubbles in `DaveNotificationManager.kt`
	- [x] Handle `sessionId` in `MainActivity.kt` for direct navigation
- [x] Verification
	- [x] Verify message visibility in new sessions
	- [x] Verify notification navigation works correctly
	- [x] Verify bubbles no longer auto-expand
- [/] Fix Vocal Synth and Manifest Errors
	- [x] Analyze `JobCancellationException` in `DaveVoiceManager.kt`
	- [x] Analyze `attributionTag` error in `AndroidManifest.xml`
	- [/] Implement fixes for vocal synth and manifest
	- [ ] Verify error reduction in logs
