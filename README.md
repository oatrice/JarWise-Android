# 🤖 Android Workspace Overview (Native)

**Tech Stack**:
- Framework: Jetpack Compose
- Language: Kotlin
- Architecture: MVVM + Clean Architecture

**Modules**:
- `:app`: Main application
- `:core`: Shared logic (optional)

**Note**: `ui/theme/Color.kt` (in this repo) is **auto-generated**.

To update colors, you must modify the source of truth in the **[JarWise-Root](https://github.com/oatrice/JarWise-Root)** repository:
1.  **Source File**: `tokens/colors.json` (In **JarWise-Root**)
2.  **Script**: `scripts/sync_tokens.js` (In **JarWise-Root**)

Do not edit `Color.kt` directly.
