# 🤖 Android Workspace Overview (Native)
![Version](https://img.shields.io/badge/version-1.0-blue.svg)

**Tech Stack**:
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white) 
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)

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
