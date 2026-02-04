# 📋 Summary
This PR implements a complete data migration feature that allows users to import their transaction history from the Money Manager app (.mmbak backup and .xls export files). The feature includes a dedicated migration screen with file selection UI, API integration for uploading files to the backend migration service, and comprehensive error handling and logging throughout the migration process.

## ✅ Checklist
- [x] 🏗️ I have moved the related issue to "In Progress" on the Kanban board

# 🎯 Type
- [ ] 🐛 Bug fix
- [x] ✨ New feature
- [ ] ⚡ Performance improvement
- [ ] 🔧 Refactoring
- [ ] 🎨 UI Update (Jetpack Compose)
- [ ] 🤖 SDK/Dependency Update
- [ ] 💥 Breaking change

# 📱 Android Specific Checks
- [x] Verified on Emulator
- [ ] Verified on Real Device
- [x] Screen Orientation Support (Portrait/Landscape)
- [x] Dark/Light Mode Tested

# 📝 Changes

## New Features
- **Migration Screen**: Added a new dedicated screen (`MigrationScreen.kt`) with Material3 design for importing Money Manager data
  - File picker support for .mmbak (backup) and .xls (Excel) files
  - Real-time migration status updates (Idle, Loading, Success, Error states)
  - Clear instructions and user-friendly UI with proper error feedback
  - Navigation integration from Settings screen to Migration screen

- **API Integration**: Implemented complete API layer for migration
  - `MigrationApi.kt`: Retrofit interface for multipart file upload endpoint
  - `MigrationModels.kt`: Response models for migration and status tracking
  - `MigrationRepository.kt`: Repository layer handling file upload and error management
  - Network module configuration with OkHttp and Retrofit

- **ViewModel Architecture**: Created `MigrationViewModel.kt` with proper state management
  - File URI and filename tracking with StateFlow
  - Migration process orchestration
  - State reset functionality for navigation scenarios
  - Comprehensive error handling with user-friendly messages

## Infrastructure & Security Improvements
- **Network Security Configuration**: Added `network_security_config.xml` to enforce secure network policies
- **Enhanced Logging**: Expanded `AppLogger.kt` with dedicated API and migration logging utilities
- **Dependency Management**: Added Retrofit, OkHttp, and Gson libraries for network operations

## UI/UX Enhancements
- Settings screen now includes navigation button to Migration feature
- Dashboard navigation from migration screen after successful import
- Proper back navigation with state cleanup
- Loading indicators and success/error feedback

## Version Bump
- Updated version from 1.7.0 to 1.8.0 to reflect the new migration feature

# 📸 UI/UX Screenshots
<!-- Screenshots to be added showing:
- Settings screen with new Migration button
- Migration screen with file selection
- Success state after migration
- Error state with user-friendly messages
-->

# 🧪 Testing
- [x] `./gradlew build` passes
- [ ] Unit Tests pass

## Manual Testing Performed
- File selection for both .mmbak and .xls files
- Migration API call with proper multipart upload
- Error handling for network failures
- State management across navigation
- UI responsiveness and Material3 theming

# 🚀 Migration/Deployment
- [ ] Database migration required (Room)
- [ ] Environment variables/Secrets updated
- [x] New Dependencies added

## New Dependencies Added
```gradle
// Networking
implementation(libs.retrofit)
implementation(libs.converter.gson)
implementation(libs.okhttp)
implementation(libs.logging.interceptor)
```

## Backend Requirements
This feature requires the backend API endpoint:
- `POST /api/v1/migrations/money-manager` - Accepts multipart/form-data with .mmbak and .xls files
- Response should include `job_id`, `status`, and `message` fields

# 🔗 Related Issues
- Closes https://github.com/oatrice/JarWise-Root/issues/65

**Breaking Changes**: No  
**Migration Required**: No (only new dependencies and optional feature)