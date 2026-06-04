# Dave's Evolution: Integrated Intelligence, Memory & Navigation

This comprehensive plan upgrades Dave across five dimensions:
1.  **Expanded External Intelligence**: Integrating **Gemini Pro/Flash** and **PoetryDB** as cloud sources.
2.  **Semantic Memory Optimization**: Implementing **Neural Consolidation** and **Temporal Conflict Resolution** to make local memory more efficient.
3.  **Modern Thinking Indicator**: Replacing the static "Dave is thinking" text with a **Dynamic Neural Status** system that shows real-time progress (e.g., "QUERYING GEMINI...", "ANALYZING SHAKESPEARE...").
4.  **University Restore**: Fixing broken access and adding a dedicated sidebar button for **Dave University**.
5.  **The Sanctum Enhancement**: Exposing memory health metrics in the Sanctum UI.

## Proposed Changes

### [Network Layer](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/data/network)

#### [NEW] [GeminiApiService.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/data/network/GeminiApiService.kt)
- Interface for Google Gemini API for high-level reasoning and broad "information pulling".

#### [NEW] [PoetryDbApiService.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/data/network/PoetryDbApiService.kt)
- Interface for `poetrydb.org` to fetch classic literature by Shakespeare, Dickinson, etc.

---

### [Repository Layer](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/data/repository)

#### [ChatRepository.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/data/repository/ChatRepository.kt)
- **Service Injection**: Inject `GeminiApiService` and `PoetryDbApiService`.
- **Task Routing**: Detect intent for "Gemini search" or "Poetry retrieval" in `routeEliteTask`.
- **Memory Optimization**:
    - Implement `resolveTemporalConflicts` to archive old, contradicting memories.
    - Implement `consolidateMemories` to merge redundant entries.
- **Thinking State**: Expose a `Flow<String>` of current thinking status (e.g., "NEURAL_LINK_ESTABLISHED", "FETCHING_POETRY").

---

### [UI Layer](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/ui)

#### [ChatScreen.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/ui/chat/ChatScreen.kt)
- **Thinking Indicator**: Update `DaveIsTypingIndicator` to observe the new dynamic thinking status from the ViewModel.
- **Sidebar**: Add "Dave University" as a primary Navigation Drawer item.
- **Project Tab**: Fix "Learn" button to call `onEnterLessons` navigation callback.

#### [SanctumScreen.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/ui/sanctum/SanctumScreen.kt)
- Add a **"Neural Coherence"** metric card reflecting the health/count of active semantic memories.

#### [MainActivity.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/MainActivity.kt)
- Wire up the `onEnterLessons` navigation to the `DaveRoute.Lessons` destination.

## Verification Plan

### Automated Tests
- `./gradlew assembleDebug` to ensure no regression in build stability.

### Manual Verification
1.  **Intelligence**: Ask Dave "Pull info from Gemini about Quantum Computing" and verify the thinking indicator shows "QUERYING GEMINI...".
2.  **Poetry**: Ask for a specific Emily Dickinson poem and verify the indicator shows "CONSULTING POETRY_DB...".
3.  **Memory**: Mention a preference, then change it. Verify in logs that conflict resolution is triggered.
4.  **Navigation**: Open the sidebar, click "Dave University", and ensure it navigates to the Lessons screen.
5.  **Thinking Indicator**: Verify the indicator text is dynamic and reflects the current task accurately.
