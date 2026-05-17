# Project Plan

Implement Text-to-Speech (TTS) for Dave AI with a male voice. Add a toggle in the UI to enable/disable Dave's voice. Verify all features (Claude, Drive, Maps, YouTube, Persistence, TTS) at the end.

## Project Brief

# Project Brief: Dave AI

Dave AI is an advanced Android assistant that serves as a personalized intelligence hub. It combines the reasoning capabilities of Claude AI with essential productivity, media, and location services, all delivered through a persistent, voice-enabled, and adaptive Material 3 interface.

## Features
*   **Persistent AI Assistant with Memory:** A chat-based interface powered by Claude AI, utilizing a local Room database to store full conversation history and provide long-term contextual memory.
*   **Dave's Voice (TTS):** Integrated Android native Text-to-Speech functionality allowing the assistant to respond with a male voice profile, featuring a dedicated UI toggle for voice output control.
*   **Integrated Google Ecosystem:** Seamless access to Google Drive for file management, Google Maps for location-based context, and the YouTube Data API for media discovery and trending content.
*   **Adaptive Material 3 Experience:** A fluid, state-driven UI that optimizes layouts for phones, foldables, and tablets using the Compose Material Adaptive library and Jetpack Navigation 3.

## High-Level Technical Stack
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose with Material Design 3
*   **Navigation & Adaptive:** Jetpack Navigation 3 (State-driven) and Compose Material Adaptive library
*   **Persistence:** Room Database (for chat history and assistant context)
*   **Audio:** Android TextToSpeech (TTS) API
*   **APIs & SDKs:** Claude AI API, Google Drive API, Google Maps SDK, and YouTube Data API
*   **Networking:** Retrofit & OkHttp
*   **Concurrency:** Kotlin Coroutines & Flow

## Implementation Steps
**Total Duration:** 120h 46m 24s

### Task_1_API_Integration: Setup networking and data layer for Claude AI integration.
- **Status:** COMPLETED
- **Updates:** Fixed Claude model name to 'claude-opus-4-5-20251101' and handled Drive 403 error with a user-friendly message.
- **Acceptance Criteria:**
  - Retrofit service for Claude API implemented
  - Data models for chat requests/responses defined
  - Repository for AI communication created
  - API Key integrated securely
- **Duration:** 20h 1m 17s

### Task_2_Chat_UI_Navigation: Complete the conversational chat interface and state-driven navigation.
- **Status:** COMPLETED
- **Updates:** Completed Task_2_Chat_UI_Navigation.
- **Acceptance Criteria:**
  - Message bubble UI created with Jetpack Compose
  - ViewModel manages chat history and API calls
  - Navigation 3 implemented for app flow
  - Edge-to-edge display enabled
- **Duration:** 10h 9m 58s

### Task_3_Google_Integrations: Integrate Google Drive API and Google Maps SDK.
- **Status:** COMPLETED
- **Updates:** Completed Task_3_Google_Integrations.
- **Acceptance Criteria:**
  - Google Drive API implemented with key AIzaSyBfLurTk8y2DzYkREEvvg4YtsFQX1WMXOA
  - Google Maps SDK implemented with key AIzaSyAs9v4ETjCopzwxecJG6i336mHcBo31S1Y
  - File browsing and location visualization features added
- **Duration:** 10h 9m 14s

### Task_4_Adaptive_M3_Refinement: Apply adaptive layouts and Material 3 vibrant styling.
- **Status:** COMPLETED
- **Updates:** Completed Task_4_Adaptive_M3_Refinement.
- **Acceptance Criteria:**
  - Material 3 color scheme (vibrant/energetic) implemented
  - Adaptive layout (List-Detail/Pane) for different screen sizes added
  - Adaptive app icon created
  - Full Material 3 aesthetic applied
- **Duration:** 10h 4m 42s

### Task_5_Final_Verification: Run and verify the complete application.
- **Status:** COMPLETED
- **Updates:** Final verification passed.
- **Acceptance Criteria:**
  - App builds and runs without crashes
  - Claude AI, Drive, and Maps integrations working correctly
  - UI follows Material 3 and adaptive guidelines
  - All existing tests pass
  - App does not crash
- **Duration:** 30h 10m 39s

### Task_6_Persistence_And_Logic: Implement Room persistence and update business logic for chat context.
- **Status:** COMPLETED
- **Updates:** Completed Task_6_Persistence_And_Logic.
- **Acceptance Criteria:**
  - Room database setup for message persistence
  - ChatRepository updated to sync with Room and include history in AI calls
  - ChatViewModel handles history loading and state updates
- **Duration:** 10h 3m 58s

### Task_7_Chat_Management_UI_And_Verify: Add chat management UI controls and perform final verification.
- **Status:** COMPLETED
- **Updates:** Completed Task_7_Chat_Management_UI_And_Verify.
- **Acceptance Criteria:**
  - 'New Chat' and 'Delete Chat' buttons added to UI
  - Chat history persists across app restarts
  - Claude AI correctly uses conversation context
  - App builds successfully and does not crash
  - All existing tests pass
- **Duration:** 10h 1m 21s

### Task_8_YouTube_Integration: Integrate YouTube Data API and implement a video discovery screen.
- **Status:** COMPLETED
- **Updates:** Completed Task_8_YouTube_Integration.
- **Acceptance Criteria:**
  - YouTube Data API service implemented with key AIzaSyCZKD261yPoc95xXpQw5NF1uoNcoKwzDXs
  - Video search and listing UI created using Jetpack Compose
  - Navigation updated to include YouTube screen
  - Video metadata (title, thumbnails) displayed correctly
- **Duration:** 10h 3m 22s

### Task_9_TTS_Implementation: Implement Dave's Voice (TTS) with a male voice and UI toggle.
- **Status:** COMPLETED
- **Updates:** Completed Task_9_TTS_Implementation.
- Integrated Android TextToSpeech API.
- Implemented `TtsManager` for voice control.
- Added a male voice selection logic.
- Integrated a voice toggle button in the Chat UI.
- Assistant responses are spoken when TTS is enabled.
- **Acceptance Criteria:**
  - Android TextToSpeech API integrated
  - Male voice profile configured for Dave
  - UI toggle in Chat screen to enable/disable voice output
  - Assistant responses are spoken aloud when enabled
- **Duration:** 10h 1m 53s

### Task_10_Final_System_Verification: Comprehensive run and verify of all integrated features.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - App builds and runs without crashes
  - Claude, Drive, Maps, YouTube, and Persistence verified
  - TTS functionality and UI toggle verified
  - UI remains adaptive and follows M3 guidelines
  - All existing tests pass
- **StartTime:** 2026-05-16 22:19:47 BST

