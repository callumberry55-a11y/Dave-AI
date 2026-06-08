# Option 4: UI & Aesthetic Enhancements Implementation Plan

Transform the `LandingScreen` into a functional Dashboard Hub and introduce the "Aura Marketplace" for advanced theme and persona management.

## User Review Required

- **Dashboard Layout**: Should the widgets be fixed or user-reorderable? (Starting with fixed).
- **Aura Presets**: Any specific community themes you'd like to see pre-installed? (e.g., "Cyberpunk", "Minimalist").

## Proposed Changes

### Dashboard Hub

#### [LandingScreen.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/ui/landing/LandingScreen.kt)

- Refactor `LandingScreen` to include a scrollable list of interactive widgets below the orbiting elements.
- Integrate existing data like hardware stats, current news, and weather into these widgets.
- Add a "Quick Action" section for one-tap system controls (Flashlight, DND, etc.).

#### [NEW] [DashboardWidgets.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/ui/landing/DashboardWidgets.kt)

- Create modular widget components: `NewsWidget`, `FinanceWidget`, `SystemStatsWidget`, `WeatherWidget`.
- Ensure widgets match the "Glass" aesthetic used in the rest of the app.

---

### Aura Marketplace & Personality Editor

#### [GlassSidebar.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/ui/components/GlassSidebar.kt)

- Add "Aura Marketplace" and "Digital Persona" as interactive items under the "AURA CONFIGURATION" section.
- Use distinct icons (e.g., `Icons.Rounded.Store` and `Icons.Rounded.Face`).

#### [ChatScreen.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/ui/chat/ChatScreen.kt) & [RiddleScreen.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/ui/riddle/RiddleScreen.kt)

- Update `GlassSidebar` callbacks to include `onEnterMarketplace` and `onEnterPersonaEditor`.
- Wire these callbacks to the navigation controller in `MainActivity`.

#### [MainActivity.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/MainActivity.kt)

- Define navigation logic for `DaveRoute.AuraMarketplace` and `DaveRoute.PersonalityEditor`.

#### [NEW] [AuraMarketplaceScreen.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/ui/aura/AuraMarketplaceScreen.kt)

- A screen to browse and apply predefined Aura presets.
- Each preset modifies: `primaryColor`, `meshAnimationSpeed`, `glowStrength`, `blurIntensity`, and `digitalPersona`.

#### [NEW] [PersonalityEditorScreen.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/ui/aura/PersonalityEditorScreen.kt)

- A granular editor for the `digitalPersona` string.
- Sliders for "Sarcasm", "Technical Depth", and "Empathy" that Dave uses to adjust his response style.

---

### Navigation & Data

#### [DaveRoute.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/ui/navigation/DaveRoute.kt)

- Add `AuraMarketplace` and `PersonalityEditor` objects to `DaveRoute`.

#### [ChatViewModel.kt](file:///C:/Users/PaulB/AndroidStudioProjects/DaveAI/app/src/main/java/com/example/daveai/ui/chat/ChatViewModel.kt)

- Add functions to apply full Aura presets.
- Add granular state updates for personality traits.

## Verification Plan

### Manual Verification
1. **Dashboard**: Navigate to the Landing Screen and verify widgets display live or mock data correctly.
2. **Preset Application**: Apply a "Cyberpunk" preset from the Marketplace and ensure all UI elements and background animations update instantly.
3. **Personality Shift**: Change sarcasm levels in the Editor and verify Dave's responses in the Chat screen reflect the change.
4. **Persistence**: Restart the app and ensure selected Aura and Dashboard settings remain.
