# 🤖 Android Workspace Overview (Native)
![Version](https://img.shields.io/badge/version-1.0-blue.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Compose_BOM-2024.02.01-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Android](https://img.shields.io/badge/Android_SDK-34-3DDC84?style=for-the-badge&logo=android&logoColor=white)




- **Architecture**: MVVM + Clean Architecture + Koin

**Modules**:
- `:app`: Main application
- `:core`: Shared logic (optional)

**Note**: `ui/theme/Color.kt` (in this repo) is **auto-generated**.

To update colors, you must modify the source of truth in the **[JarWise-Root](https://github.com/oatrice/JarWise-Root)** repository:
1.  **Source File**: `tokens/colors.json` (In **JarWise-Root**)
2.  **Script**: `scripts/sync_tokens.js` (In **JarWise-Root**)

Do not edit `Color.kt` directly.

## ✨ Features
*   **Currency Support**: Native currency management using DataStore Preferences and dynamic UI updates.
*   **Mobile Experience**: Optimized Dashboard layout with quick access to Import Slip and Settings.
*   **Transaction Management**: Add and view transactions (income, expense, transfer) with wallet and date selection.
*   **Jar Management**: Configure and manage financial jars, including editing names, icons, and percentages. Supports a hierarchical (parent-child) structure for complex allocation strategies.
*   **Authentication**: Secure account access with Google Sign-In.
*   **Data Backup & Restore**: Cloud-based backup and restore to safeguard your data.
*   **Data Migration**: Import your financial data from Money Manager.
*   **Financial Reports**: Visualize spending habits with an expense graph and access detailed reports.

## What to do:
1. Look at the commits - what was ACTUALLY changed?
2. Find the MINIMAL section in README that relates to these changes
3. Make ONLY the necessary text updates
4. If commits are about scripts/docs/sync → only update relevant sections
5. If no section needs updating → respond with EXACTLY "No updates needed"

Return the FULL README with MINIMAL changes (or "No updates needed"):