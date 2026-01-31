# PR Draft Prompt

You are an AI assistant helping to create a Pull Request description.
    
TASK: [Web | Android] Manage Jars (Edit %, Name, Icon)
ISSUE: {
  "title": "[Web | Android] Manage Jars (Edit %, Name, Icon)",
  "number": 17
}

GIT CONTEXT:
COMMITS:
8f77971 feat: [Web | Android] Manage Jars (Edit %, Name, Icon)...
8e1d756 ✨ feat(jar): add jar management feature
cb2e11e ✨ feat(test): add jar configuration test infrastructure
fb81229 ✨ feat(ui): add jar management screen and integrate with dashboard
d72cd6e ✨ feat(jars): add jar configuration management system
fffa67e feat: [Web | Android] Enhance Add Transaction (Date & Wa...
48e0cfc feat: [Web | Android] Enhance Add Transaction (Date & Wa...
b374223 ✨ feat(database): add transaction migration and testing infrastructure
a644602 ✨ feat(wallet): add wallet management and date selection
a0ab41f 📝 docs(changelog): Update changelog and version for 1.1.0 release
6348fb9 ✨ feat(ui): add preview composables for BottomNav and DashboardTopBar
65e8b7c 🛠️ refactor(ui): improve navigation and layout consistency
1ac946d ✨ feat(navigation): implement unified navigation system
01bd764 🎨 ui: Hide date in transaction cards
ef70a51 ✨ feat(ui): Add transaction grouping by date with daily totals
3bf3776 ✨ feat(transactions): add draft transaction functionality
120e631 docs: update CHANGELOG v1.0.0 and bump version
a265786 docs: update CHANGELOG with v1.0.0 - sample data update
7e3f6e1 ✨ feat(mock-data): Update mock data with new values

STATS:
.idea/gradle.xml                                   |   1 +
 .luma_rules.json                                   |  29 ++
 .luma_state.json                                   |  20 ++
 CHANGELOG.md                                       |  27 +-
 README.md                                          |   5 +-
 app/build.gradle.kts                               |  13 +-
 .../com.oatrice.jarwise.data.AppDatabase/2.json    |  70 +++++
 .../com.oatrice.jarwise.data.AppDatabase/3.json    |  76 ++++++
 .../com.oatrice.jarwise.data.AppDatabase/4.json    | 120 ++++++++
 .../java/com/oatrice/jarwise/data/MigrationTest.kt |  48 ++++
 .../main/java/com/oatrice/jarwise/MainActivity.kt  |  87 ++++--
 .../java/com/oatrice/jarwise/data/AppDatabase.kt   |  35 ++-
 .../com/oatrice/jarwise/data/GeneratedMockData.kt  |  17 +-
 .../java/com/oatrice/jarwise/data/JarConfig.kt     |  31 +++
 .../java/com/oatrice/jarwise/data/JarConfigDao.kt  |  35 +++
 .../java/com/oatrice/jarwise/data/Transaction.kt   |   6 +-
 .../com/oatrice/jarwise/data/TransactionDao.kt     |  13 +
 .../jarwise/data/repository/JarConfigRepository.kt |  53 ++++
 .../com/oatrice/jarwise/ui/AddTransactionScreen.kt | 138 +++++++++-
 .../java/com/oatrice/jarwise/ui/DashboardScreen.kt | 239 ++++------------
 .../java/com/oatrice/jarwise/ui/MainViewModel.kt   |  67 ++++-
 .../com/oatrice/jarwise/ui/SlipImportScreen.kt     |  38 ++-
 .../oatrice/jarwise/ui/TransactionHistoryScreen.kt | 225 ++++++++++-----
 .../com/oatrice/jarwise/ui/components/BottomNav.kt | 165 +++++++++++
 .../jarwise/ui/components/DashboardTopBar.kt       | 164 +++++++++++
 .../jarwise/ui/components/TransactionCard.kt       | 117 +++++---
 .../jarwise/ui/managejars/ManageJarsScreen.kt      | 301 +++++++++++++++++++++
 .../jarwise/ui/managejars/ManageJarsViewModel.kt   | 176 ++++++++++++
 .../jarwise/ui/utils/ScrollVisibilityState.kt      |  65 +++++
 .../java/com/oatrice/jarwise/utils/Constants.kt    |  25 +-
 .../jarwise/utils/TransactionGroupingUtils.kt      |  93 +++++++
 .../oatrice/jarwise/data/TransactionDraftTest.kt   |  79 ++++++
 .../jarwise/ui/managejars/FakeJarConfigDao.kt      |  54 ++++
 .../ui/managejars/ManageJarsViewModelTest.kt       | 146 ++++++++++
 .../oatrice/jarwise/utils/MainDispatcherRule.kt    |  23 ++
 .../jarwise/utils/TransactionGroupingUtilsTest.kt  | 105 +++++++
 draft_pr_prompt.txt                                |  68 +++++
 gradle/libs.versions.toml                          |   1 +
 scripts/run_tests.sh                               |   4 +-
 39 files changed, 2645 insertions(+), 334 deletions(-)

DIFF:
diff --git a/.idea/gradle.xml b/.idea/gradle.xml
index 97f0a8e..639c779 100644
--- a/.idea/gradle.xml
+++ b/.idea/gradle.xml
@@ -1,5 +1,6 @@
 <?xml version="1.0" encoding="UTF-8"?>
 <project version="4">
+  <component name="GradleMigrationSettings" migrationVersion="1" />
   <component name="GradleSettings">
     <option name="linkedExternalProjectsSettings">
       <GradleProjectSettings>
diff --git a/.luma_rules.json b/.luma_rules.json
new file mode 100644
index 0000000..528e8ca
--- /dev/null
+++ b/.luma_rules.json
@@ -0,0 +1,29 @@
+{
+    "project_name": "JarWise Android",
+    "version": "1.0",
+    "kanban": {
+        "project_number": 7,
+        "owner": "oatrice"
+    },
+    "preflight_checks": [
+        {
+            "id": "build_check",
+            "name": "Build Check",
+            "type": "command",
+            "command": "./scripts/build_android.sh assembleDebug",
+            "required": true,
+            "message": "Build must pass before creating PR"
+        }
+    ],
+    "context_rules": [
+        "ALWAYS use ./scripts/build_android.sh instead of ./gradlew directly",
+        "Use ./scripts/run_tests.sh for running tests",
+        "The wrapper scripts set the correct JAVA_HOME (Android Studio JDK)",
+        "Build command: ./scripts/build_android.sh assembleDebug",
+        "Test command: ./scripts/run_tests.sh"
+    ],
+    "context_sources": [
+        "README.md",
+        "docs/ARCHITECTURE.md"
+    ]
+}
\ No newline at end of file
diff --git a/.luma_state.json b/.luma_state.json
new file mode 100644
index 0000000..9849019
--- /dev/null
+++ b/.luma_state.json
@@ -0,0 +1,20 @@
+{
+  "version": "1.0",
+  "project_key": "3",
+  "phase": "coding",
+  "active_issue": {
+    "number": 17,
+    "title": "[Web | Android] Manage Jars (Edit %, Name, Icon)",
+    "html_url": "https://github.com/oatrice/JarWise-Root/issues/17",
+    "body": "# 🎯 Objective\nImplement the \"Manage Jars\" feature to allow users to customize their 6 Jars system by editing jar names, percentages, icons, and colors.\n\n## 📝 Specifications\n- [ ] **View Jar Details**: Tap on a jar to see full details and edit options\n- [ ] **Edit Jar Name**: Allow renaming jars (e.g., \"Necessities\" → \"Bills & Rent\")\n- [ ] **Edit Percentage**: Adjust allocation percentage with validation (total must = 100%)\n- [ ] **Edit Icon/Color**: Change jar icon and theme color\n- [ ] **Reset to Default**: Option to restore default 6 Jars configuration\n- [ ] **Persistence**: Save changes to local storage (mock) or backend\n\n## 🔗 References\n- **Feature ID**: `CORE-04` (See FEATURES.md)\n- **Design Reference**: Jar card style from Dashboard\n- **Default Jars**:\n  | Jar | Default % | Purpose |\n  | :--- | :---: | :--- |\n  | Necessities | 55% | Essential expenses |\n  | Education | 10% | Self-improvement |\n  | Savings | 10% | Long-term savings |\n  | Play | 10% | Entertainment |\n  | Investment | 10% | Financial growth |\n  | Give | 5% | Charity/gifts |\n\n## 🏗️ Implementation Guidelines\n- **Web**: Modal or dedicated settings page with form inputs\n- **Android**: Bottom sheet or separate Composable screen\n- **Validation**: Real-time % validation showing remaining allocation\n- **UX**: Show warning when total ≠ 100%, disable save until valid\n",
+    "project_item_id": "PVTI_lAHOATfKEM4BMuLizgjwwBg",
+    "project_id": "PVT_kwHOATfKEM4BMuLi",
+    "repository": "oatrice/JarWise-Root"
+  },
+  "active_branch": "feat/17-manage-jars",
+  "started_at": "2026-01-31T12:13:42.486236",
+  "checklist": {},
+  "pr_url": null,
+  "pr_number": null,
+  "last_updated": "2026-01-31T12:13:44.274667"
+}
\ No newline at end of file
diff --git a/CHANGELOG.md b/CHANGELOG.md
index 661f8d1..95f5c71 100644
--- a/CHANGELOG.md
+++ b/CHANGELOG.md
@@ -1,4 +1,29 @@
-# Android Changelog
+# Changelog
+
+## [1.3.0] - 2026-01-31
+### Added
+- **Jar Management**: Implemented a new system for users to create, manage, and save their financial "jars".
+- **UI**: Introduced a dedicated "Manage Jars" screen, accessible from the dashboard, to configure jars.
+
+## [1.2.0] - 2026-01-30
+### Added
+- **Wallet Management**: Users can now create and manage multiple wallets to better organize their finances.
+- **Date Selection**: Implemented a date picker on the transaction screen, allowing users to log entries for past or future dates.
+
+### Changed
+- **UI**: Redesigned the 'Add Transaction' screen to incorporate the new wallet and date selection options.
+
+## [1.1.0] - 2026-01-30
+### Added
+- **Transactions**: Implemented draft transaction functionality, allowing users to save and complete entries later.
+- **UI**: The transaction history screen now groups entries by date and displays daily totals for improved readability.
+
+### Changed
+- **Navigation**: Overhauled the app's navigation system for a more unified and consistent user experience.
+
+## [1.0.0] - 2026-01-18
+### Changed
+- **Data**: Updated the sample data displayed within the app.
 
 ## [0.1.0] - 202

PR TEMPLATE:
# 📋 Summary
<!-- Brief description of changes for the Android Application -->

## ✅ Checklist
- [ ] 🏗️ I have moved the related issue to "In Progress" on the Kanban board

# 🎯 Type
- [ ] 🐛 Bug fix
- [ ] ✨ New feature
- [ ] ⚡ Performance improvement
- [ ] 🔧 Refactoring
- [ ] 🎨 UI Update (Jetpack Compose)
- [ ] 🤖 SDK/Dependency Update
- [ ] 💥 Breaking change

# 📱 Android Specific Checks
- [ ] Verified on Emulator
- [ ] Verified on Real Device
- [ ] Screen Orientation Support (Portrait/Landscape)
- [ ] Dark/Light Mode Tested

# 📝 Changes
<!-- Describe what changed in detail -->

# 📸 UI/UX Screenshots
<!-- Include screenshots from the Android device/emulator. MUST include screenshots for UI changes. -->

# 🧪 Testing
- [ ] `./gradlew build` passes
- [ ] Unit Tests pass

# 🚀 Migration/Deployment
- [ ] Database migration required (Room)
- [ ] Environment variables/Secrets updated
- [ ] New Dependencies added

```bash
# Migration commands if applicable
```

# 🔗 Related Issues
<!-- Link to related issues or PRs using FULL URL e.g. https://github.com/oatrice/JarWise-Root/issues/1 -->
- Closes #
- Related to #
- Fixes #

**Breaking Changes**: <!-- Yes/No -->
**Migration Required**: <!-- Yes/No -->


INSTRUCTIONS:
1. Generate a comprehensive PR description in Markdown format.
2. If a template is provided, fill it out intelligently.
3. If no template, use a standard structure: Summary, Changes, Impact.
4. Focus on 'Why' and 'What'.
5. Do not include 'Here is the PR description' preamble. Just the body.
