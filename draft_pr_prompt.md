# PR Draft Prompt

You are an AI assistant helping to create a Pull Request description.
    
TASK: [Web | Android] Manage Jars (Edit %, Name, Icon)
ISSUE: {
  "title": "[Web | Android] Manage Jars (Edit %, Name, Icon)",
  "number": 17
}

GIT CONTEXT:
COMMITS:
0315bea feat: [Web | Android] Manage Jars (Edit %, Name, Icon)...
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
 draft_pr_prompt.md                                 | 241 +++++++++++++++++
 draft_pr_prompt.txt                                |  68 +++++
 gradle/libs.versions.toml                          |   1 +
 scripts/run_tests.sh                               |   4 +-
 40 files changed, 2886 insertions(+), 334 deletions(-)

KEY FILE DIFFS:
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
diff --git a/app/build.gradle.kts b/app/build.gradle.kts
index e4ba5aa..74e0edb 100644
--- a/app/build.gradle.kts
+++ b/app/build.gradle.kts
@@ -13,7 +13,7 @@ android {
         minSdk = 24
         targetSdk = 34
         versionCode = 1
-        versionName = "1.0"
+        versionName = "1.3.0"
 
         testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
         vectorDrawables {
@@ -86,10 +86,21 @@ dependencies {
     implementation(libs.mlkit.barcode.scanning)
 
     testImplementation(libs.junit)
+
+    testImplementation(libs.androidx.room.testing)
+    testImplementation("org.robolectric:robolectric:4.11.1")
+    testImplementation(libs.androidx.core.ktx)
+    testImplementation(libs.androidx.junit)
     androidTestImplementation(libs.androidx.junit)
+    androidTestImplementation(libs.androidx.room.testing)
     androidTestImplementation(libs.androidx.espresso.core)
     androidTestImplementation(platform(libs.androidx.compose.bom))
     androidTestImplementation(libs.androidx.ui.test.junit4)
     debugImplementation(libs.androidx.ui.tooling)
     debugImplementation(libs.androidx.ui.test.manifest)
+    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
+}
+
+ksp {
+    arg("room.schemaLocation", "$projectDir/schemas")
 }
diff --git a/app/src/androidTest/java/com/oatrice/jarwise/data/MigrationTest.kt b/app/src/androidTest/java/com/oatrice/jarwise/data/MigrationTest.kt
new file mode 100644
index 0000000..d0758ee
--- /dev/null
+++ b/app/src/androidTest/java/com/oatrice/jarwise/data/MigrationTest.kt
@@ -0,0 +1,48 @@
+package com.oatrice.jarwise.data
+
+import androidx.room.testing.MigrationTestHelper
+import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
+import androidx.test.ext.junit.runners.AndroidJUnit4
+import androidx.test.platform.app.InstrumentationRegistry
+import org.junit.Rule
+import org.junit.Test
+import org.junit.runner.RunWith
+import java.io.IOException
+
+@RunWith(AndroidJUnit4::class)
+class MigrationTest {
+
+    private val TEST_DB = "migration-test"
+
+    @get:Rule
+    val helper: MigrationTestHelper = MigrationTestHelper(
+        InstrumentationRegistry.getInstrumentation(),
+        AppDatabase::class.java.canonicalName!!,
+        FrameworkSQLiteOpenHelperFactory()
+    )
+
+    @Test
+    @Throws(IOException::class)
+    fun migrate2To3() {
+        var db = helper.createDatabase(TEST_DB, 2).apply {
+            // Insert data manually for version 2
+            execSQL("INSERT INTO transactions (id, amount, note, jarId, date, type, status) VALUES (1, 100.0, 'Test Tx', 'necessities', '2024-01-01', 'expense', 'completed')")
+            close()
+        }
+
+        // Migrate to version 3
+        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, AppDatabase.MIGRATION_2_3)
+
+        // Query to validate data survival and new column default value
+        val cursor = db.query("SELECT * FROM transactions WHERE id = 1")
+        cursor.moveToFirst()
+        
+        // Check new column 'walletId' exists and has default value
+        val walletIdIndex = cursor.getColumnIndex("walletId")
+        val walletId = cursor.getString(walletIdIndex)
+        
+        assert(walletId == "wallet-cash")
+        
+        cursor.close()
+    }
+}
diff --git a/app/src/main/java/com/oatrice/jarwise/MainActivity.kt b/app/src/main/java/com/oatrice/jarwise/MainActivity.kt
index 0d798c4..5961cd0 100644
--- a/app/src/main/java/com/oatrice/jarwise/MainActivity.kt
+++ b/app/src/main/java/com/oatrice/jarwise/MainActivity.kt
@@ -15,8 +15,11 @@ import com.oatrice.jarwise.data.AppDatabase
 
 import com.oatrice.jarwise.data.GeneratedMockData
 import com.oatrice.jarwise.data.repository.CurrencyRepository
+import com.oatrice.jarwise.data.repository.JarConfigRepository
 import com.oatrice.jarwise.data.repository.UserPreferencesRepository
 import com.oatrice.jarwise.data.service.SlipDetectorServiceImpl
+import com.oatrice.jarwise.ui.managejars.ManageJarsScreen
+import com.oatrice.jarwise.ui.managejars.ManageJarsViewModel
 import com.oatrice.jarwise.ui.AddTransactionScreen
 import com.oatrice.jarwise.ui.DashboardScreen
 import com.oatrice.jarwise.ui.MainViewModel
@@ -35,6 +38,7 @@ sealed class Screen {
     data object AddTransaction : Screen()
     data object SlipImport : Screen()
     data object Settings : Screen()
+    data object ManageJars : Screen()
 }
 
 class MainActivity : ComponentActivity() {
@@ -44,18 +48,34 @@ class MainActivity : ComponentActivity() {
         val db = Room.databaseBuilder(
             applicationContext,
             AppDatabase::class.java, "jarwise-db"
-        ).build()
+        )
+            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
+            .build()
         
         val userPreferencesRepository = UserPreferencesRepository(applicationContext)
         val currencyRepository = CurrencyRepository(userPreferencesRepository)
         
-        val viewModel: MainViewModel by viewModels { MainViewModel.Factory(db.transactionDao(), currencyRepository) }
+        // JarConfig Repository
+        val jarConfigRepository = JarConfigRepository(db.jarConfigDao())
+        
+        
+        val viewModel: MainViewModel by viewModels { 
+            MainViewModel.Factory(
+                db.transactionDao(), 
+                currencyRepository,
+                jarConfigRepository
+            ) 
+        }
 
         val slipRepository = com.oatrice.jarwise.data.repository.SlipRepository(applicationContext)
         val slipDetector = SlipDetectorServiceImpl(applicationContext)
         val slipViewModel: SlipViewModel by viewModels { 
             SlipViewModel.Factory(slipRepository, slipDetector) 
         }
+        
+        val manageJarsViewModel: ManageJarsViewModel by viewModels {
+            ManageJarsViewModel.Factory(jarConfigRepository)
+        }
 
         enableEdgeToEdge()
         setContent {
@@ -66,22 +86,37 @@ class MainActivity : ComponentActivity() {
                 val selectedCurrency by viewModel.selectedCurrency.collectAsState()
                 val recentImages by slipViewModel.recentImages.collectAsState()
 
+                val handleNavigation: (com.oatrice.jarwise.ui.components.NavPage) -> Unit = { page ->
+                    when (page) {
+                        com.oatrice.jarwise.ui.components.NavPage.DASHBOARD -> currentScreen = Screen.Dashboard
+                        com.oatrice.jarwise.ui.components.NavPage.HISTORY -> currentScreen = Screen.TransactionHistory
+                        com.oatrice.jarwise.ui.components.NavPage.ADD -> currentScreen = Screen.AddTransaction
+                        // Add other destinations here when ready (WALLET, PROFILE)
+                        else -> {}
+                    }
+                }
+
                 Surface(
                     modifier = Modifier.fillMaxSize(),
                     color = MaterialTheme.colorScheme.background
                 ) {
                     when (currentScreen) {
-                        is Screen.Dashboard -> DashboardScreen(
-                            jars = GeneratedMockData.jars, // Ideally usage ViewModel for jars too
-                            transactions = transactions,
-                            formattedTotalBalance = formattedTotalBalance,
-                            selectedCurrency = selectedCurrency,
-                            onNavigateToHistory = { currentScreen = Screen.TransactionHistory },
-                            onNavigateToScan = { currentScreen = Screen.Scan },
-                            onNavigateToImport = { currentScreen = Screen.SlipImport },
-                            onNavigateToAdd = { currentScreen = Screen.AddTransaction },
-                            onNavigateToSettings = { currentScreen = Screen.Settings }
-                        )
+                        is Screen.Dashboard -> {
+                            val jars by viewModel.jars.collectAsState()
+                            DashboardScreen(
+                                jars = jars,
+                                transactions = transactions,
+                                formattedTotalBalance = formattedTotalBalance,
+                                selectedCurrency = selectedCurrency,
+                                onNavigateToHistory = { currentScreen = Screen.TransactionHistory },
+                                onNavigateToScan = { currentScreen = Screen.Scan },
+                                onNavigateToImport = { currentScreen = Screen.SlipImport },
+                                onNavigateToAdd = { currentScreen = Screen.AddTransaction },
+                                onNavigateToSettings = { currentScreen = Screen.Settings },
+                                onNavigateToManageJars = { currentScreen = Screen.ManageJars },
+                                onNavigate = handleNavigation
+                            )
+                        }
                         is Screen.Settings -> SettingsScreen(
                              onBack = { currentScreen = Screen.Dashboard },
                              viewModel = viewModel
@@ -89,7 +124,8 @@ class MainActivity : ComponentActivity() {
                         is Screen.TransactionHistory -> TransactionHistoryScreen(
                             transactions = transactions,
                             selectedCurrency = selectedCurrency,
-                            onBack = { currentScreen = Screen.Dashboard }
+                            onBack = { currentScreen = Screen.Dashboard },
+                            onNavigate = handleNavigation
                         )
                         is Screen.Scan -> ScanScreen(
                             onNavigateBack = { currentScreen = Screen.Dashboard },
@@ -121,20 +157,33 @@ class MainActivity : ComponentActivity() {
                                         sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                                         sdf.format(it)
                                     }
-                                    viewModel.saveTransaction(amount, jarId, note, date)
+                                    viewModel.saveTransaction(amount, jarId, "wallet-bank", note, date)
                                     android.widget.Toast.makeText(applicationContext, "Slip saved successfully", android.widget.Toast.LENGTH_SHORT).show()
-                                    // Navigate back or show success? For now, stay on screen or go to dashboard.
-                                    // currentScreen = Screen.Dashboard // Optional: auto-navigate
+                                },
+                                onSaveDraft = { _, parsedSlip, jarId ->
+                                    val amount = parsedSlip.amount ?: 0.0
+                                    val note = "Slip: ${parsedSlip.bankName ?: "Unknown"}"
+                                    val date = parsedSlip.date?.let {
+                                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
+                                        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
+                                        sdf.format(it)
+                                    }
+                                    viewModel.saveDraft(amount, jarId, "wallet-bank", note, date)
+                                    android.widget.Toast.makeText(applicationContext, "Draft saved!", android.widget.Toast.LENGTH_SHORT).show()
                                 }
                             )
                         }
                         is Screen.AddTransaction -> AddTransactionScreen(
                             onBack = { currentScreen = Screen.Dashboard },
-                            onSave = { amount, jarId, note ->
-                                viewModel.saveTransaction(amount, jarId, note)
+                            onSave = { amount, jarId, walletId, note, date ->
+                                viewModel.saveTransaction(amount, jarId, walletId, note, date)
                                 currentScreen = Screen.Dashboard
                             }
                         )
+                        is Screen.ManageJars -> ManageJarsScreen(
+                            viewModel = manageJarsViewModel,
+                            onBack = { currentScreen = Screen.Dashboard }
+                        )
                     }
                 }
             }
diff --git a/app/src/main/java/com/oatrice/jarwise/data/AppDatabase.kt b/app/src/main/java/com/oatrice/jarwise/data/AppDatabase.kt
index 1bd536a..57b1730 100644
--- a/app/src/main/java/com/oatrice/jarwise/data/AppDatabase.kt
+++ b/app/src/main/java/com/oatrice/jarwise/data/AppDatabase.kt
@@ -2,8 +2,41 @@ package com.oatrice.jarwise.data
 
 import androidx.room.Database
 import androidx.room.RoomDatabase
+import androidx.room.migration.Migration
+import androidx.sqlite.db.SupportSQLiteDatabase
 
-@Database(entities = [Transaction::class], version = 1, exportSchema = false)
+@Database(entities = [Transaction::class, JarConfig::class], version = 4, exportSchema = true)
 abstract class AppDatabase : RoomDatabase() {
     abstract fun transactionDao(): TransactionDao
+    abstract fun jarConfigDao(): JarConfigDao
+
+    companion object {
+        val MIGRATION_1_2 = object : Migration(1, 2) {
+            override fun migrate(db: SupportSQLiteDatabase) {
+                db.execSQL("ALTER TABLE transactions ADD COLUMN type TEXT NOT NULL DEFAULT 'expense'")
+                db.execSQL("ALTER TABLE transactions ADD COLUMN status TEXT NOT NULL DEFAULT 'completed'")
+            }
+        }
+
+        val MIGRATION_2_3 = object : Migration(2, 3) {
+            override fun migrate(db: SupportSQLiteDatabase) {
+                db.execSQL("ALTER TABLE transactions ADD COLUMN walletId TEXT NOT NULL DEFAULT 'wallet-cash'")
+            }
+        }
+
+        val MIGRATION_3_4 = object : Migration(3, 4) {
+            override fun migrate(db: SupportSQLiteDatabase) {
+                db.execSQL("""
+                    CREATE TABLE IF NOT EXISTS jar_configs (
+                        id TEXT PRIMARY KEY NOT NULL,
+                        name TEXT NOT NULL,
+                        percentage INTEGER NOT NULL,
+                        colorName TEXT NOT NULL,
+                        iconName TEXT NOT NULL
+                    )
+                """.trimIndent())
+            }
+        }
+    }
 }
+
diff --git a/app/src/main/java/com/oatrice/jarwise/data/GeneratedMockData.kt b/app/src/main/java/com/oatrice/jarwise/data/GeneratedMockData.kt
index fcd9f11..e1887e6 100644
--- a/app/src/main/java/com/oatrice/jarwise/data/GeneratedMockData.kt
+++ b/app/src/main/java/com/oatrice/jarwise/data/GeneratedMockData.kt
@@ -8,6 +8,7 @@ import com.oatrice.jarwise.ui.theme.*
 
 // WARNING: This file is auto-generated. Do not edit directly.
 // Generated from: shared-spec/data/mockData.json
+// Generated at: 2026-01-18T06:16:41.633Z
 
 object GeneratedMockData {
     val jars = listOf(
@@ -30,7 +31,7 @@ object GeneratedMockData {
             level = 12,
             icon = Icons.Rounded.AttachMoney,
             color = Green400,
-            shadowColor = Green500,
+            shadowColor = Blue500,
             barColor = Green500
         ),
         Jar(
@@ -41,7 +42,7 @@ object GeneratedMockData {
             level = 2,
             icon = Icons.Rounded.Gamepad,
             color = Pink400,
-            shadowColor = Pink500,
+            shadowColor = Blue500,
             barColor = Pink500
         ),
         Jar(
@@ -52,7 +53,7 @@ object GeneratedMockData {
             level = 1,
             icon = Icons.Rounded.School,
             color = Yellow400,
-            shadowColor = Yellow500,
+            shadowColor = Blue500,
             barColor = Yellow500
         ),
         Jar(
@@ -63,7 +64,7 @@ object GeneratedMockData {
             level = 5,
             icon = Icons.Rounded.Flight,
             color = Purple400,
-            shadowColor = Purple500,
+            shadowColor = Blue500,
             barColor = Purple500
         ),
         Jar(
@@ -74,7 +75,7 @@ object GeneratedMockData {
             level = 1,
             icon = Icons.Rounded.Favorite,
             color = Red400,
-            shadowColor = Red500,
+            shadowColor = Blue500,
             barColor = Red500
         )
     )
@@ -87,7 +88,7 @@ object GeneratedMockData {
             category = "Play",
             date = "Today, 10:43 AM",
             icon = Icons.Rounded.Headphones,
-            color = Green500.copy(alpha = 0.1f),
+            color = Green400.copy(alpha = 0.1f),
             iconTint = Green400
         ),
         Transaction(
@@ -97,7 +98,7 @@ object GeneratedMockData {
             category = "Necessities",
             date = "Yesterday, 6:30 PM",
             icon = Icons.Rounded.ShoppingBag,
-            color = Blue500.copy(alpha = 0.1f),
+            color = Blue400.copy(alpha = 0.1f),
             iconTint = Blue400
         ),
         Transaction(
@@ -107,7 +108,7 @@ object GeneratedMockData {
             category = "Education",
             date = "Dec 28, 2025",
             icon = Icons.Rounded.School,
-            color = Yellow500.copy(alpha = 0.1f),
+            color = Yellow400.copy(alpha = 0.1f),
             iconTint = Yellow400
         )
     )
diff --git a/app/src/main/java/com/oatrice/jarwise/data/JarConfig.kt b/app/src/main/java/com/oatrice/jarwise/data/JarConfig.kt
new file mode 100644
index 0000000..7ca6c72
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/JarConfig.kt
@@ -0,0 +1,31 @@
+package com.oatrice.jarwise.data
+
+import androidx.room.Entity
+import androidx.room.PrimaryKey
+
+/**
+ * Room Entity for storing jar configuration
+ * Allows users to customize jar name, percentage, color, and icon
+ */
+@Entity(tableName = "jar_configs")
+data class JarConfig(
+    @PrimaryKey val id: String,
+    val name: String,
+    val percentage: Int,      // 0-100, total across all jars must = 100
+    val colorName: String,    // "blue", "green", "pink", "yellow", "purple", "red", "cyan", "orange"
+    val iconName: String      // "home", "dollar", "gamepad", "school", "flight", "heart", "work", "savings"
+) {
+    companion object {
+        /**
+         * Default 6 Jars configuration based on T. Harv Eker's money management system
+         */
+        val DEFAULTS = listOf(
+            JarConfig("1", "Necessities", 55, "blue", "home"),
+            JarConfig("2", "Financial Freedom", 10, "green", "dollar"),
+            JarConfig("3", "Play", 10, "pink", "gamepad"),
+            JarConfig("4", "Education", 10, "yellow", "school"),
+            JarConfig("5", "Long-term Savings", 10, "purple", "flight"),
+            JarConfig("6", "Give", 5, "red", "heart")
+        )
+    }
+}
diff --git a/app/src/main/java/com/oatrice/jarwise/data/JarConfigDao.kt b/app/src/main/java/com/oatrice/jarwise/data/JarConfigDao.kt
new file mode 100644
index 0000000..d726ded
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/JarConfigDao.kt
@@ -0,0 +1,35 @@
+package com.oatrice.jarwise.data
+
+import androidx.room.*
+import kotlinx.coroutines.flow.Flow
+
+/**
+ * DAO for JarConfig CRUD operations
+ */
+@Dao
+interface JarConfigDao {
+    
+    @Query("SELECT * FROM jar_configs ORDER BY id")
+    fun getAllFlow(): Flow<List<JarConfig>>
+    
+    @Query("SELECT * FROM jar_configs O
... (Diff truncated for size) ...

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
