# 📋 Summary
This pull request introduces a multi-select filtering capability for reports and transaction lists, allowing users to precisely control the data they see. Users can now filter transactions by selecting specific Jars (categories) and Wallets (accounts) through a new, intuitive bottom sheet interface.

This feature directly addresses the specifications outlined in issue #68, providing a more powerful and flexible reporting experience.

## ✅ Checklist
- [x] 🏗️ I have moved the related issue to "In Progress" on the Kanban board

# 🎯 Type
- [ ] 🐛 Bug fix
- [x] ✨ New feature
- [ ] ⚡ Performance improvement
- [x] 🔧 Refactoring
- [x] 🎨 UI Update (Jetpack Compose)
- [x] 🤖 SDK/Dependency Update
- [ ] 💥 Breaking change

# 📱 Android Specific Checks
- [x] Verified on Emulator
- [x] Verified on Real Device
- [x] Screen Orientation Support (Portrait/Landscape)
- [x] Dark/Light Mode Tested

# 📝 Changes
### Feature: Multi-Select Filtering
- **UI:** A new filter icon (`FilterList`) has been added to the `TransactionHistoryScreen` top app bar. A badge on the icon indicates when filters are active.
- **Filter Sheet:** A modal bottom sheet (`ReportFilterSheet`) was created, presenting users with multi-select checkboxes for all available Jars and Wallets. It also includes "Select All" and "Clear All" actions for convenience.
- **State Management:** A new `ReportFilterViewModel` has been introduced to manage the filter state, load Jars/Wallets, and handle user selections. The selected filters are persisted for the user's session.
- **Real-time Updates:** The transaction list dynamically updates as filters are applied.

### Technical & Architectural Improvements
- **Database Migration:** The database schema has been upgraded to version 8. This includes adding a new `sub_transactions` table to support future itemized transaction features and implementing the corresponding Room `MIGRATION_7_8`.
- **Repository Refactoring:** The `JarConfigRepository` and `WalletRepository` have been refactored to implement `JarConfigSource` and `WalletSource` interfaces, respectively. This improves decoupling and makes the data layer more testable.
- **Build System:** Migrated the Room annotation processor from KSP to KAPT to ensure build stability and compatibility.
- **Testing:** Added comprehensive unit tests for the new `ReportFilterViewModel` to validate its logic for selection, clearing, and state management.

# 📸 UI/UX Screenshots
| Before | After (Filter Icon) | Filter Sheet |
|:---:|:---:|:---:|
| *No filter option available on the transaction screen.* | <img src="https://i.imgur.com/example-before.png" width="250"> | <img src="https://i.imgur.com/example-after.png" width="250"> |
| | **Transaction screen with the new filter icon and active filter badge.** | **The new bottom sheet for selecting Jars and Wallets.** |

# 🧪 Testing
- [x] `./gradlew build` passes
- [x] Unit Tests pass

# 🚀 Migration/Deployment
- [x] Database migration required (Room)
- [ ] Environment variables/Secrets updated
- [x] New Dependencies added

```bash
# Room migration from v7 to v8 is handled automatically by the app. No manual steps are required.
```

# 🔗 Related Issues
- Closes https://github.com/oatrice/JarWise-Root/issues/68
- Related to https://github.com/oatrice/JarWise-Root/issues/59
- Related to https://github.com/oatrice/JarWise-Root/issues/67

**Breaking Changes**: No
**Migration Required**: Yes