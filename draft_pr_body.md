# 📋 Summary
This Pull Request introduces a comprehensive Financial Reports feature for both Web and Android platforms. Users can now view various financial reports including summary, trend, and breakdown by category and jar, complete with interactive charts and customizable date range filtering. Additionally, the feature includes the ability to export report data to CSV format.

## ✅ Checklist
- [x] 🏗️ I have moved the related issue to "In Progress" on the Kanban board

# 🎯 Type
- [ ] 🐛 Bug fix
- [x] ✨ New feature
- [ ] ⚡ Performance improvement
- [ ] 🔧 Refactoring
- [x] 🎨 UI Update (Jetpack Compose)
- [ ] 🤖 SDK/Dependency Update
- [ ] 💥 Breaking change

# 📱 Android Specific Checks
- [ ] Verified on Emulator
- [ ] Verified on Real Device
- [ ] Screen Orientation Support (Portrait/Landscape)
- [ ] Dark/Light Mode Tested

# 📝 Changes
This PR implements the Financial Reports and Data Export feature as outlined in Issue #59.

**Key Changes Include:**

*   **Financial Reports Screen:** A new screen to display various financial insights.
*   **API Integration:** Added `ReportApi`, `ReportDto`, and `ReportRepository` for fetching report data from the backend.
*   **Dependency Injection:** Integrated `ReportApi` and `ReportRepository` into Koin modules (`NetworkModule`, `RepositoryModule`, `ViewModelModule`).
*   **Report Data Display:**
    *   Summary section showing total income, expense, and net balance.
    *   Trend chart visualizing income and expense over time using a line chart.
    *   Breakdown charts by category and jar, typically represented as pie charts, showing proportional distribution of finances.
    *   Comparison data for current vs. previous periods.
*   **Date Range Filtering:** Implemented flexible date range selection, including:
    *   Predefined ranges: Month, Quarter, Year, 7-day, 30-day, All.
    *   Custom date range selection via a date picker.
*   **CSV Data Export:** Added functionality to export the displayed report data into a CSV file.
*   **UI/UX Enhancements:**
    *   Improved chart axis scaling, formatting, and legends for better readability.
    *   Updated `BottomNav` component to accept a modifier parameter.
    *   Visual updates to report cards and overall layout using Jetpack Compose.
*   **Version Bump:** Incremented the Android app's `versionName` to `1.12.0`.
*   **Unit Tests:** Added unit tests for `ReportsViewModel` to ensure data fetching and processing logic is correct.

# 📸 UI/UX Screenshots
<!-- Include screenshots from the Android device/emulator. MUST include screenshots for UI changes. -->
(Please add screenshots here, showing different report views, date range selections, and possibly the export prompt)

# 🧪 Testing
- [x] `./gradlew build` passes
- [x] Unit Tests pass

# 🚀 Migration/Deployment
- [ ] Database migration required (Room)
- [ ] Environment variables/Secrets updated
- [ ] New Dependencies added

```bash
# Migration commands if applicable
```

# 🔗 Related Issues
- Closes https://github.com/oatrice/JarWise-Root/issues/59

**Breaking Changes**: No
**Migration Required**: No